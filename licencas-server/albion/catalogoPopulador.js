const { query } = require('./db');

// url do dump da comunidade com todos os itens do jogo
const URL_ITENS = 'https://raw.githubusercontent.com/ao-data/ao-bin-dumps/master/formatted/items.json';

async function popularCatalogo() {
    console.log('iniciando populacao do catalogo de itens...');

    try {
        const resposta = await fetch(URL_ITENS);
        if (!resposta.ok) {
            console.error('erro ao buscar lista de itens, status:', resposta.status);
            return;
        }

        const itens = await resposta.json();
        console.log('total de itens encontrados no dump:', itens.length);

        let inseridos = 0;
        let ignorados = 0;

        for (const item of itens) {
            const itemId = item?.UniqueName;
            if (!itemId) continue;

            // ignora itens sem tier no nome (montarias especiais, itens de evento etc)
            if (!itemId.startsWith('T')) {
                ignorados++;
                continue;
            }

            await query(
                `INSERT INTO itens_catalogo (item_id)
                 VALUES ($1)
                 ON CONFLICT (item_id) DO NOTHING`,
                [itemId]
            );
            inseridos++;
        }

        console.log('catalogo populado. inseridos:', inseridos, '| ignorados:', ignorados);
    } catch (err) {
        console.error('erro ao popular catalogo:', err.message);
    }
}

module.exports = { popularCatalogo };