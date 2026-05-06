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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.albionmarket.service.CalculadoraService;
//import java.text.Normalizer;

import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * tela de refino precos do recurso refinado, receita com precos dos materiais brutos e calculadora
 */
public class TelaRefino {

    // contexto
    private final Stage palco;
    private final ItemDefinition item;
    private final int tier;
    private final int enchant;
    private boolean possuiPremium = false;


    // servicos
    private final ApiService apiService = new ApiService();
    private final CraftService craftService = new CraftService();

    // controles da lateral
    private Label labelStatus;
    private ProgressIndicator progresso;

    // campos de calculo
    private TextField campoQuantidade;
    private TextField campoRetorno;
    private TextField campoSinergiaBarraca;
    private Label labelItemValue;

    // tabelas
    private TableView<LinhaPreco> tabelaPrecos;
    private TableView<LinhaMaterial> tabelaReceita;
    private TableView<LinhaMaterialPreco> tabelaMateriais;
    private VBox painelCalculo;

    // dados
    private ReceitaCraft receitaAtual;
    private long itemValue = 0;


    private double lucroAtual = 0;
    private double custoAtual = 0;
    private double receitaAtual2 = 0;

    // toggle de edicao manual de precos
    private boolean modoEdicaoManual = false;

    // estado dos filtros da tela anterior para restaurar ao clicar voltar
    private final EstadoSelecao estadoSelecao;

    private final String itemIdApi;
    private final String itemIdRender;

    // modelo da tabela de precos do refinado
    public static class LinhaPreco {
        public final String itemId, qualidade, cidade, corCidade;
        public final String sellMin, atualizado;

        public LinhaPreco(String itemId, String qualidade, String cidade, String corCidade,
                          String sellMin, String atualizado) {
            this.itemId = itemId;
            this.qualidade = qualidade;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.sellMin = sellMin;
            this.atualizado = atualizado;
        }
    }

    // modelo da tabela de receita (materiais brutos)
    public static class LinhaMaterial {
        public final String iconeUrl, nome, tipo, cidade, corCidade;
        public final String buyMax, atualizado;
        public final int qtd;

        public LinhaMaterial(String iconeUrl, String nome, String tipo, int qtd,
                             String cidade, String corCidade, String buyMax, String atualizado) {
            this.iconeUrl = iconeUrl;
            this.nome = nome;
            this.tipo = tipo;
            this.qtd = qtd;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.buyMax = buyMax;
            this.atualizado = atualizado;
        }
    }

    // modelo da tabela de precos dos materiais brutos
    public static class LinhaMaterialPreco {
        public final String nome, tipo, cidade, corCidade, buyMax, atualizado;
        public final int qtdNecessaria;

        public LinhaMaterialPreco(String nome, String tipo, int qtdNecessaria,
                                  String cidade, String corCidade, String buyMax, String atualizado) {
            this.nome = nome;
            this.tipo = tipo;
            this.qtdNecessaria = qtdNecessaria;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.buyMax = buyMax;
            this.atualizado = atualizado;
        }
    }

    // modelo da tabela de calculo
    public static class LinhaCalculo {
        public final String nomeColuna, valor;

        public LinhaCalculo(String nomeColuna, String valor) {
            this.nomeColuna = nomeColuna;
            this.valor = valor;
        }
    }

    public TelaRefino(Stage palco, ItemDefinition item, int tier, int enchant) {
        this(palco, item, tier, enchant, null);
    }

    public TelaRefino(Stage palco, ItemDefinition item, int tier, int enchant, EstadoSelecao estadoSelecao) {
        this.palco = palco;
        this.item = item;
        this.tier = tier;
        this.enchant = enchant;
        this.estadoSelecao = estadoSelecao;
        int t = AlbionIdUtil.tierEfetivo(tier);
        int e = AlbionIdUtil.enchantEfetivo(enchant);
        this.itemIdApi = AlbionIdUtil.buildApiId(item.getId(), t, e);
        this.itemIdRender = e > 0 ? "T" + t + "_" + item.getId() + "_LEVEL" + e
                : "T" + t + "_" + item.getId();
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setLeft(criarLateral());
        raiz.setCenter(criarAreaCentral());

        palco.setTitle("Refino de: " + item.getNome());
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        buscarTudo();
    }

