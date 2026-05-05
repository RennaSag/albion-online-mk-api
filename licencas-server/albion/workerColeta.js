const { query } = require('./db');

const CIDADES = 'Caerleon,Bridgewatch,FortSterling,Lymhurst,Martlock,Thetford,BlackMarket,Brecilien';
const ALBION_API = 'https://west.albion-online-data.com/api/v2/stats/prices';
const TAMANHO_LOTE = 40;
const LOTES_PARALELOS = 5;
const DELAY_ENTRE_LOTES_MS = 5000;

// pega os proximos itens sem coleta ou com coleta mais antiga
// limite = TAMANHO_LOTE * LOTES_PARALELOS = 200 itens por ciclo
async function buscarProximoLote(limite) {
    const resultado = await query(
        `SELECT item_id FROM itens_catalogo
         ORDER BY ultima_coleta NULLS FIRST
         LIMIT $1`,
        [limite]
    );
    return resultado.rows.map(r => r.item_id);
}

// busca precos de um lote de ate 40 itens na api do albion data
async function buscarPrecos(itemIds) {
    const ids = itemIds.join(',');
    const url = `${ALBION_API}/${ids}.json?locations=${CIDADES}&qualities=1,2,3,4,5`;

    const resposta = await fetch(url, {
        signal: AbortSignal.timeout(15000)
    });

    if (!resposta.ok) {
        throw new Error(`erro na api albion: ${resposta.status}`);
    }

    return await resposta.json();
}

// salva preco atual (upsert) e historico (so se o preco mudou)
async function salvarPrecos(precos) {
    for (const p of precos) {
        if (!p.item_id || !p.city) continue;
        if (p.sell_price_min === 0 && p.buy_price_max === 0) continue;

        // upsert em precos_atual
        await query(
            `INSERT INTO precos_atual (item_id, cidade, qualidade, sell_min, buy_max, sell_date, buy_date, updated_at)
             VALUES ($1, $2, $3, $4, $5, $6, $7, NOW())
             ON CONFLICT (item_id, cidade, qualidade)
             DO UPDATE SET
                sell_min   = EXCLUDED.sell_min,
                buy_max    = EXCLUDED.buy_max,
                sell_date  = EXCLUDED.sell_date,
                buy_date   = EXCLUDED.buy_date,
                updated_at = NOW()`,
            [p.item_id, p.city, p.quality, p.sell_price_min, p.buy_price_max, p.sell_price_min_date, p.buy_price_max_date]
        );

        // busca o preco atual pra comparar se mudou
        const atual = await query(
            `SELECT sell_min, buy_max FROM precos_atual
             WHERE item_id = $1 AND cidade = $2 AND qualidade = $3`,
            [p.item_id, p.city, p.quality]
        );

        const precoAnterior = atual.rows[0];
        const precoMudou = !precoAnterior
            || precoAnterior.sell_min !== p.sell_price_min
            || precoAnterior.buy_max !== p.buy_price_max;

        // so salva historico se o preco mudou
        if (precoMudou) {
            await query(
                `INSERT INTO precos_historico (item_id, cidade, qualidade, sell_min, buy_max)
                 VALUES ($1, $2, $3, $4, $5)`,
                [p.item_id, p.city, p.quality, p.sell_price_min, p.buy_price_max]
            );
        }
    }
}

// atualiza o timestamp de ultima coleta dos itens do lote
async function marcarComoColetado(itemIds) {
    await query(
        `UPDATE itens_catalogo
         SET ultima_coleta = NOW()
         WHERE item_id = ANY($1)`,
        [itemIds]
    );
}

// deleta historico com mais de 3 dias
async function limparHistoricoAntigo() {
    const resultado = await query(
        `DELETE FROM precos_historico
         WHERE coletado_em < NOW() - INTERVAL '3 days'`
    );
    if (resultado.rowCount > 0) {
        console.log('historico antigo deletado:', resultado.rowCount, 'registros');
    }
}

// ciclo principal do worker — busca 5 lotes de 40 em paralelo = 200 itens por ciclo
async function cicloColeta() {
    try {
        const itemIds = await buscarProximoLote(TAMANHO_LOTE * LOTES_PARALELOS);
        if (itemIds.length === 0) {
            console.log('catalogo vazio, aguardando...');
            return;
        }

        // divide em lotes de 40 e busca todos em paralelo
        const lotes = [];
        for (let i = 0; i < itemIds.length; i += TAMANHO_LOTE) {
            lotes.push(itemIds.slice(i, i + TAMANHO_LOTE));
        }

        const resultados = await Promise.all(lotes.map(buscarPrecos));
        const todosPrecos = resultados.flat();

        await salvarPrecos(todosPrecos);
        await marcarComoColetado(itemIds);

        console.log('lote coletado:', itemIds.length, 'itens |', todosPrecos.length, 'precos salvos');
    } catch (err) {
        console.error('erro no ciclo de coleta:', err.message);
    }
}

// inicia o worker em loop com delay entre cada ciclo
function iniciarWorker() {
    console.log('worker de coleta iniciado');

    // limpeza do historico uma vez por dia a meia noite
    const agora = new Date();
    const meianoite = new Date();
    meianoite.setHours(24, 0, 0, 0);
    const msAteMeianoite = meianoite - agora;

    setTimeout(() => {
        limparHistoricoAntigo();
        setInterval(limparHistoricoAntigo, 24 * 60 * 60 * 1000);
    }, msAteMeianoite);

    // loop de coleta
    const loop = async () => {
        await cicloColeta();
        setTimeout(loop, DELAY_ENTRE_LOTES_MS);
    };

    loop();
}

module.exports = { iniciarWorker };