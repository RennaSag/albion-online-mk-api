const { Pool } = require('pg');

const poolAlbion = new Pool({
    connectionString: process.env.ALBION_DATABASE_URL,
    ssl: { rejectUnauthorized: false },
    max: 5,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000
});

async function queryAlbion(text, params) {
    const client = await poolAlbion.connect();
    try {
        return await client.query(text, params);
    } finally {
        client.release();
    }
}

module.exports = { poolAlbion, queryAlbion };