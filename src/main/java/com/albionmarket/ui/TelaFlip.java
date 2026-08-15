package com.albionmarket.ui;

import com.albionmarket.model.CidadeInfo;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.service.BancoDeDadosItens;
import com.albionmarket.service.IconeCacheService;
import com.albionmarket.service.OperacaoService;
import com.albionmarket.util.FormatadorUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Tela de resultados de flip entre cidades.
 * Sem paginação — resultados aparecem progressivamente conforme chegam da API.
 * Requisições paralelas para máxima velocidade.
 */
public class TelaFlip {

    private static final String API_BASE    = "https://west.albion-online-data.com/api/v2/stats/prices";
    private static final String CIDADES_API = "Caerleon,Martlock,Bridgewatch,FortSterling,Lymhurst,Thetford";

    // IDs por requisição HTTP — API suporta bem até ~100
    private static final int IDS_POR_SUBLOTE = 100;
    // Requisições simultâneas
    private static final int PARALELO = 5;

    private final Stage  palco;
    private final String tipoCompra;
    private final String tipoVenda;
    private final long   lucroMin;
    private final long   lucroMax;

    // lista observável compartilhada entre a task e a tabela
    private final ObservableList<LinhaFlip> linhas = FXCollections.observableArrayList();

    private TableView<LinhaFlip> tabela;
    private Label                labelStatus;
    private Label                labelContagem;
    private ProgressIndicator    progresso;

    // mapa sufixo-base → nome amigável, montado uma vez no construtor
    // ex: "MAIN_SWORD" → "Espada Larga"
    private final Map<String, String> nomesPorId = new HashMap<>();

    private final ExecutorService pool = Executors.newFixedThreadPool(PARALELO + 1);

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(pool)
            .build();

    // -------------------------------------------------------------------------
    // Modelo de linha
    // -------------------------------------------------------------------------
    public static class LinhaFlip {
        public final String itemId;       // ex: "T5_MAIN_SWORD@2"
        public final String nomeItem;     // ex: "Espada Larga"
        public final int    qualidade;
        public final String cidadeCompra;
        public final long   precoCompra;
        public final String cidadeVenda;
        public final long   precoVenda;
        public final long   lucroBruto;
        public final double lucroPercentual;
        public final String dataCompra;
        public final String dataVenda;
        public int quantidade = 1;
        public javafx.scene.control.Label lblLucroRef = null;
        public javafx.scene.control.Label lblCustoRef = null;

