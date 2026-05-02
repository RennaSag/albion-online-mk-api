package com.albionmarket.service;

import com.albionmarket.util.FormatadorUtil;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Responsável por tudo que envolve operações salvas em disco:
 * onde ficam, como salvar e como montar o JSON.
 */
public class OperacaoService {

    private OperacaoService() {}

    /**
     * Retorna o diretório onde as operações são salvas,
     * criando-o se não existir.
     */
    public static Path getDiretorio() {
        Path dir = Paths.get(
                System.getenv("LOCALAPPDATA"), "AlbionMarket", "operacoes"
        );
        try {
            Files.createDirectories(dir);
        } catch (Exception ignored) {}
        return dir;
    }

    /**
     * Salva um JSON de operação no diretório padrão.
     *
     * @param itemIdCompleto  ex: "T5_MAIN_SWORD@2"
     * @param conteudoJson    string JSON já montada
     * @return nome do arquivo salvo
     */
    public static String salvar(String itemIdCompleto, String conteudoJson) throws IOException {
        String nomeArquivo = "operacao_" + itemIdCompleto + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".json";
        Files.writeString(getDiretorio().resolve(nomeArquivo), conteudoJson);
        return nomeArquivo;
    }

    /**
     * Monta o trecho JSON com os locais de compra de cada material.
     * Recebe uma lista de linhas no formato: nome, cidade, quantidade, tipo.
     */
    public static String montarLocaisJson(java.util.List<LinhaLocais> linhas) {
        StringBuilder sb = new StringBuilder("[");
        boolean primeiro = true;
        for (LinhaLocais l : linhas) {
            if (!primeiro) sb.append(", ");
            primeiro = false;
            sb.append("{\"material\": \"").append(l.nome.replace("\"", "\\\""))
                    .append("\", \"quantidade\": ").append(l.quantidade)
                    .append(", \"cidade\": \"").append(l.nomeCidade).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Estrutura simples para montar o JSON de locais. */
    public static class LinhaLocais {
        public final String nome;
        public final int quantidade;
        public final String nomeCidade;

        public LinhaLocais(String nome, int quantidade, String nomeCidade) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.nomeCidade = nomeCidade;
        }
    }
}