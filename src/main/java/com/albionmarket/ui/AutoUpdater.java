package com.albionmarket.ui;

import java.nio.file.*;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

import com.google.gson.*;


public class AutoUpdater {

    private static final String URL_GITHUB_API =
            "https://api.github.com/repos/RennaSag/albion-online-mk-api/releases/latest";

    // lê a versão do arquivo gerado pelo instalador
    private static String lerVersaoAtual() {
        try {
            Path arquivo = Path.of(System.getenv("LOCALAPPDATA"),
                    "AlbionMarket", "version.txt");
            String versao = Files.readString(arquivo).strip();
            System.out.println("[DEBUG] version.txt lido: " + versao);
            return versao;
        } catch (Exception e) {
            System.out.println("[DEBUG] version.txt não encontrado, retornando 0.0.0");
            return "0.0.0";
        }
    }


    public static void verificar(Stage palco) {


        new Thread(() -> {
            try {
                String versaoAtual = lerVersaoAtual();

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(URL_GITHUB_API))
                        .timeout(Duration.ofSeconds(8))
                        .header("Accept", "application/vnd.github+json")
                        .GET()
                        .build();

                HttpResponse<String> resp =
                        client.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() != 200) return;

                JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

                String tagName = json.get("tag_name").getAsString();
                String versaoServidor = tagName.startsWith("v")
                        ? tagName.substring(1) : tagName;


                try {
                    Path logDir = Path.of(System.getenv("LOCALAPPDATA"), "AlbionMarket");
                    Files.createDirectories(logDir);
                    Files.writeString(logDir.resolve("debug_update.txt"),
                            "versaoAtual: '" + versaoAtual + "'\n" +
                                    "versaoServidor: '" + versaoServidor + "'\n" +
                                    "isNova: " + isVersaoMaisNova(versaoServidor, versaoAtual) + "\n"
                    );
                } catch (Exception ignored) {}

                if (!isVersaoMaisNova(versaoServidor, versaoAtual)) return;

                JsonArray assets = json.getAsJsonArray("assets");
                String downloadUrl = null;

                for (JsonElement el : assets) {
                    JsonObject asset = el.getAsJsonObject();
                    if (asset.get("name").getAsString().endsWith(".msi")) {
                        downloadUrl = asset.get("browser_download_url").getAsString();
                        break;
                    }
                }

                if (downloadUrl == null) return;

                final String urlFinal = downloadUrl;
                final String versaoFinal = versaoServidor;

                Platform.runLater(() ->
                        new TelaUpdate(palco, urlFinal, versaoFinal).mostrar()
                );

            } catch (Exception e) {
                // sem internet - ignora
            }
        }, "thread-updater").start();



    }

    // compara versões semanticamente
    private static boolean isVersaoMaisNova(String servidor, String atual) {
        try {
            int[] s = parseVersao(servidor);
            int[] a = parseVersao(atual);
            for (int i = 0; i < 3; i++) {
                if (s[i] > a[i]) return true;
                if (s[i] < a[i]) return false;
            }
            return false;
        } catch (Exception e) {
            return !servidor.equals(atual);
        }
    }

    private static int[] parseVersao(String v) {
        String[] partes = v.split("\\.");
        return new int[]{
                Integer.parseInt(partes[0]),
                Integer.parseInt(partes.length > 1 ? partes[1] : "0"),
                Integer.parseInt(partes.length > 2 ? partes[2] : "0")
        };
    }
}