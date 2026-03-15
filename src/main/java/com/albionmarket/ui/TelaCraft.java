package com.albionmarket.ui;

import com.albionmarket.model.*;
import com.albionmarket.model.EstadoCraftSelecao;
import com.albionmarket.service.ApiService;
import com.albionmarket.service.BancoDeDadosCraft;
import com.albionmarket.service.CraftService;
import com.albionmarket.service.ItemValues;
import com.albionmarket.util.FormatadorUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Tela de craft: preços do item, receita com preços dos materiais e calculadora.
 */
public class TelaCraft {

    // contexto
    private final Stage palco;
    private final ItemDefinition item;
    private final int tier;
    private final int enchant;
    private final String itemIdCompleto;

    // serviços
    private final ApiService apiService = new ApiService();
    private final CraftService craftService = new CraftService();

    // controles da lateral
    private final List<CheckBox> checksCidades = new ArrayList<>();
    private Label labelStatus;
    private ProgressIndicator progresso;

    // campos de cálculo
    private TextField campoQuantidade;
    private TextField campoRetorno;
    private TextField campoTaxaMercado;
    private TextField campoTaxaBarraca;
    private Label labelItemValue;

    // tabelas
    private TableView<LinhaPreco> tabelaPrecos;
    private TableView<LinhaMaterial> tabelaReceita;
    private TableView<LinhaMaterialPreco> tabelaMateriais;
    private TableView<LinhaCalculo> tabelaCalculo;

    // dados
    private ReceitaCraft receitaAtual;
    private long itemValue = 0; // puxado da gameinfo API para cálculo de nutrição

    // toggle de edição manual de preços
    private boolean modoEdicaoManual = false;

    // modelo tabela de preços do item
    public static class LinhaPreco {
        public final String itemId, qualidade, cidade, corCidade;
        public final String sellMin, atualizado;
        // qtd de cada recurso/artefato separado por tipo
        public final int qtdRecurso1, qtdRecurso2, qtdRecurso3, qtdArtefatos;

        public LinhaPreco(String itemId, String qualidade, String cidade,
                          String corCidade, String sellMin, String atualizado,
                          int qtdRecurso1, int qtdRecurso2, int qtdRecurso3, int qtdArtefatos) {
            this.itemId = itemId;
            this.qualidade = qualidade;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.sellMin = sellMin;
            this.atualizado = atualizado;
            this.qtdRecurso1 = qtdRecurso1;
            this.qtdRecurso2 = qtdRecurso2;
            this.qtdRecurso3 = qtdRecurso3;
            this.qtdArtefatos = qtdArtefatos;
        }
    }


    //criador do json pra salvar as informações da operacao
    private void salvarOperacao() {
        try {
            int t = (tier == -1) ? 4 : tier;
            int e = (enchant == -1) ? 0 : enchant;

            // monta JSON manualmente (sem dependência externa)
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"item\": \"").append(item.getNome().replace("\"", "\\\"")).append("\",\n");
            sb.append("  \"itemId\": \"").append(itemIdCompleto).append("\",\n");
            sb.append("  \"tier\": ").append(t).append(",\n");
            sb.append("  \"encantamento\": ").append(e).append(",\n");
            sb.append("  \"parametros\": {\n");
            sb.append("    \"quantidade\": \"").append(campoQuantidade.getText()).append("\",\n");
            sb.append("    \"taxaRetorno\": \"").append(campoRetorno.getText()).append("\",\n");
            sb.append("    \"taxaMercado\": \"").append(campoTaxaMercado.getText()).append("\",\n");
            sb.append("    \"taxaBarraca\": \"").append(campoTaxaBarraca.getText()).append("\",\n");
            sb.append("    \"itemValue\": \"").append(labelItemValue.getText()).append("\"\n");
            sb.append("  },\n");


            double precoVendaSalvar = tabelaPrecos.getItems().stream()
                    .mapToDouble(l -> parseSilver(l.sellMin)).max().orElse(0.0);
            double custoMatSalvar = 0;
            if (tabelaMateriais != null && !tabelaMateriais.getItems().isEmpty()) {
                for (LinhaMaterialPreco lm : tabelaMateriais.getItems())
                    custoMatSalvar += parseSilver(lm.buyMax) * lm.qtdNecessaria;
            } else if (tabelaReceita != null) {
                for (LinhaMaterial lm : tabelaReceita.getItems())
                    custoMatSalvar += parseSilver(lm.buyMax) * lm.qtd;
            }
            double qtdSalvar     = parseDoubleSafe(campoQuantidade, 1.0);
            double retornoSalvar = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
            double mercadoSalvar = parseDoubleSafe(campoTaxaMercado, 3.0) / 100.0;
            double barracaSalvar = parseDoubleSafe(campoTaxaBarraca, 3.0);
            double qtdFinalSalvar   = qtdSalvar / (1.0 - retornoSalvar);
            double nutricaoSalvar   = (itemValue * qtdFinalSalvar) * 0.1125;
            double taxaCraftSalvar  = (barracaSalvar * nutricaoSalvar) / 100.0;
            double custoTotalSalvar = (custoMatSalvar * qtdSalvar) + taxaCraftSalvar;
            double receitaSalvar    = qtdFinalSalvar * precoVendaSalvar;
            double taxaMercSalvar   = receitaSalvar * mercadoSalvar;
            double lucroSalvar      = receitaSalvar - custoTotalSalvar - taxaMercSalvar;

            String[] melhorCidadeHolder = {"—"};
            double[] melhorVHolder = {0};
            for (LinhaPreco lp : tabelaPrecos.getItems()) {
                double v = parseSilver(lp.sellMin);
                if (v > melhorVHolder[0]) { melhorVHolder[0] = v; melhorCidadeHolder[0] = lp.cidade; }
            }
            String nomeCidadeVendaSalvar = BancoDeDadosCraft.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(melhorCidadeHolder[0]))
                    .map(CidadeInfo::getNome).findFirst().orElse(melhorCidadeHolder[0]);


