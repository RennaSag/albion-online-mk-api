package com.albionmarket.model;

import java.util.List;

/** aba de seleção das categoria principais, como "Armas Corpo a Corpo", "Cajados", "Armaduras" etc */
public class Categoria {

    private final String icone;
    private final String nome;
    private final List<Subcategoria> subcategorias;

    public Categoria(String icone, String nome, List<Subcategoria> subcategorias) {
        // aqui o ícon eu deixei vazio "" na classe Subcategoria.java, podendo colocar um iconezinho caso queira
        this.icone = icone;
        this.nome = nome;
        this.subcategorias = subcategorias;
    }

    public String getIcone()                          { return icone; }
    public String getNome()                           { return nome; }
    public List<Subcategoria> getSubcategorias()      { return subcategorias; }

    @Override
    public String toString() { return icone + " " + nome; }
}