        public LinhaFlip(String itemId, String nomeItem, int qualidade,
                         String cidadeCompra, long precoCompra,
                         String cidadeVenda,  long precoVenda,
                         long lucroBruto,     double lucroPercentual,
                         String dataCompra,   String dataVenda) {
            this.itemId          = itemId;
            this.nomeItem        = nomeItem;
            this.qualidade       = qualidade;
            this.cidadeCompra    = cidadeCompra;
            this.precoCompra     = precoCompra;
            this.cidadeVenda     = cidadeVenda;
            this.precoVenda      = precoVenda;
            this.lucroBruto      = lucroBruto;
            this.lucroPercentual = lucroPercentual;
            this.dataCompra      = dataCompra;
            this.dataVenda       = dataVenda;
        }
    }

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------
    public TelaFlip(Stage palco, String tipoCompra, String tipoVenda,
                    long lucroMin, long lucroMax) {
        this.palco      = palco;
        this.tipoCompra = tipoCompra;
        this.tipoVenda  = tipoVenda;
        this.lucroMin   = lucroMin;
        this.lucroMax   = lucroMax;

        // monta o mapa de nomes uma unica vez
        for (ItemDefinition item : BancoDeDadosItens.getTodosItens()) {
            if (item.getId() != null && !item.getId().isBlank()) {
                nomesPorId.put(item.getId(), item.getNome());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Resolve nome amigavel a partir do apiId completo (ex: "T5_MAIN_SWORD@2")
    // -------------------------------------------------------------------------
    private String resolverNome(String apiId) {
        // T5_MAIN_SWORD@2  →  MAIN_SWORD
        String base = apiId.replaceAll("^T\\d_", "").replaceAll("@\\d$", "");
        String nome = nomesPorId.get(base);
        return (nome != null) ? nome : base;
    }

    // -------------------------------------------------------------------------
    // Ponto de entrada
    // -------------------------------------------------------------------------
    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1a1a1a;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarTabela());

        palco.setTitle("Flip de Mercado — Albion Online");
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        iniciarBusca();
    }

    // -------------------------------------------------------------------------
    // Cabecalho
    // -------------------------------------------------------------------------
    private HBox criarCabecalho() {
        Label icone = new Label("⇄");
        icone.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 22px;");

        Label titulo = new Label("Flip de Mercado");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #e8e8e8;");

        String lblComp = tipoCompra.equals("sell") ? "Compra Direta" : "Pedido de Compra";
        String lblVend = tipoVenda.equals("buy")   ? "Venda Direta"  : "Pedido de Venda";
        Label badgeComp  = criarBadge(lblComp, "#1e3a5f", "#5a8dee");
        Label badgeVend  = criarBadge(lblVend, "#1a3a26", "#3dba6e");
        Label badgeLucro = criarBadge(
                "Lucro min: " + formatarPreco(lucroMin) +
                        (lucroMax > 0 ? "  max: " + formatarPreco(lucroMax) : ""),
                "#3a2a10", "#e0b84a");

        HBox badges = new HBox(8, badgeComp, badgeVend, badgeLucro);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox textos = new VBox(4, titulo, badges);
        textos.setAlignment(Pos.CENTER_LEFT);

        HBox esquerda = new HBox(10, icone, textos);
        esquerda.setAlignment(Pos.CENTER_LEFT);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        progresso = new ProgressIndicator();
        progresso.setMaxSize(18, 18);
        progresso.setStyle("-fx-accent: #5a8dee;");
        progresso.setVisible(false);

        labelContagem = new Label("");
        labelContagem.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        labelStatus = new Label("Preparando busca...");
        labelStatus.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        VBox statusBox = new VBox(2, labelContagem, labelStatus);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnVoltar = new Button("Voltar");
        String estiloVoltar = "-fx-background-color: transparent; -fx-font-size: 13px; " +
                "-fx-cursor: hand; -fx-text-fill: #5a8dee; -fx-border-width: 0; -fx-padding: 4 6;";
        btnVoltar.setStyle(estiloVoltar);
        btnVoltar.setOnMouseEntered(e -> btnVoltar.setStyle(estiloVoltar + " -fx-opacity: 0.7;"));
        btnVoltar.setOnMouseExited (e -> btnVoltar.setStyle(estiloVoltar));
        btnVoltar.setOnAction(e -> {
            pool.shutdownNow();
            new TelaFlipSelecao(palco).mostrar();
        });

        HBox cab = new HBox(esquerda, espacador, progresso, statusBox, new Label("  "), btnVoltar);
        cab.setAlignment(Pos.CENTER);
        cab.setSpacing(12);
        cab.setPadding(new Insets(16, 24, 16, 24));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");
        return cab;
    }

    private Label criarBadge(String texto, String bg, String fg) {
        Label b = new Label(texto);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; " +
                "-fx-background-radius: 4;");
        return b;
    }

