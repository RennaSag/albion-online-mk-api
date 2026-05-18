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
 * Tela de seleção de filtros para análise de flip entre cidades.
 * - Lucro mínimo e máximo configuráveis
 */
public class TelaFlipSelecao {

    private final Stage palco;

    private ComboBox<String> cbTipoCompra;
    private ComboBox<String> cbTipoVenda;
    private Slider sliderLucroMin;
    private Slider sliderLucroMax;
    private Label labelLucroMin;
    private Label labelLucroMax;

    // estilo base dos cards de secao
    private static final String ESTILO_CARD =
            "-fx-background-color: #252525; -fx-border-color: #383838; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;";

    public TelaFlipSelecao(Stage palco) {
        this.palco = palco;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1a1a1a;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarConteudo());

        palco.setTitle("Albion Online — Flip de Itens");
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);
    }



    private HBox criarCabecalho() {
        Label icone = new Label("⇄");
        icone.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 22px;");

        Label titulo = new Label("Flip de Mercado");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #e8e8e8;");

        Label subtitulo = new Label("Encontre arbitragens entre cidades");
        subtitulo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        VBox textos = new VBox(1, titulo, subtitulo);
        textos.setAlignment(Pos.CENTER_LEFT);

        HBox esquerda = new HBox(10, icone, textos);
        esquerda.setAlignment(Pos.CENTER_LEFT);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label btnHome = new Label("← Início");
        btnHome.setStyle("-fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #5a8dee;");
        btnHome.setOnMouseEntered(e -> btnHome.setStyle("-fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #5a8dee; -fx-opacity: 0.7;"));
        btnHome.setOnMouseExited(e -> btnHome.setStyle("-fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #5a8dee; -fx-opacity: 1;"));
        btnHome.setOnMouseClicked(e -> new TelaHome(palco).mostrar());

        HBox cab = new HBox(esquerda, espacador, btnHome);
        cab.setAlignment(Pos.CENTER);
        cab.setPadding(new Insets(16, 24, 16, 24));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");
        return cab;
    }



    private ScrollPane criarConteudo() {
        VBox conteudo = new VBox(20);
        conteudo.setPadding(new Insets(30, 0, 30, 0));
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.setStyle("-fx-background-color: #1a1a1a;");

        // titulo da secao
        Label lblTitulo = new Label("Configurar busca");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #e8e8e8;");

        Label lblDesc = new Label("Defina como quer comprar, vender e qual faixa de lucro filtrar.");
        lblDesc.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        VBox header = new VBox(4, lblTitulo, lblDesc);
        header.setMaxWidth(640);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox corpo = new VBox(14,
                header,
                criarCardTipos(),
                criarCardLucro(),
                criarBotoesAcao()
        );
        corpo.setMaxWidth(640);
        corpo.setAlignment(Pos.TOP_CENTER);

        conteudo.getChildren().add(corpo);

        ScrollPane scroll = new ScrollPane(conteudo);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a1a; -fx-background: #1a1a1a;");
        return scroll;
    }



    private VBox criarCardTipos() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle(ESTILO_CARD);

        Label titulo = new Label("Tipo de operação");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));
        titulo.setStyle("-fx-text-fill: #c0c0c0;");

        // linha separadora
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #333;");

        // compra
        VBox blocoCompra = new VBox(6);
        Label lblCompra = new Label("COMPRAR");
        lblCompra.setStyle("-fx-text-fill: #888; -fx-font-size: 10px; -fx-font-weight: bold;");

        cbTipoCompra = new ComboBox<>();
        cbTipoCompra.setItems(FXCollections.observableArrayList(
                "Compra Direta",
                "Pedido de Compra"
        ));
        cbTipoCompra.setValue("Compra Direta");
        cbTipoCompra.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTipoCompra);
        blocoCompra.getChildren().addAll(lblCompra, cbTipoCompra);

        // venda
        VBox blocoVenda = new VBox(6);
        Label lblVenda = new Label("VENDER");
        lblVenda.setStyle("-fx-text-fill: #888; -fx-font-size: 10px; -fx-font-weight: bold;");

        cbTipoVenda = new ComboBox<>();
        cbTipoVenda.setItems(FXCollections.observableArrayList(
                "Venda Direta",
                "Pedido de Venda"
        ));
        cbTipoVenda.setValue("Venda Direta");
        cbTipoVenda.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTipoVenda);
        blocoVenda.getChildren().addAll(lblVenda, cbTipoVenda);

        // dica explicativa
        Label dica = new Label(
                "ℹ  Compra Direta + Venda Direta = operação instantânea, sem espera.\n" +
                        "    Pedidos de Compra + Pedido de Venda = maior lucro potencial, mas você precisa esperar."
        );
        dica.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
        dica.setWrapText(true);

        card.getChildren().addAll(titulo, sep, blocoCompra, blocoVenda, dica);
        return card;
    }



    private VBox criarCardLucro() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle(ESTILO_CARD);

        Label titulo = new Label("Faixa de lucro bruto");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));
        titulo.setStyle("-fx-text-fill: #c0c0c0;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #333;");


        Label lblMin = new Label("Lucro mínimo");
        lblMin.setStyle("-fx-text-fill: #888; -fx-font-size: 10px; -fx-font-weight: bold;");

        labelLucroMin = new Label("0");
        labelLucroMin.setStyle("-fx-text-fill: #5a8dee; -fx-font-weight: bold; -fx-font-size: 14px;");

        sliderLucroMin = new Slider(0, 10_000_000, 0);
        sliderLucroMin.setBlockIncrement(100_000);
        sliderLucroMin.setMajorTickUnit(1_000_000);
        sliderLucroMin.setMaxWidth(Double.MAX_VALUE);
        sliderLucroMin.setStyle("-fx-accent: #5a8dee;");
        sliderLucroMin.valueProperty().addListener((obs, ant, novo) -> {
            labelLucroMin.setText(formatarSlider(novo.longValue()));
            // garante que min nao ultrapassa max
            if (sliderLucroMax != null && novo.doubleValue() > sliderLucroMax.getValue()) {
                sliderLucroMax.setValue(novo.doubleValue());
            }
        });

        HBox linhaMin = new HBox(10, sliderLucroMin, labelLucroMin);
        linhaMin.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(sliderLucroMin, Priority.ALWAYS);

        // --- lucro máximo ---
        Label lblMax = new Label("Lucro máximo  (0 = sem limite)");
        lblMax.setStyle("-fx-text-fill: #888; -fx-font-size: 10px; -fx-font-weight: bold;");

        labelLucroMax = new Label("Sem limite");
        labelLucroMax.setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-font-size: 14px;");

        sliderLucroMax = new Slider(0, 10_000_000, 0);
        sliderLucroMax.setBlockIncrement(100_000);
        sliderLucroMax.setMajorTickUnit(1_000_000);
        sliderLucroMax.setMaxWidth(Double.MAX_VALUE);
        sliderLucroMax.setStyle("-fx-accent: #3dba6e;");
        sliderLucroMax.valueProperty().addListener((obs, ant, novo) -> {
            long val = novo.longValue();
            labelLucroMax.setText(val == 0 ? "Sem limite" : formatarSlider(val));
            // garante que max nao fica abaixo de min
            if (val > 0 && val < sliderLucroMin.getValue()) {
                sliderLucroMin.setValue(val);
            }
        });

        HBox linhaMax = new HBox(10, sliderLucroMax, labelLucroMax);
        linhaMax.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(sliderLucroMax, Priority.ALWAYS);

        card.getChildren().addAll(titulo, sep, lblMin, linhaMin, lblMax, linhaMax);
        return card;
    }



    private HBox criarBotoesAcao() {
        Button btnBuscar = new Button("Buscar Oportunidades");
        btnBuscar.setPrefWidth(200);
        btnBuscar.setPrefHeight(42);
        btnBuscar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px;");
        btnBuscar.setOnMouseEntered(e -> btnBuscar.setStyle(
                "-fx-background-color: #4a7ede; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px;"));
        btnBuscar.setOnMouseExited(e -> btnBuscar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px;"));
        btnBuscar.setOnAction(e -> onBuscar());

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setPrefWidth(120);
        btnVoltar.setPrefHeight(42);
        btnVoltar.setStyle(
                "-fx-background-color: #2e2e2e; -fx-text-fill: #aaa; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px; " +
                        "-fx-border-color: #444; -fx-border-radius: 8; -fx-border-width: 1;");
        btnVoltar.setOnAction(e -> new TelaHome(palco).mostrar());

        HBox hb = new HBox(12, btnBuscar, btnVoltar);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(4, 0, 0, 0));
        return hb;
    }



    private void onBuscar() {
        // "sell" = campo sell_price_min (compra direta / pedido de venda)
        // "buy"  = campo buy_price_max  (pedido de compra / venda direta)
        //
        // Compra Direta: você paga o preço listado → usa sell_price_min → tipoCompra = "sell"
        // Pedido de Compra: você coloca uma ordem → usa buy_price_max → tipoCompra = "buy"
        // Venda Direta: você vende para ordem de compra existente → usa buy_price_max → tipoVenda = "buy"
        // Pedido de Venda: você lista um preço → usa sell_price_min → tipoVenda = "sell"

        String tipoCompra = cbTipoCompra.getValue().startsWith("Compra Direta") ? "sell" : "buy";
        String tipoVenda  = cbTipoVenda.getValue().startsWith("Venda Direta")   ? "buy"  : "sell";

        long lucroMin = (long) sliderLucroMin.getValue();
        long lucroMax = (long) sliderLucroMax.getValue(); // 0 = sem limite

        new TelaFlip(palco, tipoCompra, tipoVenda, lucroMin, lucroMax).mostrar();
    }



    private String formatarSlider(long val) {
        if (val >= 1_000_000) return String.format("%.1fM", val / 1_000_000.0);
        if (val >= 1_000)     return String.format("%.0fK", val / 1_000.0);
        return String.valueOf(val);
    }

    private void estilizarComboBox(ComboBox<?> cb) {
        cb.setStyle(
                "-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0; " +
                        "-fx-border-color: #404040; -fx-border-radius: 6; -fx-background-radius: 6;");
    }
}