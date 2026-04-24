package com.albionmarket.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import com.google.gson.*;

public class AutoUpdater {

    private static final String VERSAO_ATUAL = "1.0.0";
    private static final String URL_VERSION  =
            "https://albion-licencas-api.onrender.com/version";

    public static void verificar(Stage palco) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(URL_VERSION))
                        .timeout(Duration.ofSeconds(8))
                        .GET().build();

                HttpResponse<String> resp =
                        client.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() != 200) return;

                JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
                String versaoServidor = json.get("version").getAsString();
                String downloadUrl   = json.get("downloadUrl").getAsString();

                if (!versaoServidor.equals(VERSAO_ATUAL)) {
                    Platform.runLater(() ->
                            new TelaUpdate(palco, downloadUrl, versaoServidor).mostrar()
                    );
                }

            } catch (Exception e) {
                // sem internet ou servidor fora ignora e abre normalmente
            }
        }, "thread-updater").start();
    }
}