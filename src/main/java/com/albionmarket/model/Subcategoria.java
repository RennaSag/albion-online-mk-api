package com.albionmarket.model;

import java.util.List;

/**
 * subcategoria dentro de uma categoria categoria Armas Corpo a Corpo>Espada
 */
public class Subcategoria {

    private final String nome; //nome do material ou materiais
    private final List<ItemDefinition> itens; //vetor o/os recursos da receita

    public Subcategoria(String nome, List<ItemDefinition> itens) {
        this.nome = nome;
        this.itens = itens;
    }

    public String getNome() {
        return nome;
    }

    public List<ItemDefinition> getItens() {
        return itens;
    }

    @Override
    public String toString() {
        return nome;
    }
}
