const express = require('express');
const router = express.Router();
const { query } = require('./db');

// mesmo endpoint da api publica do albion data
// GET /api/v2/stats/prices/T5_MAIN_SWORD.json?locations=Caerleon,Martlock&qualities=1,2
router.get('/stats/prices/:itemIds.json', async (req, res) => {
    try {
        const itemIds = req.params.itemIds.split(',');
        const locations = req.query.locations ? req.query.locations.split(',') : null;
        const qualities = req.query.qualities ? req.query.qualities.split(',').map(Number) : null;

        // monta a query dinamicamente de acordo com os filtros
        let sql = `
            SELECT
                item_id,
                cidade     AS city,
                qualidade  AS quality,
                sell_min   AS sell_price_min,
                buy_max    AS buy_price_max,
                sell_date  AS sell_price_min_date,
                buy_date   AS buy_price_max_date,
                updated_at
            FROM precos_atual
            WHERE item_id = ANY($1)
        `;
        const params = [itemIds];
        let paramIndex = 2;

        if (locations) {
            sql += ` AND cidade = ANY($${paramIndex})`;
            params.push(locations);
            paramIndex++;
        }

        if (qualities) {
            sql += ` AND qualidade = ANY($${paramIndex})`;
            params.push(qualities);
            paramIndex++;
        }

        const resultado = await query(sql, params);

        res.json(resultado.rows);
    } catch (err) {
        console.error('erro ao buscar precos:', err.message);
        res.status(500).json({ erro: 'erro interno' });
    }
});

module.exports = router;