package com.albionmarket.service;

import com.albionmarket.model.ItemDefinition;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço de busca fuzzy de itens por texto digitado
 * Suporta busca parcial por nome, keywords e ID, com tolerância a acentos, mas ainda não testei tudo
 * pra ver se essa busca ta bala mesmo
 */
public class BuscaService {

    private final List<ItemDefinition> todosItens;

    public BuscaService() {
        this.todosItens = BancoDeDadosCraft.getTodosItens();
    }

    /**
     * Busca itens cujo nome, keywords ou ID contenham o texto informado
     *
     * @param query  texto digitado pelo usuário
     * @param limite número máximo de resultados
     * @return lista de ItemDefinition ordenada por relevância
     */

    public List<ItemDefinition> buscar(String query, int limite) {
        if (query == null || query.trim().length() < 2) return List.of();

        String q = normalizar(query.trim());
        String[] palavras = q.split("\\s+");

        List<ResultadoInterno> candidatos = new ArrayList<>();

        for (ItemDefinition item : todosItens) {
            String campo = normalizar(item.getNome() + " " + item.getKeywords() + " " + item.getId());
            int pontuacao = 0;
            //variavel pra pontuação da busca fuzzy, igual matéria de IA da faculdade, como dar doce pra criança quando ela se comporta

            for (String palavra : palavras) {
                if (campo.contains(palavra)) pontuacao += palavra.length();
            }

            // bonus se o nome começa exatamente com a query
            if (normalizar(item.getNome()).equals(q)) pontuacao += 50;
            if (normalizar(item.getNome()).startsWith(q)) pontuacao += 20;
            if (pontuacao > 0) candidatos.add(new ResultadoInterno(item, pontuacao));


        }

        candidatos.sort(Comparator.comparingInt(ResultadoInterno::getPontuacao).reversed());

        List<ItemDefinition> resultado = new ArrayList<>();
        for (int i = 0; i < Math.min(limite, candidatos.size()); i++) {
            resultado.add(candidatos.get(i).getItem());
        }
        return resultado;
    }

     // remove acentos e converte para lowercase para comparacao normalizada
    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();
    }

    // classe interna de suporte para ordenação por pontuação
    private static class ResultadoInterno {
        private final ItemDefinition item;
        private final int pontuacao;

        ResultadoInterno(ItemDefinition item, int pontuacao) {
            this.item = item;
            this.pontuacao = pontuacao;
        }

        ItemDefinition getItem() {
            return item;
        }

        int getPontuacao() {
            return pontuacao;
        }
    }
}
