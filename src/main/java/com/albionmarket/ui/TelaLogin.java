package com.albionmarket.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class TelaLogin {

    private static final String USUARIO_PADRAO = "admin";
    private static final String SENHA_PADRAO = "admin";
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

        Label subtitulo = new Label("Consulta de Preços do Mercado");
        subtitulo.getStyleClass().add("login-subtitulo");

        Label labelUser = new Label("Usuário");
        labelUser.getStyleClass().add("login-label");

        TextField campoUsuario = new TextField();
        campoUsuario.setPromptText("Digite seu usuário");
        campoUsuario.getStyleClass().add("login-campo");
        campoUsuario.setMaxWidth(280);

        Label labelSenha = new Label("Senha");
        labelSenha.getStyleClass().add("login-label");

        PasswordField campoSenha = new PasswordField();
        campoSenha.setPromptText("Digite sua senha");
        campoSenha.getStyleClass().add("login-campo");
        campoSenha.setMaxWidth(280);

        CheckBox lembrar = new CheckBox("Lembrar de mim");
        lembrar.setStyle("-fx-text-fill: #ccc;");

        String usuarioSalvo = prefs.get("usuario", "");
        String senhaSalva = prefs.get("senha", "");
        if (!usuarioSalvo.isEmpty()) {
            campoUsuario.setText(usuarioSalvo);
            campoSenha.setText(senhaSalva);
            lembrar.setSelected(true);
        }

        Label msgErro = new Label();
        msgErro.getStyleClass().add("login-erro");
        msgErro.setVisible(false);

        Button btnEntrar = new Button("Entrar");
        btnEntrar.getStyleClass().add("login-botao");
        btnEntrar.setMaxWidth(280);
        btnEntrar.setDefaultButton(true);

        Button btnSair = new Button("Sair");
        btnSair.getStyleClass().add("login-botao");
        btnSair.setMaxWidth(280);

        Runnable acaoLogin = () -> {
            String usuario = campoUsuario.getText().trim();
            String senha = campoSenha.getText();
            if (USUARIO_PADRAO.equals(usuario) && SENHA_PADRAO.equals(senha)) {
                if (lembrar.isSelected()) {
                    prefs.put("usuario", usuario);
                    prefs.put("senha", senha);
                } else {
                    prefs.remove("usuario");
                    prefs.remove("senha");
                }
                abrirTelaHome();
            }
        };

        btnEntrar.setOnAction(e -> acaoLogin.run());
        btnSair.setOnAction(e -> System.exit(0));
        campoSenha.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) acaoLogin.run();
        });

        VBox form = new VBox(8,
                labelUser, campoUsuario,
                labelSenha, campoSenha,
                msgErro, lembrar,
                btnEntrar, btnSair
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(280);

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

    private void abrirTelaHome() {
        new TelaHome(palco).mostrar();
    }
}