package com.albionmarket.model;

import java.util.List;

public class VersaoInfo {


    public enum TipoMudanca {
        NOVIDADE,    // ✦ funcionalidade nova
        MELHORIA,    // ↑ melhoria de algo existente
        CORRECAO,    // ✓ bug corrigido
        REMOCAO      // ✗ algo removido
    }


    public static class Mudanca {
        public final TipoMudanca tipo;
        public final String descricao;

        public Mudanca(TipoMudanca tipo, String descricao) {
            this.tipo = tipo;
            this.descricao = descricao;
        }
    }


    private final String versao;          // ex: "1.2.0"
    private final String data;            // ex: "Abril 2025"
    private final String titulo;          // ex: "Grande Atualização de Craft"
    private final List<Mudanca> mudancas;

    public VersaoInfo(String versao, String data, String titulo, List<Mudanca> mudancas) {
        this.versao = versao;
        this.data = data;
        this.titulo = titulo;
        this.mudancas = mudancas;
    }

    public String getVersao() {
        return versao;
    }

    public String getData() {
        return data;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<Mudanca> getMudancas() {
        return mudancas;
    }
}