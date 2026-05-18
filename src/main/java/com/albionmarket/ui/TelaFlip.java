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

    private static final String API_BASE   = "https://west.albion-online-data.com/api/v2/stats/prices";
    private static final String CIDADES_API = "Caerleon,Martlock,Bridgewatch,FortSterling,Lymhurst,Thetford";
    private static final int    TAM_LOTE   = 40;

    private final Stage  palco;
    private final String tipoCompra;  // "sell" ou "buy"
    private final String tipoVenda;   // "buy"  ou "sell"
    private final long   lucroMin;
    private final long   lucroMax;    // 0 = sem limite

    private List<LinhaFlip> todasLinhas = new ArrayList<>();
    private TableView<LinhaFlip> tabela;
    private Label  labelStatus;
    private Label  labelContagem;
    private ProgressIndicator progresso;

    private List<String> todosIds  = new ArrayList<>();
    private int loteAtual = 0;
    private volatile boolean buscando = false;

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();



    public static class LinhaFlip {
        public final String itemId;
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

        public LinhaFlip(String itemId, int qualidade,
                         String cidadeCompra, long precoCompra,
                         String cidadeVenda, long precoVenda,
                         long lucroBruto, double lucroPercentual,
                         String dataCompra, String dataVenda) {
            this.itemId          = itemId;
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



    public TelaFlip(Stage palco, String tipoCompra, String tipoVenda, long lucroMin, long lucroMax) {
        this.palco      = palco;
        this.tipoCompra = tipoCompra;
        this.tipoVenda  = tipoVenda;
        this.lucroMin   = lucroMin;
        this.lucroMax   = lucroMax;
    }



    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1a1a1a;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarAreaCentral());

        palco.setTitle("Flip de Mercado — Albion Online");
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
        String lblVend = tipoVenda.equals("buy")   ? "Venda Direta"  : "Pedido de Venda";
        Label badgeComp = criarBadge(lblComp, "#1e3a5f", "#5a8dee");
        Label badgeVend = criarBadge(lblVend, "#1a3a26", "#3dba6e");
        Label badgeLucro = criarBadge(
                "Lucro: " + formatarPreco(lucroMin) + (lucroMax > 0 ? " – " + formatarPreco(lucroMax) : "+"),
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

        Label placeholder = new Label("Carregando oportunidades...");
        placeholder.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        tabela.setPlaceholder(placeholder);

        // --- coluna Item ---
        TableColumn<LinhaFlip, String> colItem = colTexto("Item", 200);
        colItem.setCellValueFactory(r -> {
            String id = r.getValue().itemId;
            String tierStr = (id.length() > 1 && id.charAt(0) == 'T' && Character.isDigit(id.charAt(1)))
                    ? " T" + id.charAt(1) : "";
            String enchStr = id.contains("@") ? "." + id.split("@")[1] : "";
            String sufixo  = id.replaceAll("^T\\d_", "").replaceAll("@\\d$", "");
            String nome = BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixo))
                    .map(ItemDefinition::getNome)
                    .findFirst().orElse(sufixo);
            return prop(nome + tierStr + enchStr);
        });

        // --- coluna Qualidade ---
        TableColumn<LinhaFlip, String> colQual = colTexto("Qual.", 70);
        colQual.setCellValueFactory(r -> prop(nomeQualidade(r.getValue().qualidade)));

        // --- coluna Comprar em (com bolinha colorida) ---
        TableColumn<LinhaFlip, String> colCompra = colCidade("Comprar em", 130);
        colCompra.setCellValueFactory(r -> prop(r.getValue().cidadeCompra));

        // --- coluna Preço de Compra (vermelho) ---
        TableColumn<LinhaFlip, String> colPrecoCompra = colTexto("Preço Compra", 120);
        colPrecoCompra.setCellValueFactory(r -> prop(formatarPreco(r.getValue().precoCompra)));
        colPrecoCompra.setCellFactory(tc -> celulaCor("#e05555", true));

        // --- coluna Data Compra ---
        TableColumn<LinhaFlip, String> colDataC = colTexto("Atualiz.", 70);
        colDataC.setCellValueFactory(r -> prop(formatarData(r.getValue().dataCompra)));
        colDataC.setCellFactory(tc -> celulaData());

        // --- coluna Vender em (com bolinha colorida) ---
        TableColumn<LinhaFlip, String> colVenda = colCidade("Vender em", 130);
        colVenda.setCellValueFactory(r -> prop(r.getValue().cidadeVenda));

        // --- coluna Preço de Venda (verde) ---
        TableColumn<LinhaFlip, String> colPrecoVenda = colTexto("Preço Venda", 120);
        colPrecoVenda.setCellValueFactory(r -> prop(formatarPreco(r.getValue().precoVenda)));
        colPrecoVenda.setCellFactory(tc -> celulaCor("#3dba6e", true));

        // --- coluna Data Venda ---
        TableColumn<LinhaFlip, String> colDataV = colTexto("Atualiz.", 70);
        colDataV.setCellValueFactory(r -> prop(formatarData(r.getValue().dataVenda)));
        colDataV.setCellFactory(tc -> celulaData());

        // --- coluna Lucro Bruto (azul, destaque) ---
        TableColumn<LinhaFlip, String> colLucro = colTexto("Lucro Bruto", 130);
        colLucro.setCellValueFactory(r -> prop(formatarPreco(r.getValue().lucroBruto)));
        colLucro.setCellFactory(tc -> celulaCor("#5a8dee", true));

        // --- coluna % ---
        TableColumn<LinhaFlip, String> colPct = colTexto("%", 65);
        colPct.setCellValueFactory(r -> prop(r.getValue().lucroPercentual + "%"));
        colPct.setCellFactory(tc -> celulaCor("#e0b84a", false));

        // --- coluna Qtd (editável) ---
        TableColumn<LinhaFlip, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setPrefWidth(55);
        colQtd.setCellValueFactory(r ->
                new javafx.beans.property.SimpleIntegerProperty(r.getValue().quantidade).asObject());
        colQtd.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(
                new javafx.util.converter.IntegerStringConverter()));
        colQtd.setOnEditCommit(ev -> {
            LinhaFlip l = ev.getRowValue();
            l.quantidade = (ev.getNewValue() != null && ev.getNewValue() > 0) ? ev.getNewValue() : 1;
            tabela.refresh();
        });

        // --- coluna Lucro Total ---
        TableColumn<LinhaFlip, String> colTotal = colTexto("Lucro Total", 130);
        colTotal.setCellValueFactory(r -> prop(formatarPreco(r.getValue().lucroBruto * r.getValue().quantidade)));
        colTotal.setCellFactory(tc -> celulaCor("#5a8dee", true));

        // --- coluna Salvar ---
        TableColumn<LinhaFlip, Void> colSalvar = new TableColumn<>("Salvar");
        colSalvar.setPrefWidth(80);
        colSalvar.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Salvar");
            {
                btn.setStyle(
                        "-fx-background-color: #1a3a26; -fx-text-fill: #3dba6e; " +
                                "-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 11px; " +
                                "-fx-border-color: #2a5a3a; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 4 10;");
                btn.setOnAction(e -> salvar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabela.getColumns().addAll(
                colItem, colQual, colCompra, colPrecoCompra, colDataC,
                colVenda, colPrecoVenda, colDataV,
                colLucro, colPct, colQtd, colTotal, colSalvar
        );

        VBox area = new VBox(tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        area.setStyle("-fx-background-color: #1a1a1a;");
        return area;
    }



    private void iniciarBusca() {
        todasLinhas.clear();
        todosIds.clear();
        loteAtual = 0;


        for (ItemDefinition item : BancoDeDadosItens.getTodosItens()) {
            for (int tier = 4; tier <= 8; tier++) {
                for (int enc = 0; enc <= 3; enc++) {
                    todosIds.add(item.buildApiId(tier, enc));
                }
            }
        }

        atualizarStatus("Buscando preços... (0/" + todosIds.size() + " IDs)", true);
        buscarProximoLote();
    }

    private void buscarProximoLote() {
        int inicio = loteAtual * TAM_LOTE;
        if (inicio >= todosIds.size()) {
            atualizarStatus("Concluído", false);
            return;
        }

        List<String> lote = todosIds.subList(inicio, Math.min(inicio + TAM_LOTE, todosIds.size()));
        loteAtual++;

        int idsProcessados = Math.min(loteAtual * TAM_LOTE, todosIds.size());

        Task<List<LinhaFlip>> tarefa = new Task<>() {
            @Override
            protected List<LinhaFlip> call() throws Exception {
                String idsStr = String.join(",", lote);
                // busca todas as qualidades de uma vez
                String url = API_BASE + "/" + idsStr
                        + ".json?locations=" + CIDADES_API
                        + "&qualities=1,2,3,4,5";

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .GET().build();

                HttpResponse<String> resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200)
                    throw new Exception("Erro HTTP " + resp.statusCode());

                List<JsonObject> registros = new ArrayList<>();
                JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
                for (JsonElement el : arr) registros.add(el.getAsJsonObject());

                return calcularOportunidades(registros);
            }
        };

        tarefa.setOnSucceeded(e -> {
            todasLinhas.addAll(tarefa.getValue());
            todasLinhas.sort((a, b) -> Long.compare(b.lucroBruto, a.lucroBruto));
            tabela.setItems(FXCollections.observableArrayList(todasLinhas));

            atualizarStatus(
                    idsProcessados + "/" + todosIds.size() + " IDs processados",
                    idsProcessados < todosIds.size()
            );
            labelContagem.setText(todasLinhas.size() + " oportunidades");

            // continua buscando automaticamente
            buscarProximoLote();
        });

        tarefa.setOnFailed(e -> {
            atualizarStatus("Erro: " + tarefa.getException().getMessage(), false);
            // tenta continuar mesmo com falha em um lote
            buscarProximoLote();
        });

        new Thread(tarefa, "thread-flip-lote-" + loteAtual).start();
    }



    private List<LinhaFlip> calcularOportunidades(List<JsonObject> registros) {
        // agrupa por (item_id, qualidade)
        Map<String, List<JsonObject>> agrupado = new LinkedHashMap<>();
        for (JsonObject o : registros) {
            long sellMin = o.get("sell_price_min").getAsLong();
            long buyMax  = o.get("buy_price_max").getAsLong();
            if (sellMin == 0 && buyMax == 0) continue;
            String chave = o.get("item_id").getAsString() + "|" + o.get("quality").getAsInt();
            agrupado.computeIfAbsent(chave, k -> new ArrayList<>()).add(o);
        }

        String campoCompra     = tipoCompra.equals("sell") ? "sell_price_min" : "buy_price_max";
        String campoDataCompra = tipoCompra.equals("sell") ? "sell_price_min_date" : "buy_price_max_date";
        String campoVenda      = tipoVenda.equals("buy")   ? "buy_price_max" : "sell_price_min";
        String campoDataVenda  = tipoVenda.equals("buy")   ? "buy_price_max_date" : "sell_price_min_date";

        List<LinhaFlip> resultado = new ArrayList<>();

        for (List<JsonObject> entradas : agrupado.values()) {
            if (entradas.size() < 2) continue;

            String itemId = entradas.get(0).get("item_id").getAsString();
            int    qual   = entradas.get(0).get("quality").getAsInt();

            // menor preço de compra entre todas as cidades
            JsonObject melhorCompra = null;
            long menorPreco = Long.MAX_VALUE;
            for (JsonObject o : entradas) {
                long preco = o.get(campoCompra).getAsLong();
                if (preco > 0 && preco < menorPreco) {
                    menorPreco  = preco;
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
                    maiorPreco  = preco;
                    melhorVenda = o;
                }
            }
            if (melhorVenda == null) continue;

            long   lucroBruto  = maiorPreco - menorPreco;
            if (lucroBruto < lucroMin) continue;
            if (lucroMax > 0 && lucroBruto > lucroMax) continue;

            double lucroPerc = menorPreco > 0
                    ? Math.round((lucroBruto * 10000.0) / menorPreco) / 100.0
                    : 0;

            String dataC = obterCampo(melhorCompra, campoDataCompra);
            String dataV = obterCampo(melhorVenda,  campoDataVenda);

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



    private TableColumn<LinhaFlip, String> colTexto(String titulo, double largura) {
        TableColumn<LinhaFlip, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        return col;
    }

    private TableColumn<LinhaFlip, String> colCidade(String titulo, double largura) {
        TableColumn<LinhaFlip, String> col = colTexto(titulo, largura);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String cor = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getNome().equals(v) || c.getApiId().equals(v))
                        .map(CidadeInfo::getCor).findFirst().orElse("#888");
                Circle ponto = new Circle(5, Color.web(cor));
                Label  label = new Label(v);
                label.setStyle("-fx-text-fill: #e0e0e0;");
                HBox hb = new HBox(6, ponto, label);
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });
        return col;
    }

    private TableCell<LinhaFlip, String> celulaCor(String cor, boolean negrito) {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                String peso = negrito ? "bold" : "normal";
                setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: " + peso + "; -fx-alignment: CENTER-RIGHT;");
            }
        };
    }

    private TableCell<LinhaFlip, String> celulaData() {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-alignment: CENTER;");
            }
        };
    }



    private javafx.beans.property.SimpleStringProperty prop(String s) {
        return new javafx.beans.property.SimpleStringProperty(s);
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
            try { data = java.time.Instant.parse(isoStr); }
            catch (Exception ex) {
                data = java.time.LocalDateTime.parse(isoStr).toInstant(java.time.ZoneOffset.UTC);
            }
            long min = java.time.temporal.ChronoUnit.MINUTES.between(data, java.time.Instant.now());
            if (min <  2) return "agora";
            if (min < 60) return min + "min";
            if (min < 1440) return (min / 60) + "h";
            return (min / 1440) + "d";
        } catch (Exception e) { return "—"; }
    }

    private String nomeQualidade(int q) {
        return switch (q) {
            case 1 -> "Normal";
            case 2 -> "Boa";
            case 3 -> "Notável";
            case 4 -> "Excelente";
            case 5 -> "Obra-prima";
            default -> "?";
        };
    }
}