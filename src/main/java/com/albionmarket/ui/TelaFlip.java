package com.albionmarket.ui;

import com.albionmarket.model.CidadeInfo;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.service.BancoDeDadosItens;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import javafx.application.Platform;

/**
 * tela de resultados de flip com paginacao e calculo de lucro por quantidade
 */
public class TelaFlip {

    private static final String API_BASE = "https://west.albion-online-data.com/api/v2/stats/prices";
    private static final String CIDADES_API = "Caerleon,Martlock,Bridgewatch,FortSterling,Lymhurst,Thetford";
    private static final int POR_PAGINA = 10;

    private final Stage palco;
    private final int qualidade;
    private final String tipoCompra; // "sell" ou "buy"
    private final String tipoVenda;  // "sell" ou "buy"
    private final long lucroMinimo;

    // lista completa calculada de oportunidades, paginada localmente
    private List<LinhaFlip> todasLinhas = new ArrayList<>();
    private int paginaAtual = 1;
    private int totalPaginas = 1;

    private TableView<LinhaFlip> tabelaResultados;
    private Label labelStatus;
    private Label labelPagina;
    private ProgressIndicator progresso;
    private Button btnAnterior;
    private Button btnProxima;

    private List<String> todosIds = new ArrayList<>();
    private int loteAtual = 0;
    private static final int TAM_LOTE = 50;

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // modelo da tabela
    public static class LinhaFlip {
        public final String itemId;
        public final int qualidade;
        public final String cidadeCompra;
        public final long precoCompra;
        public final String cidadeVenda;
        public final long precoVenda;
        public final long lucroBruto;
        public final double lucroPercentual;
        public int quantidade;

        public LinhaFlip(String itemId, int qualidade, String cidadeCompra, long precoCompra,
                         String cidadeVenda, long precoVenda, long lucroBruto, double lucroPercentual) {
            this.itemId = itemId;
            this.qualidade = qualidade;
            this.cidadeCompra = cidadeCompra;
            this.precoCompra = precoCompra;
            this.cidadeVenda = cidadeVenda;
            this.precoVenda = precoVenda;
            this.lucroBruto = lucroBruto;
            this.lucroPercentual = lucroPercentual;
            this.quantidade = 1;
        }
    }

    public TelaFlip(Stage palco, int qualidade, String tipoCompra, String tipoVenda, long lucroMinimo) {
        this.palco = palco;
        this.qualidade = qualidade;
        this.tipoCompra = tipoCompra;
        this.tipoVenda = tipoVenda;
        this.lucroMinimo = lucroMinimo;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarAreaCentral());
        raiz.setBottom(criarRodape());

