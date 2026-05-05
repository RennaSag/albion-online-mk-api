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

// endpoint de arbitragem, encontra oportunidades de comprar numa cidade e vender em outra
// GET /api/v2/arbitragem?qualidade=1&tipo=sell&lucro_minimo=10000&limite=50
//
// parametros opcionais:
//   qualidade    - 1=normal, 2=boa, 3=notavel, 4=excelente, 5=obra-prima
//   tipo         - "sell" usa sell_min, "buy" usa buy_max (padrao: sell), pra pedidos de venda e pedidos de compra
//   lucro_minimo - lucro bruto minimo em prata
//   limite       - quantidade maxima de resultados (padrao de 50 e maximo de 200)
router.get('/arbitragem', async (req, res) => {
    try {
        const qualidade   = req.query.qualidade    ? Number(req.query.qualidade)    : null;
        const tipo        = req.query.tipo === 'buy' ? 'buy_max' : 'sell_min';
        const lucroMinimo = req.query.lucro_minimo  ? Number(req.query.lucro_minimo) : 0;
        const limite      = Math.min(Number(req.query.limite) || 50, 200);

        const params = [lucroMinimo, limite];
        let paramIndex = 3;
        let filtroQualidade = '';

        if (qualidade) {
            filtroQualidade = `AND a.qualidade = $${paramIndex} AND b.qualidade = $${paramIndex}`;
            params.push(qualidade);
            paramIndex++;
        }

        const sql = `
            SELECT
                a.item_id,
                a.qualidade,
                a.cidade                                        AS cidade_compra,
                a.${tipo}                                       AS preco_compra,
                b.cidade                                        AS cidade_venda,
                b.${tipo}                                       AS preco_venda,
                b.${tipo} - a.${tipo}                           AS lucro_bruto,
                ROUND(
                    ((b.${tipo} - a.${tipo})::numeric / a.${tipo}) * 100
                , 2)                                            AS lucro_percentual,
                a.updated_at                                    AS atualizado_compra,
                b.updated_at                                    AS atualizado_venda
            FROM precos_atual a
            JOIN precos_atual b
                ON  a.item_id   = b.item_id
                AND a.qualidade = b.qualidade
                AND a.cidade   != b.cidade
            WHERE a.${tipo} > 0
              AND b.${tipo} > 0
              AND b.${tipo} > a.${tipo}
              AND b.${tipo} - a.${tipo} >= $1
              ${filtroQualidade}
            ORDER BY lucro_bruto DESC
            LIMIT $2
        `;

        const resultado = await query(sql, params);
        res.json(resultado.rows);
    } catch (err) {
        console.error('erro ao buscar arbitragem:', err.message);
        res.status(500).json({ erro: 'erro interno' });
    }
});

module.exports = router;