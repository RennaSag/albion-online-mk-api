package com.albionmarket.model;

/**
 * carrega o estado dos filtros da TelaCraftSelecao
 * para restaurar quando o usuário clicar em Voltar na TelaCraft.
 */
public class EstadoCraftSelecao {

    public final ItemDefinition item;
    public final int            tier;
    public final int            enchant;
    public final String         textoBusca;

    public EstadoCraftSelecao(ItemDefinition item, int tier, int enchant, String textoBusca) {
        this.item       = item;
        this.tier       = tier;
        this.enchant    = enchant;
        this.textoBusca = textoBusca;
    }
}