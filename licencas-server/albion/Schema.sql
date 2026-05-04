-- catalogo de todos os itens do jogo
-- populado uma vez na inicializacao e atualizado quando sair patch
CREATE TABLE IF NOT EXISTS itens_catalogo (
    item_id       VARCHAR(100) PRIMARY KEY,
    ultima_coleta TIMESTAMPTZ
);

-- preco mais recente de cada item por cidade e qualidade
-- upsert: substitui sempre que chegar preco novo
CREATE TABLE IF NOT EXISTS precos_atual (
    item_id    VARCHAR(100) NOT NULL,
    cidade     VARCHAR(50)  NOT NULL,
    qualidade  SMALLINT     NOT NULL,
    sell_min   BIGINT       DEFAULT 0,
    buy_max    BIGINT       DEFAULT 0,
    sell_date  TIMESTAMPTZ,
    buy_date   TIMESTAMPTZ,
    updated_at TIMESTAMPTZ  DEFAULT NOW(),
    PRIMARY KEY (item_id, cidade, qualidade)
);

-- historico dos ultimos 7 dias
-- nunca faz update, so insert
-- job de limpeza deleta o que tiver com mais de 7 dias
CREATE TABLE IF NOT EXISTS precos_historico (
    id          BIGSERIAL   PRIMARY KEY,
    item_id     VARCHAR(100) NOT NULL,
    cidade      VARCHAR(50)  NOT NULL,
    qualidade   SMALLINT     NOT NULL,
    sell_min    BIGINT       DEFAULT 0,
    buy_max     BIGINT       DEFAULT 0,
    coletado_em TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_hist_item_cidade ON precos_historico(item_id, cidade);
CREATE INDEX IF NOT EXISTS idx_hist_tempo       ON precos_historico(item_id, coletado_em DESC);
CREATE INDEX IF NOT EXISTS idx_catalogo_coleta  ON itens_catalogo(ultima_coleta NULLS FIRST);
