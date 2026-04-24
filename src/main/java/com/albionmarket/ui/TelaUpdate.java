package com.albionmarket.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;

public class TelaUpdate {

    private final Stage palco;
    private final String downloadUrl;
    private final String novaVersao;

    public TelaUpdate(Stage palco, String downloadUrl, String novaVersao) {
        this.palco       = palco;
        this.downloadUrl = downloadUrl;
        this.novaVersao  = novaVersao;
    }

    public void mostrar() {
        Label titulo = new Label("Atualizacao Disponivel");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label info = new Label("Nova versao " + novaVersao + " encontrada.\nBaixando automaticamente...");
        info.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
        info.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        ProgressBar barra = new ProgressBar(0);
        barra.setPrefWidth(400);
        barra.setPrefHeight(20);
        barra.setStyle("-fx-accent: #5a8dee;");

        Label labelPct = new Label("0%");
        labelPct.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        Label labelStatus = new Label("Conectando...");
        labelStatus.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        VBox raiz = new VBox(16, titulo, info, barra, labelPct, labelStatus);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(50));
        raiz.setStyle("-fx-background-color: #1e1e1e;");

        palco.getScene().setRoot(raiz);

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(15))
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(downloadUrl))
                        .timeout(Duration.ofMinutes(5))
                        .GET()
                        .build();

                // destino na pasta temp do sistema
                Path destino = Path.of(
                        System.getProperty("java.io.tmpdir"),
                        "AlbionMarketUpdate.msi");

                Platform.runLater(() -> labelStatus.setText("Baixando..."));

                HttpResponse<InputStream> resp =
                        client.send(req, HttpResponse.BodyHandlers.ofInputStream());

                long total = resp.headers()
                        .firstValueAsLong("content-length")
                        .orElse(-1L);

                try (InputStream in  = resp.body();
                     OutputStream out = Files.newOutputStream(destino)) {

                    byte[] buf = new byte[8192];
                    long baixado = 0;
                    int lido;

                    while ((lido = in.read(buf)) != -1) {
                        out.write(buf, 0, lido);
                        baixado += lido;

                        if (total > 0) {
                            double pct = (double) baixado / total;
                            long baixadoFinal = baixado;
                            Platform.runLater(() -> {
                                barra.setProgress(pct);
                                labelPct.setText(String.format("%.0f%%", pct * 100));
                                labelStatus.setText(
                                        formatarBytes(baixadoFinal) + " / " + formatarBytes(total));
                            });
                        }
                    }
                }

                Platform.runLater(() -> {
                    barra.setProgress(1.0);
                    labelPct.setText("100%");
                    labelStatus.setText("Download concluido. Instalando...");
                });

                // grava o bat intermediario que:
                // 1. espera o processo do app fechar (timeout de 10s)
                // 2. executa o msi silenciosamente
                // 3. abre o novo exe apos instalar
                // 4. se deleta
                Path batPath = Path.of(System.getProperty("java.io.tmpdir"), "albion_update.bat");

                // pega o caminho do exe atual pra reabrir depois da instalacao
                String exeAtual = ProcessHandle.current()
                        .info()
                        .command()
                        .orElse("");

                // pasta de instalacao padrao - ajuste se o seu instalador usa outra pasta
                String pastaInstalacao = System.getenv("ProgramFiles") + "\\AlbionMarket";
                String exeNovo = pastaInstalacao + "\\AlbionMarket.exe";

                String conteudoBat =
                        "@echo off\r\n" +
                                // espera o processo java/javaw fechar - timeout de 10 segundos
                                "timeout /t 2 /nobreak >nul\r\n" +
                                // instala silenciosamente sem reiniciar
                                "msiexec /i \"" + destino.toString() + "\" /quiet /norestart\r\n" +
                                // aguarda o instalador terminar
                                "timeout /t 3 /nobreak >nul\r\n" +
                                // abre o novo exe se existir
                                "if exist \"" + exeNovo + "\" start \"\" \"" + exeNovo + "\"\r\n" +
                                // remove o bat e o msi baixado
                                "del \"" + batPath.toString() + "\"\r\n" +
                                "del \"" + destino.toString() + "\"\r\n";

                Files.writeString(batPath, conteudoBat);

                // executa o bat em segundo plano e fecha o app
                Runtime.getRuntime().exec(new String[]{
                        "cmd.exe", "/c", "start", "/b", batPath.toString()
                });

                // pequena pausa pra garantir que o cmd iniciou antes do exit
                Thread.sleep(500);

                Platform.runLater(Platform::exit);

            } catch (Exception e) {
                Platform.runLater(() -> {
                    labelStatus.setText("Erro no update: " + e.getMessage()
                            + "\nO programa continuara normalmente.");

                    // apos 3s vai pra tela de login mesmo com erro
                    new Thread(() -> {
                        try { Thread.sleep(3000); } catch (Exception ignored) {}
                        Platform.runLater(() -> new TelaLogin(palco).mostrar());
                    }).start();
                });
            }
        }, "thread-download").start();
    }

    private String formatarBytes(long bytes) {
        if (bytes >= 1_000_000) return String.format("%.1f MB", bytes / 1_000_000.0);
        if (bytes >= 1_000)     return String.format("%.0f KB", bytes / 1_000.0);
        return bytes + " B";
    }
}