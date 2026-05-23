package com.albionmarket.ui;

import com.albionmarket.model.*;
import com.albionmarket.service.*;
import com.albionmarket.util.AlbionIdUtil;
import com.albionmarket.util.FormatadorUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import com.albionmarket.service.OperacaoService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class TelaCraftRefino {

    private final Stage palco;
    private final ItemDefinition item;
    private final int tier;
    private final int enchant;
    private final String itemIdCompleto;
    private boolean possuiPremium = false;

    private boolean modoEdicaoManual = false;

    private final EstadoSelecao estadoSelecao;

    private PriceEntry precoDiarioVazioEntryCache;


    private final ApiService apiService = new ApiService();
    private final CraftService craftService = new CraftService();


    private TextField campoQuantidade;
    private TextField campoRetornoCraft;
    private TextField campoBarracaCraft;
    private TextField campoRetornoRefino;
    private TextField campoBarracaRefino;


    private Label labelStatus;
    private ProgressIndicator progresso;


    private TableView<LinhaPreco> tabelaPrecos;
    private TableView<LinhaReceita> tabelaReceitaCraft;
    private TableView<LinhaMaterial> tabelaMateriais;
    private VBox painelCalculo;

    private ReceitaCraft receitaCraft;
    private final Map<String, ReceitaCraft> receitasRefino = new LinkedHashMap<>();
    private long itemValue = 0;


    private double precoDiarioVazioApi = 0;
    private double precoDiarioCheioApi = 0;

    private double lucroAtual = 0;
    private double custoAtual = 0;


    public static class LinhaPreco {
        public final String itemId, qualidade, cidade, corCidade, sellMin, atualizado;

        public LinhaPreco(String itemId, String qualidade, String cidade,
                          String corCidade, String sellMin, String atualizado) {
            this.itemId = itemId;
            this.qualidade = qualidade;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.sellMin = sellMin;
            this.atualizado = atualizado;
        }
    }


    public static class LinhaReceita {
        public final String iconeUrl, nome, tipo;
        public final int qtd;

        public LinhaReceita(String iconeUrl, String nome, String tipo, int qtd) {
            this.iconeUrl = iconeUrl;
            this.nome = nome;
            this.tipo = tipo;
            this.qtd = qtd;
        }
    }

    public static class LinhaMaterial {
        public final String iconeUrl, nome, tipo, cidade, corCidade, precoCompra, atualizado;
        public final int qtdTotal;

        public LinhaMaterial(String iconeUrl, String nome, String tipo, int qtdTotal,
                             String cidade, String corCidade, String precoCompra, String atualizado) {
            this.iconeUrl = iconeUrl;
            this.nome = nome;
            this.tipo = tipo;
            this.qtdTotal = qtdTotal;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.precoCompra = precoCompra;
            this.atualizado = atualizado;
        }
    }


    public TelaCraftRefino(Stage palco, ItemDefinition item, int tier, int enchant,
                           EstadoSelecao estadoSelecao) {
        this.palco = palco;
        this.item = item;
        this.tier = tier;
        this.enchant = enchant;
        this.estadoSelecao = estadoSelecao;

        int t = AlbionIdUtil.tierEfetivo(tier);
        int e = AlbionIdUtil.enchantEfetivo(enchant);
        this.itemIdCompleto = AlbionIdUtil.buildApiId(item.getId(), t, e);
    }


    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setLeft(criarLateral());
        raiz.setCenter(criarAreaCentral());

        palco.setTitle("Craft + Refino: " + item.getNome());
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        buscarTudo();
    }


    private HBox criarCabecalho() {
        Label titulo = new Label("Craft + Refino");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Calculadora completa - " + item.getNome());
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label btnHome = new Label("Inicio");
        btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand;");
        btnHome.setOnMouseEntered(e -> btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand; -fx-opacity: 0.7;"));
        btnHome.setOnMouseExited(e -> btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand;"));
        btnHome.setOnMouseClicked(e -> new TelaHome(palco).mostrar());

        HBox cab = new HBox(textos, espacador, btnHome);
        cab.setAlignment(Pos.CENTER_LEFT);
        cab.setPadding(new Insets(14, 20, 14, 20));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cab;
    }


    private ScrollPane criarLateral() {
        VBox painel = new VBox(14);
        painel.setPadding(new Insets(16));
        painel.setPrefWidth(290);
        painel.setStyle("-fx-background-color: #252525;");


        ImageView icone = new ImageView();
        icone.setFitWidth(140);
        icone.setFitHeight(90);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);
        icone.setImage(new Image(
                "https://render.albiononline.com/v1/item/" + itemIdCompleto + ".png", true));

        Label nomeItem = new Label(item.getNome());
        nomeItem.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 13px;");
        nomeItem.setWrapText(true);

        int t = AlbionIdUtil.tierEfetivo(tier);
        int e = AlbionIdUtil.enchantEfetivo(enchant);
        Label infoItem = new Label("Tier " + t + (e > 0 ? "  ·  Ench. ." + e : ""));
        infoItem.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        VBox boxIcone = new VBox(6, icone, nomeItem, infoItem);
        boxIcone.setAlignment(Pos.CENTER);
        boxIcone.setPadding(new Insets(0, 0, 10, 0));
        painel.getChildren().addAll(boxIcone, new Separator());


        painel.getChildren().add(secao("Parametros de Craft"));
        campoQuantidade = campoCampo("1");
        campoRetornoCraft = campoCampo("15.2");
        campoBarracaCraft = campoCampo("3.0");
        painel.getChildren().addAll(
                label("Quantidade a craftar"), campoQuantidade,
                label("Retorno do craft (%)"), campoRetornoCraft,
                label("Taxa da barraca craft"), campoBarracaCraft
        );

        painel.getChildren().add(new Separator());


        painel.getChildren().add(secao("Parametros de Refino"));
        campoRetornoRefino = campoCampo("36.7");
        campoBarracaRefino = campoCampo("3.0");
        painel.getChildren().addAll(
                label("Retorno do refino (%)"), campoRetornoRefino,
                label("Taxa da barraca refino"), campoBarracaRefino
        );

        painel.getChildren().add(new Separator());


        // switch inserir precos manualmente
        javafx.scene.canvas.Canvas canvasSwitch = new javafx.scene.canvas.Canvas(44, 22);
        final boolean[] estadoSwitch = {false};

        Runnable desenharSwitch = () -> {
            javafx.scene.canvas.GraphicsContext gc = canvasSwitch.getGraphicsContext2D();
            gc.clearRect(0, 0, 44, 22);
            gc.setFill(estadoSwitch[0]
                    ? javafx.scene.paint.Color.web("#5a8dee")
                    : javafx.scene.paint.Color.web("#555"));
            gc.fillRoundRect(0, 0, 44, 22, 22, 22);
            double bx = estadoSwitch[0] ? 24 : 2;
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(bx, 2, 18, 18);
        };
        desenharSwitch.run();

        Label labelSwitch = new Label("Inserir precos manualmente");
        labelSwitch.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");

        HBox switchBox = new HBox(8, canvasSwitch, labelSwitch);
        switchBox.setAlignment(Pos.CENTER_LEFT);
        switchBox.setCursor(javafx.scene.Cursor.HAND);
        switchBox.setOnMouseClicked(ev -> {
            estadoSwitch[0] = !estadoSwitch[0];
            desenharSwitch.run();
            modoEdicaoManual = estadoSwitch[0];
            ativarEdicaoManual(estadoSwitch[0]);
        });
        painel.getChildren().add(switchBox);


        javafx.scene.canvas.Canvas canvasPremium = new javafx.scene.canvas.Canvas(44, 22);
        final boolean[] estadoPremium = {false};

        Runnable desenharPremium = () -> {
            javafx.scene.canvas.GraphicsContext gc = canvasPremium.getGraphicsContext2D();
            gc.clearRect(0, 0, 44, 22);
            gc.setFill(estadoPremium[0]
                    ? javafx.scene.paint.Color.web("#e0b84a")
                    : javafx.scene.paint.Color.web("#555"));
            gc.fillRoundRect(0, 0, 44, 22, 22, 22);
            double bx = estadoPremium[0] ? 24 : 2;
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(bx, 2, 18, 18);
        };
        desenharPremium.run();

        Label labelPremium = new Label("Possui premium?");
        labelPremium.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");

        HBox switchPremium = new HBox(8, canvasPremium, labelPremium);
        switchPremium.setAlignment(Pos.CENTER_LEFT);
        switchPremium.setCursor(javafx.scene.Cursor.HAND);
        switchPremium.setOnMouseClicked(ev -> {
            estadoPremium[0] = !estadoPremium[0];
            desenharPremium.run();
            possuiPremium = estadoPremium[0];
            atualizarCalculo();
        });
        painel.getChildren().add(switchPremium);
        painel.getChildren().add(new Separator());


        progresso = new ProgressIndicator();
        progresso.setMaxSize(24, 24);
        progresso.setVisible(false);
        labelStatus = new Label("Carregando...");
        labelStatus.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        labelStatus.setWrapText(true);
        HBox statusBox = new HBox(8, progresso, labelStatus);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        painel.getChildren().add(statusBox);

        Button btnAtualizar = new Button("Atualizar Valores");
        btnAtualizar.setMaxWidth(Double.MAX_VALUE);
        btnAtualizar.setStyle("-fx-background-color: #5a8dee; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 0;");
        btnAtualizar.setOnAction(ev -> buscarTudo());

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);


        Button btnSalvarOperacao = new Button("Salvar Operacao");
        btnSalvarOperacao.setMaxWidth(Double.MAX_VALUE);
        btnSalvarOperacao.setStyle("-fx-background-color: #3dba6e; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 0;");
        btnSalvarOperacao.setOnAction(ev -> {
            salvarOperacao();
            btnSalvarOperacao.setDisable(true);
            btnSalvarOperacao.setText("Operacao Salva");
        });



        Button btnVoltar = new Button("Voltar");
        btnVoltar.setMaxWidth(Double.MAX_VALUE);
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setOnAction(v -> new TelaCraftRefinoSelecao(palco, estadoSelecao).mostrar());

        painel.getChildren().addAll(btnAtualizar, espaco, btnSalvarOperacao, btnVoltar);

        ScrollPane scroll = new ScrollPane(painel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #252525; -fx-background-color: #252525;");
        return scroll;
    }


    private ScrollPane criarAreaCentral() {

        Label tituloPrecos = new Label("Precos no Mercado");
        tituloPrecos.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        tabelaPrecos = new TableView<>();
        tabelaPrecos.setStyle("-fx-background-color: #1e1e1e;");
        tabelaPrecos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaPrecos.setPlaceholder(new Label("Buscando precos..."));
        tabelaPrecos.setPrefHeight(180);

        tabelaPrecos.getColumns().addAll(
                coluna("Qualidade", 110,
                        r -> new javafx.beans.property.SimpleStringProperty(r.getValue().qualidade)),
                criarColunaCidade(),
                criarColunaPrecoVenda("Preco de Venda", 130),
                coluna("Atualizacao", 100,
                        r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado))
        );


        Label tituloReceitaCraft = new Label("Receita de Craft");
        tituloReceitaCraft.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloReceitaCraft.setPadding(new Insets(12, 0, 6, 0));

        tabelaReceitaCraft = new TableView<>();
        tabelaReceitaCraft.setStyle("-fx-background-color: #1e1e1e;");
        tabelaReceitaCraft.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaReceitaCraft.setPlaceholder(new Label("Carregando receita..."));

        TableColumn<LinhaReceita, String> colIconeRC = new TableColumn<>("  ");
        colIconeRC.setPrefWidth(60);
        colIconeRC.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().iconeUrl));
        colIconeRC.setCellFactory(tc -> new TableCell<>() {
            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(28);
                iv.setFitHeight(28);
                iv.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setGraphic(null);
                    return;
                }
                iv.setImage(new Image(url, 28, 28, true, true, true));
                setGraphic(iv);
            }
        });

        TableColumn<LinhaReceita, String> colTipoRC = new TableColumn<>("Tipo");
        colTipoRC.setPrefWidth(80);
        colTipoRC.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().tipo));
        colTipoRC.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    return;
                }
                setText(v);
                setStyle("Artefato".equals(v)
                        ? "-fx-text-fill: #9b59b6; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e0b84a;");
            }
        });

        TableColumn<LinhaReceita, String> colQtdRC = new TableColumn<>("Qtd p/ 1");
        colQtdRC.setPrefWidth(80);
        colQtdRC.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getValue().qtd)));
        colQtdRC.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : v);
                setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        tabelaReceitaCraft.getColumns().addAll(
                colIconeRC,
                coluna("Material (refinado)", 180,
                        r -> new javafx.beans.property.SimpleStringProperty(r.getValue().nome)),
                colTipoRC, colQtdRC
        );


        Label tituloMateriais = new Label("Materiais Necessarios");
        tituloMateriais.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloMateriais.setPadding(new Insets(12, 0, 6, 0));

        tabelaMateriais = new TableView<>();
        tabelaMateriais.setStyle("-fx-background-color: #1e1e1e;");
        tabelaMateriais.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaMateriais.setPlaceholder(new Label("Carregando materiais..."));

        TableColumn<LinhaMaterial, String> colIconeM = new TableColumn<>("  ");
        colIconeM.setPrefWidth(60);
        colIconeM.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().iconeUrl));
        colIconeM.setCellFactory(tc -> new TableCell<>() {
            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(28);
                iv.setFitHeight(28);
                iv.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setGraphic(null);
                    return;
                }
                iv.setImage(new Image(url, 28, 28, true, true, true));
                setGraphic(iv);
            }
        });

        TableColumn<LinhaMaterial, String> colTipoM = new TableColumn<>("Tipo");
        colTipoM.setPrefWidth(90);
        colTipoM.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().tipo));
        colTipoM.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    return;
                }
                setText(v);
                String cor = switch (v) {
                    case "Bruto" -> "#e0b84a";
                    case "Refinado" -> "#5a8dee";
                    case "Retorno" -> "#9b59b6";
                    case "Artefato" -> "#9b59b6";
                    case "Direto" -> "#e0b84a";
                    default -> "#ccc";
                };
                setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: bold;");
            }
        });

        TableColumn<LinhaMaterial, String> colQtdM = new TableColumn<>("Qtd Total");
        colQtdM.setPrefWidth(100);
        colQtdM.setCellValueFactory(r -> {
            LinhaMaterial lm = r.getValue();
            int qtdCraft = parseIntSafe(campoQuantidade, 1);
            return new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(lm.qtdTotal * qtdCraft));
        });
        colQtdM.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : v);
                setStyle("-fx-text-fill: #e0b84a; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        TableColumn<LinhaMaterial, String> colPrecoM = new TableColumn<>("Preco de Compra");
        colPrecoM.setPrefWidth(140);
        colPrecoM.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().precoCompra));
        colPrecoM.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || "-".equals(v)) {
                    setText("-");
                    setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(v);
                    setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        TableColumn<LinhaMaterial, String> colCidadeM = new TableColumn<>("Local");
        colCidadeM.setPrefWidth(130);
        colCidadeM.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        colCidadeM.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || "-".equals(v)) {
                    setText("-");
                    setGraphic(null);
                    return;
                }
                LinhaMaterial lm = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(lm.corCidade));
                String nome = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(v))
                        .map(CidadeInfo::getNome).findFirst().orElse(v);
                HBox hb = new HBox(6, ponto, new Label(nome));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });

        tabelaMateriais.getColumns().addAll(
                colIconeM,
                coluna("Material", 180,
                        r -> new javafx.beans.property.SimpleStringProperty(r.getValue().nome)),
                colTipoM, colQtdM, colPrecoM, colCidadeM,
                coluna("Atualizacao", 100,
                        r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado))
        );


        Label tituloCalc = new Label("Calculadora de Lucro");
        tituloCalc.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloCalc.setPadding(new Insets(12, 0, 6, 0));

        painelCalculo = new VBox(10);
        painelCalculo.setStyle("-fx-background-color: #1e1e1e;");

        VBox area = new VBox(10,
                tituloPrecos, tabelaPrecos,
                tituloReceitaCraft, tabelaReceitaCraft,
                tituloMateriais, tabelaMateriais,
                tituloCalc, painelCalculo);
        area.setPadding(new Insets(16));
        area.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(tabelaPrecos, Priority.SOMETIMES);
        VBox.setVgrow(tabelaReceitaCraft, Priority.SOMETIMES);
        VBox.setVgrow(tabelaMateriais, Priority.SOMETIMES);
        VBox.setVgrow(painelCalculo, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(area);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }


    private void buscarTudo() {
        List<String> cidades = (estadoSelecao != null
                && estadoSelecao.cidades != null
                && !estadoSelecao.cidades.isEmpty())
                ? estadoSelecao.cidades
                : BancoDeDadosItens.CIDADES.stream()
                .map(CidadeInfo::getApiId).collect(Collectors.toList());

        List<String> cidadesSemBM = cidades.stream()
                .filter(c -> !c.equals("BlackMarket"))
                .collect(Collectors.toList());

        int tEfetivo = AlbionIdUtil.tierEfetivo(tier);
        int eEfetivo = AlbionIdUtil.enchantEfetivo(enchant);

        progresso.setVisible(true);
        labelStatus.setText("Buscando receitas e precos...");
        tabelaPrecos.setItems(FXCollections.emptyObservableList());
        tabelaReceitaCraft.setItems(FXCollections.emptyObservableList());
        tabelaMateriais.setItems(FXCollections.emptyObservableList());
        receitasRefino.clear();

        ExecutorService pool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        Task<Void> tarefa = new Task<>() {

            private List<PriceEntry> precos;
            private ReceitaCraft receita;

            private final Map<String, List<PriceEntry>> precosMateriais = new java.util.concurrent.ConcurrentHashMap<>();

            private PriceEntry precoDiarioVazioEntry;
            private PriceEntry precoDiarioCheioEntry;
            private List<PriceEntry> precosDiarioCheioTodos;

            @Override
            protected Void call() throws Exception {


                CompletableFuture<List<PriceEntry>> futurePrecos =
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return apiService.buscarPrecos(
                                        item.getId(), tEfetivo, eEfetivo, -1, cidades);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }, pool);

                CompletableFuture<ReceitaCraft> futureReceita =
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return craftService.buscarReceita(itemIdCompleto);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }, pool);

                CompletableFuture<Long> futureItemValue =
                        CompletableFuture.supplyAsync(
                                () -> ItemValues.getValor(itemIdCompleto), pool);

                precos = futurePrecos.get();
                receita = futureReceita.get();
                itemValue = futureItemValue.get();

                if (receita == null || receita.getMateriais().isEmpty()) return null;


                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {

                    // artefato: busca preco direto sem tentar receita de refino
                    if (mat.isArtefato()) {
                        final String idArtefato = mat.getUniqueName();
                        futures.add(CompletableFuture.runAsync(() -> {
                            try {
                                String[] p = idArtefato.split("_", 2);
                                int tMat = (p[0].startsWith("T") && p[0].length() == 2)
                                        ? Integer.parseInt(p[0].substring(1)) : tEfetivo;
                                String sufMat = p.length > 1 ? p[1] : idArtefato;
                                List<PriceEntry> pMat = apiService.buscarPrecos(sufMat, tMat, 0, -1, cidadesSemBM);

                            } catch (Exception ex) {}
                        }, pool));
                        continue;
                    }

                    final String idRefinado = mat.getUniqueName();
                    final String idRefinadoComEnch = (eEfetivo > 0)
                            ? idRefinado + "_LEVEL" + eEfetivo
                            : idRefinado;

                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            ReceitaCraft recRefino = craftService.buscarReceita(idRefinadoComEnch);

                            if (recRefino == null) {
                                // sem receita de refino (ornamento, token): busca preco direto
                                String[] p = idRefinado.split("_", 2);
                                int tMat = (p[0].startsWith("T") && p[0].length() == 2)
                                        ? Integer.parseInt(p[0].substring(1)) : tEfetivo;
                                String sufMat = p.length > 1 ? p[1] : idRefinado;
                                List<PriceEntry> pMat = apiService.buscarPrecos(sufMat, tMat, 0, -1, cidadesSemBM);
                                synchronized (precosMateriais) {
                                    precosMateriais.computeIfAbsent(idRefinado, k -> java.util.Collections.synchronizedList(new ArrayList<>())).addAll(pMat);
                                }
                                return;
                            }
                            synchronized (receitasRefino) {
                                receitasRefino.put(idRefinado, recRefino);
                            }


                            List<CompletableFuture<Void>> futuresIngredientes = new ArrayList<>();
                            for (ReceitaCraft.MaterialCraft matRefino : recRefino.getMateriais()) {
                                final String idMat = matRefino.getUniqueName();
                                final boolean ehRetorno = matRefino.isArtefato();
                                final String[] p = idMat.split("_", 2);
                                final int tMat = (p[0].startsWith("T") && p[0].length() == 2)
                                        ? Integer.parseInt(p[0].substring(1)) : tEfetivo;
                                final String sufMat = p.length > 1 ? p[1] : idMat;

                                futuresIngredientes.add(CompletableFuture.runAsync(() -> {
                                    try {
                                        List<PriceEntry> pMat;
                                        if (!ehRetorno && eEfetivo > 0) {
                                            pMat = apiService.buscarPrecos(sufMat + "_LEVEL" + eEfetivo, tMat, eEfetivo, -1, cidadesSemBM);
                                        } else {
                                            pMat = apiService.buscarPrecos(sufMat, tMat, 0, -1, cidadesSemBM);
                                        }
                                        synchronized (precosMateriais) {
                                            precosMateriais.computeIfAbsent(idMat, k -> java.util.Collections.synchronizedList(new ArrayList<>())).addAll(pMat);
                                        }
                                    } catch (Exception ex) {}
                                }, pool));
                            }
                            CompletableFuture.allOf(futuresIngredientes.toArray(new CompletableFuture[0])).get();
                        } catch (Exception ex) {}
                    }, pool));
                }


                String sufixoDiario = BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
                CompletableFuture<Void> futureDiarios = CompletableFuture.completedFuture(null);

                if (sufixoDiario != null && tEfetivo >= 2) {
                    futureDiarios = CompletableFuture.runAsync(() -> {
                        try {
                            String sufVazio = sufixoDiario + "_EMPTY";
                            String sufCheio = sufixoDiario + "_FULL";

                            CompletableFuture<List<PriceEntry>> fVazio =
                                    CompletableFuture.supplyAsync(() -> {
                                        try {
                                            return apiService.buscarPrecos(sufVazio, tEfetivo, 0, 1, cidadesSemBM);
                                        } catch (Exception ex) {
                                            return List.of();
                                        }
                                    }, pool);

                            CompletableFuture<List<PriceEntry>> fCheio =
                                    CompletableFuture.supplyAsync(() -> {
                                        try {
                                            return apiService.buscarPrecos(sufCheio, tEfetivo, 0, 1, cidadesSemBM);
                                        } catch (Exception ex) {
                                            return List.of();
                                        }
                                    }, pool);

                            List<PriceEntry> listaVazio = fVazio.get();
                            List<PriceEntry> listaCheio = fCheio.get();

                            precosDiarioCheioTodos = listaCheio;
                            precoDiarioVazioEntry = listaVazio.stream()
                                    .filter(pe2 -> pe2.getSellMin() > 0)
                                    .min(Comparator.comparingLong(PriceEntry::getSellMin))
                                    .orElse(null);
                            precoDiarioCheioEntry = listaCheio.stream()
                                    .filter(pe2 -> pe2.getBuyMax() > 0)
                                    .max(Comparator.comparingLong(PriceEntry::getBuyMax))
                                    .orElse(null);
                        } catch (Exception ex) {

                        }
                    }, pool);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                futureDiarios.get();
                return null;
            }

            @Override
            protected void succeeded() {
                receitaCraft = receita;

                precoDiarioVazioApi = precoDiarioVazioEntry != null
                        ? (double) precoDiarioVazioEntry.getSellMin() : 0;
                precoDiarioCheioApi = precoDiarioCheioEntry != null
                        ? (double) precoDiarioCheioEntry.getBuyMax() : 0;

                precoDiarioVazioEntryCache = precoDiarioVazioEntry;

                atualizarTabelaPrecos(precos, precosDiarioCheioTodos);
                atualizarTabelaReceitaCraft(receita);
                atualizarTabelaMateriais(receita, precosMateriais, precoDiarioVazioEntry);
                atualizarCalculo();
                progresso.setVisible(false);
                labelStatus.setText("Dados atualizados.");
                pool.shutdown();
            }

            @Override
            protected void failed() {
                progresso.setVisible(false);
                labelStatus.setText("Erro: " + getException().getMessage());
                pool.shutdown();
            }
        };

        new Thread(tarefa, "thread-craft-refino").start();
    }


    private void atualizarTabelaPrecos(List<PriceEntry> entradas,
                                       List<PriceEntry> precosDiarioCheio) {
        Map<String, PriceEntry> melhor = new LinkedHashMap<>();
        for (PriceEntry pe : entradas) {
            String chave = pe.getItemId() + "|" + pe.getCidade();
            PriceEntry atual = melhor.get(chave);
            if (atual == null
                    || (pe.getSellMin() > 0
                    && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                melhor.put(chave, pe);
            }
        }

        List<LinhaPreco> linhas = new ArrayList<>();
        for (PriceEntry pe : melhor.values()) {
            if (pe.getSellMin() == 0 && pe.getBuyMax() == 0) continue;
            String cor = BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888");
            linhas.add(new LinhaPreco(
                    pe.getItemId(),
                    FormatadorUtil.nomeQualidade(pe.getQualidade()),
                    pe.getCidade(), cor,
                    FormatadorUtil.formatarPreco(pe.getSellMin()),
                    FormatadorUtil.formatarData(
                            (pe.getSellDate() != null && !pe.getSellDate().startsWith("0001"))
                                    ? pe.getSellDate() : pe.getBuyDate())));
        }
        linhas.sort(Comparator.comparing(l -> l.cidade));


        int tEfetivo = AlbionIdUtil.tierEfetivo(tier);
        String sufixoDiario = BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
        if (sufixoDiario != null && tEfetivo >= 2 && precosDiarioCheio != null) {
            for (PriceEntry pd : precosDiarioCheio) {
                if (pd.getBuyMax() <= 0) continue;
                String cor = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(pd.getCidade()))
                        .map(CidadeInfo::getCor).findFirst().orElse("#888");
                String nomeDiario = BancoDeDadosItens.getNomeDiario(sufixoDiario);
                linhas.add(new LinhaPreco(
                        "Diario Cheio",
                        "Diario de " + nomeDiario + " (cheio)",
                        pd.getCidade(), cor,
                        FormatadorUtil.formatarPreco(pd.getBuyMax()),
                        FormatadorUtil.formatarData(pd.getBuyDate())));
            }
        }

        tabelaPrecos.setItems(FXCollections.observableArrayList(linhas));
        tabelaPrecos.setPrefHeight(28.0 + linhas.size() * 40.0);
        tabelaPrecos.setMaxHeight(tabelaPrecos.getPrefHeight());
    }

    private void atualizarTabelaReceitaCraft(ReceitaCraft receita) {
        if (receita == null) return;

        int tAtual = AlbionIdUtil.tierEfetivo(tier);
        int eAtual = AlbionIdUtil.enchantEfetivo(enchant);

        List<LinhaReceita> linhas = new ArrayList<>();
        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            boolean ehArtefato = mat.isArtefato();

            String iconeId = (!ehArtefato && eAtual > 0)
                    ? idMat + "_LEVEL" + eAtual : idMat;
            String iconeUrl = "https://render.albiononline.com/v1/item/" + iconeId + ".png";

            String sufixo = idMat.contains("_")
                    ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T'
                    && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : tAtual;

            String nomeRec = BancoDeDadosItens.getNomeRecurso(sufixo, tierMat);
            String nome = nomeRec != null ? nomeRec
                    : BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixo))
                    .map(ItemDefinition::getNome).findFirst().orElse(idMat);
            String nomeExibir = (!ehArtefato && eAtual > 0) ? nome + " ." + eAtual : nome;
            String tipo = ehArtefato ? "Artefato" : "Recurso";

            linhas.add(new LinhaReceita(iconeUrl, nomeExibir, tipo, mat.getCount()));
        }

        tabelaReceitaCraft.setItems(FXCollections.observableArrayList(linhas));
        tabelaReceitaCraft.setPrefHeight(28.0 + linhas.size() * 40.0);
        tabelaReceitaCraft.setMaxHeight(tabelaReceitaCraft.getPrefHeight());
    }


    private void atualizarTabelaMateriais(ReceitaCraft receita,
                                          Map<String, List<PriceEntry>> precosMateriais,
                                          PriceEntry precoDiarioVazio) {
        if (receita == null) return;

        int tAtual = AlbionIdUtil.tierEfetivo(tier);
        int eAtual = AlbionIdUtil.enchantEfetivo(enchant);


        Set<String> sufixosRefinados = Set.of(
                "CLOTH", "LEATHER", "PLANKS", "METALBAR", "STONEBLOCK");
        Set<String> sufixosBrutos = Set.of(
                "FIBER", "ORE", "WOOD", "HIDE", "ROCK");


        Map<String, Integer> qtdPorId = new LinkedHashMap<>();
        Map<String, String> tipoPorId = new LinkedHashMap<>();

        for (ReceitaCraft.MaterialCraft matCraft : receita.getMateriais()) {
            String idRefinado = matCraft.getUniqueName();
            int qtdRefinado = matCraft.getCount();

            // se for artefato real da receita de craft, adiciona direto sem buscar refino
            if (matCraft.isArtefato()) {
                qtdPorId.merge(idRefinado, qtdRefinado, Integer::sum);
                tipoPorId.put(idRefinado, "Artefato");
                continue;
            }

            ReceitaCraft recRefino = receitasRefino.get(idRefinado);

            // material sem receita de refino (ornamento, token de faccao, etc)
            // tambem adiciona direto na tabela
            if (recRefino == null) {
                qtdPorId.merge(idRefinado, qtdRefinado, Integer::sum);
                tipoPorId.put(idRefinado, "Item especial");
                continue;
            }

            for (ReceitaCraft.MaterialCraft matRefino : recRefino.getMateriais()) {
                String idMat = matRefino.getUniqueName();
                String sufixo = idMat.contains("_")
                        ? idMat.substring(idMat.indexOf('_') + 1) : idMat;

                boolean ehRetorno = matRefino.isArtefato()
                        && !sufixosRefinados.contains(sufixo)
                        && !sufixosBrutos.contains(sufixo);
                boolean ehRefinado = sufixosRefinados.contains(sufixo);

                String tipo = ehRetorno ? "Retorno" : ehRefinado ? "Refinado" : "Bruto";

                int qtd = matRefino.getCount() * qtdRefinado;
                qtdPorId.merge(idMat, qtd, Integer::sum);
                tipoPorId.put(idMat, tipo);
            }
        }


        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        for (List<PriceEntry> lista : precosMateriais.values()) {
            for (PriceEntry pe : lista) {
                String chave = pe.getItemId();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null
                        || (pe.getSellMin() > 0
                        && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }

        List<LinhaMaterial> linhas = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : qtdPorId.entrySet()) {
            String idMat = entrada.getKey();
            int qtdTotal = entrada.getValue();
            String tipo = tipoPorId.getOrDefault(idMat, "Bruto");

            String sufixo = idMat.contains("_")
                    ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T'
                    && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : tAtual;

            String nomeRec = BancoDeDadosItens.getNomeRecurso(sufixo, tierMat);
            String nome = nomeRec != null ? nomeRec
                    : BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixo))
                    .map(ItemDefinition::getNome).findFirst().orElse(idMat);


            boolean ehRetorno = "Retorno".equals(tipo);
            String nomeExibir = (eAtual > 0 && !ehRetorno) ? nome + " ." + eAtual : nome;


            String chavePreco = (!ehRetorno && eAtual > 0)
                    ? idMat + "_LEVEL" + eAtual + "@" + eAtual
                    : idMat;
            PriceEntry pe = melhorCompra.get(chavePreco);

            System.out.println("chaves em melhorCompra: " + melhorCompra.keySet());
            System.out.println("buscando chave: " + chavePreco);

            String iconeId = (!ehRetorno && eAtual > 0)
                    ? idMat + "_LEVEL" + eAtual : idMat;
            String iconeUrl = "https://render.albiononline.com/v1/item/" + iconeId + ".png";

            String preco = pe != null ? FormatadorUtil.formatarPreco(pe.getSellMin()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String cor = pe != null
                    ? BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888")
                    : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getSellDate() != null && !pe.getSellDate().startsWith("0001"))
                            ? pe.getSellDate() : pe.getBuyDate()) : "-";

            linhas.add(new LinhaMaterial(iconeUrl, nomeExibir, tipo, qtdTotal,
                    cidade, cor, preco, data));
        }


        int tierDiario = AlbionIdUtil.tierEfetivo(tier);
        String sufixoDiario = BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
        if (sufixoDiario != null && tierDiario >= 2 && precoDiarioVazio != null) {
            String idVazio = "T" + tierDiario + "_" + sufixoDiario + "_EMPTY";
            String iconeVazio = "https://render.albiononline.com/v1/item/" + idVazio + ".png";
            String nomeDiario = BancoDeDadosItens.getNomeDiario(sufixoDiario);

            String precoVazioStr = FormatadorUtil.formatarPreco(precoDiarioVazio.getSellMin());
            String cidadeVazio = precoDiarioVazio.getCidade();
            String corVazio = BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(cidadeVazio))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888");
            String dataVazio = FormatadorUtil.formatarData(precoDiarioVazio.getSellDate());

            linhas.add(new LinhaMaterial(
                    iconeVazio,
                    "Diario de " + nomeDiario + " (vazio)",
                    "Diario",
                    1,
                    cidadeVazio, corVazio,
                    precoVazioStr,
                    dataVazio));
        }


        tabelaMateriais.setItems(FXCollections.observableArrayList(linhas));
        tabelaMateriais.setPrefHeight(28.0 + linhas.size() * 40.0);
        tabelaMateriais.setMaxHeight(tabelaMateriais.getPrefHeight());
    }


    private void atualizarCalculo() {
        if (painelCalculo == null) return;

        double qtdCraft = parseDoubleSafe(campoQuantidade, 1.0);
        double retCraft = parseDoubleSafe(campoRetornoCraft, 15.2) / 100.0;
        double barracaCraft = parseDoubleSafe(campoBarracaCraft, 3.0);
        double retRefino = parseDoubleSafe(campoRetornoRefino, 36.7) / 100.0;
        double barracaRefino = parseDoubleSafe(campoBarracaRefino, 3.0);


        double qtdFinalCraft = qtdCraft / (1.0 - retCraft);


        double custoMateriais = 0;
        if (tabelaMateriais != null) {
            for (LinhaMaterial lm : tabelaMateriais.getItems()) {
                if ("Retorno".equals(lm.tipo)) continue;
                double preco = FormatadorUtil.parseSilver(lm.precoCompra);

                double qtdEfetiva = lm.qtdTotal * qtdFinalCraft * (1.0 - retRefino);
                custoMateriais += preco * qtdEfetiva;
            }
        }


        double nutricaoRefino = itemValue * qtdFinalCraft * 0.1125;
        double taxaBarracaRefinoValor = (barracaRefino * nutricaoRefino) / 100.0;


        double melhorVenda = 0;
        String melhorCidadeTemp = "-";
        for (LinhaPreco lp : tabelaPrecos.getItems()) {
            if ("Diario Cheio".equals(lp.itemId)) continue;
            double v = FormatadorUtil.parseSilver(lp.sellMin);
            if (v > melhorVenda) {
                melhorVenda = v;
                melhorCidadeTemp = lp.cidade;
            }
        }
        final String melhorCidadeApi = melhorCidadeTemp;

        String nomeCidadeVenda = BancoDeDadosItens.CIDADES.stream()
                .filter(c -> c.getApiId().equals(melhorCidadeApi))
                .map(CidadeInfo::getNome).findFirst().orElse(melhorCidadeApi);


        CalculadoraService.ResultadoCalculo calc = CalculadoraService.calcular(
                qtdCraft, retCraft, barracaCraft,
                itemValue, custoMateriais, melhorVenda, possuiPremium);

        double qtdFinal = calc.qtdFinal;
        double custoComTaxa = calc.custoMateriais;
        double taxaCraft = calc.taxaBarraca;
        double receitaTotal = calc.receitaTotal;

        double custoTotal = custoComTaxa + taxaCraft + taxaBarracaRefinoValor;
        double taxaMercado = calc.taxaMercado;
        double lucroBase = receitaTotal - custoTotal - taxaMercado;


        int tEfetivo = AlbionIdUtil.tierEfetivo(tier);
        int eEfetivo = AlbionIdUtil.enchantEfetivo(enchant);
        int qtdMatNaoArtefato = (receitaCraft == null) ? 0
                : receitaCraft.getMateriais().stream()
                .filter(m -> !m.isArtefato())
                .mapToInt(ReceitaCraft.MaterialCraft::getCount)
                .sum();

        double diariosCompletos = CalculadoraService.calcularDiarios(
                tEfetivo, eEfetivo, qtdMatNaoArtefato, qtdFinal);

        double lucroDiarios = CalculadoraService.calcularLucroDiarios(
                diariosCompletos, precoDiarioVazioApi, precoDiarioCheioApi);

        double lucroComDiarios = lucroBase + lucroDiarios;

        lucroAtual = lucroComDiarios;
        custoAtual = custoTotal;


        List<String[]> metricas = new ArrayList<>();
        metricas.add(new String[]{"Qtd a craftar", FormatadorUtil.fmt(qtdCraft) + " un"});
        metricas.add(new String[]{"Qtd final craftada", String.format("%.2f un", qtdFinal)});
        metricas.add(new String[]{"Melhor preco de venda", FormatadorUtil.fmtSilver(melhorVenda)});
        metricas.add(new String[]{"Local de venda", nomeCidadeVenda});
        metricas.add(new String[]{"Custo dos materiais", FormatadorUtil.fmtSilver(custoComTaxa)});
        metricas.add(new String[]{"Taxa barraca de craft", FormatadorUtil.fmtSilver(taxaCraft)});
        metricas.add(new String[]{"Taxa barraca de refino", FormatadorUtil.fmtSilver(taxaBarracaRefinoValor)});
        metricas.add(new String[]{"Diarios completos", String.format("%.2f un", diariosCompletos)});
        metricas.add(new String[]{"Lucro c/ diarios", FormatadorUtil.fmtSilver(lucroDiarios)});

        FlowPane fluxo = new FlowPane(10, 10);
        fluxo.setPrefWrapLength(Double.MAX_VALUE);
        for (String[] m : metricas)
            fluxo.getChildren().add(criarCard(m[0], m[1], "#e0e0e0", "#2a2a2a"));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #444;");

        HBox destaque = new HBox(16);
        destaque.setAlignment(Pos.CENTER);

        VBox cardCusto = criarCardDestaque("Custo Total", FormatadorUtil.fmtSilver(custoTotal), "#e05555");
        VBox cardReceita = criarCardDestaque("Receita Total", FormatadorUtil.fmtSilver(receitaTotal), "#3dba6e");
        VBox cardLucro = criarCardDestaque(
                lucroComDiarios >= 0 ? "Lucro" : "Prejuizo",
                (lucroComDiarios >= 0 ? "+" : "") + FormatadorUtil.fmtSilver(lucroComDiarios),
                lucroComDiarios >= 0 ? "#5a8dee" : "#e05555");

        HBox.setHgrow(cardCusto, Priority.ALWAYS);
        HBox.setHgrow(cardReceita, Priority.ALWAYS);
        HBox.setHgrow(cardLucro, Priority.ALWAYS);
        destaque.getChildren().addAll(cardCusto, cardReceita, cardLucro);

        painelCalculo.getChildren().setAll(fluxo, sep, destaque);
    }


    private TableColumn<LinhaPreco, String> criarColunaCidade() {
        TableColumn<LinhaPreco, String> col = new TableColumn<>("Cidade");
        col.setPrefWidth(130);
        col.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                LinhaPreco lp = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(lp.corCidade));
                String nome = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(v))
                        .map(CidadeInfo::getNome).findFirst().orElse(v);
                HBox hb = new HBox(6, ponto, new Label(nome));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });
        return col;
    }

    private TableColumn<LinhaPreco, String> criarColunaPrecoVenda(String titulo, double largura) {
        TableColumn<LinhaPreco, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().sellMin));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || "-".equals(v)) {
                    setText("-");
                    setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(v);
                    setStyle("-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });
        return col;
    }

    private <T> TableColumn<T, String> coluna(
            String titulo, double largura,
            javafx.util.Callback<TableColumn.CellDataFeatures<T, String>,
                    javafx.beans.value.ObservableValue<String>> callback) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(callback);
        return col;
    }

    private VBox criarCard(String titulo, String valor, String corValor, String corFundo) {
        Label lblT = new Label(titulo);
        lblT.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        Label lblV = new Label(valor);
        lblV.setStyle("-fx-text-fill: " + corValor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        VBox card = new VBox(3, lblT, lblV);
        card.setPadding(new Insets(8, 14, 8, 14));
        card.setStyle("-fx-background-color: " + corFundo + "; "
                + "-fx-background-radius: 6; -fx-border-color: #333; "
                + "-fx-border-radius: 6; -fx-border-width: 1;");
        return card;
    }

    private VBox criarCardDestaque(String titulo, String valor, String cor) {
        Label lblT = new Label(titulo);
        lblT.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label lblV = new Label(valor);
        lblV.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        VBox card = new VBox(4, lblT, lblV);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #252525; -fx-background-radius: 8; "
                + "-fx-border-color: " + cor + "; -fx-border-radius: 8; -fx-border-width: 1.5;");
        return card;
    }

    private Label secao(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
        return lbl;
    }

    private Label label(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        return lbl;
    }

    private TextField campoCampo(String valor) {
        TextField tf = new TextField(valor);
        tf.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        tf.textProperty().addListener((obs, ant, novo) -> {
            if (tabelaMateriais != null) tabelaMateriais.refresh();
            atualizarCalculo();
        });
        return tf;
    }


    private double parseDoubleSafe(TextField campo, double padrao) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (Exception ex) {
            return padrao;
        }
    }

    private int parseIntSafe(TextField campo, int padrao) {
        try {
            return Math.max(1, Integer.parseInt(campo.getText().trim()));
        } catch (Exception ex) {
            return padrao;
        }
    }


    private void salvarOperacao() {
        try {
            int t = AlbionIdUtil.tierEfetivo(tier);
            int e = AlbionIdUtil.enchantEfetivo(enchant);

            String melhorCidadeApiTemp = "-";
            double melhorV = 0;
            for (LinhaPreco lp : tabelaPrecos.getItems()) {
                if ("Diario Cheio".equals(lp.itemId)) continue;
                double v = FormatadorUtil.parseSilver(lp.sellMin);
                if (v > melhorV) {
                    melhorV = v;
                    melhorCidadeApiTemp = lp.cidade;
                }
            }
            final String melhorCidadeApi = melhorCidadeApiTemp;
            String nomeCidadeVenda = BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(melhorCidadeApi))
                    .map(CidadeInfo::getNome)
                    .findFirst().orElse(melhorCidadeApi);

            String locaisJson = cidadesPorMaterialJson();

            double qtdInicial = parseDoubleSafe(campoQuantidade, 1.0);
            double taxaRetorno = parseDoubleSafe(campoRetornoCraft, 15.2) / 100.0;
            double qtdFinal = qtdInicial / (1.0 - taxaRetorno);

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"item\": \"").append(item.getNome().replace("\"", "\\\"")).append("\",\n");
            sb.append("  \"itemId\": \"").append(itemIdCompleto).append("\",\n");
            sb.append("  \"tier\": ").append(t).append(",\n");
            sb.append("  \"encantamento\": ").append(e).append(",\n");
            sb.append("  \"parametros\": {\n");
            sb.append("    \"quantidade\": \"").append(campoQuantidade.getText()).append("\",\n");
            sb.append("    \"taxaRetornoCraft\": \"").append(campoRetornoCraft.getText()).append("\",\n");
            sb.append("    \"taxaBarracaCraft\": \"").append(campoBarracaCraft.getText()).append("\",\n");
            sb.append("    \"taxaRetornoRefino\": \"").append(campoRetornoRefino.getText()).append("\",\n");
            sb.append("    \"taxaBarracaRefino\": \"").append(campoBarracaRefino.getText()).append("\"\n");
            sb.append("  },\n");
            sb.append("  \"calculadora\": {\n");
            sb.append("    \"Quantidade a craftar\": \"").append(FormatadorUtil.fmt(qtdInicial)).append(" un\",\n");
            sb.append("    \"Qtd final craftada\": \"").append(String.format("%.2f un", qtdFinal)).append("\",\n");
            sb.append("    \"Melhor preco de venda\": \"").append(FormatadorUtil.fmtSilver(melhorV)).append("\",\n");
            sb.append("    \"Local de venda\": \"").append(nomeCidadeVenda).append("\",\n");
            sb.append("    \"Custo dos materiais\": \"").append(FormatadorUtil.fmtSilver(custoAtual)).append("\",\n");
            sb.append("    \"Local de compra dos materiais\": ").append(locaisJson).append(",\n");
            sb.append("    \"Custo total\": \"").append(FormatadorUtil.fmtSilver(custoAtual)).append("\",\n");
            sb.append("    \"Lucro/Prejuizo\": \"").append(lucroAtual >= 0 ? "+" : "").append(FormatadorUtil.fmtSilver(lucroAtual)).append("\"\n");
            sb.append("  }\n");
            sb.append("}\n");

            String nomeArquivo = OperacaoService.salvar(itemIdCompleto, sb.toString());
            labelStatus.setText("Operacao salva: " + nomeArquivo);

        } catch (Exception ex) {
            labelStatus.setText("Erro ao salvar: " + ex.getMessage());
        }
    }

    private String cidadesPorMaterialJson() {
        if (tabelaMateriais == null || tabelaMateriais.getItems().isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean primeiro = true;
        for (LinhaMaterial lm : tabelaMateriais.getItems()) {
            if (!primeiro) sb.append(", ");
            primeiro = false;
            String nomeCidade = BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(lm.cidade))
                    .map(CidadeInfo::getNome)
                    .findFirst().orElse(lm.cidade != null ? lm.cidade : "-");
            int qtdReal = lm.qtdTotal * parseIntSafe(campoQuantidade, 1);
            sb.append("{\"material\": \"").append(lm.nome.replace("\"", "\\\""))
                    .append("\", \"quantidade\": ").append(qtdReal)
                    .append(", \"cidade\": \"").append(nomeCidade).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }


    private void ativarEdicaoManual(boolean ativo) {
        // coluna de preco de venda na tabela de precos
        for (TableColumn<LinhaPreco, ?> col : tabelaPrecos.getColumns()) {
            if (!"Preco de Venda".equals(col.getText())) continue;
            @SuppressWarnings("unchecked")
            TableColumn<LinhaPreco, String> colStr = (TableColumn<LinhaPreco, String>) col;
            if (ativo) {
                colStr.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
                colStr.setOnEditCommit(ev -> {
                    LinhaPreco antiga = ev.getRowValue();
                    int idx = tabelaPrecos.getItems().indexOf(antiga);
                    tabelaPrecos.getItems().set(idx, new LinhaPreco(
                            antiga.itemId, antiga.qualidade, antiga.cidade,
                            antiga.corCidade, ev.getNewValue(), antiga.atualizado));
                    atualizarCalculo();
                });
            } else {
                colStr.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String v, boolean empty) {
                        super.updateItem(v, empty);
                        if (empty || v == null || "-".equals(v)) {
                            setText("-");
                            setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                        } else {
                            setText(v);
                            setStyle("-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                        }
                    }
                });
            }
        }
        tabelaPrecos.setEditable(ativo);

        // coluna de preco de compra na tabela de materiais
        for (TableColumn<LinhaMaterial, ?> col : tabelaMateriais.getColumns()) {
            if (!"Preco de Compra".equals(col.getText())) continue;
            @SuppressWarnings("unchecked")
            TableColumn<LinhaMaterial, String> colStr = (TableColumn<LinhaMaterial, String>) col;
            if (ativo) {
                colStr.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
                colStr.setOnEditCommit(ev -> {
                    LinhaMaterial antiga = ev.getRowValue();
                    int idx = tabelaMateriais.getItems().indexOf(antiga);
                    tabelaMateriais.getItems().set(idx, new LinhaMaterial(
                            antiga.iconeUrl, antiga.nome, antiga.tipo, antiga.qtdTotal,
                            antiga.cidade, antiga.corCidade, ev.getNewValue(), antiga.atualizado));
                    atualizarCalculo();
                });
            } else {
                colStr.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String v, boolean empty) {
                        super.updateItem(v, empty);
                        if (empty || v == null || "-".equals(v)) {
                            setText("-");
                            setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                        } else {
                            setText(v);
                            setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                        }
                    }
                });
            }
        }
        tabelaMateriais.setEditable(ativo);
    }


}