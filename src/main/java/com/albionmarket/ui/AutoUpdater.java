package com.albionmarket.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import com.google.gson.*;

public class AutoUpdater {

    // versao embutida no build
    private static final String VERSAO_ATUAL = "1.0.1";

    // endpoint da api do github releases - troque pelo seu usuario e repositorio
    private static final String URL_GITHUB_API =
            "https://api.github.com/repos/RennaSag/albion-online-mk-api/releases/latest";

    public static void verificar(Stage palco) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(URL_GITHUB_API))
                        .timeout(Duration.ofSeconds(8))
                        // header recomendado pela api do github
                        .header("Accept", "application/vnd.github+json")
                        .GET()
                        .build();

                HttpResponse<String> resp =
                        client.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() != 200) return;

                JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

                // tag_name vem no formato "v1.2.0" - remove o "v" pra comparar
                String tagName = json.get("tag_name").getAsString();
                String versaoServidor = tagName.startsWith("v")
                        ? tagName.substring(1)
                        : tagName;

                if (versaoServidor.equals(VERSAO_ATUAL)) return;

                // pega a url de download do primeiro asset .msi da release
                JsonArray assets = json.getAsJsonArray("assets");
                String downloadUrl = null;

                for (JsonElement el : assets) {
                    JsonObject asset = el.getAsJsonObject();
                    String nome = asset.get("name").getAsString();
                    if (nome.endsWith(".msi")) {
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
                // sem internet ou github fora - ignora e abre normalmente
            }
        }, "thread-updater").start();
    }
}