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

// endpoint de arbitragem — encontra oportunidades de comprar numa cidade e vender em outra
//
// GET /api/v2/arbitragem
//   ?tipo_compra=sell       sell=compra direta, buy=pedido de compra
//   &tipo_venda=buy         sell=venda direta,  buy=pedido de venda
//   &qualidade=1            1=normal 2=boa 3=notavel 4=excelente 5=obra-prima (opcional)
//   &lucro_minimo=50000     lucro bruto minimo em prata (opcional, padrao 0)
//   &pagina=1               pagina atual, comeca em 1 (opcional, padrao 1)
router.get('/arbitragem', async (req, res) => {
    try {
        // tipo_compra: campo usado pra calcular o custo de compra
        // sell = compra direta (paga o sell_min da cidade de compra)
        // buy  = pedido de compra (coloca ordem no buy_max da cidade de compra)
        const tipoCompra = req.query.tipo_compra === 'buy' ? 'buy_max' : 'sell_min';

        // tipo_venda: campo usado pra calcular a receita de venda
        // sell = venda direta (vende no sell_min da cidade de venda)
        // buy  = pedido de venda (coloca ordem no buy_max da cidade de venda)
        const tipoVenda = req.query.tipo_venda === 'buy' ? 'buy_max' : 'sell_min';

        const qualidade   = req.query.qualidade    ? Number(req.query.qualidade)    : null;
        const lucroMinimo = req.query.lucro_minimo ? Number(req.query.lucro_minimo) : 0;
        const pagina      = Math.max(Number(req.query.pagina) || 1, 1);
        const porPagina   = 10;
        const offset      = (pagina - 1) * porPagina;

        const params = [lucroMinimo, porPagina, offset];
        let paramIndex = 4;
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
                a.cidade                                            AS cidade_compra,
                a.${tipoCompra}                                     AS preco_compra,
                b.cidade                                            AS cidade_venda,
                b.${tipoVenda}                                      AS preco_venda,
                b.${tipoVenda} - a.${tipoCompra}                    AS lucro_bruto,
                ROUND(
                    ((b.${tipoVenda} - a.${tipoCompra})::numeric / NULLIF(a.${tipoCompra}, 0)) * 100
                , 2)                                                AS lucro_percentual,
                a.updated_at                                        AS atualizado_compra,
                b.updated_at                                        AS atualizado_venda
            FROM precos_atual a
            JOIN precos_atual b
                ON  a.item_id   = b.item_id
                AND a.qualidade = b.qualidade
                AND a.cidade   != b.cidade
            WHERE a.${tipoCompra} > 0
              AND b.${tipoVenda}  > 0
              AND b.${tipoVenda}  > a.${tipoCompra}
              AND b.${tipoVenda} - a.${tipoCompra} >= $1
              ${filtroQualidade}
            ORDER BY lucro_bruto DESC
            LIMIT $2 OFFSET $3
        `;

        // busca total de resultados pra calcular total de paginas
        const sqlTotal = `
            SELECT COUNT(*) AS total
            FROM precos_atual a
            JOIN precos_atual b
                ON  a.item_id   = b.item_id
                AND a.qualidade = b.qualidade
                AND a.cidade   != b.cidade
            WHERE a.${tipoCompra} > 0
              AND b.${tipoVenda}  > 0
              AND b.${tipoVenda}  > a.${tipoCompra}
              AND b.${tipoVenda} - a.${tipoCompra} >= $1
              ${filtroQualidade}
        `;

        const paramsTotal = qualidade ? [lucroMinimo, qualidade] : [lucroMinimo];
        const [resultado, total] = await Promise.all([
            query(sql, params),
            query(sqlTotal, paramsTotal)
        ]);

        const totalResultados = Number(total.rows[0].total);
        const totalPaginas    = Math.ceil(totalResultados / porPagina);

        res.json({
            pagina,
            total_paginas:    totalPaginas,
            total_resultados: totalResultados,
            resultados:       resultado.rows
        });
    } catch (err) {
        console.error('erro ao buscar arbitragem:', err.message);
        res.status(500).json({ erro: 'erro interno' });
    }
});

module.exports = router;