package com.albionmarket.service;

import javafx.scene.image.Image;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IconeCacheService {

    private static final Path DIR_CACHE = Paths.get(
            System.getenv("LOCALAPPDATA"), "AlbionMarket", "icones");

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // pool pequeno e dedicado só pra gravar o cache em background — não deve
    // competir com as buscas de preço/receita que já usam seus próprios pools
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    // evita baixar o mesmo icone duas vezes em paralelo se ele aparecer
    // em mais de uma linha/tabela ao mesmo tempo
    private static final Set<String> EM_DOWNLOAD = ConcurrentHashMap.newKeySet();

    private IconeCacheService() {
    }

    /**
     * Equivalente a {@code new Image(url, backgroundLoading)}, mas usando o cache local
     * quando disponível.
     */
    public static Image obterIcone(String url, boolean backgroundLoading) {
        Path arquivo = caminhoCache(url);
        if (arquivo != null && Files.isRegularFile(arquivo)) {
            return new Image(arquivo.toUri().toString());
        }
        agendarDownload(url, arquivo);
        return new Image(url, backgroundLoading);
    }

    /**
     * Equivalente a {@code new Image(url, w, h, preserveRatio, smooth, backgroundLoading)},
     * mas usando o cache local quando disponível.
     */
    public static Image obterIcone(String url, double largura, double altura,
                                   boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
        Path arquivo = caminhoCache(url);
        if (arquivo != null && Files.isRegularFile(arquivo)) {
            return new Image(arquivo.toUri().toString(), largura, altura, preserveRatio, smooth, false);
        }
        agendarDownload(url, arquivo);
        return new Image(url, largura, altura, preserveRatio, smooth, backgroundLoading);
    }

    private static final int MAX_TENTATIVAS = 3;

    private static void agendarDownload(String url, Path destino) {
        if (destino == null) return;
        if (!EM_DOWNLOAD.add(url)) return; // ja tem um download desse icone em andamento

        POOL.submit(() -> {
            try {
                // o servidor de imagens da albion e instavel (504/404 momentaneos sob
                // carga) — tenta de novo com um pequeno atraso antes de desistir
                for (int tentativa = 0; tentativa < MAX_TENTATIVAS; tentativa++) {
                    try {
                        if (tentativa > 0) Thread.sleep(tentativa * 1000L);

                        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                                .timeout(Duration.ofSeconds(10))
                                .GET().build();
                        HttpResponse<byte[]> resp = CLIENTE.send(req, HttpResponse.BodyHandlers.ofByteArray());

                        if (resp.statusCode() == 200 && resp.body().length > 0) {
                            Files.createDirectories(destino.getParent());
                            Files.write(destino, resp.body());
                            return;
                        }
                        // o mesmo icone pode responder 404 e depois 200 em tentativas
                        // diferentes (servidor gerando a imagem sob demanda), entao
                        // vale tentar de novo mesmo em erro "nao encontrado"
                    } catch (Exception ignored) {
                        // tenta de novo, ou desiste na proxima aparicao do icone
                    }
                }
            } finally {
                EM_DOWNLOAD.remove(url);
            }
        });
    }

    // deriva um nome de arquivo unico e seguro a partir da url do icone
    private static Path caminhoCache(String url) {
        if (url == null || url.isBlank()) return null;
        String nome = url
                .replaceFirst("^https?://", "")
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        return DIR_CACHE.resolve(nome);
    }
}