        palco.setTitle("Flip de Mercado");
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        buscarOportunidades();
    }

    private HBox criarCabecalho() {
        Label titulo = new Label("Flip de Mercado");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Flip de Mercado");
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label btnHome = new Label("Inicio");
        btnHome.setStyle("-fx-font-size: 15px; -fx-cursor: hand;");
        btnHome.setOnMouseEntered(e -> btnHome.setStyle("-fx-font-size: 20px; -fx-cursor: hand; -fx-opacity: 0.7;"));
        btnHome.setOnMouseExited(e -> btnHome.setStyle("-fx-font-size: 20px; -fx-cursor: hand;"));
        btnHome.setOnMouseClicked(e -> new TelaHome(palco).mostrar());

        HBox cab = new HBox(textos, espacador, btnHome);
        cab.setAlignment(Pos.CENTER_LEFT);
        cab.setPadding(new Insets(14, 20, 14, 20));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cab;
    }

    @SuppressWarnings("unchecked")
    private VBox criarAreaCentral() {
        progresso = new ProgressIndicator();
        progresso.setMaxSize(24, 24);
        progresso.setVisible(false);

        labelStatus = new Label("Buscando oportunidades...");
        labelStatus.setStyle("-fx-text-fill: #999;");

        HBox barraStatus = new HBox(10, progresso, labelStatus);
        barraStatus.setAlignment(Pos.CENTER_LEFT);
        barraStatus.setPadding(new Insets(10, 16, 8, 16));

        tabelaResultados = new TableView<>();
        tabelaResultados.setStyle("-fx-background-color: #1e1e1e;");
        tabelaResultados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaResultados.setPlaceholder(new Label("Nenhuma oportunidade encontrada."));
        tabelaResultados.setEditable(true);

        // item
        TableColumn<LinhaFlip, String> colItem = new TableColumn<>("Item");
        colItem.setPrefWidth(200);
        colItem.setCellValueFactory(r -> {
            String idCompleto = r.getValue().itemId;
            String tierStr = "";
            if (idCompleto.length() > 1 && idCompleto.charAt(0) == 'T'
                    && Character.isDigit(idCompleto.charAt(1))) {
                tierStr = String.valueOf(idCompleto.charAt(1));
            }
            String enchStr = "";
            if (idCompleto.contains("@")) {
                enchStr = idCompleto.split("@")[1];
            }
            String sufixo = idCompleto.replaceAll("^T\\d_", "").replaceAll("@\\d$", "");
            String nome = BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixo))
                    .map(ItemDefinition::getNome)
                    .findFirst()
                    .orElse(sufixo);
            String exibir = nome + " " + tierStr;
            if (!enchStr.isEmpty()) exibir += "." + enchStr;
            return new javafx.beans.property.SimpleStringProperty(exibir);
        });

        // qualidade
        TableColumn<LinhaFlip, String> colQual = new TableColumn<>("Qualidade");
        colQual.setPrefWidth(90);
        colQual.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                nomeQualidade(r.getValue().qualidade)));

        // cidade de compra
        TableColumn<LinhaFlip, String> colCompra = new TableColumn<>("Comprar em");
        colCompra.setPrefWidth(130);
        colCompra.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidadeCompra));
        colCompra.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    return;
                }
                String cor = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getNome().equals(v) || c.getApiId().equals(v))
                        .map(CidadeInfo::getCor).findFirst().orElse("#888");
                Circle ponto = new Circle(5, Color.web(cor));
                HBox hb = new HBox(6, ponto, new Label(v));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });

        // preco de compra
        TableColumn<LinhaFlip, String> colPrecoCompra = new TableColumn<>("Preco Compra");
        colPrecoCompra.setPrefWidth(120);
        colPrecoCompra.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                formatarPreco(r.getValue().precoCompra)));
        colPrecoCompra.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
            }
        });

        // cidade de venda
        TableColumn<LinhaFlip, String> colVenda = new TableColumn<>("Vender em");
        colVenda.setPrefWidth(130);
        colVenda.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidadeVenda));
        colVenda.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    return;
                }
                String cor = BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getNome().equals(v) || c.getApiId().equals(v))
                        .map(CidadeInfo::getCor).findFirst().orElse("#888");
                Circle ponto = new Circle(5, Color.web(cor));
                HBox hb = new HBox(6, ponto, new Label(v));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });

        // preco de venda
        TableColumn<LinhaFlip, String> colPrecoVenda = new TableColumn<>("Preco Venda");
        colPrecoVenda.setPrefWidth(120);
        colPrecoVenda.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                formatarPreco(r.getValue().precoVenda)));
        colPrecoVenda.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
            }
        });

        // lucro bruto
        TableColumn<LinhaFlip, String> colLucro = new TableColumn<>("Lucro Bruto");
        colLucro.setPrefWidth(120);
        colLucro.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                formatarPreco(r.getValue().lucroBruto)));
        colLucro.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #5a8dee; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
            }
        });

        // lucro percentual
        TableColumn<LinhaFlip, String> colPct = new TableColumn<>("Lucro %");
        colPct.setPrefWidth(80);
        colPct.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                r.getValue().lucroPercentual + "%"));
        colPct.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #e0b84a; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
            }
        });

        // quantidade editavel
        TableColumn<LinhaFlip, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setPrefWidth(70);
        colQtd.setCellValueFactory(r -> new javafx.beans.property.SimpleIntegerProperty(r.getValue().quantidade).asObject());
        colQtd.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(
                new javafx.util.converter.IntegerStringConverter()));
        colQtd.setOnEditCommit(ev -> {
            LinhaFlip linha = ev.getRowValue();
            linha.quantidade = ev.getNewValue() != null && ev.getNewValue() > 0 ? ev.getNewValue() : 1;
            tabelaResultados.refresh();
        });

        // lucro total = lucro bruto * quantidade
        TableColumn<LinhaFlip, String> colTotal = new TableColumn<>("Lucro Total");
        colTotal.setPrefWidth(130);
        colTotal.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                formatarPreco(r.getValue().lucroBruto * r.getValue().quantidade)));
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #5a8dee; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
            }
        });

        tabelaResultados.getColumns().addAll(
                colItem, colQual, colCompra, colPrecoCompra,
                colVenda, colPrecoVenda, colLucro, colPct, colQtd, colTotal);

        Button btnSalvar = new Button("Salvar Operacao");
        btnSalvar.setStyle("-fx-background-color: #3dba6e; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 20;");
        btnSalvar.setOnAction(e -> salvarOperacao());

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 20;");
        btnVoltar.setOnAction(e -> new TelaFlipSelecao(palco).mostrar());

        HBox botoes = new HBox(10, btnSalvar, btnVoltar);
        botoes.setPadding(new Insets(10, 16, 10, 16));
        botoes.setAlignment(Pos.CENTER_LEFT);

        VBox area = new VBox(barraStatus, tabelaResultados, botoes);
        VBox.setVgrow(tabelaResultados, Priority.ALWAYS);
        area.setStyle("-fx-background-color: #1e1e1e;");
        return area;
    }

    private HBox criarRodape() {
        btnAnterior = new Button("< Anterior");
        btnAnterior.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16;");
        btnAnterior.setDisable(true);
        btnAnterior.setOnAction(e -> exibirPagina(paginaAtual - 1));

        labelPagina = new Label("Pagina 1 de 1");
        labelPagina.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

        btnProxima = new Button("Proxima >");
        btnProxima.setStyle("-fx-background-color: #5a8dee; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16;");
        btnProxima.setDisable(true);
        btnProxima.setOnAction(e -> buscarProximoLote());

        HBox rodape = new HBox(16, btnAnterior, labelPagina, btnProxima);
        rodape.setAlignment(Pos.CENTER);
        rodape.setPadding(new Insets(12));
        rodape.setStyle("-fx-background-color: #252525; -fx-border-color: #333; -fx-border-width: 1 0 0 0;");
        return rodape;
    }

    /**
     * busca todos os itens do BancoDeDadosItens na API publica do Albion,
     * calcula as oportunidades de arbitragem em memoria e popula todasLinhas.
     * a paginacao e feita localmente por exibirPagina().
     */
    private void buscarOportunidades() {
        progresso.setVisible(true);
        labelStatus.setText("Buscando precos...");
        tabelaResultados.setItems(FXCollections.emptyObservableList());
        todasLinhas.clear();
        btnAnterior.setDisable(true);
        btnProxima.setDisable(true);

        todosIds = new ArrayList<>();
        for (ItemDefinition item : BancoDeDadosItens.getTodosItens()) {
            for (int tier = 4; tier <= 8; tier++) {
                for (int enc = 0; enc <= 3; enc++) {
                    todosIds.add(item.buildApiId(tier, enc));
                }
            }
        }
        loteAtual = 0;
        progresso.setVisible(false);
        labelStatus.setText("Pronto. Clique em Proxima para buscar oportunidades.");
        btnProxima.setDisable(false);
    }

    private List<LinhaFlip> calcularOportunidades(List<JsonObject> registros) {
        Map<String, List<JsonObject>> agrupado = new LinkedHashMap<>();
        for (JsonObject o : registros) {
            long sellMin = o.get("sell_price_min").getAsLong();
            long buyMax = o.get("buy_price_max").getAsLong();
            if (sellMin == 0 && buyMax == 0) continue;

            String chave = o.get("item_id").getAsString() + "|" + o.get("quality").getAsInt();
            agrupado.computeIfAbsent(chave, k -> new ArrayList<>()).add(o);
        }

        List<LinhaFlip> oportunidades = new ArrayList<>();
        for (List<JsonObject> entradas : agrupado.values()) {
            if (entradas.size() < 2) continue;

            String itemId = entradas.get(0).get("item_id").getAsString();
            int qual = entradas.get(0).get("quality").getAsInt();
            String campoCompra = tipoCompra.equals("buy") ? "buy_price_max" : "sell_price_min";
            String campoVenda = tipoVenda.equals("buy") ? "buy_price_max" : "sell_price_min";

            JsonObject melhorCompra = null;
            long menorPrecoCompra = Long.MAX_VALUE;
            for (JsonObject o : entradas) {
                long preco = o.get(campoCompra).getAsLong();
                if (preco > 0 && preco < menorPrecoCompra) {
                    menorPrecoCompra = preco;
                    melhorCompra = o;
                }
            }

            JsonObject melhorVenda = null;
            long maiorPrecoVenda = Long.MIN_VALUE;
            for (JsonObject o : entradas) {
                if (melhorCompra != null &&
                        o.get("city").getAsString().equals(melhorCompra.get("city").getAsString())) continue;
                long preco = o.get(campoVenda).getAsLong();
                if (preco > 0 && preco > maiorPrecoVenda) {
                    maiorPrecoVenda = preco;
                    melhorVenda = o;
                }
            }

            if (melhorCompra == null || melhorVenda == null) continue;

            long lucroBruto = maiorPrecoVenda - menorPrecoCompra;
            if (lucroBruto < lucroMinimo) continue;

            double lucroPercentual = menorPrecoCompra > 0
                    ? Math.round((lucroBruto * 10000.0) / menorPrecoCompra) / 100.0
                    : 0;

            oportunidades.add(new LinhaFlip(
                    itemId, qual,
                    melhorCompra.get("city").getAsString(), menorPrecoCompra,
                    melhorVenda.get("city").getAsString(), maiorPrecoVenda,
                    lucroBruto, lucroPercentual
            ));
        }
        return oportunidades;
    }

    /**
     * pagina a lista ja calculada localmente, sem nova requisicao
     */
    private void exibirPagina(int pagina) {
        paginaAtual = pagina;
        int inicio = (pagina - 1) * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, todasLinhas.size());
        List<LinhaFlip> paginaLinhas = todasLinhas.subList(inicio, fim);

        tabelaResultados.setItems(FXCollections.observableArrayList(paginaLinhas));
        labelPagina.setText("Pagina " + paginaAtual + " de " + totalPaginas);
        btnAnterior.setDisable(paginaAtual <= 1);
        btnProxima.setDisable(paginaAtual >= totalPaginas);
    }

    private void salvarOperacao() {
        if (tabelaResultados.getItems().isEmpty()) return;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"tipo\": \"flip\",\n");
            sb.append("  \"tipo_compra\": \"").append(tipoCompra).append("\",\n");
            sb.append("  \"tipo_venda\": \"").append(tipoVenda).append("\",\n");
            sb.append("  \"itens\": [\n");

            List<LinhaFlip> linhas = tabelaResultados.getItems();
            for (int i = 0; i < linhas.size(); i++) {
                LinhaFlip l = linhas.get(i);
                sb.append("    {\n");
                sb.append("      \"item_id\": \"").append(l.itemId).append("\",\n");
                sb.append("      \"qualidade\": ").append(l.qualidade).append(",\n");
                sb.append("      \"cidade_compra\": \"").append(l.cidadeCompra).append("\",\n");
                sb.append("      \"preco_compra\": ").append(l.precoCompra).append(",\n");
                sb.append("      \"cidade_venda\": \"").append(l.cidadeVenda).append("\",\n");
                sb.append("      \"preco_venda\": ").append(l.precoVenda).append(",\n");
                sb.append("      \"quantidade\": ").append(l.quantidade).append(",\n");
                sb.append("      \"lucro_total\": ").append(l.lucroBruto * l.quantidade).append("\n");
                sb.append("    }").append(i < linhas.size() - 1 ? "," : "").append("\n");
            }

            sb.append("  ]\n");
            sb.append("}\n");

            String nomeArquivo = "flip_"
                    + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".json";

            java.nio.file.Path dir = java.nio.file.Paths.get(
                    System.getenv("LOCALAPPDATA"), "AlbionMarket", "operacoes");
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Files.writeString(dir.resolve(nomeArquivo), sb.toString());

            labelStatus.setText("Operacao salva: " + nomeArquivo);
        } catch (Exception ex) {
            labelStatus.setText("Erro ao salvar: " + ex.getMessage());
        }
    }

    private String formatarPreco(long valor) {
        if (valor <= 0) return "-";
        return java.text.NumberFormat.getNumberInstance(new java.util.Locale("pt", "BR")).format(valor);
    }

    private String nomeQualidade(int q) {
        return switch (q) {
            case 1 -> "Normal";
            case 2 -> "Boa";
            case 3 -> "Notavel";
            case 4 -> "Excelente";
            case 5 -> "Obra-prima";
            default -> "Todas";
        };
    }

    private void buscarProximoLote() {
        if (loteAtual * TAM_LOTE >= todosIds.size()) return;

        progresso.setVisible(true);
        btnProxima.setDisable(true);

        int inicio = loteAtual * TAM_LOTE;
        List<String> lote = todosIds.subList(inicio, Math.min(inicio + TAM_LOTE, todosIds.size()));
        loteAtual++;

        Task<List<LinhaFlip>> tarefa = new Task<>() {
            @Override
            protected List<LinhaFlip> call() throws Exception {
                String idsStr = String.join(",", lote);
                String qualidadeParam = qualidade > 0 ? String.valueOf(qualidade) : "1,2,3,4,5";
                String url = API_BASE + "/" + idsStr
                        + ".json?locations=" + CIDADES_API
                        + "&qualities=" + qualidadeParam;

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .GET().build();

                HttpResponse<String> resp = cliente.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200)
                    throw new Exception("erro http: " + resp.statusCode());

                List<JsonObject> registros = new ArrayList<>();
                JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
                for (JsonElement el : arr) registros.add(el.getAsJsonObject());

                return calcularOportunidades(registros);
            }
        };

        tarefa.setOnSucceeded(e -> {
            todasLinhas.addAll(tarefa.getValue());
            todasLinhas.sort((a, b) -> Long.compare(b.lucroBruto, a.lucroBruto));
            totalPaginas = Math.max(1, (int) Math.ceil(todasLinhas.size() / (double) POR_PAGINA));
            exibirPagina(paginaAtual);
            progresso.setVisible(false);
            labelStatus.setText(todasLinhas.size() + " oportunidades encontradas");
            boolean temMaisLotes = loteAtual * TAM_LOTE < todosIds.size();
            btnProxima.setDisable(!temMaisLotes && paginaAtual >= totalPaginas);
        });

        tarefa.setOnFailed(e -> {
            progresso.setVisible(false);
            labelStatus.setText("Erro: " + tarefa.getException().getMessage());
            btnProxima.setDisable(false);
        });

        new Thread(tarefa, "thread-flip").start();
    }

}