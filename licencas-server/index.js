const express = require('express');
const { Pool } = require('pg');
const { v4: uuidv4 } = require('uuid');
const nodemailer = require('nodemailer');
require('dotenv').config();

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
});

async function inicializarBanco() {
    await pool.query(`
        CREATE TABLE IF NOT EXISTS licencas (
            id SERIAL PRIMARY KEY,
            email TEXT NOT NULL UNIQUE,
            chave TEXT NOT NULL UNIQUE,
            ativo BOOLEAN DEFAULT TRUE,
            expiracao TIMESTAMP,
            email_enviado BOOLEAN DEFAULT FALSE,
            criado_em TIMESTAMP DEFAULT NOW()
        )
    `);


    await pool.query(`
        ALTER TABLE licencas
        ADD COLUMN IF NOT EXISTS email_enviado BOOLEAN DEFAULT FALSE
    `);

    console.log('banco inicializado');
}



app.post('/webhook/hotmart', async (req, res) => {
    try {
        const evento = req.body;
        console.log('webhook recebido:', evento.event);

        if (evento.event === 'PURCHASE_APPROVED') {

            // validação do payload antes de tudo
            const email = evento?.data?.buyer?.email;
            if (!email) {
                console.error('webhook PURCHASE_APPROVED sem email no payload:', JSON.stringify(evento));
                return res.sendStatus(200); // 200 pra Hotmart não ficar tentando reenviar
            }

            const chave = uuidv4();
            const expiracao = new Date();
            expiracao.setDate(expiracao.getDate() + 5);


            await pool.query(
                `INSERT INTO licencas (email, chave, ativo, expiracao, email_enviado)
                 VALUES ($1, $2, true, $3, false)
                 ON CONFLICT (email) DO UPDATE
                 SET chave = $2, ativo = true, expiracao = $3, email_enviado = false`,
                [email, chave, expiracao]
            );
            console.log('licenca criada para:', email);


            try {
                await enviarEmailChave(email, chave);
                await pool.query(
                    'UPDATE licencas SET email_enviado = true WHERE email = $1',
                    [email]
                );
                console.log('email enviado para:', email);
            } catch (emailErr) {

                console.error('ERRO ao enviar email para', email, ':', emailErr.message);
            }
        }

        if (evento.event === 'SUBSCRIPTION_CANCELLATION') {
            const email = evento?.data?.buyer?.email;
            if (!email) {
                console.error('webhook SUBSCRIPTION_CANCELLATION sem email:', JSON.stringify(evento));
                return res.sendStatus(200);
            }

            await pool.query(
                'UPDATE licencas SET ativo = false WHERE email = $1',
                [email]
            );
            console.log('licenca cancelada para:', email);
        }

        res.sendStatus(200);
    } catch (err) {
        console.error('erro no webhook:', err);
        res.sendStatus(500);
    }
});



app.get('/validar', async (req, res) => {
    const chave = req.query.chave;
    if (!chave) return res.json({ valido: false, motivo: 'chave ausente' });

    try {
        const result = await pool.query(
            'SELECT * FROM licencas WHERE chave = $1',
            [chave]
        );

        if (result.rows.length === 0) {
            return res.json({ valido: false, motivo: 'chave nao encontrada' });
        }

        const licenca = result.rows[0];

        if (!licenca.ativo) {
            return res.json({ valido: false, motivo: 'assinatura cancelada' });
        }

        if (new Date(licenca.expiracao) < new Date()) {
            return res.json({ valido: false, motivo: 'assinatura expirada' });
        }

        return res.json({
            valido: true,
            expira: licenca.expiracao,
            email: licenca.email
        });
    } catch (err) {
        console.error('erro ao validar:', err);
        res.json({ valido: false, motivo: 'erro interno' });
    }
});



