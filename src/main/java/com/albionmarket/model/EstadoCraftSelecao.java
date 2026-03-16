package com.albionmarket.model;

import java.util.List;

public class EstadoCraftSelecao {

    public final ItemDefinition item;
    public final int tier;
    public final int enchant;
    public final String textoBusca;
    public final List<String> cidades;

    // construtor antigo sem cidades, para compatibilidade
    /*
    public EstadoCraftSelecao(ItemDefinition item, int tier, int enchant, String textoBusca) {
        this.item       = item;
        this.tier       = tier;
        this.enchant    = enchant;
        this.textoBusca = textoBusca;
        this.cidades    = null;
    }
    */


    // construtor novo com cidades
    public EstadoCraftSelecao(ItemDefinition item, int tier, int enchant,
                              String textoBusca, List<String> cidades) {
        this.item = item;
        this.tier = tier;
        this.enchant = enchant;
        this.textoBusca = textoBusca;
        this.cidades = cidades;
    }
}