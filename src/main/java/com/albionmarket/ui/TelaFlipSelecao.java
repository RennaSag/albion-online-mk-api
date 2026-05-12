package com.albionmarket.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * tela de selecao de filtros para analise de flip entre cidades
 */
public class TelaFlipSelecao {

    private final Stage palco;

    private ComboBox<String> cbQualidade;
    private ComboBox<String> cbTipoCompra;
    private ComboBox<String> cbTipoVenda;
    private Slider sliderLucroMinimo;
    private Label labelLucroMinimo;

    public TelaFlipSelecao(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarConteudo());

        palco.setTitle("Albion Online - Flip de Itens");
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);
    }

    private HBox criarCabecalho() {
        Label titulo = new Label("Flip de Mercado");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Encontre oportunidades de arbitragem entre cidades");
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);
        textos.setAlignment(Pos.CENTER);

        HBox cab = new HBox(textos);
        cab.setAlignment(Pos.CENTER);
        cab.setPadding(new Insets(14, 20, 14, 20));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cab;
    }

    private VBox criarConteudo() {
        VBox conteudo = new VBox(10);
        conteudo.setPadding(new Insets(15, 60, 15, 60));
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(conteudo, Priority.ALWAYS);
        conteudo.getChildren().addAll(
                criarBlocoFiltros(),
                criarBotoesAcao()
        );
        return conteudo;
    }

    private VBox criarBlocoFiltros() {
        VBox bloco = new VBox(6);
        bloco.setMaxWidth(600);


        // qualidade
        bloco.getChildren().add(criarSecao("Qualidade"));
        cbQualidade = new ComboBox<>();
        cbQualidade.setItems(FXCollections.observableArrayList(
                "Todas", "Normal", "Boa", "Notavel", "Excelente", "Obra-prima"
        ));
        cbQualidade.setValue("Todas");
        cbQualidade.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbQualidade);
        bloco.getChildren().add(cbQualidade);

        // tipo de compra
        bloco.getChildren().add(criarSecao("Tipo de Compra"));
        cbTipoCompra = new ComboBox<>();
        cbTipoCompra.setItems(FXCollections.observableArrayList(
                "Compra Direta",
                "Pedido de Compra"
        ));
        cbTipoCompra.setValue("Compra Direta");
        cbTipoCompra.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTipoCompra);

        //Label descCompra = new Label("Compra Direta = paga o preco listado agora  |  Pedido de Compra = coloca uma ordem de compra");
        //descCompra.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        //descCompra.setWrapText(true);

        //bloco.getChildren().addAll(cbTipoCompra, descCompra);
        bloco.getChildren().addAll(cbTipoCompra);

        // tipo de venda
        bloco.getChildren().add(criarSecao("Tipo de Venda"));
        cbTipoVenda = new ComboBox<>();
        cbTipoVenda.setItems(FXCollections.observableArrayList(
                "Venda Direta",
                "Pedido de Venda"
        ));
        cbTipoVenda.setValue("Venda Direta");
        cbTipoVenda.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTipoVenda);

        //Label descVenda = new Label("Venda Direta = vende no preco listado agora  |  Pedido de Venda = coloca uma ordem de venda");
        //descVenda.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        //descVenda.setWrapText(true);

        //bloco.getChildren().addAll(cbTipoVenda, descVenda);
        bloco.getChildren().addAll(cbTipoVenda);

        // lucro minimo
        bloco.getChildren().add(criarSecao("Lucro Minimo"));

        labelLucroMinimo = new Label("0");
        labelLucroMinimo.setStyle("-fx-text-fill: #5a8dee; -fx-font-weight: bold; -fx-font-size: 12px;");

        sliderLucroMinimo = new Slider(0, 5_000_000, 0);
        sliderLucroMinimo.setBlockIncrement(100_000);
        sliderLucroMinimo.setMajorTickUnit(1_000_000);
        sliderLucroMinimo.setShowTickMarks(false);
        sliderLucroMinimo.setMaxWidth(Double.MAX_VALUE);
        sliderLucroMinimo.setStyle("-fx-accent: #5a8dee;");

        sliderLucroMinimo.valueProperty().addListener((obs, ant, novo) -> {
            long val = novo.longValue();
            if (val >= 1_000_000)
                labelLucroMinimo.setText(String.format("%.1fM", val / 1_000_000.0));
            else if (val >= 1_000)
                labelLucroMinimo.setText(String.format("%.0fK", val / 1_000.0));
            else
                labelLucroMinimo.setText(String.valueOf(val));
        });

        bloco.getChildren().addAll(sliderLucroMinimo, labelLucroMinimo);

        HBox wrapper = new HBox(bloco);
        wrapper.setAlignment(Pos.CENTER);

        VBox container = new VBox(bloco);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(800); // controla o tamanho aqui
        return container;
    }

    private VBox criarBotoesAcao() {
        Button btnBuscar = new Button("Buscar Oportunidades");
        btnBuscar.setPrefWidth(190);
        btnBuscar.setPrefHeight(40);
        btnBuscar.setStyle("-fx-background-color: #5a8dee; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnBuscar.setOnAction(e -> onBuscar());

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setPrefWidth(190);
        btnVoltar.setPrefHeight(40);
        btnVoltar.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnVoltar.setOnAction(e -> new TelaHome(palco).mostrar());

        VBox vb = new VBox(8, btnBuscar, btnVoltar);
        vb.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(20, 0, 0, 0));
        return vb;
    }

    private void onBuscar() {
        int qualidade = parseQualidade(cbQualidade.getValue());
        String tipoCompra = cbTipoCompra.getValue().equals("Pedido de Compra") ? "buy" : "sell";
        String tipoVenda = cbTipoVenda.getValue().equals("Pedido de Venda") ? "buy" : "sell";
        long lucroMinimo = parseLucroMinimo();

        new TelaFlip(palco, qualidade, tipoCompra, tipoVenda, lucroMinimo).mostrar();
    }

    private int parseQualidade(String val) {
        return switch (val) {
            case "Normal" -> 1;
            case "Boa" -> 2;
            case "Notavel" -> 3;
            case "Excelente" -> 4;
            case "Obra-prima" -> 5;
            default -> 0; // 0 = todas
        };
    }

    private long parseLucroMinimo() {
        return (long) sliderLucroMinimo.getValue();
    }

    private Label criarSecao(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
        return lbl;
    }

    private void estilizarComboBox(ComboBox<?> cb) {
        cb.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
    }
}