    // -------------------------------------------------------------------------
    // Tabela sem paginacao — scroll livre
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private VBox criarTabela() {
        tabela = new TableView<>();
        // SortedList vinculada ao comparator da tabela: mantem a ordenacao
        // escolhida pelo usuario mesmo com linhas chegando progressivamente da API.
        SortedList<LinhaFlip> ordenada = new SortedList<>(linhas);
        ordenada.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenada);
        tabela.setStyle("-fx-background-color: #1a1a1a; -fx-table-cell-border-color: #2a2a2a;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setEditable(true);
        tabela.setRowFactory(tv -> {
            TableRow<LinhaFlip> row = new TableRow<>();
            row.setPrefHeight(80);
            return row;
        });

        Label placeholder = new Label("Aguardando resultados...");
        placeholder.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        tabela.setPlaceholder(placeholder);

        // -- coluna Item -------------------------------------------------------
        TableColumn<LinhaFlip, LinhaFlip> colItem = new TableColumn<>("Item");
        colItem.setPrefWidth(260);
        colItem.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colItem.setCellFactory(tc -> new TableCell<>() {
            private final ImageView iv        = new ImageView();
            private final Label     lblNome   = new Label();
            private final Label     lblBadges = new Label();
            private final HBox      raiz;

            {
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);

                lblNome.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 13px;");
                lblNome.setMaxWidth(190);
                lblNome.setWrapText(true);

                lblBadges.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

                VBox textos = new VBox(3, lblNome, lblBadges);
                textos.setAlignment(Pos.CENTER_LEFT);

                raiz = new HBox(10, iv, textos);
                raiz.setAlignment(Pos.CENTER_LEFT);
                raiz.setPadding(new Insets(4, 0, 4, 0));
            }

            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setGraphic(null); return; }

                String id      = l.itemId;
                char   tierCh  = (id.length() > 1 && id.startsWith("T")) ? id.charAt(1) : '?';
                String enchant = id.contains("@") ? id.substring(id.indexOf('@') + 1) : "0";

                lblNome.setText(l.nomeItem);
                lblBadges.setText("T" + tierCh + "  ."+enchant + "  " + nomeQualidade(l.qualidade));

                try {
                    iv.setImage(IconeCacheService.obterIcone(
                            "https://render.albiononline.com/v1/item/" + id + ".png?size=48",
                            48, 48, true, true, true));
                } catch (Exception ignored) {}

                setGraphic(raiz);
            }
        });

        // -- coluna Compra -----------------------------------------------------
        TableColumn<LinhaFlip, LinhaFlip> colCompra = new TableColumn<>("Compra");
        colCompra.setPrefWidth(175);
        colCompra.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colCompra.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setGraphic(null); return; }

                Label lblCidade = new Label(nomeCidade(l.cidadeCompra));
                lblCidade.setStyle("-fx-text-fill: " + corCidade(l.cidadeCompra, "#e05555") +
                        "; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label lblPreco = new Label(formatarPreco(l.precoCompra));
                lblPreco.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

                Label lblTempo = new Label(formatarData(l.dataCompra));
                lblTempo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

                Label lblModo = new Label(tipoCompra.equals("sell") ? "Compra direta" : "Pedido de compra");
                lblModo.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                VBox vb = new VBox(2, lblCidade, lblPreco, lblTempo, lblModo);
                vb.setPadding(new Insets(4, 0, 4, 0));
                setGraphic(vb);
            }
        });

        // -- coluna Venda ------------------------------------------------------
        TableColumn<LinhaFlip, LinhaFlip> colVenda = new TableColumn<>("Venda");
        colVenda.setPrefWidth(175);
        colVenda.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colVenda.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setGraphic(null); return; }

                Label lblCidade = new Label(nomeCidade(l.cidadeVenda));
                lblCidade.setStyle("-fx-text-fill: " + corCidade(l.cidadeVenda, "#3dba6e") +
                        "; -fx-font-weight: bold; -fx-font-size: 13px;");

                Label lblPreco = new Label(formatarPreco(l.precoVenda));
                lblPreco.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px;");

                Label lblTempo = new Label(formatarData(l.dataVenda));
                lblTempo.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                Label lblModo = new Label(tipoVenda.equals("buy") ? "Venda direta" : "Pedido de venda");
                lblModo.setStyle("-fx-text-fill: #444; -fx-font-size: 10px;");

                VBox vb = new VBox(2, lblCidade, lblPreco, lblTempo, lblModo);
                vb.setPadding(new Insets(4, 0, 4, 0));
                setGraphic(vb);
            }
        });

        // -- coluna Custo unitario + taxa --------------------------------------
        TableColumn<LinhaFlip, LinhaFlip> colCusto = new TableColumn<>("Custo + Taxa");
        colCusto.setPrefWidth(140);
        colCusto.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colCusto.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setGraphic(null); return; }

                long taxa         = Math.round(l.precoVenda * 0.05);
                long custoComTaxa = l.precoCompra + taxa;

                Label lblCusto = new Label(formatarPreco(custoComTaxa));
                lblCusto.setStyle("-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label lblTaxa = new Label("Taxa: " + formatarPreco(taxa));
                lblTaxa.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

                VBox vb = new VBox(2, lblCusto, lblTaxa);
                vb.setPadding(new Insets(4, 0, 4, 0));
                setGraphic(vb);
            }
        });

        // -- coluna Quantidade -------------------------------------------------
        TableColumn<LinhaFlip, LinhaFlip> colQtd = new TableColumn<>("Qtd");
        colQtd.setPrefWidth(100);
        colQtd.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colQtd.setCellFactory(tc -> new TableCell<>() {

            private final Button    btnMenos = new Button("-");
            private final Button    btnMais  = new Button("+");
            private final TextField campoQtd = new TextField("1");
            private final HBox      raiz;
            private Runnable        atualizarLucro = null;

            {
                String estiloBotao =
                        "-fx-background-color: #2a2a2a; -fx-text-fill: #e0e0e0; " +
                                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                                "-fx-background-radius: 4; -fx-border-color: #444; " +
                                "-fx-border-radius: 4; -fx-border-width: 1; " +
                                "-fx-min-width: 28px; -fx-max-width: 28px; " +
                                "-fx-min-height: 28px; -fx-max-height: 28px; -fx-padding: 0;";
                btnMenos.setStyle(estiloBotao);
                btnMais.setStyle(estiloBotao);

                campoQtd.setStyle(
                        "-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0; " +
                                "-fx-font-size: 13px; -fx-alignment: center; " +
                                "-fx-border-color: #444; -fx-border-radius: 4; " +
                                "-fx-background-radius: 4; -fx-pref-width: 36px; -fx-max-width: 36px;");
                campoQtd.setAlignment(Pos.CENTER);

                btnMenos.setOnAction(e -> {
                    LinhaFlip l = getItem();
                    if (l == null || l.quantidade <= 1) return;
                    l.quantidade--;
                    campoQtd.setText(String.valueOf(l.quantidade));
                    if (atualizarLucro != null) atualizarLucro.run();
                });

                btnMais.setOnAction(e -> {
                    LinhaFlip l = getItem();
                    if (l == null) return;
                    l.quantidade++;
                    campoQtd.setText(String.valueOf(l.quantidade));
                    if (atualizarLucro != null) atualizarLucro.run();
                });

                campoQtd.textProperty().addListener((obs, ant, novo) -> {
                    LinhaFlip l = getItem();
                    if (l == null) return;
                    try {
                        int v = Integer.parseInt(novo.trim());
                        if (v > 0) {
                            l.quantidade = v;
                            if (atualizarLucro != null) atualizarLucro.run();
                        }
                    } catch (NumberFormatException ignored) {}
                });

                raiz = new HBox(4, btnMenos, campoQtd, btnMais);
                raiz.setAlignment(Pos.CENTER);
                raiz.setPadding(new Insets(4, 0, 4, 0));
            }

            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setGraphic(null); atualizarLucro = null; return; }

                atualizarLucro = () -> {
                    if (l.lblLucroRef != null) {
                        long lucroTotal  = l.lucroBruto * l.quantidade;
                        boolean positivo = lucroTotal >= 0;
                        l.lblLucroRef.setText((positivo ? "+" : "") + formatarPreco(lucroTotal));
                        l.lblLucroRef.setStyle("-fx-text-fill: " + (positivo ? "#3dba6e" : "#e05555")
                                + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }
                    if (l.lblCustoRef != null) {
                        l.lblCustoRef.setText("Custo: " + formatarPreco(l.precoCompra * l.quantidade));
                    }
                };

                campoQtd.setText(String.valueOf(l.quantidade));
                setGraphic(raiz);
            }
        });

        // -- coluna Lucro ------------------------------------------------------
        TableColumn<LinhaFlip, LinhaFlip> colLucro = new TableColumn<>("Lucro");
        colLucro.setPrefWidth(175);
        colLucro.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colLucro.setCellFactory(tc -> new TableCell<>() {
            private final Label  lblLucro  = new Label();
            private final Label  lblPct    = new Label();
            private final Label  lblCusto  = new Label(); // custo total = compra * qtd
            private final Button btnSalvar = new Button("Salvar");
            private final VBox   vb        = new VBox(2, lblLucro, lblPct, lblCusto, btnSalvar);

            private static final String ESTILO_SALVAR =
                    "-fx-background-color: #1a3a26; -fx-text-fill: #3dba6e; " +
                            "-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 10px; " +
                            "-fx-border-color: #2a5a3a; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 3 8;";
            private static final String ESTILO_SALVO =
                    "-fx-background-color: #3dba6e; -fx-text-fill: #0f1f16; " +
                            "-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 10px; " +
                            "-fx-border-color: #2a5a3a; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 3 8;";

            {
                btnSalvar.setStyle(ESTILO_SALVAR);
                lblCusto.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
                vb.setPadding(new Insets(4, 0, 4, 0));
            }

            private void atualizar(LinhaFlip l) {
                long lucroTotal  = l.lucroBruto * l.quantidade;
                boolean positivo = lucroTotal >= 0;

                lblLucro.setText((positivo ? "+" : "") + formatarPreco(lucroTotal));
                lblLucro.setStyle("-fx-text-fill: " + (positivo ? "#3dba6e" : "#e05555")
                        + "; -fx-font-weight: bold; -fx-font-size: 14px;");

                lblPct.setText(String.format("%.2f%% Profit", l.lucroPercentual));
                lblPct.setStyle("-fx-text-fill: " + (positivo ? "#2a9a5e" : "#c04545")
                        + "; -fx-font-size: 12px;");

                lblCusto.setText("Custo: " + formatarPreco(l.precoCompra * l.quantidade));
            }

            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) { setGraphic(null); return; }
                atualizar(l);
                l.lblLucroRef = lblLucro;
                l.lblCustoRef = lblCusto;
                btnSalvar.setDisable(false);
                btnSalvar.setText("Salvar");
                btnSalvar.setStyle(ESTILO_SALVAR);
                btnSalvar.setOnAction(e -> {
                    boolean ok = salvar(l);
                    btnSalvar.setText(ok ? "✓ Salvo" : "Erro");
                    btnSalvar.setStyle(ok ? ESTILO_SALVO : ESTILO_SALVAR);
                    btnSalvar.setDisable(true);
                    PauseTransition pausa = new PauseTransition(javafx.util.Duration.seconds(1.5));
                    pausa.setOnFinished(ev -> {
                        btnSalvar.setText("Salvar");
                        btnSalvar.setStyle(ESTILO_SALVAR);
                        btnSalvar.setDisable(false);
                    });
                    pausa.play();
                });
                setGraphic(vb);
            }
        });

        colItem.setComparator(Comparator.comparing((LinhaFlip l) -> l.nomeItem, String.CASE_INSENSITIVE_ORDER));
        colCompra.setComparator(Comparator.comparingLong((LinhaFlip l) -> l.precoCompra));
        colVenda.setComparator(Comparator.comparingLong((LinhaFlip l) -> l.precoVenda));
        colCusto.setComparator(Comparator.comparingLong((LinhaFlip l) -> l.precoCompra + Math.round(l.precoVenda * 0.05)));
        colQtd.setComparator(Comparator.comparingInt((LinhaFlip l) -> l.quantidade));
        colLucro.setComparator(Comparator.comparingDouble((LinhaFlip l) -> l.lucroPercentual));

        tabela.getColumns().addAll(colItem, colCompra, colVenda, colCusto, colQtd, colLucro);

        // ordenacao padrao: melhor oportunidade (maior lucro %) primeiro
        colLucro.setSortType(TableColumn.SortType.DESCENDING);
        tabela.getSortOrder().setAll(colLucro);

        VBox area = new VBox(tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        area.setStyle("-fx-background-color: #1a1a1a;");
        return area;
    }

    // -------------------------------------------------------------------------
    // Busca progressiva — cada sublote adiciona linhas na tabela imediatamente
    // -------------------------------------------------------------------------
    private void iniciarBusca() {
        linhas.clear();

        // monta lista completa de IDs, pulando entradas com ID vazio
        List<String> todosIds = new ArrayList<>();
        for (ItemDefinition item : BancoDeDadosItens.getTodosItens()) {
            if (item.getId() == null || item.getId().isBlank()) continue;
            for (int tier = 4; tier <= 8; tier++) {
                for (int enc = 0; enc <= 4; enc++) {
                    todosIds.add(item.buildApiId(tier, enc));
                }
            }
        }

        // divide em sublotes
        List<List<String>> sublotes = new ArrayList<>();
        for (int i = 0; i < todosIds.size(); i += IDS_POR_SUBLOTE) {
            sublotes.add(new ArrayList<>(todosIds.subList(i, Math.min(i + IDS_POR_SUBLOTE, todosIds.size()))));
        }

        int total = sublotes.size();
        atualizarStatus("Buscando " + todosIds.size() + " IDs em " + total + " lotes paralelos...", true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Semaphore semaforo   = new Semaphore(PARALELO);
                int[]     concluidos = {0};

                List<CompletableFuture<Void>> futuros = sublotes.stream()
                        .map(sublote -> CompletableFuture.runAsync(() -> {
                            try {
                                semaforo.acquire();

                                List<JsonObject> registros = buscarSublote(sublote);
                                List<LinhaFlip>  novas     = calcularOportunidades(registros);

                                if (!novas.isEmpty()) {
                                    Platform.runLater(() -> {
                                        linhas.addAll(novas);
                                        labelContagem.setText(linhas.size() + " oportunidades encontradas");
                                    });
                                }

                                synchronized (concluidos) {
                                    concluidos[0]++;
                                    int c = concluidos[0];
                                    Platform.runLater(() ->
                                            labelStatus.setText("Lote " + c + "/" + total + " concluido")
                                    );
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                semaforo.release();
                            }
                        }, pool))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futuros.toArray(new CompletableFuture[0])).get();
                return null;
            }
        };

        tarefa.setOnSucceeded(e -> atualizarStatus(
                "Busca concluida — " + linhas.size() + " oportunidades.", false));
        tarefa.setOnFailed(e -> atualizarStatus(
                "Erro: " + tarefa.getException().getMessage(), false));

        new Thread(tarefa, "flip-busca").start();
    }

    // -------------------------------------------------------------------------
    // Requisicao HTTP de um sublote com retentativa em 429
    // -------------------------------------------------------------------------
    private List<JsonObject> buscarSublote(List<String> sublote) {
        String url = API_BASE + "/" + String.join(",", sublote)
                + ".json?locations=" + CIDADES_API
                + "&qualities=1,2,3,4,5";
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET().build();

            HttpResponse<String> resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 429) {
                Thread.sleep(1500);
                resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());
            }

            if (resp.statusCode() == 200) {
                JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
                List<JsonObject> lista = new ArrayList<>(arr.size());
                for (JsonElement el : arr) lista.add(el.getAsJsonObject());
                return lista;
            }
        } catch (Exception ignored) {}
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // Calculo de oportunidades para os registros de um sublote
    // -------------------------------------------------------------------------
    private List<LinhaFlip> calcularOportunidades(List<JsonObject> registros) {
        Map<String, List<JsonObject>> agrupado = new LinkedHashMap<>();
        for (JsonObject o : registros) {
            if (o.get("sell_price_min").getAsLong() == 0 &&
                    o.get("buy_price_max").getAsLong()  == 0) continue;
            String chave = o.get("item_id").getAsString() + "|" + o.get("quality").getAsInt();
            agrupado.computeIfAbsent(chave, k -> new ArrayList<>()).add(o);
        }

        String campoCompra     = tipoCompra.equals("sell") ? "sell_price_min"     : "buy_price_max";
        String campoDataCompra = tipoCompra.equals("sell") ? "sell_price_min_date" : "buy_price_max_date";
        String campoVenda      = tipoVenda.equals("buy")   ? "buy_price_max"       : "sell_price_min";
        String campoDataVenda  = tipoVenda.equals("buy")   ? "buy_price_max_date"  : "sell_price_min_date";

        List<LinhaFlip> resultado = new ArrayList<>();

        for (List<JsonObject> entradas : agrupado.values()) {
            if (entradas.size() < 2) continue;

            String itemId = entradas.get(0).get("item_id").getAsString();
            int    qual   = entradas.get(0).get("quality").getAsInt();

            // testa TODO par de cidades distintas (no maximo 6 cidades -> 30 pares, e barato)
            // pra achar o par de compra/venda com o maior lucro de verdade.
            // Antes pegava so a cidade com o MENOR preco de compra global e so depois
            // procurava a melhor venda entre as cidades restantes — isso perde a melhor
            // oportunidade sempre que a cidade mais barata pra comprar tambem e a que tem
            // a melhor venda (o codigo excluia ela da comparacao de venda por ser a mesma
            // cidade da compra escolhida, mesmo sem checar se outro par de cidades dava
            // mais lucro no total).
            JsonObject melhorCompra = null, melhorVenda = null;
            long precoCompraFinal = 0, precoVendaFinal = 0;
            long melhorLucro = Long.MIN_VALUE;

            for (JsonObject compra : entradas) {
                long pCompra = compra.get(campoCompra).getAsLong();
                if (pCompra <= 0) continue;
                String cidadeCompra = compra.get("city").getAsString();

                for (JsonObject venda : entradas) {
                    if (venda.get("city").getAsString().equals(cidadeCompra)) continue;
                    long pVenda = venda.get(campoVenda).getAsLong();
                    if (pVenda <= 0) continue;

                    long lucro = pVenda - pCompra;
                    if (lucro > melhorLucro) {
                        melhorLucro = lucro;
                        melhorCompra = compra;
                        melhorVenda = venda;
                        precoCompraFinal = pCompra;
                        precoVendaFinal = pVenda;
                    }
                }
            }
            if (melhorCompra == null || melhorVenda == null) continue;

            long lucroBruto = precoVendaFinal - precoCompraFinal;
            if (lucroBruto < lucroMin) continue;
            if (lucroMax > 0 && lucroBruto > lucroMax) continue;

            double lucroPerc = precoCompraFinal > 0
                    ? Math.round((lucroBruto * 10000.0) / precoCompraFinal) / 100.0 : 0;

            resultado.add(new LinhaFlip(
                    itemId,
                    resolverNome(itemId),
                    qual,
                    melhorCompra.get("city").getAsString(), precoCompraFinal,
                    melhorVenda.get("city").getAsString(),  precoVendaFinal,
                    lucroBruto, lucroPerc,
                    obterCampo(melhorCompra, campoDataCompra),
                    obterCampo(melhorVenda,  campoDataVenda)
            ));
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // Salvar operacao em JSON
    // -------------------------------------------------------------------------
    // salva no mesmo formato/local que TelaCraft, TelaRefino e TelaCraftRefino usam
    // (arquivo "operacao_*.json" via OperacaoService) pra aparecer em TelaOperacoesAtivas
    private boolean salvar(LinhaFlip l) {
        try {
            String id = l.itemId;
            String tier = (id.length() > 1 && id.startsWith("T")) ? String.valueOf(id.charAt(1)) : "4";
            String enchant = id.contains("@") ? id.substring(id.indexOf('@') + 1) : "0";

            double custoTotal = (double) l.precoCompra * l.quantidade;
            double lucroTotal = (double) l.lucroBruto * l.quantidade;

            String locaisJson = "[{\"material\": \"" + l.nomeItem.replace("\"", "\\\"")
                    + "\", \"quantidade\": " + l.quantidade
                    + ", \"cidade\": \"" + nomeCidade(l.cidadeCompra) + "\"}]";

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"item\": \"").append(l.nomeItem.replace("\"", "\\\"")).append("\",\n");
            sb.append("  \"itemId\": \"").append(l.itemId).append("\",\n");
            sb.append("  \"tier\": ").append(tier).append(",\n");
            sb.append("  \"encantamento\": ").append(enchant).append(",\n");
            sb.append("  \"calculadora\": {\n");
            sb.append("    \"Quantidade a craftar\": \"").append(l.quantidade).append(" un\",\n");
            sb.append("    \"Qtd final craftada\": \"").append(String.format("%.2f un", (double) l.quantidade)).append("\",\n");
            sb.append("    \"Melhor preco de venda\": \"").append(FormatadorUtil.fmtSilver(l.precoVenda)).append("\",\n");
            sb.append("    \"Local de venda\": \"").append(nomeCidade(l.cidadeVenda)).append("\",\n");
            sb.append("    \"Custo dos materiais\": \"").append(FormatadorUtil.fmtSilver(custoTotal)).append("\",\n");
            sb.append("    \"Local de compra dos materiais\": ").append(locaisJson).append(",\n");
            sb.append("    \"Custo total\": \"").append(FormatadorUtil.fmtSilver(custoTotal)).append("\",\n");
            sb.append("    \"Lucro/Prejuizo\": \"").append(lucroTotal >= 0 ? "+" : "")
                    .append(FormatadorUtil.fmtSilver(lucroTotal)).append("\"\n");
            sb.append("  }\n");
            sb.append("}\n");

            String nomeArquivo = OperacaoService.salvar(l.itemId, sb.toString());
            labelStatus.setText("Salvo: " + nomeArquivo);
            return true;
        } catch (Exception ex) {
            labelStatus.setText("Erro ao salvar: " + ex.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Utilitarios
    // -------------------------------------------------------------------------
    private String obterCampo(JsonObject o, String campo) {
        return (o.has(campo) && !o.get(campo).isJsonNull()) ? o.get(campo).getAsString() : null;
    }

    private void atualizarStatus(String msg, boolean carregando) {
        Platform.runLater(() -> {
            labelStatus.setText(msg);
            progresso.setVisible(carregando);
        });
    }

    private String corCidade(String apiIdOuNome, String padrao) {
        return BancoDeDadosItens.CIDADES.stream()
                .filter(c -> c.getApiId().equals(apiIdOuNome) || c.getNome().equals(apiIdOuNome))
                .map(CidadeInfo::getCor).findFirst().orElse(padrao);
    }

    private String nomeCidade(String apiIdOuNome) {
        return BancoDeDadosItens.CIDADES.stream()
                .filter(c -> c.getApiId().equals(apiIdOuNome) || c.getNome().equals(apiIdOuNome))
                .map(CidadeInfo::getNome)
                .findFirst().orElse(apiIdOuNome != null ? apiIdOuNome : "-");
    }

    private String formatarPreco(long valor) {
        if (valor <= 0) return "—";
        return java.text.NumberFormat.getNumberInstance(new java.util.Locale("pt", "BR")).format(valor);
    }

    private String formatarData(String isoStr) {
        if (isoStr == null || isoStr.isBlank() || isoStr.startsWith("0001")) return "—";
        try {
            java.time.Instant data;
            try { data = java.time.Instant.parse(isoStr); }
            catch (Exception ex) {
                data = java.time.LocalDateTime.parse(isoStr).toInstant(java.time.ZoneOffset.UTC);
            }
            long min = java.time.temporal.ChronoUnit.MINUTES.between(data, java.time.Instant.now());
            if (min < 2)    return "agora";
            if (min < 60)   return min + "min";
            if (min < 1440) return (min / 60) + "h";
            return (min / 1440) + "d";
        } catch (Exception e) { return "—"; }
    }

    private String nomeQualidade(int q) {
        return switch (q) {
            case 1 -> "Normal";
            case 2 -> "Boa";
            case 3 -> "Notavel";
            case 4 -> "Excelente";
            case 5 -> "Obra-prima";
            default -> "?";
        };
    }
}