            sb.append("  \"calculadora\": {\n");
            sb.append("    \"Quantidade a craftar\": \"").append(fmt(qtdSalvar)).append(" un\",\n");
            sb.append("    \"Qtd final craftada\": \"").append(String.format("%.2f un", qtdFinalSalvar)).append("\",\n");
            sb.append("    \"Melhor preco de venda\": \"").append(fmtSilver(precoVendaSalvar)).append("\",\n");
            sb.append("    \"Local\": \"").append(nomeCidadeVendaSalvar).append("\",\n");
            sb.append("    \"Custo dos materiais\": \"").append(fmtSilver(custoMatSalvar * qtdSalvar)).append("\",\n");
            sb.append("    \"Local de compra dos materiais\": \"").append(melhorCidadeMateriais()).append("\",\n");
            sb.append("    \"Custo total\": \"").append(fmtSilver(custoTotalSalvar)).append("\",\n");
            sb.append("    \"Lucro/Prejuizo\": \"").append(lucroSalvar >= 0 ? "+" : "").append(fmtSilver(lucroSalvar)).append("\"\n");
            sb.append("  }\n");

            sb.append("}\n");

            // salva em arquivo com timestamp
            String nomeArquivo = "operacao_"
                    + itemIdCompleto + "_"
                    + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".json";
            java.nio.file.Path caminho = java.nio.file.Paths.get(nomeArquivo);
            java.nio.file.Files.writeString(caminho, sb.toString());

