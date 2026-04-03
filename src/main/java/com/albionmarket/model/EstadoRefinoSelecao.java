package com.albionmarket.model;

import java.util.List;

/**
 * Mantém o estado dos filtros da TelaRefinoSelecao
 * para restaurá-los quando o usuário clicar em Voltar na TelaRefino.
 */
public class EstadoRefinoSelecao {

    public final ItemDefinition item;
    public final int tier;
    public final int enchant;
    public final String textoBusca;
    public final List<String> cidades;

    public EstadoRefinoSelecao(ItemDefinition item, int tier, int enchant,
                               String textoBusca, List<String> cidades) {
        this.item       = item;
        this.tier       = tier;
        this.enchant    = enchant;
        this.textoBusca = textoBusca;
        this.cidades    = cidades;
    }
}