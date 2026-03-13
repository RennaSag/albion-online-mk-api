package com.albionmarket.model;

/** metadados das cidades do jogo (ID da API, nome de exibição, cor do marcador). */
public class CidadeInfo {

    private final String apiId;   // ID usado na API pra idenditificar a cidade
    private final String nome;    // nome de exibição em portugues
    private final String cor;     // cor hexadecimal para o marcador visual

    public CidadeInfo(String apiId, String nome, String cor) {
        this.apiId = apiId;
        this.nome  = nome;
        this.cor   = cor;
    }

    public String getApiId() { return apiId; }
    public String getNome()  { return nome; }
    public String getCor()   { return cor; }

    @Override
    public String toString() { return nome; }
}
