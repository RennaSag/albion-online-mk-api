package com.albionmarket.ui;

import com.albionmarket.service.BancoDeDadosChangelog;
import com.albionmarket.model.VersaoInfo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class TelaChangelog {

    private final Stage palco;

    public TelaChangelog(Stage palco) {
        this.palco = palco;
    }


    private static final String CHAVE_VERSAO = "changelog_ultima_versao_vista";


    private static final String COR_FUNDO = "#1e1e1e";
    private static final String COR_CARD = "#2a2a2a";
    private static final String COR_CARD_BORDA = "#3a3a3a";
    private static final String COR_DESTAQUE = "#2980b9";
    private static final String COR_TEXTO = "#E0E0E0";
    private static final String COR_TEXTO_FRACO = "#888888";
    private static final String COR_NOVIDADE = "#FFD700";
    private static final String COR_MELHORIA = "#4FC3F7";
    private static final String COR_CORRECAO = "#81C784";
    private static final String COR_REMOCAO = "#EF5350";


    private static final String ICONE_NOVIDADE = "✦";
    private static final String ICONE_MELHORIA = "↑";
    private static final String ICONE_CORRECAO = "✓";
    private static final String ICONE_REMOCAO = "✗";


    public static boolean deveExibir() {
        Preferences prefs = Preferences.userNodeForPackage(TelaChangelog.class);
        String vistaPor = prefs.get(CHAVE_VERSAO, "");
        return !BancoDeDadosChangelog.APP_VERSAO_ATUAL.equals(vistaPor);
    }


    public void mostrar() {
        Stage janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL);
        janela.initOwner(palco);
        janela.setTitle("Novidades");
        janela.setWidth(520);
        janela.setHeight(600);
        janela.setResizable(false);


        Label lblTitulo = new Label("Novidades do App");
        lblTitulo.setStyle(
                "-fx-text-fill: " + COR_DESTAQUE + ";" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        Label lblVersao = new Label("Versão " + BancoDeDadosChangelog.APP_VERSAO_ATUAL);
        lblVersao.setStyle(
                "-fx-text-fill: " + COR_TEXTO_FRACO + ";" +
                        "-fx-font-size: 12px;"
        );

        VBox cabecalho = new VBox(6, lblTitulo, lblVersao);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(28, 24, 16, 24));


        VBox listaVersoes = new VBox(12);
        listaVersoes.setPadding(new Insets(8, 16, 16, 16));

        for (int i = 0; i < BancoDeDadosChangelog.VERSOES.size(); i++) {
            listaVersoes.getChildren().add(
                    construirCardVersao(BancoDeDadosChangelog.VERSOES.get(i), i == 0)
            );
        }

        ScrollPane scroll = new ScrollPane(listaVersoes);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: " + COR_FUNDO + ";" +
                        "-fx-background-color: " + COR_FUNDO + ";"
        );
        VBox.setVgrow(scroll, Priority.ALWAYS);


        Button btnFechar = new Button("Entendido");
        btnFechar.setStyle(
                "-fx-background-color: " + COR_DESTAQUE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );
        btnFechar.setPrefHeight(42);
        btnFechar.setMaxWidth(Double.MAX_VALUE);
        btnFechar.setOnAction(e -> {
            marcarComoVisto();
            janela.close();
        });

        VBox rodape = new VBox(btnFechar);
        rodape.setPadding(new Insets(8, 16, 20, 16));


        VBox raiz = new VBox(cabecalho, scroll, rodape);
        raiz.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        marcarComoVisto();
        janela.setScene(new Scene(raiz));
        janela.showAndWait();
    }


    private VBox construirCardVersao(VersaoInfo versao, boolean ehMaisRecente) {
        VBox card = new VBox(6);
        card.setStyle(
                "-fx-background-color: " + COR_CARD + ";" +
                        "-fx-padding: 14;" +
                        "-fx-border-color: " + COR_CARD_BORDA + ";" +
                        "-fx-border-width: 1;"
        );


        Label lblBadge = new Label(" v" + versao.getVersao() + " ");
        lblBadge.setStyle(
                "-fx-background-color: " + (ehMaisRecente ? COR_DESTAQUE : COR_CARD_BORDA) + ";" +
                        "-fx-text-fill: " + (ehMaisRecente ? "black" : COR_TEXTO) + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;"
        );

        Label lblData = new Label(versao.getData());
        lblData.setStyle(
                "-fx-text-fill: " + COR_TEXTO_FRACO + ";" +
                        "-fx-font-size: 11px;"
        );

        HBox topo = new HBox(8, lblBadge, lblData);
        topo.setAlignment(Pos.CENTER_LEFT);


        Label lblTitulo = new Label(versao.getTitulo());
        lblTitulo.setStyle(
                "-fx-text-fill: " + (ehMaisRecente ? COR_DESTAQUE : COR_TEXTO) + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;"
        );


        Separator divisor = new Separator();
        divisor.setStyle("-fx-background-color: " + COR_CARD_BORDA + ";");

        card.getChildren().addAll(topo, lblTitulo, divisor);


        for (VersaoInfo.Mudanca m : versao.getMudancas()) {
            card.getChildren().add(construirLinhaMudanca(m));
        }

        return card;
    }


    private HBox construirLinhaMudanca(VersaoInfo.Mudanca mudanca) {
        Label lblIcone = new Label(iconeParaTipo(mudanca.tipo));
        lblIcone.setStyle(
                "-fx-text-fill: " + corParaTipo(mudanca.tipo) + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-min-width: 20px;"
        );

        Label lblTexto = new Label(mudanca.descricao);
        lblTexto.setStyle(
                "-fx-text-fill: " + COR_TEXTO + ";" +
                        "-fx-font-size: 12px;"
        );
        lblTexto.setWrapText(true);
        HBox.setHgrow(lblTexto, Priority.ALWAYS);

        HBox linha = new HBox(8, lblIcone, lblTexto);
        linha.setAlignment(Pos.TOP_LEFT);
        return linha;
    }


    private String iconeParaTipo(VersaoInfo.TipoMudanca tipo) {
        return switch (tipo) {
            case NOVIDADE -> ICONE_NOVIDADE;
            case MELHORIA -> ICONE_MELHORIA;
            case CORRECAO -> ICONE_CORRECAO;
            case REMOCAO -> ICONE_REMOCAO;
        };
    }

    private String corParaTipo(VersaoInfo.TipoMudanca tipo) {
        return switch (tipo) {
            case NOVIDADE -> COR_NOVIDADE;
            case MELHORIA -> COR_MELHORIA;
            case CORRECAO -> COR_CORRECAO;
            case REMOCAO -> COR_REMOCAO;
        };
    }

    private void marcarComoVisto() {
        Preferences.userNodeForPackage(TelaChangelog.class)
                .put(CHAVE_VERSAO, BancoDeDadosChangelog.APP_VERSAO_ATUAL);
    }
}