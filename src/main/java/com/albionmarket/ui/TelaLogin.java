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
import javafx.scene.control.CheckBox;

import java.util.prefs.Preferences;

public class TelaLogin {

    /* credenciais padrão para login
        a ideia é integrar isso com a API do HotMart pra vender esse software e
        a pessoa só ter acesso se ela comprar, pq eu tbm quero ganhar dinheiro
     */
    private static final String USUARIO_PADRAO = "admin"; //por equanto n tem nenhuma validação
    private static final String SENHA_PADRAO = "admin";
    private final Preferences prefs = Preferences.userNodeForPackage(TelaLogin.class);

    private final Stage palco;

    public TelaLogin(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        // titulo
        Label titulo = new Label("Albion Online Market");
        titulo.getStyleClass().add("login-titulo");

        Label subtitulo = new Label("Consulta de Preços do Mercado");
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

        //guardar login, caixa de selecao
        CheckBox lembrar = new CheckBox("Lembrar de mim");
        lembrar.setStyle("-fx-text-fill: #ccc;");


        String usuarioSalvo = prefs.get("usuario", "");
        String senhaSalva = prefs.get("senha", "");

        // ele salva pra eu n ter q digitar toda vez, botão lembrar de mim
        if (!usuarioSalvo.isEmpty()) {
            campoUsuario.setText(usuarioSalvo);
            campoSenha.setText(senhaSalva);
            lembrar.setSelected(true);
        }

        // mensagem de erro
        Label msgErro = new Label();
        msgErro.getStyleClass().add("login-erro");
        msgErro.setVisible(false);

        // botão entrar
        Button btnEntrar = new Button("Entrar");
        btnEntrar.getStyleClass().add("login-botao");
        btnEntrar.setMaxWidth(280);
        btnEntrar.setDefaultButton(true);

        //botao pra sair
        Button btnSair = new Button("Sair");
        btnSair.getStyleClass().add("login-botao");
        btnSair.setMaxWidth(280);
        btnSair.setDefaultButton(true);

        // ação de login
        Runnable acaoLogin = () -> {
            String usuario = campoUsuario.getText().trim();
            String senha = campoSenha.getText();

            //condicional pra abrir a tela se o login estiver correto
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

        //acao de sair
        Runnable acaoSair = () -> {
            System.exit(0);
        };


        //acao aplicada nos botoes
        btnEntrar.setOnAction(e -> acaoLogin.run());
        btnSair.setOnAction(e -> acaoSair.run());


        // enter no campo de senha também faz login
        campoSenha.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) acaoLogin.run();
        });

        // layout
        VBox form = new VBox(8,
                labelUser, campoUsuario,
                labelSenha, campoSenha,
                msgErro,
                lembrar,
                btnEntrar,
                btnSair
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(280);

        VBox raiz = new VBox(20, titulo, subtitulo, form);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(60));
        raiz.getStyleClass().add("login-raiz");


        Scene cena = new Scene(raiz);
        //sem tamanho fixo pq o main ja traz tudo em tela cheia
        // Scene cena = new Scene(raiz, 1920, 1080);


        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );

        palco.setTitle("Albion Market - Login");
        palco.setScene(cena);

        //ta redundante? ta, mas se n colocar isso ele n traz em tela cheia sempre, mt paia, ent deixa assim msm
        // repeti isso em outras funcoes de botões voltar
        palco.setMaximized(false);
        palco.setMaximized(true);
        palco.show();
    }

    private void abrirTelaHome() {
        new TelaHome(palco).mostrar();
        /* definido no Main q sempre vai abrir maximizado
         */
    }

}