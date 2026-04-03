package com.albionmarket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

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
            palco.setTitle("Albion Online - Analisador de Mercado");
            palco.setMinWidth(1280);
            palco.setMinHeight(720);

            palco.getScene().setRoot(new TelaPesquisaPrecos().getCriarLayout());
        });

        btnCraftar.setOnAction(e -> {
            Stage palco = (Stage) btnCraftar.getScene().getWindow();
            new TelaCraftSelecao(palco).mostrar();
        });

        btnRefinar.setOnAction(e -> {
            Stage palco = (Stage) btnRefinar.getScene().getWindow();
            new TelaRefinoSelecao(palco).mostrar();
        });

        btnOperacoes.setOnAction(e -> {
            new TelaOperacoesAtivas(palco).mostrar();
        });

        VBox botoes = new VBox(20, btnPesquisar, btnCraftar, btnRefinar, btnOperacoes);
        botoes.setAlignment(Pos.CENTER);
        // esse é o alinhamento dos botões pra ficar verticalmente, troquei de Hbox pra Vbox

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setPrefWidth(180);

        btnVoltar.setOnAction(e -> new TelaLogin(palco).mostrar());

        VBox raiz = new VBox(30, titulo, botoes, espaco, btnVoltar);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(80));
        raiz.getStyleClass().add("home-raiz");

        palco.setTitle("Albion Online - Analisador de Mercado");
        palco.getScene().setRoot(raiz);
    }

    private Button criarBotao(String texto, String estilo) {
        Button btn = new Button(texto);
        btn.getStyleClass().add(estilo);
        btn.setPrefWidth(250);
        btn.setPrefHeight(80);
        // largura e altura do tamanho dos botoes

        return btn;
    }
}