    // cabecalho igual ao craft
    private HBox criarCabecalho() {
        Label titulo = new Label("Refino");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Calculadora de Refino - " + item.getNome());
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        // voltar pra home
        Label btnHome = new Label("Início");
        btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand;");
        btnHome.setTooltip(new Tooltip("Voltar para Home"));
        btnHome.setOnMouseEntered(e -> btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand; -fx-opacity: 0.7;"));
        btnHome.setOnMouseExited(e -> btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand;"));
        btnHome.setOnMouseClicked(e -> new TelaHome(palco).mostrar());

        HBox cab = new HBox(textos, espacador, btnHome);
        cab.setAlignment(Pos.CENTER_LEFT);
        cab.setPadding(new Insets(14, 20, 14, 20));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cab;
    }

    // lateral esquerda
    private ScrollPane criarLateral() {
        VBox painel = new VBox(14);
        painel.setPadding(new Insets(16));
        painel.setPrefWidth(280);
        painel.setStyle("-fx-background-color: #252525;");

        // icone do recurso refinado
        ImageView icone = new ImageView();
        icone.setFitWidth(160);
        icone.setFitHeight(100);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);
        icone.setImage(new Image("https://render.albiononline.com/v1/item/" + itemIdRender + ".png", true));
        Label nomeItem = new Label(item.getNome());
        nomeItem.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 13px;");
        nomeItem.setWrapText(true);

        int t = (tier == -1) ? 4 : tier;
        int e = (enchant == -1) ? 0 : enchant;
        Label infoItem = new Label("Tier " + t + (e > 0 ? "  ·  Ench. ." + e : ""));
        infoItem.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        VBox boxIcone = new VBox(6, icone, nomeItem, infoItem);
        boxIcone.setAlignment(Pos.CENTER);
        boxIcone.setPadding(new Insets(0, 0, 10, 0));
        painel.getChildren().addAll(boxIcone, separador());

        // parametros de refino
        painel.getChildren().add(secao("Parâmetros de Refino"));
        campoQuantidade = campoCampo("1");
        campoRetorno = campoCampo("15.2");
        campoSinergiaBarraca = campoCampo("3.0");
        labelItemValue = new Label("-");
        labelItemValue.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 12px; -fx-font-weight: bold;");

        painel.getChildren().addAll(
                label("Quantidade a refinar"), campoQuantidade,
                label("Taxa de retorno (%)"), campoRetorno,
                label("Taxa da barraca"), campoSinergiaBarraca
        );

        painel.getChildren().add(separador());

        // switch inserir precos manualmente igual ao craft
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

        Label labelSwitch = new Label("Inserir preços manualmente");
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

        // switch premium
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


        Label labelPremium = new Label("Possui premium ativa?");
        labelPremium.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");

        HBox switchPremiumBox = new HBox(8, canvasPremium, labelPremium);
        switchPremiumBox.setAlignment(Pos.CENTER_LEFT);
        switchPremiumBox.setCursor(javafx.scene.Cursor.HAND);
        switchPremiumBox.setOnMouseClicked(ev -> {
            estadoPremium[0] = !estadoPremium[0];
            desenharPremium.run();
            possuiPremium = estadoPremium[0];
            atualizarTabelaCalculo();
        });
        painel.getChildren().add(switchPremiumBox);
        painel.getChildren().add(separador());

        // status
        progresso = new ProgressIndicator();
        progresso.setMaxSize(24, 24);
        progresso.setVisible(false);
        labelStatus = new Label("Carregando dados...");
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
        btnVoltar.setOnAction(v -> new TelaRefinoSelecao(palco, estadoSelecao).mostrar());

        painel.getChildren().addAll(btnAtualizar, espaco, btnSalvarOperacao, btnVoltar);

        ScrollPane scroll = new ScrollPane(painel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #252525; -fx-background-color: #252525;");
        return scroll;
    }

    // area central igual ao craft mas com labels de refino
    private ScrollPane criarAreaCentral() {
        Label tituloPrecos = new Label("Preços no Mercado");
        tituloPrecos.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        tabelaPrecos = new TableView<>();
        tabelaPrecos.setStyle("-fx-background-color: #1e1e1e;");
        tabelaPrecos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaPrecos.setPlaceholder(new Label("Buscando preços..."));
        tabelaPrecos.setPrefHeight(220);

        TableColumn<LinhaPreco, String> colQual = coluna("Qualidade", 110,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().qualidade));
        TableColumn<LinhaPreco, String> colCidadePreco = criarColunaCidade();
        TableColumn<LinhaPreco, String> colSell = criarColunaPreco("Preço de Venda", 130);
        TableColumn<LinhaPreco, String> colDataPreco = coluna("Última Atualização", 100,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaPrecos.getColumns().addAll(colQual, colCidadePreco, colSell, colDataPreco);

        // receita de refino vinda da api
        Label tituloReceita = new Label("Receita de Refino");
        tituloReceita.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloReceita.setPadding(new Insets(12, 0, 6, 0));

        tabelaReceita = new TableView<>();
        tabelaReceita.setStyle("-fx-background-color: #1e1e1e;");
        tabelaReceita.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaReceita.setPlaceholder(new Label("Carregando receita..."));

        // coluna de icone do material bruto
        TableColumn<LinhaMaterial, String> colIcone = new TableColumn<>("  ");
        colIcone.setPrefWidth(70);
        colIcone.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().iconeUrl));
        colIcone.setCellFactory(tc -> new TableCell<>() {
            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(32);
                iv.setFitHeight(32);
                iv.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setGraphic(null);
                    return;
                }
                Image img = new Image(url, 32, 32, true, true, true);
                img.errorProperty().addListener((obs, ant, erro) -> {
                    if (erro) System.out.println("ERRO ao carregar: " + url + " | " + img.getException());
                });
                iv.setImage(img);
                setGraphic(iv);
            }
        });

        TableColumn<LinhaMaterial, String> colNomeMat = coluna("Material", 180,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().nome));

        // tipo: bruto em amarelo, retorno em roxo (retorno é o que a api chama de artefato no refino)
        TableColumn<LinhaMaterial, String> colTipoMat = new TableColumn<>("Tipo");
        colTipoMat.setPrefWidth(80);
        colTipoMat.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().tipo));
        colTipoMat.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    return;
                }
                setText(v);
                setStyle(v.equals("Retorno")
                        ? "-fx-text-fill: #9b59b6; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e0b84a;");
            }
        });

        TableColumn<LinhaMaterial, String> colQtd1 = new TableColumn<>("Qtd p/ 1 item");
        colQtd1.setPrefWidth(100);
        colQtd1.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getValue().qtd)));
        colQtd1.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        tabelaReceita.getColumns().addAll(colIcone, colNomeMat, colTipoMat, colQtd1);

        // tabela de precos dos materiais brutos
        Label tituloMateriais = new Label("Precos dos Materiais");
        tituloMateriais.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloMateriais.setPadding(new Insets(12, 0, 6, 0));

        tabelaMateriais = new TableView<>();
        tabelaMateriais.setStyle("-fx-background-color: #1e1e1e;");
        tabelaMateriais.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaMateriais.setPlaceholder(new Label("Carregando..."));

        TableColumn<LinhaMaterialPreco, String> colMatNome = coluna("Material", 200,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().nome));

        // qtd total = qtd da receita * quantidade a refinar
        // o tipo retorno nao multiplica pq ele eh o bonus, nao o material consumido
        TableColumn<LinhaMaterialPreco, String> colMatQtd = new TableColumn<>("Qtd necessaria");
        colMatQtd.setPrefWidth(120);
        colMatQtd.setCellValueFactory(r -> {
            LinhaMaterialPreco lm = r.getValue();
            int q = parseIntSafe(campoQuantidade, 1);
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(lm.qtdNecessaria * q));
        });
        colMatQtd.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #e0b84a; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        TableColumn<LinhaMaterialPreco, String> colMatBuy = new TableColumn<>("Preco de Compra");
        colMatBuy.setPrefWidth(140);
        colMatBuy.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().buyMax));
        colMatBuy.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("-")) {
                    setText("-");
                    setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(v);
                    setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        TableColumn<LinhaMaterialPreco, String> colMatCidade = new TableColumn<>("Local");
        colMatCidade.setPrefWidth(130);
        colMatCidade.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        colMatCidade.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("-")) {
                    setText("-");
                    setGraphic(null);
                    return;
                }
                LinhaMaterialPreco linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));
                String nome = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(v))
                        .map(CidadeInfo::getNome).findFirst().orElse(v);
                HBox hb = new HBox(6, ponto, new Label(nome));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });

        TableColumn<LinhaMaterialPreco, String> colMatData = coluna("Ultima Atualizacao", 110,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaMateriais.getColumns().addAll(colMatNome, colMatQtd, colMatBuy, colMatCidade, colMatData);

        // calculadora de lucro
        Label tituloCalculo = new Label("Calculadora de Lucro");
        tituloCalculo.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloCalculo.setPadding(new Insets(12, 0, 6, 0));

        painelCalculo = new VBox(10);
        painelCalculo.setStyle("-fx-background-color: #1e1e1e;");

        VBox area = new VBox(10,
                tituloPrecos, tabelaPrecos,
                tituloReceita, tabelaReceita,
                tituloMateriais, tabelaMateriais,
                tituloCalculo, painelCalculo);

        area.setPadding(new Insets(16));
        area.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(tabelaPrecos, Priority.SOMETIMES);
        VBox.setVgrow(tabelaReceita, Priority.SOMETIMES);
        VBox.setVgrow(tabelaMateriais, Priority.SOMETIMES);
        VBox.setVgrow(painelCalculo, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(area);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    // logica principal igual ao craft, busca precos e receita da api
    private void buscarTudo() {
        List<String> cidades = (estadoSelecao != null && estadoSelecao.cidades != null && !estadoSelecao.cidades.isEmpty())
                ? estadoSelecao.cidades
                : com.albionmarket.service.BancoDeDadosItens.CIDADES.stream().map(CidadeInfo::getApiId).collect(Collectors.toList());

        List<String> cidadesSemBM = cidades.stream()
                .filter(c -> !c.equals("BlackMarket"))
                .collect(Collectors.toList());

        progresso.setVisible(true);
        labelStatus.setText("Buscando precos e receita...");
        tabelaPrecos.setItems(FXCollections.emptyObservableList());
        tabelaReceita.setItems(FXCollections.emptyObservableList());
        if (tabelaMateriais != null) tabelaMateriais.setItems(FXCollections.emptyObservableList());

        int tierEfetivo    = (tier    == -1) ? 4 : tier;
        int enchantEfetivo = (enchant == -1) ? 0 : enchant;

        ExecutorService pool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        Task<Void> tarefa = new Task<>() {

            private List<PriceEntry> precos;
            private ReceitaCraft     receita;
            private List<PriceEntry> precosMateirais;

            @Override
            protected Void call() throws Exception {

                // etapa 1: precos do refinado e receita em paralelo
                String sufixoBusca = enchantEfetivo > 0
                        ? item.getId() + "_LEVEL" + enchantEfetivo
                        : item.getId();

                CompletableFuture<List<PriceEntry>> futurePrecos = CompletableFuture.supplyAsync(() -> {
                    try {
                        return apiService.buscarPrecos(sufixoBusca, tierEfetivo, enchantEfetivo, -1, cidades);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }, pool);

                CompletableFuture<ReceitaCraft> futureReceita = CompletableFuture.supplyAsync(() -> {
                    try {
                        String itemIdSemEnchant = itemIdApi.contains("@") ? itemIdApi.split("@")[0] : itemIdApi;
                        return craftService.buscarReceita(itemIdSemEnchant);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }, pool);

                CompletableFuture<Long> futureItemValue = CompletableFuture.supplyAsync(
                        () -> ItemValues.getValor(itemIdApi), pool);

                // aguarda as tres antes de continuar
                precos    = futurePrecos.get();
                receita   = futureReceita.get();
                itemValue = futureItemValue.get();

                if (receita == null || receita.getMateriais().isEmpty()) return null;

                // etapa 2: cada material em paralelo
                List<CompletableFuture<List<PriceEntry>>> futuresMateriais = new ArrayList<>();

                for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
                    futuresMateriais.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            String idMat = mat.getUniqueName();
                            String[] partes = idMat.split("_", 2);
                            int tMat = (partes[0].startsWith("T") && partes[0].length() == 2)
                                    ? Integer.parseInt(partes[0].substring(1)) : 4;
                            String sufixo = partes.length > 1 ? partes[1] : idMat;

                            if (enchantEfetivo == 0) {
                                return apiService.buscarPrecos(sufixo, tMat, 0, -1, cidadesSemBM);
                            } else {
                                String sufixoLevel = sufixo + "_LEVEL" + enchantEfetivo;
                                return apiService.buscarPrecos(sufixoLevel, tMat, enchantEfetivo, -1, cidadesSemBM);
                            }
                        } catch (Exception ex) {
                            return List.of();
                        }
                    }, pool));
                }

                // aguarda todos os materiais
                CompletableFuture.allOf(
                        futuresMateriais.toArray(new CompletableFuture[0])
                ).get();

                // agrega resultados
                precosMateirais = new ArrayList<>();
                for (CompletableFuture<List<PriceEntry>> f : futuresMateriais) {
                    precosMateirais.addAll(f.get());
                }

                return null;
            }

            @Override
            protected void succeeded() {
                receitaAtual = receita;
                atualizarTabelaPrecos(precos);
                atualizarTabelaReceita(receita, precosMateirais);
                atualizarTabelaMateriais(receita, precosMateirais);
                atualizarTabelaCalculo();
                labelItemValue.setText(itemValue > 0 ? String.format("%,d", itemValue) : "nao cadastrado");
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

        new Thread(tarefa, "thread-refino").start();
    }

    private void atualizarTabelaPrecos(List<PriceEntry> entradas) {
        Map<String, PriceEntry> melhor = new LinkedHashMap<>();
        for (PriceEntry pe : entradas) {
            String chave = pe.getItemId() + "|" + pe.getCidade();
            PriceEntry atual = melhor.get(chave);
            if (atual == null || (pe.getSellMin() > 0 && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                melhor.put(chave, pe);
            }
        }

        List<LinhaPreco> linhas = new ArrayList<>();
        for (PriceEntry pe : melhor.values()) {
            if (pe.getSellMin() == 0 && pe.getBuyMax() == 0) continue;
            String corCidade = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888");

            linhas.add(new LinhaPreco(
                    pe.getItemId(),
                    FormatadorUtil.nomeQualidade(pe.getQualidade()),
                    pe.getCidade(),
                    corCidade,
                    FormatadorUtil.formatarPreco(pe.getSellMin()),
                    FormatadorUtil.formatarData((pe.getSellDate() != null && !pe.getSellDate().startsWith("0001"))
                            ? pe.getSellDate() : pe.getBuyDate())));
        }
        linhas.sort(Comparator.comparing(l -> l.cidade));

        tabelaPrecos.setItems(FXCollections.observableArrayList(linhas));
        double altPrecos = 28.0 + (linhas.size() * 40.0);
        tabelaPrecos.setPrefHeight(altPrecos);
        tabelaPrecos.setMaxHeight(altPrecos);
    }


    private void atualizarTabelaReceita(ReceitaCraft receita, List<PriceEntry> precosMateirais) {
        if (receita == null) {
            tabelaReceita.setPlaceholder(new Label("Receita não disponível para este item."));
            return;
        }

        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        if (precosMateirais != null) {
            for (PriceEntry pe : precosMateirais) {
                String chave = pe.getItemId();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null || (pe.getBuyMax() > 0 && (atual.getBuyMax() == 0 || pe.getBuyMax() > atual.getBuyMax()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }

        int eAtual = (enchant == -1) ? 0 : enchant;
        int tAtual = (tier == -1) ? 4 : tier;

        List<LinhaMaterial> linhas = new ArrayList<>();
        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();

            // identifica o tipo pelo sufixo, nao pelo isArtefato() da api
            String sufixoVerificacao = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            boolean ehRefinado = sufixoVerificacao.equals("CLOTH") || sufixoVerificacao.equals("LEATHER")
                    || sufixoVerificacao.equals("PLANKS") || sufixoVerificacao.equals("METALBAR")
                    || sufixoVerificacao.equals("STONEBLOCK");
            boolean ehBrutoRaw = sufixoVerificacao.equals("FIBER") || sufixoVerificacao.equals("ORE")
                    || sufixoVerificacao.equals("WOOD") || sufixoVerificacao.equals("HIDE")
                    || sufixoVerificacao.equals("ROCK");
            boolean ehRetorno = mat.isArtefato() && !ehRefinado && !ehBrutoRaw;

            String iconeId;
            if (eAtual > 0 && !ehRetorno) {
                iconeId = idMat + "_LEVEL" + eAtual;
            } else {
                iconeId = idMat;
            }

            String iconeUrl = "https://render.albiononline.com/v1/item/" + iconeId + ".png";

            String sufixoMat = sufixoVerificacao; // já calculado acima
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : tAtual;

            String nomeRecurso = com.albionmarket.service.BancoDeDadosItens.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : com.albionmarket.service.BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome).findFirst()
                    .orElse(idMat);

            String nomeExibir = (eAtual > 0 && !ehRetorno) ? nomeMat + " ." + eAtual : nomeMat;
            String tipo = ehRetorno ? "Retorno" : ehRefinado ? "Refinado" : "Bruto";

            String chaveCompra = (eAtual > 0)
                    ? idMat + "_LEVEL" + eAtual + "@" + eAtual
                    : idMat;
            PriceEntry pe = melhorCompra.get(chaveCompra);

            String buyMax = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String corCidade = pe != null
                    ? com.albionmarket.service.BancoDeDadosItens.CIDADES.stream().filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001"))
                            ? pe.getBuyDate() : pe.getSellDate()) : "-";

            linhas.add(new LinhaMaterial(iconeUrl, nomeExibir, tipo, mat.getCount(), cidade, corCidade, buyMax, data));
        }

        tabelaReceita.setItems(FXCollections.observableArrayList(linhas));
        double alturaCalculada = 28.0 + (linhas.size() * 40.0);
        tabelaReceita.setPrefHeight(alturaCalculada);
        tabelaReceita.setMaxHeight(alturaCalculada);
    }


    private void atualizarTabelaMateriais(ReceitaCraft receita, List<PriceEntry> precosMateirais) {
        if (tabelaMateriais == null || receita == null) return;

        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        if (precosMateirais != null) {
            for (PriceEntry pe : precosMateirais) {
                String chave = pe.getItemId();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null || (pe.getBuyMax() > 0 && (atual.getBuyMax() == 0 || pe.getBuyMax() > atual.getBuyMax()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }

        int eAtual = (enchant == -1) ? 0 : enchant;
        int tAtual = (tier == -1) ? 4 : tier;

        List<LinhaMaterialPreco> linhas = new ArrayList<>();
        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            String sufixoVerificacao = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            boolean ehRefinado = sufixoVerificacao.equals("CLOTH") || sufixoVerificacao.equals("LEATHER")
                    || sufixoVerificacao.equals("PLANKS") || sufixoVerificacao.equals("METALBAR")
                    || sufixoVerificacao.equals("STONEBLOCK");
            boolean ehBrutoRaw = sufixoVerificacao.equals("FIBER") || sufixoVerificacao.equals("ORE")
                    || sufixoVerificacao.equals("WOOD") || sufixoVerificacao.equals("HIDE")
                    || sufixoVerificacao.equals("ROCK");
            boolean ehRetorno = mat.isArtefato() && !ehRefinado && !ehBrutoRaw;

            String sufixoMat = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : tAtual;

            String nomeRecurso = com.albionmarket.service.BancoDeDadosItens.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : com.albionmarket.service.BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome).findFirst()
                    .orElse(idMat);
            String nomeExibir = (eAtual > 0 && !ehRetorno) ? nomeMat + " ." + eAtual : nomeMat;

            String tipo = ehRetorno ? "Retorno" : ehRefinado ? "Refinado" : "Bruto";

            String chaveCompra = (eAtual > 0)
                    ? idMat + "_LEVEL" + eAtual + "@" + eAtual
                    : idMat;
            PriceEntry pe = melhorCompra.get(chaveCompra);
            String buyMax = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String corCidade = pe != null
                    ? com.albionmarket.service.BancoDeDadosItens.CIDADES.stream().filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001"))
                            ? pe.getBuyDate() : pe.getSellDate()) : "-";

            linhas.add(new LinhaMaterialPreco(nomeExibir, tipo, mat.getCount(), cidade, corCidade, buyMax, data));
        }

        tabelaMateriais.setItems(FXCollections.observableArrayList(linhas));
        double alt = 28.0 + (linhas.size() * 40.0);
        tabelaMateriais.setPrefHeight(alt);
        tabelaMateriais.setMaxHeight(alt);
    }

    private void ativarEdicaoManual(boolean ativo) {
        for (TableColumn<LinhaPreco, ?> col : tabelaPrecos.getColumns()) {
            if (col.getText().equals("Preço de Venda")) {
                @SuppressWarnings("unchecked")
                TableColumn<LinhaPreco, String> colStr = (TableColumn<LinhaPreco, String>) col;
                if (ativo) {
                    colStr.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
                    colStr.setOnEditCommit(ev -> {
                        LinhaPreco antiga = ev.getRowValue();
                        int idx = tabelaPrecos.getItems().indexOf(antiga);
                        tabelaPrecos.getItems().set(idx, new LinhaPreco(
                                antiga.itemId, antiga.qualidade, antiga.cidade, antiga.corCidade,
                                ev.getNewValue(), antiga.atualizado));
                        atualizarTabelaCalculo();
                    });
                } else {
                    colStr.setCellFactory(tc -> new TableCell<>() {
                        @Override
                        protected void updateItem(String v, boolean empty) {
                            super.updateItem(v, empty);
                            if (empty || v == null || v.equals("-")) {
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
        }
        tabelaPrecos.setEditable(ativo);

        if (tabelaMateriais != null) {
            for (TableColumn<LinhaMaterialPreco, ?> col : tabelaMateriais.getColumns()) {
                if (col.getText().equals("Preco de Compra")) {
                    @SuppressWarnings("unchecked")
                    TableColumn<LinhaMaterialPreco, String> colStr = (TableColumn<LinhaMaterialPreco, String>) col;
                    if (ativo) {
                        colStr.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
                        colStr.setOnEditCommit(ev -> {
                            LinhaMaterialPreco antiga = ev.getRowValue();
                            int idx = tabelaMateriais.getItems().indexOf(antiga);
                            tabelaMateriais.getItems().set(idx, new LinhaMaterialPreco(
                                    antiga.nome, antiga.tipo, antiga.qtdNecessaria,
                                    antiga.cidade, antiga.corCidade, ev.getNewValue(), antiga.atualizado));
                            atualizarTabelaCalculo();
                        });
                    } else {
                        colStr.setCellFactory(tc -> new TableCell<>() {
                            @Override
                            protected void updateItem(String v, boolean empty) {
                                super.updateItem(v, empty);
                                if (empty || v == null || v.equals("-")) {
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
            }
            tabelaMateriais.setEditable(ativo);
        }

        tabelaReceita.setEditable(ativo);
    }


    @SuppressWarnings("unchecked")
    private void atualizarTabelaCalculo() {
        if (painelCalculo == null) return;

        double qtdProduzir = parseDoubleSafe(campoQuantidade, 1.0);
        double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
        double taxaBarraca = parseDoubleSafe(campoSinergiaBarraca, 3.0);

        double qtdFinal = qtdProduzir / (1.0 - taxaRetorno);
        double nutricao = (itemValue * qtdFinal) * 0.1125;
        double taxaCraftTotal = (taxaBarraca * nutricao) / 100.0;
        double taxaCompra = possuiPremium ? 0.03 : 0.05;
        double taxaVenda = possuiPremium ? 0.025 : 0.05;

        // custo dos brutos — retorno não entra
        double custoMateriais = 0;
        if (tabelaMateriais != null && !tabelaMateriais.getItems().isEmpty()) {
            for (LinhaMaterialPreco lm : tabelaMateriais.getItems())
                if (!"Retorno".equals(lm.tipo))
                    custoMateriais += FormatadorUtil.parseSilver(lm.buyMax) * lm.qtdNecessaria * qtdProduzir;
        } else if (tabelaReceita != null) {
            for (LinhaMaterial lm : tabelaReceita.getItems())
                if (!"Retorno".equals(lm.tipo))
                    custoMateriais += FormatadorUtil.parseSilver(lm.buyMax) * lm.qtd * qtdProduzir;
        }

        double custoMatComTaxa = custoMateriais + (custoMateriais * taxaCompra);
        double custoTotal = custoMatComTaxa + taxaCraftTotal;

        double melhorVenda = 0;
        String melhorCidadeTemp = "-";
        for (LinhaPreco lp : tabelaPrecos.getItems()) {
            double v = FormatadorUtil.parseSilver(lp.sellMin);
            if (v > melhorVenda) {
                melhorVenda = v;
                melhorCidadeTemp = lp.cidade;
            }
        }
        final String melhorCidade = melhorCidadeTemp;
        String nomeCidadeVenda = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                .filter(c -> c.getApiId().equals(melhorCidade))
                .map(CidadeInfo::getNome).findFirst().orElse(melhorCidade);

        double receitaTotal = qtdFinal * melhorVenda;
        double taxaMercadoValor = receitaTotal * taxaVenda;
        double lucro = receitaTotal - custoTotal - taxaMercadoValor;

        lucroAtual = lucro;
        custoAtual = custoTotal;
        receitaAtual2 = receitaTotal;

        java.util.function.Function<String, String> cidadeParaNome = apiId ->
                com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(apiId))
                        .map(CidadeInfo::getNome)
                        .findFirst().orElse(apiId != null ? apiId : "-");

        List<String[]> metricas = new ArrayList<>(Arrays.asList(
                new String[]{"Qtd a refinar", FormatadorUtil.fmt(qtdProduzir) + " un"},
                new String[]{"Qtd final refinada", String.format("%.2f un", qtdFinal)},
                //new String[]{"Taxa de retorno", String.format("%.1f%%", taxaRetorno * 100)},
                new String[]{"Melhor preço de venda", FormatadorUtil.fmtSilver(melhorVenda)},
                new String[]{"Local de venda", nomeCidadeVenda},
                new String[]{"Custo dos materiais", FormatadorUtil.fmtSilver(custoMatComTaxa)},
                new String[]{"Taxa da barraca", FormatadorUtil.fmtSilver(taxaCraftTotal)}
        ));

        if (tabelaMateriais != null) {
            for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
                if ("Bruto".equals(lm.tipo)) {
                    metricas.add(new String[]{"Qtd Bruto", String.valueOf(lm.qtdNecessaria * (int) qtdProduzir)});
                    metricas.add(new String[]{"Local Bruto", cidadeParaNome.apply(lm.cidade)});
                } else if ("Refinado".equals(lm.tipo)) {
                    metricas.add(new String[]{"Qtd Refinado tier abaixo", String.valueOf(lm.qtdNecessaria * (int) qtdProduzir)});
                    metricas.add(new String[]{"Local Refinado tier abaixo", cidadeParaNome.apply(lm.cidade)});
                }
            }
        }

        FlowPane fluxoNormal = new FlowPane(10, 10);
        fluxoNormal.setPrefWrapLength(Double.MAX_VALUE);
        for (String[] m : metricas)
            fluxoNormal.getChildren().add(criarCard(m[0], m[1], "#e0e0e0", "#2a2a2a"));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #444;");

        HBox linhaDestaque = new HBox(16);
        linhaDestaque.setAlignment(Pos.CENTER);

        VBox cardCusto = criarCardDestaque("Custo Total", FormatadorUtil.fmtSilver(custoTotal), "#e05555");
        VBox cardReceita = criarCardDestaque("Receita Total", FormatadorUtil.fmtSilver(receitaTotal), "#3dba6e");
        VBox cardLucro = criarCardDestaque(
                lucro >= 0 ? "Lucro" : "Prejuízo",
                (lucro >= 0 ? "+" : "") + FormatadorUtil.fmtSilver(lucro),
                lucro >= 0 ? "#5a8dee" : "#e05555");

        HBox.setHgrow(cardCusto, Priority.ALWAYS);
        HBox.setHgrow(cardReceita, Priority.ALWAYS);
        HBox.setHgrow(cardLucro, Priority.ALWAYS);
        linhaDestaque.getChildren().addAll(cardCusto, cardReceita, cardLucro);

        painelCalculo.getChildren().setAll(fluxoNormal, sep, linhaDestaque);
    }


    // utilitarios
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


    private TableColumn<LinhaPreco, String> criarColunaCidade() {
        TableColumn<LinhaPreco, String> col = new TableColumn<>("Cidade");
        col.setPrefWidth(130);
        col.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                LinhaPreco linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));
                String nome = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
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

    private TableColumn<LinhaPreco, String> criarColunaPreco(String titulo, double largura) {
        TableColumn<LinhaPreco, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().sellMin));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("-")) {
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

    private <T> TableColumn<T, String> coluna(String titulo, double largura,
                                              javafx.util.Callback<TableColumn.CellDataFeatures<T, String>,
                                                      javafx.beans.value.ObservableValue<String>> callback) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(callback);
        return col;
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

    private VBox criarCard(String titulo, String valor, String corValor, String corFundo) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-text-fill: " + corValor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        VBox card = new VBox(3, lblTitulo, lblValor);
        card.setPadding(new Insets(8, 14, 8, 14));
        card.setStyle("-fx-background-color: " + corFundo + "; "
                + "-fx-background-radius: 6; -fx-border-color: #333; "
                + "-fx-border-radius: 6; -fx-border-width: 1;");
        return card;
    }

    private VBox criarCardDestaque(String titulo, String valor, String corAcento) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-text-fill: " + corAcento + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        VBox card = new VBox(4, lblTitulo, lblValor);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #252525; "
                + "-fx-background-radius: 8; "
                + "-fx-border-color: " + corAcento + "; "
                + "-fx-border-radius: 8; "
                + "-fx-border-width: 1.5;");
        return card;
    }

    private Separator separador() {
        return new Separator();
    }


    private TextField campoCampo(String valor) {
        TextField tf = new TextField(valor);
        tf.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        tf.textProperty().addListener((obs, ant, novo) -> {
            if (tabelaPrecos != null) tabelaPrecos.refresh();
            if (tabelaMateriais != null) tabelaMateriais.refresh();
            atualizarTabelaCalculo();
        });
        return tf;
    }


    private void salvarOperacao() {
        try {
            int t = AlbionIdUtil.tierEfetivo(tier);
            int e = AlbionIdUtil.enchantEfetivo(enchant);

            // pega melhor cidade de venda da tabela
            // depois
            String melhorCidadeApiTemp = "-";
            double melhorV = 0;
            for (LinhaPreco lp : tabelaPrecos.getItems()) {
                double v = FormatadorUtil.parseSilver(lp.sellMin);
                if (v > melhorV) {
                    melhorV = v;
                    melhorCidadeApiTemp = lp.cidade;
                }
            }
            final String melhorCidadeApi = melhorCidadeApiTemp;
            String nomeCidadeVenda = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(melhorCidadeApi))
                    .map(CidadeInfo::getNome)
                    .findFirst().orElse(melhorCidadeApi);

            String locaisJson = cidadesPorMaterialJson();

            double qtdInicial = parseDoubleSafe(campoQuantidade, 1.0);
            double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
            double qtdFinal = qtdInicial / (1.0 - taxaRetorno);

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"item\": \"").append(item.getNome().replace("\"", "\\\"")).append("\",\n");
            sb.append("  \"itemId\": \"").append(itemIdApi).append("\",\n");
            sb.append("  \"tier\": ").append(t).append(",\n");
            sb.append("  \"encantamento\": ").append(e).append(",\n");
            sb.append("  \"parametros\": {\n");
            sb.append("    \"quantidade\": \"").append(campoQuantidade.getText()).append("\",\n");
            sb.append("    \"taxaRetorno\": \"").append(campoRetorno.getText()).append("\",\n");
            sb.append("    \"taxaBarraca\": \"").append(campoSinergiaBarraca.getText()).append("\"\n");
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

            String nomeArquivo = OperacaoService.salvar(itemIdApi, sb.toString());
            labelStatus.setText("Operação salva: " + nomeArquivo);

        } catch (Exception ex) {
            labelStatus.setText("Erro ao salvar: " + ex.getMessage());
        }
    }


    private String cidadesPorMaterialJson() {
        if (tabelaMateriais == null || tabelaMateriais.getItems().isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean primeiro = true;
        for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
            if (!primeiro) sb.append(", ");
            primeiro = false;
            String nomeCidade = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(lm.cidade))
                    .map(CidadeInfo::getNome)
                    .findFirst().orElse(lm.cidade != null ? lm.cidade : "-");
            int qtdReal = lm.qtdNecessaria * parseIntSafe(campoQuantidade, 1);
            sb.append("{\"material\": \"").append(lm.nome.replace("\"", "\\\""))
                    .append("\", \"quantidade\": ").append(qtdReal)
                    .append(", \"cidade\": \"").append(nomeCidade).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

}