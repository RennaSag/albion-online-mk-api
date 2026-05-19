package com.albionmarket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.prefs.Preferences;

public class TelaHome {

    private final Stage palco;

    public TelaHome(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        Label titulo = new Label("O que vamos fazer hoje?");
        titulo.getStyleClass().add("home-titulo");

        // contador de expiracao
        Label labelExpiracao = new Label(obterTextoExpiracao());
        labelExpiracao.setStyle("-fx-text-fill: " + obterCorExpiracao() + "; -fx-font-size: 12px;");

        Button btnPesquisar = criarBotao("Pesquisar", "home-botao");
        Button btnCraftar = criarBotao("Craftar", "home-botao");
        Button btnRefinar = criarBotao("Refinar", "home-botao");
        Button btnOperacoes = criarBotao("Operacoes Ativas", "home-botao");
        Button btnFlip = criarBotao("Flip de Mercado", "home-botao");
        Button btnCraftRefino = criarBotao("Craft com Refino", "home-botao");

        btnCraftRefino.setOnAction(e -> {
            Stage palco = (Stage) btnCraftRefino.getScene().getWindow();
            new TelaCraftRefinoSelecao(palco).mostrar();
        });

        btnFlip.setOnAction(e -> {
            Stage palco = (Stage) btnFlip.getScene().getWindow();
            new TelaFlipSelecao(palco).mostrar();
        });

        btnPesquisar.setOnAction(e -> {
            palco.setTitle("Analisador de Mercado de Albion Online");
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

        VBox botoes = new VBox(20, btnPesquisar, btnCraftar, btnRefinar, btnCraftRefino, btnOperacoes);
        botoes.setAlignment(Pos.CENTER);

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setPrefWidth(180);
        btnVoltar.setOnAction(e -> new TelaLogin(palco).mostrar());

        Label labelContato = new Label("Suporte: rennasagcontato@gmail.com");
        labelContato.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        VBox raiz = new VBox(30, titulo, labelExpiracao, botoes, espaco, btnVoltar, labelContato);

        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(80));
        raiz.getStyleClass().add("home-raiz");

        palco.setTitle("Analisador de Mercado de Albion Online");
        palco.getScene().setRoot(raiz);
    }

    private String obterTextoExpiracao() {
        Preferences prefs = Preferences.userNodeForPackage(TelaLogin.class);
        String expiraStr = prefs.get("licenca_expira", "");
        if (expiraStr.isBlank()) return "Validade da licenca: desconhecida";

        try {
            Instant expira = Instant.parse(expiraStr);
            long dias = ChronoUnit.DAYS.between(Instant.now(), expira);
            long horas = ChronoUnit.HOURS.between(Instant.now(), expira) % 24;

            if (Instant.now().isAfter(expira)) {
                return "Licenca expirada. Adquira uma nova chave.";
            } else if (dias == 0) {
                return "Licenca expira em menos de " + (horas + 1) + " hora(s)!";
            } else {
                return "Licenca valida por mais " + dias + " dia(s) e " + horas + " hora(s)";
            }
        } catch (Exception e) {
            return "Validade da licenca: desconhecida";
        }
    }

    private String obterCorExpiracao() {
        Preferences prefs = Preferences.userNodeForPackage(TelaLogin.class);
        String expiraStr = prefs.get("licenca_expira", "");
        if (expiraStr.isBlank()) return "#888";

        try {
            Instant expira = Instant.parse(expiraStr);
            long dias = ChronoUnit.DAYS.between(Instant.now(), expira);

            if (Instant.now().isAfter(expira)) return "#e05555";
            if (dias < 2) return "#e0b84a";
            return "#3dba6e";
        } catch (Exception e) {
            return "#888";
        }
    }

    private Button criarBotao(String texto, String estilo) {
        Button btn = new Button(texto);
        btn.getStyleClass().add(estilo);
        btn.setPrefWidth(250);
        btn.setPrefHeight(80);
        return btn;
    }
}