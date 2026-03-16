package com.albionmarket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

import com.albionmarket.ui.TelaLogin;

/**
 * tela inicial pós-login
 */
public class TelaHome {

    private final Stage palco;

    public TelaHome(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        Label titulo = new Label("O que vamos fazer hoje?");
        titulo.getStyleClass().add("home-titulo");

        Button btnPesquisar = criarBotao("Pesquisar", "home-botao");
        Button btnCraftar = criarBotao("Craftar", "home-botao");
        Button btnRefinar = criarBotao("Refinar", "home-botao");
        Button btnOperacoes = criarBotao("Operações Ativas", "home-botao");

        btnPesquisar.setOnAction(e -> {
            Scene cena = new Scene(new TelaPesquisaPrecos().getCriarLayout());
            cena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
            palco.setTitle("Albion Market - Consulta de Preços");
            palco.setScene(cena);
            palco.setScene(cena);
            palco.setResizable(true);
            palco.setMaximized(false);
            palco.setMaximized(true);
        });


        // refinar ainda sem tela, só deixa o botão desabilitado por enquanto
        btnCraftar.setOnAction(e -> {
            palco.setMaximized(false);
            new TelaCraftSelecao(palco).mostrar();
        });


        btnRefinar.setDisable(true);

        btnOperacoes.setOnAction(e -> new TelaOperacoesAtivas(palco).mostrar());
        HBox botoes = new HBox(20, btnPesquisar, btnCraftar, btnRefinar, btnOperacoes);
        botoes.setAlignment(Pos.CENTER);

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setPrefWidth(180);
        //apesar de ter definido a classe de estido "home-botao", eu quero q o btnVoltar seja um pouco maior


        btnVoltar.setOnAction(e -> {
            new TelaLogin(palco).mostrar();
        });


        VBox raiz = new VBox(30, titulo, botoes, espaco, btnVoltar);
        raiz.setAlignment(Pos.CENTER);


        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(80));
        raiz.getStyleClass().add("home-raiz");

        Scene cena = new Scene(raiz);
        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );

        //isso aqui é uma questão logica pra tela home sempre abrir maximizada, mesmo n clicando em tal botao
        palco.setTitle("Albion Market - Home");
        palco.setTitle("Albion Market - Home");
        palco.setScene(cena);
        palco.show();
        palco.setMaximized(false);
        palco.setMaximized(true);
    }


    private void abrirTela(javafx.scene.layout.Pane layout,
                           int largura, int altura, String tituloPalco) {
        Scene cena = new Scene(layout, largura, altura);
        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );

    }

    private Button criarBotao(String texto, String estilo) {
        Button btn = new Button(texto);
        btn.getStyleClass().add(estilo);
        btn.setPrefWidth(180);
        btn.setPrefHeight(80);
        return btn;
    }
}