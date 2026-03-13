package com.albionmarket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class TelaLogin {

    // credenciais padrão
    private static final String USUARIO_PADRAO = "admin";
    private static final String SENHA_PADRAO   = "admin";

    private final Stage palco;

    public TelaLogin(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        // titulo
        Label titulo = new Label("Albion Online Market");
        titulo.getStyleClass().add("login-titulo");

        Label subtitulo = new Label("Consulta de Preços em Tempo Real");
        subtitulo.getStyleClass().add("login-subtitulo");

        // campos
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

        // mensagem de erro
        Label msgErro = new Label();
        msgErro.getStyleClass().add("login-erro");
        msgErro.setVisible(false);

        // botão entrar
        Button btnEntrar = new Button("Entrar");
        btnEntrar.getStyleClass().add("login-botao");
        btnEntrar.setMaxWidth(280);
        btnEntrar.setDefaultButton(true);

        // ação de login
        Runnable acaoLogin = () -> {
            String usuario = campoUsuario.getText().trim();
            String senha   = campoSenha.getText();

            if (USUARIO_PADRAO.equals(usuario) && SENHA_PADRAO.equals(senha)) {
                abrirTelaPesquisa();
            } else {
                msgErro.setText("Usuário ou senha incorretos.");
                msgErro.setVisible(true);
                campoSenha.clear();
                campoSenha.requestFocus();
            }
        };

        btnEntrar.setOnAction(e -> acaoLogin.run());

        // enter no campo de senha também faz login
        campoSenha.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) acaoLogin.run();
        });

        // layout
        VBox form = new VBox(8,
                labelUser, campoUsuario,
                labelSenha, campoSenha,
                msgErro,
                btnEntrar
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(280);

        VBox raiz = new VBox(20, titulo, subtitulo, form);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(60));
        raiz.getStyleClass().add("login-raiz");

        Scene cena = new Scene(raiz, 480, 400);
        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );

        palco.setTitle("Albion Market — Login");
        palco.setScene(cena);
        palco.setResizable(false);
        palco.show();
    }

    private void abrirTelaPesquisa() {
        TelaPesquisaPrecos tela = new TelaPesquisaPrecos();

        javafx.scene.Scene cena = new javafx.scene.Scene(tela.getCriarLayout(), 1280, 800);
        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );



        palco.setTitle("Albion Market — Consulta de Preços");
        palco.setScene(cena);
        palco.setResizable(true);
        palco.setMinWidth(900);
        palco.setMinHeight(600);

        //ja abre a tela centralizada e em tela cheia
        palco.centerOnScreen();
        palco.setMaximized(true);
    }



}