package com.albionmarket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.prefs.Preferences;

public class TelaLogin {

    private final Preferences prefs = Preferences.userNodeForPackage(TelaLogin.class);
    private final Stage palco;

    public TelaLogin(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        Label titulo = new Label("Albion Online Market");
        palco.setMinWidth(1280);
        palco.setMinHeight(720);
        titulo.getStyleClass().add("login-titulo");

        Label subtitulo = new Label("Consulta de Precos do Mercado");
        subtitulo.getStyleClass().add("login-subtitulo");

        Label labelChave = new Label("Chave de Licenca");
        labelChave.getStyleClass().add("login-label");

        TextField campoChave = new TextField();
        campoChave.setPromptText("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
        campoChave.getStyleClass().add("login-campo");
        campoChave.setMaxWidth(360);

        CheckBox lembrar = new CheckBox("Lembrar minha chave");
        lembrar.setStyle("-fx-text-fill: #ccc;");

        String chaveSalva = prefs.get("chave_licenca", "");
        if (!chaveSalva.isEmpty()) {
            campoChave.setText(chaveSalva);
            lembrar.setSelected(true);
        }

        Label msgErro = new Label();
        msgErro.getStyleClass().add("login-erro");
        msgErro.setVisible(false);

        Button btnEntrar = new Button("Entrar");
        btnEntrar.getStyleClass().add("login-botao");
        btnEntrar.setMaxWidth(360);
        btnEntrar.setDefaultButton(true);

        Button btnSair = new Button("Sair");
        btnSair.getStyleClass().add("login-botao");
        btnSair.setMaxWidth(360);
        btnSair.setOnAction(e -> System.exit(0));

        Runnable acaoLogin = () -> {
            String chave = campoChave.getText().trim();
            if (chave.isBlank()) {
                msgErro.setText("Digite sua chave de licenca.");
                msgErro.setVisible(true);
                return;
            }

            btnEntrar.setDisable(true);
            msgErro.setText("Validando...");
            msgErro.setStyle("-fx-text-fill: #888;");
            msgErro.setVisible(true);

            new Thread(() -> {
                try {
                    HttpClient cliente = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(10))
                            .build();

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create("https://albion-licencas-api.onrender.com/validar?chave=" + chave))
                            .timeout(Duration.ofSeconds(15))
                            .GET()
                            .build();

                    HttpResponse<String> resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());
                    JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
                    boolean valido = json.get("valido").getAsBoolean();

                    javafx.application.Platform.runLater(() -> {
                        btnEntrar.setDisable(false);
                        if (valido) {

                            if (lembrar.isSelected()) {
                                prefs.put("chave_licenca", chave);
                            } else {
                                prefs.remove("chave_licenca");
                            }
                            // salva a data de expiracao pra usar na home
                            String expira = json.has("expira") ? json.get("expira").getAsString() : "";
                            prefs.put("licenca_expira", expira);
                            new TelaHome(palco).mostrar();

                        } else {
                            String motivo = json.has("motivo")
                                    ? json.get("motivo").getAsString()
                                    : "chave invalida";
                            msgErro.setText("Acesso negado: " + motivo);
                            msgErro.setStyle("-fx-text-fill: #e05555;");
                            msgErro.setVisible(true);
                        }
                    });

                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        btnEntrar.setDisable(false);
                        msgErro.setText("Erro ao conectar com o servidor de licencas.");
                        msgErro.setStyle("-fx-text-fill: #e05555;");
                        msgErro.setVisible(true);
                    });
                }
            }, "thread-licenca").start();
        };

        btnEntrar.setOnAction(e -> acaoLogin.run());
        campoChave.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) acaoLogin.run();
        });

        VBox form = new VBox(8,
                labelChave, campoChave,
                msgErro, lembrar,
                btnEntrar, btnSair
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(360);

        VBox raiz = new VBox(20, titulo, subtitulo, form);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(60));
        raiz.getStyleClass().add("login-raiz");

        palco.setTitle("Albion Online - Analisador de Mercado");

        if (!palco.isShowing()) {
            Scene cena = new Scene(raiz);
            cena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
            palco.setScene(cena);
            palco.setMaximized(true);
            palco.show();
        } else {
            palco.getScene().setRoot(raiz);
        }
    }
}