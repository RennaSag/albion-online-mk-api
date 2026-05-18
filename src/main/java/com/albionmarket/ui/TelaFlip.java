package com.albionmarket.ui;

import com.albionmarket.model.CidadeInfo;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.service.BancoDeDadosItens;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tela de resultados de flip entre cidades.
 * Busca todas as qualidades automaticamente
 * Sem paginação artificial
 */
public class TelaFlip {

    private static final String API_BASE = "https://west.albion-online-data.com/api/v2/stats/prices";
    private static final String CIDADES_API = "Caerleon,Martlock,Bridgewatch,FortSterling,Lymhurst,Thetford";
    private static final int TAM_LOTE = 20;
    private static final int PAUSA_MS = 350;

    private final Stage palco;
    private final String tipoCompra;  // "sell" ou "buy"
    private final String tipoVenda;   // "buy"  ou "sell"
    private final long lucroMin;
    private final long lucroMax;    // 0 = sem limite

    private List<LinhaFlip> todasLinhas = new ArrayList<>();
    private TableView<LinhaFlip> tabela;
    private Label labelStatus;
    private Label labelContagem;
    private ProgressIndicator progresso;

    private List<String> todosIds = new ArrayList<>();
    private int loteAtual = 0;


    private static final int IDS_POR_PAGINA = 200;
    private int paginaAtual = 0;
    private int totalPaginas = 0;

    private Label labelPagina;
    private Button btnAnterior;
    private Button btnProxima;

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    public static class LinhaFlip {
        public final String itemId;
        public final int qualidade;
        public final String cidadeCompra;
        public final long precoCompra;
        public final String cidadeVenda;
        public final long precoVenda;
        public final long lucroBruto;
        public final double lucroPercentual;
        public final String dataCompra;
        public final String dataVenda;
        public int quantidade = 1;
        public javafx.scene.control.Label lblLucroRef = null;

        public LinhaFlip(String itemId, int qualidade,
                         String cidadeCompra, long precoCompra,
                         String cidadeVenda, long precoVenda,
                         long lucroBruto, double lucroPercentual,
                         String dataCompra, String dataVenda) {
            this.itemId = itemId;
            this.qualidade = qualidade;
            this.cidadeCompra = cidadeCompra;
            this.precoCompra = precoCompra;
            this.cidadeVenda = cidadeVenda;
            this.precoVenda = precoVenda;
            this.lucroBruto = lucroBruto;
            this.lucroPercentual = lucroPercentual;
            this.dataCompra = dataCompra;
            this.dataVenda = dataVenda;
        }
    }