app.post('/admin/gerar', async (req, res) => {
    try {
        const { email, token } = req.body;
        console.log('admin/gerar chamado, email:', email, 'token recebido:', token ? 'sim' : 'nao');

        if (token !== process.env.ADMIN_TOKEN) {
            return res.status(403).json({ erro: 'token invalido' });
        }

        if (!email) {
            return res.status(400).json({ erro: 'email obrigatorio' });
        }

        const chave = uuidv4();
        const expiracao = new Date();
        expiracao.setDate(expiracao.getDate() + 5);

        await pool.query(
            `INSERT INTO licencas (email, chave, ativo, expiracao, email_enviado)
             VALUES ($1, $2, true, $3, false)
             ON CONFLICT (email) DO UPDATE
             SET chave = $2, ativo = true, expiracao = $3, email_enviado = false`,
            [email, chave, expiracao]
        );
        console.log('licenca salva no banco para:', email);

        try {
            await enviarEmailChave(email, chave);
            await pool.query(
                'UPDATE licencas SET email_enviado = true WHERE email = $1',
                [email]
            );
            console.log('email enviado com sucesso para:', email);
        } catch (emailErr) {
            console.error('erro ao enviar email:', emailErr.message);
        }

        res.json({ chave, expiracao });
    } catch (err) {
        console.error('erro em admin/gerar:', err);
        res.status(500).json({ erro: err.message });
    }
});


app.post('/admin/reenviar-pendentes', async (req, res) => {
    const { token } = req.body;

    if (token !== process.env.ADMIN_TOKEN) {
        return res.status(403).json({ erro: 'token invalido' });
    }

    try {
        const pendentes = await pool.query(
            `SELECT email, chave FROM licencas
             WHERE email_enviado = false AND ativo = true`
        );

        console.log('reenvio: encontrados', pendentes.rows.length, 'pendentes');

        const resultados = { enviados: 0, falhos: 0, detalhes: [] };

        for (const row of pendentes.rows) {
            try {
                await enviarEmailChave(row.email, row.chave);
                await pool.query(
                    'UPDATE licencas SET email_enviado = true WHERE email = $1',
                    [row.email]
                );
                resultados.enviados++;
                resultados.detalhes.push({ email: row.email, status: 'ok' });
                console.log('reenvio ok:', row.email);
            } catch (e) {
                resultados.falhos++;
                resultados.detalhes.push({ email: row.email, status: 'falhou', erro: e.message });
                console.error('reenvio falhou para:', row.email, e.message);
            }
        }

        res.json(resultados);
    } catch (err) {
        console.error('erro em reenviar-pendentes:', err);
        res.status(500).json({ erro: err.message });
    }
});



app.get('/admin/pendentes', async (req, res) => {
    const token = req.query.token;

    if (token !== process.env.ADMIN_TOKEN) {
        return res.status(403).json({ erro: 'token invalido' });
    }

    try {
        const result = await pool.query(
            `SELECT email, criado_em, expiracao FROM licencas
             WHERE email_enviado = false AND ativo = true
             ORDER BY criado_em DESC`
        );
        res.json({ total: result.rows.length, pendentes: result.rows });
    } catch (err) {
        console.error('erro em /admin/pendentes:', err);
        res.status(500).json({ erro: err.message });
    }
});


app.get('/health', (req, res) => res.json({ ok: true }));

app.get('/version', (req, res) => {
    res.json({
        version: "1.0.0",
        downloadUrl: "https://github.com/RennaSag/albion-online-mk-api/releases/download/v1.0.0/Analisador.de.Mercado.do.Albion.Online-1.0.0.msi"
    });
});


async function enviarEmailChave(email, chave) {
    console.log('configurando email, user:', process.env.EMAIL_USER ? 'definido' : 'nao definido');

    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASS
        }
    });

    const info = await transporter.sendMail({
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Sua chave de acesso - Analisador de Mercado',
        text: `Obrigado pela compra!\n\nSua chave de acesso: ${chave}\n\nDigite ela no software na tela de login.`
    });

    console.log('email enviado, messageId:', info.messageId);
}


const PORT = process.env.PORT || 10000;
app.listen(PORT, '0.0.0.0', async () => {
    await inicializarBanco();
    console.log('servidor rodando na porta', PORT);
});