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

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

async function enviarEmailChave(email, chave, diasExpiracao) {
    await transporter.sendMail({
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Sua chave de acesso - Analisador de Mercado Albion',
        text: `Obrigado pela compra!\nSua chave de acesso: ${chave}\nDigite ela no software na tela de login.\nSeu acesso e valido por ${diasExpiracao} dias.`
    });
    console.log('email enviado para:', email);
}

async function inicializarBanco() {
    await pool.query(`
        CREATE TABLE IF NOT EXISTS licencas (
            id SERIAL PRIMARY KEY,
            email TEXT NOT NULL UNIQUE,
            chave TEXT NOT NULL UNIQUE,
            ativo BOOLEAN DEFAULT TRUE,
            expiracao TIMESTAMP,
            plano TEXT,
            criado_em TIMESTAMP DEFAULT NOW()
        )
    `);

    // adiciona coluna plano se o banco ja existia sem ela
    await pool.query(`
        ALTER TABLE licencas ADD COLUMN IF NOT EXISTS plano TEXT
    `);

    console.log('banco inicializado');
}

// recebe o nome ou codigo do plano da hotmart e retorna quantos dias de acesso
function diasPorPlano(offerCode, productName) {
    const nome = (offerCode + ' ' + productName).toLowerCase();

    if (nome.includes('anual') || nome.includes('12')) return 365;
    if (nome.includes('semestral') || nome.includes('6') || nome.includes('seis')) return 180;
    if (nome.includes('trimestral') || nome.includes('3') || nome.includes('tres')) return 90;
    if (nome.includes('mensal') || nome.includes('1') || nome.includes('mes')) return 30;

    // fallback: 30 dias se nao reconhecer o plano
    console.warn('plano nao reconhecido, usando 30 dias. offerCode:', offerCode, 'productName:', productName);
    return 30;
}

app.post('/webhook/hotmart', async (req, res) => {
    try {
        const evento = req.body;
        console.log('webhook recebido:', evento.event);
        console.log('payload completo:', JSON.stringify(evento));

        if (evento.event === 'PURCHASE_APPROVED') {
            const email = evento?.data?.buyer?.email;
            if (!email) {
                console.error('webhook PURCHASE_APPROVED sem email no payload:', JSON.stringify(evento));
                return res.sendStatus(200);
            }

            // pega o codigo da oferta e nome do produto pra identificar o plano
            // o campo exato pode variar, o console.log acima vai te mostrar o payload real
            const offerCode = evento?.data?.offer?.code ?? '';
            const productName = evento?.data?.product?.name ?? '';

            const dias = diasPorPlano(offerCode, productName);
            const plano = offerCode || productName || 'desconhecido';

            const chave = uuidv4();
            const expiracao = new Date();
            expiracao.setDate(expiracao.getDate() + dias);

            await pool.query(
                `INSERT INTO licencas (email, chave, ativo, expiracao, plano)
                 VALUES ($1, $2, true, $3, $4)
                 ON CONFLICT (email) DO UPDATE
                 SET chave = $2, ativo = true, expiracao = $3, plano = $4`,
                [email, chave, expiracao, plano]
            );
            console.log('licenca criada para:', email, '| plano:', plano, '| dias:', dias);

            try {
                await enviarEmailChave(email, chave, dias);
            } catch (emailErr) {
                console.error('erro ao enviar email para:', email, emailErr.message);
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

        // renovacao de assinatura: hotmart manda PURCHASE_APPROVED de novo a cada ciclo
        // o ON CONFLICT DO UPDATE ja cuida disso, atualizando a expiracao automaticamente

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
            email: licenca.email,
            plano: licenca.plano
        });
    } catch (err) {
        console.error('erro ao validar:', err);
        res.json({ valido: false, motivo: 'erro interno' });
    }
});

app.post('/admin/gerar', async (req, res) => {
    try {
        const { email, token, dias } = req.body;
        console.log('admin/gerar chamado, email:', email, 'token recebido:', token ? 'sim' : 'nao');

        if (token !== process.env.ADMIN_TOKEN) {
            return res.status(403).json({ erro: 'token invalido' });
        }

        if (!email) {
            return res.status(400).json({ erro: 'email obrigatorio' });
        }

        const chave = uuidv4();
        const expiracao = new Date();
        const diasFinal = dias ? parseInt(dias) : 30;
        expiracao.setDate(expiracao.getDate() + diasFinal);

        await pool.query(
            `INSERT INTO licencas (email, chave, ativo, expiracao, plano)
             VALUES ($1, $2, true, $3, $4)
             ON CONFLICT (email) DO UPDATE
             SET chave = $2, ativo = true, expiracao = $3, plano = $4`,
            [email, chave, expiracao, 'manual']
        );
        console.log('licenca manual criada para:', email, '| dias:', diasFinal);

        res.json({ chave, expiracao, dias: diasFinal });
    } catch (err) {
        console.error('erro em admin/gerar:', err);
        res.status(500).json({ erro: err.message });
    }
});

app.get('/obrigado', async (req, res) => {
    const email = req.query.email;
    if (!email) {
        return res.send('<h2>Email nao informado.</h2>');
    }

    try {
        const result = await pool.query(
            'SELECT chave, expiracao, plano FROM licencas WHERE email = $1 AND ativo = true',
            [email]
        );

        if (result.rows.length === 0) {
            return res.send('<h2>Licenca nao encontrada para este email.</h2>');
        }

        const { chave, expiracao, plano } = result.rows[0];

        res.send(`
            <h2>Obrigado pela compra!</h2>
            <p>Plano: <strong>${plano ?? 'nao identificado'}</strong></p>
            <p>Sua chave de acesso: <strong>${chave}</strong></p>
            <p>Valido ate: <strong>${new Date(expiracao).toLocaleDateString('pt-BR')}</strong></p>
            <p>Digite ela no software na tela de login.</p>
        `);
    } catch (err) {
        console.error('erro em /obrigado:', err);
        res.send('<h2>Erro ao buscar licenca.</h2>');
    }
});

app.get('/health', (req, res) => res.json({ ok: true }));

app.get('/version', (req, res) => {
    res.json({
        version: '1.1.1',
        downloadUrl: 'https://github.com/RennaSag/albion-online-mk-api/releases/download/v1.1.1/Analisador.de.Mercado.do.Albion.Online-1.1.1.msi'
    });
});

const PORT = process.env.PORT || 10000;
app.listen(PORT, '0.0.0.0', async () => {
    await inicializarBanco();
    console.log('servidor rodando na porta', PORT);
});