    public TelaFlip(Stage palco, String tipoCompra, String tipoVenda, long lucroMin, long lucroMax) {
        this.palco = palco;
        this.tipoCompra = tipoCompra;
        this.tipoVenda = tipoVenda;
        this.lucroMin = lucroMin;
        this.lucroMax = lucroMax;
    }


    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1a1a1a;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarAreaCentral());

        palco.setTitle("Flip de Mercado");
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        iniciarBusca();
    }


    private HBox criarCabecalho() {
        Label icone = new Label("⇄");
        icone.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 22px;");

        Label titulo = new Label("Flip de Mercado");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #e8e8e8;");

        // badges dos modos
        String lblComp = tipoCompra.equals("sell") ? "Compra Direta" : "Pedido de Compra";
        String lblVend = tipoVenda.equals("buy") ? "Venda Direta" : "Pedido de Venda";
        Label badgeComp = criarBadge(lblComp, "#1e3a5f", "#5a8dee");
        Label badgeVend = criarBadge(lblVend, "#1a3a26", "#3dba6e");
        Label badgeLucro = criarBadge(
                "Lucro: " + formatarPreco(lucroMin) + (lucroMax > 0 ? " - " + formatarPreco(lucroMax) : " Máximo"),
                "#3a2a10", "#e0b84a"
        );

        HBox badges = new HBox(8, badgeComp, badgeVend, badgeLucro);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox textos = new VBox(4, titulo, badges);
        textos.setAlignment(Pos.CENTER_LEFT);

        HBox esquerda = new HBox(10, icone, textos);
        esquerda.setAlignment(Pos.CENTER_LEFT);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        // status de carregamento
        progresso = new ProgressIndicator();
        progresso.setMaxSize(18, 18);
        progresso.setStyle("-fx-accent: #5a8dee;");
        progresso.setVisible(false);

        labelContagem = new Label("");
        labelContagem.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        labelStatus = new Label("Preparando busca...");
        labelStatus.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        VBox statusBox = new VBox(2, labelContagem, labelStatus);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        Label btnVoltar = new Label("Voltar");
        btnVoltar.setStyle("-fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #5a8dee;");
        btnVoltar.setOnMouseEntered(e -> btnVoltar.setStyle("-fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #5a8dee; -fx-opacity: 0.7;"));
        btnVoltar.setOnMouseExited(e -> btnVoltar.setStyle("-fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #5a8dee; -fx-opacity: 1;"));
        btnVoltar.setOnMouseClicked(e -> new TelaFlipSelecao(palco).mostrar());

        HBox cab = new HBox(esquerda, espacador, progresso, statusBox, new Label("  "), btnVoltar);
        cab.setAlignment(Pos.CENTER);
        cab.setSpacing(12);
        cab.setPadding(new Insets(16, 24, 16, 24));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");
        return cab;
    }

    private Label criarBadge(String texto, String bgColor, String txtColor) {
        Label badge = new Label(texto);
        badge.setStyle(
                "-fx-background-color: " + bgColor + "; -fx-text-fill: " + txtColor + "; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; " +
                        "-fx-background-radius: 4;");
        return badge;
    }


    @SuppressWarnings("unchecked")
    private VBox criarAreaCentral() {
        tabela = new TableView<>();
        tabela.setStyle("-fx-background-color: #1a1a1a; -fx-table-cell-border-color: #2a2a2a;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setEditable(true);

        tabela.setRowFactory(tv -> {
            TableRow<LinhaFlip> row = new TableRow<>();
            row.setPrefHeight(70);
            return row;
        });


        Label placeholder = new Label("Carregando oportunidades...");
        placeholder.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        tabela.setPlaceholder(placeholder);

        // coluna item: icone + nome + badges de tier/enchant/qualidade
        // coluna item: icone + nome + badges de tier/enchant/qualidade
        TableColumn<LinhaFlip, LinhaFlip> colItem = new TableColumn<>("Item");
        colItem.setPrefWidth(260);
        colItem.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colItem.setCellFactory(tc -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            private final Label lblNome = new Label();
            private final Label lblBadges = new Label();
            private final HBox raiz;

            {
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);

                lblNome.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 14px;");
                lblNome.setMaxWidth(190);
                lblNome.setWrapText(true);

                lblBadges.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

                VBox textos = new VBox(3, lblNome, lblBadges);
                textos.setAlignment(Pos.CENTER_LEFT);

                raiz = new HBox(10, iv, textos);
                raiz.setAlignment(Pos.CENTER_LEFT);
                raiz.setPadding(new Insets(4, 0, 4, 0));
            }

            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setGraphic(null);
                    return;
                }

                // extrai tier e enchant do itemId (ex: "T5_MAIN_SWORD@2")
                String id = l.itemId;
                String tier = id.startsWith("T") && id.length() > 1 ? String.valueOf(id.charAt(1)) : "?";
                String enchant = id.contains("@") ? id.substring(id.indexOf("@") + 1) : "0";

                lblNome.setText(id); // idealmente substituir pelo nome amigavel via BancoDeDadosItens
                lblBadges.setText("T" + tier + "  ·  ."+enchant + "  ·  " + nomeQualidade(l.qualidade));

                // icone da API do Albion
                String urlIcone = "https://render.albiononline.com/v1/item/" + id + ".png?size=48";
                try {
                    iv.setImage(new javafx.scene.image.Image(urlIcone, 48, 48, true, true, true));
                } catch (Exception ignored) {}

                setGraphic(raiz);
            }
        });

        // coluna compra: cidade + preco + tempo + modo
        TableColumn<LinhaFlip, LinhaFlip> colCompra = new TableColumn<>("Compra");
        colCompra.setPrefWidth(180);
        colCompra.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colCompra.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setGraphic(null);
                    return;
                }

                String cor = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(l.cidadeCompra) || c.getNome().equals(l.cidadeCompra))
                        .map(CidadeInfo::getCor).findFirst().orElse("#e05555");

                Label lblCidade = new Label(nomeCidade(l.cidadeCompra));
                lblCidade.setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label lblPreco = new Label(formatarPreco(l.precoCompra));
                lblPreco.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

                Label lblTempo = new Label(formatarData(l.dataCompra));
                lblTempo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

                String modoTexto = tipoCompra.equals("sell") ? "Compra direta." : "Pedido de compra";
                Label lblModo = new Label(modoTexto);
                lblModo.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                VBox vb = new VBox(2, lblCidade, lblPreco, lblTempo, lblModo);
                vb.setPadding(new Insets(4, 0, 4, 0));
                setGraphic(vb);
            }
        });

        // coluna venda: cidade + preco + tempo + modo
        TableColumn<LinhaFlip, LinhaFlip> colVenda = new TableColumn<>("Venda");
        colVenda.setPrefWidth(180);
        colVenda.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colVenda.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setGraphic(null);
                    return;
                }

                String cor = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(l.cidadeVenda) || c.getNome().equals(l.cidadeVenda))
                        .map(CidadeInfo::getCor).findFirst().orElse("#3dba6e");

                Label lblCidade = new Label(nomeCidade(l.cidadeVenda));
                lblCidade.setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

                Label lblPreco = new Label(formatarPreco(l.precoVenda));
                lblPreco.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px;");

                Label lblTempo = new Label(formatarData(l.dataVenda));
                lblTempo.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                String modoTexto = tipoVenda.equals("buy") ? "Venda direta." : "Pedido de venda";
                Label lblModo = new Label(modoTexto);
                lblModo.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                VBox vb = new VBox(2, lblCidade, lblPreco, lblTempo, lblModo);
                vb.setPadding(new Insets(4, 0, 4, 0));
                setGraphic(vb);
            }
        });

        // coluna custo total com taxa embutida
        TableColumn<LinhaFlip, LinhaFlip> colCusto = new TableColumn<>("Custo Total");
        colCusto.setPrefWidth(140);
        colCusto.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colCusto.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setGraphic(null);
                    return;
                }

                // taxa de 5% sobre o preco de venda (sem premium)
                long taxa = Math.round(l.precoVenda * 0.05);
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

        TableColumn<LinhaFlip, LinhaFlip> colQtd = new TableColumn<>("Qtd");
        colQtd.setPrefWidth(100);
        colQtd.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colQtd.setCellFactory(tc -> new TableCell<>() {

            private final Button btnMenos = new Button("-");
            private final Button btnMais = new Button("+");
            private final TextField campoQtd = new TextField("1");
            private final HBox raiz;
            // referencia ao label de lucro desta linha, atualizado diretamente
            private Runnable atualizarLucro = null;

            {
                String estiloBotao =
                        "-fx-background-color: #2a2a2a; -fx-text-fill: #e0e0e0; " +
                                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                                "-fx-background-radius: 4; -fx-border-color: #444; " +
                                "-fx-border-radius: 4; -fx-border-width: 1; " +
                                "-fx-min-width: 28px; -fx-max-width: 28px; " +
                                "-fx-min-height: 28px; -fx-max-height: 28px; " +
                                "-fx-padding: 0;";

                btnMenos.setStyle(estiloBotao);
                btnMais.setStyle(estiloBotao);

                campoQtd.setStyle(
                        "-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0; " +
                                "-fx-font-size: 13px; -fx-alignment: center; " +
                                "-fx-border-color: #444; -fx-border-radius: 4; " +
                                "-fx-background-radius: 4; -fx-pref-width: 36px; " +
                                "-fx-max-width: 36px;");
                campoQtd.setAlignment(Pos.CENTER);

                btnMenos.setOnAction(e -> {
                    LinhaFlip l = getItem();
                    if (l == null) return;
                    if (l.quantidade > 1) {
                        l.quantidade--;
                        campoQtd.setText(String.valueOf(l.quantidade));
                        if (atualizarLucro != null) atualizarLucro.run();
                    }
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
                    } catch (NumberFormatException ignored) {
                    }
                });

                raiz = new HBox(4, btnMenos, campoQtd, btnMais);
                raiz.setAlignment(Pos.CENTER);
                raiz.setPadding(new Insets(4, 0, 4, 0));
            }

            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setGraphic(null);
                    atualizarLucro = null;
                    return;
                }
                // registra callback pra coluna de lucro desta linha
                atualizarLucro = () -> {
                    if (l.lblLucroRef != null) {
                        long lucroTotal = l.lucroBruto * l.quantidade;
                        boolean positivo = lucroTotal >= 0;
                        l.lblLucroRef.setText((positivo ? "+" : "") + formatarPreco(lucroTotal));
                        l.lblLucroRef.setStyle("-fx-text-fill: " + (positivo ? "#3dba6e" : "#e05555")
                                + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }
                };
                campoQtd.setText(String.valueOf(l.quantidade));
                setGraphic(raiz);
            }
        });

        TableColumn<LinhaFlip, LinhaFlip> colLucro = new TableColumn<>("Lucro");
        colLucro.setPrefWidth(160);
        colLucro.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue()));
        colLucro.setCellFactory(tc -> new TableCell<>() {
            private final Label lblLucro = new Label();
            private final Label lblPct = new Label();
            private final Button btnSalvar = new Button("Salvar");
            private final VBox vb = new VBox(2, lblLucro, lblPct, btnSalvar);

            {
                btnSalvar.setStyle(
                        "-fx-background-color: #1a3a26; -fx-text-fill: #3dba6e; " +
                                "-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 10px; " +
                                "-fx-border-color: #2a5a3a; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 3 8;");
                vb.setPadding(new Insets(4, 0, 4, 0));
            }

            private void atualizar(LinhaFlip l) {
                long lucroTotal = l.lucroBruto * l.quantidade;
                boolean positivo = lucroTotal >= 0;
                lblLucro.setText((positivo ? "+" : "") + formatarPreco(lucroTotal));
                lblLucro.setStyle("-fx-text-fill: " + (positivo ? "#3dba6e" : "#e05555")
                        + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                lblPct.setText(l.lucroPercentual + "% Profit");
                lblPct.setStyle("-fx-text-fill: " + (positivo ? "#2a9a5e" : "#c04545")
                        + "; -fx-font-size: 12px;");
            }

            @Override
            protected void updateItem(LinhaFlip l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setGraphic(null);
                    return;
                }
                atualizar(l);
                l.lblLucroRef = lblLucro;
                btnSalvar.setOnAction(e -> salvar(l));
                setGraphic(vb);
            }
        });

        tabela.getColumns().addAll(colItem, colCompra, colVenda, colCusto, colQtd, colLucro);

        // barra de navegacao de paginas
        btnAnterior = new Button("Anterior");
        btnProxima = new Button("Proxima");
        labelPagina = new Label("—");

        String estiloBtnNav =
                "-fx-background-color: #2a2a2a; -fx-text-fill: #e0e0e0; " +
                        "-fx-font-weight: bold; -fx-font-size: 13px; " +
                        "-fx-background-radius: 6; -fx-border-color: #444; " +
                        "-fx-border-radius: 6; -fx-border-width: 1; -fx-padding: 6 20;";
        btnAnterior.setStyle(estiloBtnNav);
        btnProxima.setStyle(estiloBtnNav);
        labelPagina.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        // durante o carregamento so bloqueia proxima, anterior continua livre
        btnAnterior.setDisable(paginaAtual == 0);
        btnProxima.setDisable(true);

        btnAnterior.setOnAction(e -> {
            if (paginaAtual > 0) {
                paginaAtual--;
                buscarPaginaAtual();
            }
        });

        btnProxima.setOnAction(e -> {
            if (paginaAtual < totalPaginas - 1) {
                paginaAtual++;
                buscarPaginaAtual();
            }
        });

        HBox navBar = new HBox(16, btnAnterior, labelPagina, btnProxima);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(12, 0, 12, 0));
        navBar.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #2a2a2a; -fx-border-width: 1 0 0 0;");

        VBox area = new VBox(tabela, navBar);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        area.setStyle("-fx-background-color: #1a1a1a;");
        return area;
    }

    private void iniciarBusca() {
        todasLinhas.clear();
        todosIds.clear();
        paginaAtual = 0;

        for (ItemDefinition item : BancoDeDadosItens.getTodosItens()) {
            for (int tier = 4; tier <= 8; tier++) {
                for (int enc = 0; enc <= 4; enc++) {
                    todosIds.add(item.buildApiId(tier, enc));
                }
            }
        }

        totalPaginas = (int) Math.ceil((double) todosIds.size() / IDS_POR_PAGINA);
        btnProxima.setDisable(false);
        buscarPaginaAtual();
    }

    private void buscarPaginaAtual() {
        int inicio = paginaAtual * IDS_POR_PAGINA;
        int fim = Math.min(inicio + IDS_POR_PAGINA, todosIds.size());
        List<String> lote = todosIds.subList(inicio, fim);

        btnAnterior.setDisable(true);
        btnProxima.setDisable(true);
        labelPagina.setText("Buscando pagina " + (paginaAtual + 1) + " de " + totalPaginas + "...");
        atualizarStatus("Buscando " + lote.size() + " IDs...", true);
        tabela.setItems(FXCollections.emptyObservableList());

        Task<List<LinhaFlip>> tarefa = new Task<>() {


            @Override
            protected List<LinhaFlip> call() throws Exception {
                // busca em sublotes de 20 pra nao estourar a url
                List<JsonObject> todosRegistros = new ArrayList<>();
                int tamSublote = 20;
                for (int i = 0; i < lote.size(); i += tamSublote) {
                    List<String> sublote = lote.subList(i, Math.min(i + tamSublote, lote.size()));
                    String idsStr = String.join(",", sublote);
                    String url = API_BASE + "/" + idsStr
                            + ".json?locations=" + CIDADES_API
                            + "&qualities=1,2,3,4,5";

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(25))
                            .GET().build();

                    HttpResponse<String> resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());

                    if (resp.statusCode() == 429) {
                        Thread.sleep(2000);
                        resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());
                    }

                    if (resp.statusCode() == 200) {
                        JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
                        for (JsonElement el : arr) todosRegistros.add(el.getAsJsonObject());
                    }

                    // pausa curta entre sublotes
                    if (i + tamSublote < lote.size()) Thread.sleep(200);
                }
                return calcularOportunidades(todosRegistros);
            }
        };

        tarefa.setOnSucceeded(e -> {
            List<LinhaFlip> resultado = tarefa.getValue();
            tabela.setItems(FXCollections.observableArrayList(resultado));

            labelPagina.setText("Pagina " + (paginaAtual + 1) + " de " + totalPaginas
                    + "  (" + resultado.size() + " oportunidades)");
            labelContagem.setText(resultado.size() + " oportunidades nesta pagina");

            btnAnterior.setDisable(paginaAtual == 0);
            btnProxima.setDisable(paginaAtual >= totalPaginas - 1);
            atualizarStatus("Pagina " + (paginaAtual + 1) + " carregada.", false);
        });

        tarefa.setOnFailed(e -> {
            labelPagina.setText("Erro na pagina " + (paginaAtual + 1));
            btnAnterior.setDisable(paginaAtual == 0);
            btnProxima.setDisable(paginaAtual >= totalPaginas - 1);
            atualizarStatus("Erro: " + tarefa.getException().getMessage(), false);
        });

        new Thread(tarefa, "thread-flip-pagina-" + paginaAtual).start();
    }


    private List<LinhaFlip> calcularOportunidades(List<JsonObject> registros) {
        // agrupa por (item_id, qualidade)
        Map<String, List<JsonObject>> agrupado = new LinkedHashMap<>();
        for (JsonObject o : registros) {
            long sellMin = o.get("sell_price_min").getAsLong();
            long buyMax = o.get("buy_price_max").getAsLong();
            if (sellMin == 0 && buyMax == 0) continue;
            String chave = o.get("item_id").getAsString() + "|" + o.get("quality").getAsInt();
            agrupado.computeIfAbsent(chave, k -> new ArrayList<>()).add(o);
        }

        String campoCompra = tipoCompra.equals("sell") ? "sell_price_min" : "buy_price_max";
        String campoDataCompra = tipoCompra.equals("sell") ? "sell_price_min_date" : "buy_price_max_date";
        String campoVenda = tipoVenda.equals("buy") ? "buy_price_max" : "sell_price_min";
        String campoDataVenda = tipoVenda.equals("buy") ? "buy_price_max_date" : "sell_price_min_date";

        List<LinhaFlip> resultado = new ArrayList<>();

        for (List<JsonObject> entradas : agrupado.values()) {
            if (entradas.size() < 2) continue;

            String itemId = entradas.get(0).get("item_id").getAsString();
            int qual = entradas.get(0).get("quality").getAsInt();

            // menor preço de compra entre todas as cidades
            JsonObject melhorCompra = null;
            long menorPreco = Long.MAX_VALUE;
            for (JsonObject o : entradas) {
                long preco = o.get(campoCompra).getAsLong();
                if (preco > 0 && preco < menorPreco) {
                    menorPreco = preco;
                    melhorCompra = o;
                }
            }
            if (melhorCompra == null) continue;

            // maior preço de venda em cidade diferente
            JsonObject melhorVenda = null;
            long maiorPreco = Long.MIN_VALUE;
            String cidadeCompra = melhorCompra.get("city").getAsString();
            for (JsonObject o : entradas) {
                if (o.get("city").getAsString().equals(cidadeCompra)) continue;
                long preco = o.get(campoVenda).getAsLong();
                if (preco > 0 && preco > maiorPreco) {
                    maiorPreco = preco;
                    melhorVenda = o;
                }
            }
            if (melhorVenda == null) continue;

            long lucroBruto = maiorPreco - menorPreco;
            if (lucroBruto < lucroMin) continue;
            if (lucroMax > 0 && lucroBruto > lucroMax) continue;

            double lucroPerc = menorPreco > 0
                    ? Math.round((lucroBruto * 10000.0) / menorPreco) / 100.0
                    : 0;

            String dataC = obterCampo(melhorCompra, campoDataCompra);
            String dataV = obterCampo(melhorVenda, campoDataVenda);

            resultado.add(new LinhaFlip(
                    itemId, qual,
                    cidadeCompra, menorPreco,
                    melhorVenda.get("city").getAsString(), maiorPreco,
                    lucroBruto, lucroPerc,
                    dataC, dataV
            ));
        }
        return resultado;
    }


    private void salvar(LinhaFlip l) {
        try {
            String nomeArquivo = "flip_"
                    + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".json";

            String json = "{\n" +
                    "  \"tipo\": \"flip\",\n" +
                    "  \"tipo_compra\": \"" + tipoCompra + "\",\n" +
                    "  \"tipo_venda\": \"" + tipoVenda + "\",\n" +
                    "  \"item_id\": \"" + l.itemId + "\",\n" +
                    "  \"qualidade\": " + l.qualidade + ",\n" +
                    "  \"cidade_compra\": \"" + l.cidadeCompra + "\",\n" +
                    "  \"preco_compra\": " + l.precoCompra + ",\n" +
                    "  \"cidade_venda\": \"" + l.cidadeVenda + "\",\n" +
                    "  \"preco_venda\": " + l.precoVenda + ",\n" +
                    "  \"quantidade\": " + l.quantidade + ",\n" +
                    "  \"lucro_bruto_unitario\": " + l.lucroBruto + ",\n" +
                    "  \"lucro_total\": " + (l.lucroBruto * l.quantidade) + ",\n" +
                    "  \"data_registro\": \"" + java.time.Instant.now() + "\"\n" +
                    "}\n";

            java.nio.file.Path dir = java.nio.file.Paths.get(
                    System.getenv("LOCALAPPDATA"), "AlbionMarket", "operacoes");
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Files.writeString(dir.resolve(nomeArquivo), json);

            labelStatus.setText("Salvo: " + nomeArquivo);
        } catch (Exception ex) {
            labelStatus.setText("Erro ao salvar: " + ex.getMessage());
        }
    }


    private String obterCampo(JsonObject o, String campo) {
        return (o.has(campo) && !o.get(campo).isJsonNull()) ? o.get(campo).getAsString() : null;
    }

    private void atualizarStatus(String msg, boolean carregando) {
        Platform.runLater(() -> {
            labelStatus.setText(msg);
            progresso.setVisible(carregando);
        });
    }

    private String formatarPreco(long valor) {
        if (valor <= 0) return "—";
        return java.text.NumberFormat.getNumberInstance(new java.util.Locale("pt", "BR")).format(valor);
    }

    private String formatarData(String isoStr) {
        if (isoStr == null || isoStr.isBlank() || isoStr.startsWith("0001")) return "—";
        try {
            java.time.Instant data;
            try {
                data = java.time.Instant.parse(isoStr);
            } catch (Exception ex) {
                data = java.time.LocalDateTime.parse(isoStr).toInstant(java.time.ZoneOffset.UTC);
            }
            long min = java.time.temporal.ChronoUnit.MINUTES.between(data, java.time.Instant.now());
            if (min < 2) return "agora";
            if (min < 60) return min + "min";
            if (min < 1440) return (min / 60) + "h";
            return (min / 1440) + "d";
        } catch (Exception e) {
            return "—";
        }
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

    private String nomeCidade(String apiIdOuNome) {
        return BancoDeDadosItens.CIDADES.stream()
                .filter(c -> c.getApiId().equals(apiIdOuNome) || c.getNome().equals(apiIdOuNome))
                .map(CidadeInfo::getNome)
                .findFirst().orElse(apiIdOuNome != null ? apiIdOuNome : "-");
    }
}