            labelStatus.setText("Operação salva: " + nomeArquivo);

        } catch (Exception ex) {
            labelStatus.setText("Erro ao salvar: " + ex.getMessage());
        }
    }

    // modelo tabela de receita (materiais)
    public static class LinhaMaterial {
        public final String iconeUrl, nome, tipo, cidade, corCidade;
        public final String buyMax, atualizado;
        public final int qtd;

        public LinhaMaterial(String iconeUrl, String nome, String tipo,
                             int qtd, String cidade, String corCidade,
                             String buyMax, String atualizado) {
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

    // estado dos filtros da tela anterior (para restaurar ao clicar Voltar)
    private final EstadoCraftSelecao estadoSelecao;

    // modelo da tabela de precos dos materiais
    public static class LinhaMaterialPreco {
        public final String nome, cidade, corCidade, buyMax, atualizado;
        public final int qtdNecessaria;

        public LinhaMaterialPreco(String nome, int qtdNecessaria,
                                  String cidade, String corCidade,
                                  String buyMax, String atualizado) {
            this.nome = nome;
            this.qtdNecessaria = qtdNecessaria;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.buyMax = buyMax;
            this.atualizado = atualizado;
        }
    }


    // modelo da tabela de cálculo
    public static class LinhaCalculo {
        public final String nomeColuna, valor;

        public LinhaCalculo(String nomeColuna, String valor) {
            this.nomeColuna = nomeColuna;
            this.valor = valor;
        }
    }

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant) {
        this(palco, item, tier, enchant, null);
    }

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant,
                     EstadoCraftSelecao estadoSelecao) {
        this.palco = palco;
        this.item = item;
        this.tier = tier;
        this.enchant = enchant;
        this.estadoSelecao = estadoSelecao;
        int t = (tier == -1) ? 4 : tier;
        int e = (enchant == -1) ? 0 : enchant;
        String base = "T" + t + "_" + item.getId();
        this.itemIdCompleto = e > 0 ? base + "@" + e : base;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setLeft(criarLateral());
        raiz.setCenter(criarAreaCentral());

        Scene cena = new Scene(raiz, 1280, 800);
        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );

        palco.setTitle("Albion Market — Craft: " + item.getNome());
        palco.setScene(cena);
        palco.setResizable(true);
        palco.setMinWidth(900);
        palco.setMinHeight(600);
        palco.setMaximized(true);

        buscarTudo();
    }

    // cabeçalho
    private HBox criarCabecalho() {
        Label titulo = new Label("Albion Market");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Calculadora de Craft — " + item.getNome());
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);
        HBox cab = new HBox(textos);
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

        // ícone
        ImageView icone = new ImageView();
        icone.setFitWidth(160);
        icone.setFitHeight(100);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);
        icone.setImage(new Image(
                "https://render.albiononline.com/v1/item/" + itemIdCompleto + ".png", true));

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

        // parâmetros
        painel.getChildren().add(secao("Parâmetros de Craft"));
        campoQuantidade = campoCraft("1");
        campoRetorno = campoCraft("15.2");
        campoTaxaMercado = campoCraft("3.0");

        campoTaxaBarraca = campoCraft("3.0");
        labelItemValue = new Label("—");
        labelItemValue.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 12px; -fx-font-weight: bold;");

        painel.getChildren().addAll(
                label("Quantidade a craftar"), campoQuantidade,
                label("Taxa de retorno (%)"), campoRetorno,
                //label("Taxa do mercado (%)"), campoTaxaMercado,
                label("Taxa da barraca (%)"), campoTaxaBarraca
                //label("Valor do item"), labelItemValue
        );
        painel.getChildren().add(separador());

        // switch liga/desliga visual
        javafx.scene.canvas.Canvas canvasSwitch = new javafx.scene.canvas.Canvas(44, 22);
        final boolean[] estadoSwitch = {false};

        Runnable desenharSwitch = () -> {
            javafx.scene.canvas.GraphicsContext gc = canvasSwitch.getGraphicsContext2D();
            gc.clearRect(0, 0, 44, 22);
            // fundo pill
            gc.setFill(estadoSwitch[0]
                    ? javafx.scene.paint.Color.web("#5a8dee")
                    : javafx.scene.paint.Color.web("#555"));
            gc.fillRoundRect(0, 0, 44, 22, 22, 22);
            // bolinha
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

        // botão atualizar
        Button btnAtualizar = new Button("Atualizar Valores");
        btnAtualizar.setMaxWidth(Double.MAX_VALUE);
        btnAtualizar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 0;");
        btnAtualizar.setOnAction(ev -> buscarTudo());

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setMaxWidth(Double.MAX_VALUE);
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setOnAction(ev -> {
            if (estadoSelecao != null) {
                new TelaCraftSelecao(palco, estadoSelecao).mostrar();
            } else {
                new TelaCraftSelecao(palco).mostrar();
            }
        });


        //botao pra iniciar a operacao e salvar o json
        Button btnIniciarOperacao = new Button("Iniciar Operação");
        btnIniciarOperacao.setMaxWidth(Double.MAX_VALUE);
        btnIniciarOperacao.setStyle(
                "-fx-background-color: #3dba6e; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 0;");
        btnIniciarOperacao.setOnAction(ev -> {
            salvarOperacao();
            btnIniciarOperacao.setDisable(true);
            btnIniciarOperacao.setText("Operação Iniciada");
        });

        painel.getChildren().addAll(btnAtualizar, espaco, btnIniciarOperacao, btnVoltar);

        ScrollPane scroll = new ScrollPane(painel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #252525; -fx-background-color: #252525;");
        return scroll;
    }

    // area central
    private VBox criarAreaCentral() {
        // tabela de preços do item
        Label tituloPrecos = new Label("Preços no Mercado");
        tituloPrecos.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        tabelaPrecos = new TableView<>();
        tabelaPrecos.setStyle("-fx-background-color: #1e1e1e;");
        tabelaPrecos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaPrecos.setPlaceholder(new Label("Buscando preços..."));
        tabelaPrecos.setPrefHeight(220);

        TableColumn<LinhaPreco, String> colQual = coluna("Qualidade", 110,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().qualidade));
        TableColumn<LinhaPreco, String> colCidadePreco = criarColunaCidade(true);
        TableColumn<LinhaPreco, String> colSell = criarColunaPreco("Preço de Venda", 130);


        TableColumn<LinhaPreco, String> colDataPreco = coluna("Última Atualização", 100,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        // colunas dinâmicas de recursos/artefatos adicionadas em atualizarColunasRecursos()
        tabelaPrecos.getColumns().addAll(colQual, colCidadePreco, colSell, colDataPreco);


        // tabela de receita (materiais)
        Label tituloReceita = new Label("Receita de Craft");
        tituloReceita.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloReceita.setPadding(new Insets(12, 0, 6, 0));

        tabelaReceita = new TableView<>();
        tabelaReceita.setStyle("-fx-background-color: #1e1e1e;");
        tabelaReceita.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaReceita.setPlaceholder(new Label("Carregando receita..."));

        // coluna ícone
        TableColumn<LinhaMaterial, String> colIcone = new TableColumn<>("  ");
        colIcone.setPrefWidth(70);
        colIcone.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().iconeUrl));
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
                iv.setImage(new Image(url, true));
                setGraphic(iv);
            }
        });

        TableColumn<LinhaMaterial, String> colNomeMat = coluna("Material", 180,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().nome));

        TableColumn<LinhaMaterial, String> colTipoMat = new TableColumn<>("Tipo");
        colTipoMat.setPrefWidth(80);
        colTipoMat.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().tipo));
        colTipoMat.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    return;
                }
                setText(v);
                setStyle(v.equals("Artefato")
                        ? "-fx-text-fill: #9b59b6; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e0b84a;");
            }
        });

        // qtd para 1 item
        TableColumn<LinhaMaterial, String> colQtd1 = new TableColumn<>("Qtd p/ 1 item");
        colQtd1.setPrefWidth(100);
        colQtd1.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getValue().qtd)));
        colQtd1.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        TableColumn<LinhaMaterial, String> colCidadeMat = criarColunaCidadeMaterial();

        TableColumn<LinhaMaterial, String> colBuyMat = new TableColumn<>("Preço de Compra");
        colBuyMat.setPrefWidth(130);
        colBuyMat.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().buyMax));
        colBuyMat.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—");
                    setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(v);
                    setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        TableColumn<LinhaMaterial, String> colDataMat = colunaMat("Última Atualização", 110,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaReceita.getColumns().addAll(
                colIcone, colNomeMat, colTipoMat, colQtd1);

        // tabela de cálculo
        Label tituloCalculo = new Label("Calculadora de Lucro");
        tituloCalculo.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloCalculo.setPadding(new Insets(12, 0, 6, 0));

        // a tabelaCalculo terá 1 linha e N colunas (uma por métrica)
        // as colunas são criadas dinamicamente em atualizarTabelaCalculo()
        tabelaCalculo = new TableView<>();
        tabelaCalculo.setStyle("-fx-background-color: #1e1e1e;");
        tabelaCalculo.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabelaCalculo.setPlaceholder(new Label("Aguardando dados..."));
        tabelaCalculo.setPrefHeight(90);
        tabelaCalculo.setMaxHeight(90);
        tabelaCalculo.setFixedCellSize(45);

        // tabela de precos dos materiais
        Label tituloMateriais = new Label("Precos dos Materiais");
        tituloMateriais.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloMateriais.setPadding(new Insets(12, 0, 6, 0));

        tabelaMateriais = new TableView<>();
        tabelaMateriais.setStyle("-fx-background-color: #1e1e1e;");
        tabelaMateriais.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaMateriais.setPlaceholder(new Label("Carregando..."));

        TableColumn<LinhaMaterialPreco, String> colMatNome = coluna("Material", 200,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().nome));

        TableColumn<LinhaMaterialPreco, String> colMatQtd = new TableColumn<>("Qtd necessaria");
        colMatQtd.setPrefWidth(120);
        colMatQtd.setCellValueFactory(r -> {
            int q = parseIntSafe(campoQuantidade, 1);
            return new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(r.getValue().qtdNecessaria * q));
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
        colMatBuy.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().buyMax));
        colMatBuy.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—");
                    setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(v);
                    setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        TableColumn<LinhaMaterialPreco, String> colMatCidade = new TableColumn<>("Local");
        colMatCidade.setPrefWidth(130);
        colMatCidade.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        colMatCidade.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—");
                    setGraphic(null);
                    return;
                }
                LinhaMaterialPreco linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));
                String nome = BancoDeDadosCraft.CIDADES.stream()
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

        tabelaMateriais.getColumns().addAll(
                colMatNome, colMatQtd, colMatBuy, colMatCidade, colMatData);

        VBox area = new VBox(10,
                tituloPrecos, tabelaPrecos,
                tituloReceita, tabelaReceita,
                tituloMateriais, tabelaMateriais,
                tituloCalculo, tabelaCalculo
        );
        area.setPadding(new Insets(16));
        area.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(tabelaPrecos, Priority.SOMETIMES);
        VBox.setVgrow(tabelaReceita, Priority.SOMETIMES);
        VBox.setVgrow(tabelaMateriais, Priority.SOMETIMES);
        VBox.setVgrow(tabelaCalculo, Priority.ALWAYS);
        return area;
    }

    // logica principal
    private void buscarTudo() {
        List<String> cidades = (estadoSelecao != null
                && estadoSelecao.cidades != null
                && !estadoSelecao.cidades.isEmpty())
                ? estadoSelecao.cidades
                : BancoDeDadosCraft.CIDADES.stream()
                .map(CidadeInfo::getApiId)
                .collect(Collectors.toList());

        progresso.setVisible(true);
        labelStatus.setText("Buscando preços e receita...");
        tabelaPrecos.setItems(FXCollections.emptyObservableList());
        tabelaReceita.setItems(FXCollections.emptyObservableList());
        if (tabelaMateriais != null) tabelaMateriais.setItems(FXCollections.emptyObservableList());

        Task<Void> tarefa = new Task<>() {
            private List<PriceEntry> precos;
            private ReceitaCraft receita;
            private List<PriceEntry> precosMateirais;

            @Override
            protected Void call() throws Exception {
                // 1. preços do item
                precos = apiService.buscarPrecos(item.getId(),
                        (tier == -1) ? 4 : tier,
                        (enchant == -1) ? 0 : enchant,
                        -1, cidades);

                // 2. receita
                receita = craftService.buscarReceita(itemIdCompleto);
                // itemValue vem da classe local, nao da api
                itemValue = ItemValues.getValor(itemIdCompleto);

                // 3. preços dos materiais (se receita encontrada)
                if (receita != null && !receita.getMateriais().isEmpty()) {
                    List<String> idsMat = receita.getMateriais().stream()
                            .map(ReceitaCraft.MaterialCraft::getUniqueName)
                            .collect(Collectors.toList());
                    // busca cada material sem tier/enchant pois já vem completo
                    precosMateirais = new ArrayList<>();
                    for (String idMat : idsMat) {
                        try {
                            // extrai sufixo e tier do id completo (ex: T4_PLANKS)
                            String[] partes = idMat.split("_", 2);
                            int tMat = (partes[0].startsWith("T") && partes[0].length() == 2)
                                    ? Integer.parseInt(partes[0].substring(1)) : 4;
                            String sufixo = partes.length > 1 ? partes[1] : idMat;
                            precosMateirais.addAll(
                                    apiService.buscarPrecos(sufixo, tMat, 0, -1, cidades));
                        } catch (Exception ex) {
                            // ignora material com erro
                        }
                    }
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
                // exibe itemValue no label
                labelItemValue.setText(itemValue > 0
                        ? String.format("%,d", itemValue)
                        : "nao cadastrado");
                progresso.setVisible(false);
                labelStatus.setText("Dados atualizados.");
            }

            @Override
            protected void failed() {
                progresso.setVisible(false);
                labelStatus.setText("Erro: " + getException().getMessage());
            }
        };

        new Thread(tarefa, "thread-craft").start();
    }

    /**
     * monta colunas dinâmicas de recursos/artefatos conforme a receita atual.
     */
    private void atualizarColunasRecursos() {
        tabelaPrecos.getColumns().removeIf(c ->
                c.getText().startsWith("Qtd Recurso") || c.getText().equals("Qtd Artefatos"));

        if (receitaAtual == null) return;

        List<ReceitaCraft.MaterialCraft> recursos = receitaAtual.getMateriais().stream()
                .filter(m -> !m.isArtefato()).collect(Collectors.toList());
        List<ReceitaCraft.MaterialCraft> artefatos = receitaAtual.getMateriais().stream()
                .filter(ReceitaCraft.MaterialCraft::isArtefato).collect(Collectors.toList());

        int pos = tabelaPrecos.getColumns().size() - 1;
        String[] nomes = {"Qtd Recurso 1", "Qtd Recurso 2", "Qtd Recurso 3"};

        for (int i = 0; i < Math.min(recursos.size(), 3); i++) {
            final int qtdBase = recursos.get(i).getCount();
            TableColumn<LinhaPreco, String> col = new TableColumn<>(nomes[i]);
            col.setPrefWidth(115);
            col.setCellValueFactory(r -> {
                int q = parseIntSafe(campoQuantidade, 1);
                return new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(qtdBase * q));
            });
            col.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : v);
                    setStyle("-fx-text-fill: #e0b84a; -fx-alignment: CENTER;");
                }
            });
            tabelaPrecos.getColumns().add(pos + i, col);
        }

        if (!artefatos.isEmpty()) {
            final int qtdArtBase = artefatos.stream()
                    .mapToInt(ReceitaCraft.MaterialCraft::getCount).sum();
            TableColumn<LinhaPreco, String> colArt = new TableColumn<>("Qtd Artefatos");
            colArt.setPrefWidth(110);
            colArt.setCellValueFactory(r -> {
                int q = parseIntSafe(campoQuantidade, 1);
                return new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(qtdArtBase * q));
            });
            colArt.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : v);
                    setStyle("-fx-text-fill: #9b59b6; -fx-alignment: CENTER;");
                }
            });
            tabelaPrecos.getColumns().add(pos + recursos.size(), colArt);
        }
    }

    private void atualizarTabelaPrecos(List<PriceEntry> entradas) {
        int r1 = 0, r2 = 0, r3 = 0, art = 0;
        if (receitaAtual != null) {
            List<ReceitaCraft.MaterialCraft> recursos = receitaAtual.getMateriais().stream()
                    .filter(m -> !m.isArtefato()).collect(Collectors.toList());
            if (recursos.size() > 0) r1 = recursos.get(0).getCount();
            if (recursos.size() > 1) r2 = recursos.get(1).getCount();
            if (recursos.size() > 2) r3 = recursos.get(2).getCount();
            art = receitaAtual.getMateriais().stream()
                    .filter(ReceitaCraft.MaterialCraft::isArtefato)
                    .mapToInt(ReceitaCraft.MaterialCraft::getCount).sum();
        }
        final int fr1 = r1, fr2 = r2, fr3 = r3, fart = art;

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
            String corCidade = BancoDeDadosCraft.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888");

            linhas.add(new LinhaPreco(
                    pe.getItemId(),
                    FormatadorUtil.nomeQualidade(pe.getQualidade()),
                    pe.getCidade(), corCidade,
                    FormatadorUtil.formatarPreco(pe.getSellMin()),
                    FormatadorUtil.formatarData(
                            (pe.getSellDate() != null && !pe.getSellDate().startsWith("0001"))
                                    ? pe.getSellDate() : pe.getBuyDate()),
                    fr1, fr2, fr3, fart
            ));
        }
        linhas.sort(Comparator.comparing(l -> l.cidade));

        tabelaPrecos.setItems(FXCollections.observableArrayList(linhas));
        // ajusta altura para não ter linhas vazias
        double altPrecos = 28.0 + (linhas.size() * 40.0);
        tabelaPrecos.setPrefHeight(altPrecos);
        tabelaPrecos.setMaxHeight(altPrecos);
    }

    private void atualizarTabelaReceita(ReceitaCraft receita,
                                        List<PriceEntry> precosMateirais) {
        if (receita == null) {
            tabelaReceita.setPlaceholder(
                    new Label("Receita não disponível para este item."));
            return;
        }

        // indexa melhor preço de compra por itemId
        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        if (precosMateirais != null) {
            for (PriceEntry pe : precosMateirais) {
                String chave = pe.getItemId();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null
                        || (pe.getBuyMax() > 0
                        && (atual.getBuyMax() == 0 || pe.getBuyMax() > atual.getBuyMax()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }

        List<LinhaMaterial> linhas = new ArrayList<>();
        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();

            int enchantAtualR = (enchant == -1) ? 0 : enchant;
            String iconeUrl = "https://render.albiononline.com/v1/item/" + idMat + ".png";

            String sufixoMat = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T'
                    && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;

            String nomeRecurso = BancoDeDadosCraft.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : BancoDeDadosCraft.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome)
                    .findFirst().orElse(idMat);
            String nomeMat2 = enchantAtualR > 0 ? nomeMat + " ." + enchantAtualR : nomeMat;

            String tipo = mat.isArtefato() ? "Artefato" : "Recurso";

            // melhor preço de compra
            PriceEntry pe = melhorCompra.get(idMat);
            String buyMax = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "—";
            String cidade = pe != null ? pe.getCidade() : "—";
            String corCidade = pe != null
                    ? BancoDeDadosCraft.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888")
                    : "#888";
            String data = pe != null
                    ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001"))
                            ? pe.getBuyDate() : pe.getSellDate())
                    : "—";

            linhas.add(new LinhaMaterial(
                    iconeUrl, nomeMat2, tipo, mat.getCount(),
                    cidade, corCidade, buyMax, data));
        }

        tabelaReceita.setItems(FXCollections.observableArrayList(linhas));
        // ajusta altura para mostrar só as linhas necessárias (sem espaço vazio)
        double alturaLinha = 40.0;
        double alturaHeader = 28.0;
        double alturaCalculada = alturaHeader + (linhas.size() * alturaLinha);
        tabelaReceita.setPrefHeight(alturaCalculada);
        tabelaReceita.setMaxHeight(alturaCalculada);
    }

    // edição manual de preços
    // popula a tabela de precos dos materiais
    private void atualizarTabelaMateriais(ReceitaCraft receita, List<PriceEntry> precosMateirais) {
        if (tabelaMateriais == null || receita == null) return;

        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        if (precosMateirais != null) {
            for (PriceEntry pe : precosMateirais) {
                String chave = pe.getItemId();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null
                        || (pe.getBuyMax() > 0
                        && (atual.getBuyMax() == 0 || pe.getBuyMax() > atual.getBuyMax()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }

        int enchantAtual = (enchant == -1) ? 0 : enchant;
        List<LinhaMaterialPreco> linhas = new ArrayList<>();

        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            String sufixoMat = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T'
                    && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;

            String nomeRecurso = BancoDeDadosCraft.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : BancoDeDadosCraft.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome)
                    .findFirst().orElse(idMat);

            String nomeExibir = enchantAtual > 0 ? nomeMat + " ." + enchantAtual : nomeMat;

            PriceEntry pe = melhorCompra.get(idMat);
            String buyMax = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "—";
            String cidade = pe != null ? pe.getCidade() : "—";
            String corCidade = pe != null
                    ? BancoDeDadosCraft.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888")
                    : "#888";
            String data = pe != null
                    ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001"))
                            ? pe.getBuyDate() : pe.getSellDate())
                    : "—";

            linhas.add(new LinhaMaterialPreco(nomeExibir, mat.getCount(),
                    cidade, corCidade, buyMax, data));
        }

        tabelaMateriais.setItems(FXCollections.observableArrayList(linhas));
        double alt = 28.0 + (linhas.size() * 40.0);
        tabelaMateriais.setPrefHeight(alt);
        tabelaMateriais.setMaxHeight(alt);
    }

    private void ativarEdicaoManual(boolean ativo) {
        // tabela de preços: coluna "Preço de Venda" editável
        for (TableColumn<LinhaPreco, ?> col : tabelaPrecos.getColumns()) {
            if (col.getText().equals("Preço de Venda")) {
                @SuppressWarnings("unchecked")
                TableColumn<LinhaPreco, String> colStr = (TableColumn<LinhaPreco, String>) col;
                if (ativo) {
                    colStr.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
                    colStr.setOnEditCommit(ev -> {
                        // cria nova LinhaPreco com preço editado
                        LinhaPreco antiga = ev.getRowValue();
                        int idx = tabelaPrecos.getItems().indexOf(antiga);
                        tabelaPrecos.getItems().set(idx, new LinhaPreco(
                                antiga.itemId, antiga.qualidade, antiga.cidade, antiga.corCidade,
                                ev.getNewValue(), antiga.atualizado,
                                antiga.qtdRecurso1, antiga.qtdRecurso2, antiga.qtdRecurso3, antiga.qtdArtefatos
                        ));
                        atualizarTabelaCalculo();
                    });
                } else {
                    colStr.setCellFactory(tc -> new TableCell<>() {
                        @Override
                        protected void updateItem(String v, boolean empty) {
                            super.updateItem(v, empty);
                            if (empty || v == null || v.equals("—")) {
                                setText("—");
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

        // tabela de receita: coluna "Preço de Compra" editável
// tabela de materiais: coluna "Preco de Compra" editável
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
                                    antiga.nome, antiga.qtdNecessaria,
                                    antiga.cidade, antiga.corCidade,
                                    ev.getNewValue(), antiga.atualizado
                            ));
                            atualizarTabelaCalculo();
                        });
                    } else {
                        colStr.setCellFactory(tc -> new TableCell<>() {
                            @Override
                            protected void updateItem(String v, boolean empty) {
                                super.updateItem(v, empty);
                                if (empty || v == null || v.equals("—")) {
                                    setText("—");
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

    // calculadora de Lucro

    /**
     * reconstrói as colunas da tabelaCalculo (1 coluna por métrica, 1 linha de valores).
     */
    @SuppressWarnings("unchecked")
    private void atualizarTabelaCalculo() {
        if (tabelaCalculo == null) return;

        // preço de venda mais caro entre todos os itens da tabela
        double precoVenda = tabelaPrecos.getItems().stream()
                .mapToDouble(l -> parseSilver(l.sellMin))
                .max().orElse(0.0);

        double custoMateriais = 0;
        if (tabelaMateriais != null && !tabelaMateriais.getItems().isEmpty()) {
            for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
                custoMateriais += parseSilver(lm.buyMax) * lm.qtdNecessaria;
            }
        } else if (tabelaReceita != null) {
            for (LinhaMaterial lm : tabelaReceita.getItems()) {
                custoMateriais += parseSilver(lm.buyMax) * lm.qtd;
            }
        }

        double qtdProduzir = parseDoubleSafe(campoQuantidade, 1.0);
        double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
        double taxaMercado = parseDoubleSafe(campoTaxaMercado, 3.0) / 100.0;
        double taxaBarraca = parseDoubleSafe(campoTaxaBarraca, 3.0);
        long iv = itemValue; // game value puxado da API

        double qtdFinal = qtdProduzir / (1.0 - taxaRetorno);
        double nutricao = (iv * qtdFinal) * 0.1125;
        double taxaCraftTotal = (taxaBarraca * nutricao) / 100.0;
        double custoTotal = (custoMateriais * qtdProduzir) + taxaCraftTotal;
        double receitaTotal = qtdFinal * precoVenda;
        double taxaMercadoValor = receitaTotal * taxaMercado;
        double lucro = receitaTotal - custoTotal - taxaMercadoValor;

        // pega melhor preco de venda e seu local
        String melhorCidadeTemp = "—";
        double melhorVenda = 0;
        for (LinhaPreco lp : tabelaPrecos.getItems()) {
            double v = parseSilver(lp.sellMin);
            if (v > melhorVenda) {
                melhorVenda = v;
                melhorCidadeTemp = lp.cidade;
            }
        }
        final String melhorCidade = melhorCidadeTemp;
        String nomeMelhorCidade = BancoDeDadosCraft.CIDADES.stream()
                .filter(c -> c.getApiId().equals(melhorCidade))
                .map(CidadeInfo::getNome).findFirst().orElse(melhorCidade);

        // recursos e artefatos para colunas dinamicas
        List<ReceitaCraft.MaterialCraft> recursosCalc = receitaAtual == null
                ? new ArrayList<>()
                : receitaAtual.getMateriais().stream()
                .filter(m -> !m.isArtefato()).collect(Collectors.toList());
        List<ReceitaCraft.MaterialCraft> artefatosCalc = receitaAtual == null
                ? new ArrayList<>()
                : receitaAtual.getMateriais().stream()
                .filter(ReceitaCraft.MaterialCraft::isArtefato).collect(Collectors.toList());

        // monta lista de metricas
        List<String[]> listaMetricas = new ArrayList<>(Arrays.asList(
                new String[]{"Quantidade a craftar", fmt(qtdProduzir) + " un"},
                new String[]{"Qtd final craftada", String.format("%.2f un", qtdFinal)},
                new String[]{"Melhor preco de venda", fmtSilver(melhorVenda)},
                new String[]{"Local", nomeMelhorCidade},
                new String[]{"Custo dos materiais", fmtSilver(custoMateriais * qtdProduzir)},
                new String[]{"Local de compra", melhorCidadeMateriais()},
                // eu não preciso ver a nutrição
                // new String[]{"Nutricao", fmtSilver(nutricao)},
                new String[]{"Taxa da barraca", fmtSilver(taxaCraftTotal)},
                new String[]{"Custo total", fmtSilver(custoTotal)},
                new String[]{"Receita total", fmtSilver(receitaTotal)},
                //new String[]{"Taxa do mercado", "- " + fmtSilver(taxaMercadoValor)},
                new String[]{"Lucro/Prejuizo", (lucro >= 0 ? "+" : "") + fmtSilver(lucro)}
        ));

        // insere qtd de recursos e artefatos antes de nutricao
        String[] nomesRecCalc = {"Qtd Recurso 1", "Qtd Recurso 2", "Qtd Recurso 3"};
        for (int ri = 0; ri < Math.min(recursosCalc.size(), 3); ri++) {
            int qtdR = recursosCalc.get(ri).getCount() * (int) qtdProduzir;
            listaMetricas.add(listaMetricas.size() - 5, new String[]{nomesRecCalc[ri], String.valueOf(qtdR)});
        }
        if (!artefatosCalc.isEmpty()) {
            int qtdArt = artefatosCalc.stream().mapToInt(ReceitaCraft.MaterialCraft::getCount).sum()
                    * (int) qtdProduzir;
            listaMetricas.add(listaMetricas.size() - 5, new String[]{"Qtd Artefatos", String.valueOf(qtdArt)});
        }
        String[][] metricas = listaMetricas.toArray(new String[0][]);

        // reconstroi colunas a cada chamada
        tabelaCalculo.getColumns().clear();

        // modelo: Map<String, String> onde key = título da coluna
        // usa LinhaCalculo com nomeColuna=título, valor=valor
        // mas a tabela tem 1 linha e N colunas, então usa
        // List<Map<String,String>> com 1 elemento
        // Para isso, reutiliza LinhaCalculo
        // cada coluna exibe o valor fixo da métrica correspondente

        for (String[] metrica : metricas) {
            final String titulo = metrica[0];
            final String valor = metrica[1];

            TableColumn<LinhaCalculo, String> col = new TableColumn<>(titulo);
            //largura das colunas
            col.setMinWidth(140);
            col.setPrefWidth(USE_COMPUTED_SIZE_CALC);
            col.setSortable(false);
            col.setCellValueFactory(r ->
                    new javafx.beans.property.SimpleStringProperty(r.getValue().valor));
            col.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    setText(valor);
                    // lucro verde, prejuízo vermelho
                    if (titulo.equals("Lucro/Prejuízo")) {
                        setStyle(lucro >= 0
                                ? "-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER;"
                                : "-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER;");
                    } else if (titulo.equals("Custo total") || titulo.equals("Taxa da barraca")) {
                        setStyle("-fx-text-fill: #e05555; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    } else if (titulo.equals("Receita total")) {
                        setStyle("-fx-text-fill: #3dba6e; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    }
                }
            });
            tabelaCalculo.getColumns().add(col);
        }

        // 1 linha dummy para renderizar as células
        tabelaCalculo.setItems(FXCollections.observableArrayList(
                new LinhaCalculo("", "")
        ));
    }

    private static final double USE_COMPUTED_SIZE_CALC = Region.USE_COMPUTED_SIZE;

    private double parseSilver(String val) {
        if (val == null || val.equals("—")) return 0;
        try {
            return Double.parseDouble(val.replace(".", "").replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(TextField campo, double padrao) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (Exception e) {
            return padrao;
        }
    }

    private long parseLongSafe(TextField campo, long padrao) {
        try {
            return Long.parseLong(campo.getText().trim());
        } catch (Exception e) {
            return padrao;
        }
    }

    private String fmt(double v) {
        return String.format("%.0f", v);
    }

    private String fmtSilver(double v) {
        if (Math.abs(v) >= 1_000_000) return String.format("%.2fM prata", v / 1_000_000);
        if (Math.abs(v) >= 1_000) return String.format("%.1fK prata", v / 1_000);
        return String.format("%.0f prata", v);
    }

    // utilitários de UI
    private TableColumn<LinhaPreco, String> criarColunaCidade(boolean ehPreco) {
        TableColumn<LinhaPreco, String> col = new TableColumn<>("Cidade");
        col.setPrefWidth(130);
        col.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
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
                String nome = BancoDeDadosCraft.CIDADES.stream()
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

    private TableColumn<LinhaMaterial, String> criarColunaCidadeMaterial() {
        TableColumn<LinhaMaterial, String> col = new TableColumn<>("Cidade");
        col.setPrefWidth(130);
        col.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—");
                    setGraphic(null);
                    return;
                }
                LinhaMaterial linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));
                String nome = BancoDeDadosCraft.CIDADES.stream()
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
        col.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().sellMin));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—");
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

    private TableColumn<LinhaMaterial, String> colunaMat(String titulo, double largura,
                                                         javafx.util.Callback<TableColumn.CellDataFeatures<LinhaMaterial, String>,
                                                                 javafx.beans.value.ObservableValue<String>> callback) {
        TableColumn<LinhaMaterial, String> col = new TableColumn<>(titulo);
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

    private Separator separador() {
        return new Separator();
    }

    private TextField campoCraft(String valor) {
        TextField tf = new TextField(valor);
        tf.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        tf.textProperty().addListener((obs, ant, novo) -> {
            if (tabelaPrecos != null) tabelaPrecos.refresh();
            atualizarTabelaCalculo();
        });
        return tf;
    }

    private int parseIntSafe(TextField campo, int padrao) {
        try {
            return Math.max(1, Integer.parseInt(campo.getText().trim()));
        } catch (Exception ex) {
            return padrao;
        }
    }


    private String melhorCidadeMateriais() {
        if (tabelaMateriais == null || tabelaMateriais.getItems().isEmpty()) return "—";
        Map<String, Long> contagem = tabelaMateriais.getItems().stream()
                .filter(l -> l.cidade != null && !l.cidade.equals("—"))
                .collect(Collectors.groupingBy(l -> l.cidade, Collectors.counting()));
        return contagem.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> BancoDeDadosCraft.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(e.getKey()))
                        .map(CidadeInfo::getNome).findFirst().orElse(e.getKey()))
                .orElse("—");
    }
}