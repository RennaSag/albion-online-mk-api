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
            criado_em TIMESTAMP DEFAULT NOW()
        )
    `);
    console.log('banco inicializado');
}

app.post('/webhook/hotmart', async (req, res) => {
    try {
        const evento = req.body;
        console.log('webhook recebido:', evento.event);

        if (evento.event === 'PURCHASE_APPROVED') {
            const email = evento.data.buyer.email;
            const chave = uuidv4();
            const expiracao = new Date();

            //tempo de duracao da key inicial, 5 dias
            expiracao.setDate(expiracao.getDate() + 5);

            await pool.query(
                `INSERT INTO licencas (email, chave, ativo, expiracao)
                 VALUES ($1, $2, true, $3)
                 ON CONFLICT (email) DO UPDATE
                 SET chave = $2, ativo = true, expiracao = $3`,
                [email, chave, expiracao]
            );

            await enviarEmailChave(email, chave);
            console.log('licenca criada para:', email);
        }

        if (evento.event === 'SUBSCRIPTION_CANCELLATION') {
            const email = evento.data.buyer.email;
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

        const chave = uuidv4();
        const expiracao = new Date();


        expiracao.setDate(expiracao.getDate() + 5);

        await pool.query(
            `INSERT INTO licencas (email, chave, ativo, expiracao)
             VALUES ($1, $2, true, $3)
             ON CONFLICT (email) DO UPDATE
             SET chave = $2, ativo = true, expiracao = $3`,
            [email, chave, expiracao]
        );

        console.log('licenca salva no banco, tentando enviar email...');

        try {
            await enviarEmailChave(email, chave);
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

app.get('/version', (req, res) => {
  res.json({
    version: "1.0.1",
    downloadUrl: "https://raw.githubusercontent.com/RennaSag/albion-online-mk-api/main/instalador/Analisador de Mercado do Albion Online-1.0.0.msi"
  });
});