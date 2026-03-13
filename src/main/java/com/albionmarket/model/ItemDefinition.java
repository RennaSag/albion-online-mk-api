package com.albionmarket.model;

/**
 * itens do banco de dados com seu sufixo de ID, nome em portugues e palavras chave para busca
 */
public class ItemDefinition {

    private final String id;       // sufixo da API, "MAIN_SWORD", "2H_CLAYMORE"
    private final String nome;     // nome oficial na tradução em portugues
    private final String keywords; // palavras extras para busca fuzzy

    public ItemDefinition(String id, String nome, String keywords) {
        this.id = id;
        this.nome = nome;
        this.keywords = keywords;
    }

    /** monta o ID completo para a API com tier e encantamento sintaxe: T5_MAIN_SWORD@2, tier, iten, encantamento*/
    public String buildApiId(int tier, int encantamento) {
        String base = "T" + tier + "_" + id;
        return encantamento > 0 ? base + "@" + encantamento : base;
    }

    public String getId()       { return id; }
    public String getNome()     { return nome; }
    public String getKeywords() { return keywords; }

    @Override
    public String toString() { return nome; }
}
