package com.albionmarket.model;

import java.util.List;

/**
 * Mantém o estado dos filtros das telas de seleção
 * (craft e refino) para restaurar ao clicar Voltar.
 * Substitui EstadoCraftSelecao e EstadoRefinoSelecao que eram idênticas.
 */
public class EstadoSelecao {

    public final ItemDefinition item;
    public final int tier;
    public final int enchant;
    public final String textoBusca;
    public final List<String> cidades;

    public EstadoSelecao(ItemDefinition item, int tier, int enchant,
                         String textoBusca, List<String> cidades) {
        this.item       = item;
        this.tier       = tier;
        this.enchant    = enchant;
        this.textoBusca = textoBusca;
        this.cidades    = cidades;
    }
}