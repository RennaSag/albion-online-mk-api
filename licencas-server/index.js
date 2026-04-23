const express = require('express');
const { Pool } = require('pg');
const { v4: uuidv4 } = require('uuid');
const nodemailer = require('nodemailer');
require('dotenv').config();

const app = express();
app.use(express.json());

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
});

// cria a tabela se nao existir na primeira execucao
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

// webhook chamado pelo hotmart quando alguem compra
app.post('/webhook/hotmart', async (req, res) => {
    try {
        const evento = req.body;
        console.log('webhook recebido:', evento.event);

        if (evento.event === 'PURCHASE_APPROVED') {
            const email = evento.data.buyer.email;
            const chave = uuidv4();

            const expiracao = new Date();
            expiracao.setDate(expiracao.getDate() + 30);

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

// endpoint chamado pelo software java na inicializacao
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

// endpoint pra voce gerar licenca manualmente (pra testar)
app.post('/admin/gerar', async (req, res) => {
    const { email, token } = req.body;
    if (token !== process.env.ADMIN_TOKEN) {
        return res.status(403).json({ erro: 'token invalido' });
    }
    const chave = uuidv4();
    const expiracao = new Date();
    expiracao.setDate(expiracao.getDate() + 30);

    await pool.query(
        `INSERT INTO licencas (email, chave, ativo, expiracao)
         VALUES ($1, $2, true, $3)
         ON CONFLICT (email) DO UPDATE
         SET chave = $2, ativo = true, expiracao = $3`,
        [email, chave, expiracao]
    );
    res.json({ chave, expiracao });
});

// endpoint de saude pra evitar o sleep no render
app.get('/health', (req, res) => res.json({ ok: true }));

async function enviarEmailChave(email, chave) {
    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASS
        }
    });

    await transporter.sendMail({
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Sua chave de acesso - Albion Market',
        text: `Obrigado pela compra!\n\nSua chave de acesso: ${chave}\n\nDigite ela no software na tela de login.`
    });
}

const PORT = process.env.PORT || 10000;
app.listen(PORT, '0.0.0.0', async () => {
    await inicializarBanco();
    console.log('servidor rodando na porta', PORT);
});