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
 * tela de craft preços do item, receita com preços dos materiais e calculadora.
 */
public class TelaCraft {

    // contexto
    private final Stage palco;
    private final ItemDefinition item;
    private final int tier;
    private final int enchant;
    private final String itemIdCompleto;
    private boolean possuiPremium = false;

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
    private TextField campoSinergiaBarraca;
    private Label labelItemValue;

    // tabelas
    private TableView<LinhaPreco> tabelaPrecos;
    private TableView<LinhaMaterial> tabelaReceita;
    private TableView<LinhaMaterialPreco> tabelaMateriais;
    private TableView<LinhaCalculo> tabelaCalculo;

    // dados
    private ReceitaCraft receitaAtual;
    private long itemValue = 0;

    // toggle de edição manual de preços
    private boolean modoEdicaoManual = false;

    // modelo tabela de preços do item
    public static class LinhaPreco {
        public final String itemId, qualidade, cidade, corCidade;
        public final String sellMin, atualizado;
        public final int qtdRecurso1, qtdRecurso2, qtdRecurso3, qtdArtefatos;

        public LinhaPreco(String itemId, String qualidade, String cidade, String corCidade, String sellMin, String atualizado, int qtdRecurso1, int qtdRecurso2, int qtdRecurso3, int qtdArtefatos) {
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

    // criador do json pra salvar as informações da operacao, precisando corrigir, ta sem alguns campos
    private void salvarOperacao() {
        try {
            int t = (tier == -1) ? 4 : tier;
            int e = (enchant == -1) ? 0 : enchant;

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
            sb.append("    \"taxaBarraca\": \"").append(campoSinergiaBarraca.getText()).append("\",\n");
            sb.append("    \"itemValue\": \"").append(labelItemValue.getText()).append("\"\n");
            sb.append("  },\n");

            // recalcula tudo localmente para salvar
            double qtdCraftInicial = parseDoubleSafe(campoQuantidade, 1.0);
            //quantidade q vou craftar

            double sinergiaPercentual = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
            //taxa de retorno bonus, sinergia

            double taxaDeCraftDaBarraca = parseDoubleSafe(campoSinergiaBarraca, 3.0);
            //taxa da barraca

            double qtdFinalCraftada = qtdCraftInicial / (1.0 - sinergiaPercentual);
            //qtd final q vou ter com o bonus


            double nutricaoTotal = (itemValue * qtdFinalCraftada) * 0.1125;
            //nutricao de tudo

            double taxaDaBarracaDeCraft = (taxaDeCraftDaBarraca * nutricaoTotal) / 100.0;
            //taxa q vou pagar pra barraca

            double taxaMercado = possuiPremium ? 0.03 : 0.05;
            //taxa do mercado com e sem premium, pra compra e venda


            double custoMateriais = 0;
            if (tabelaMateriais != null && !tabelaMateriais.getItems().isEmpty()) {
                for (LinhaMaterialPreco lm : tabelaMateriais.getItems())
                    custoMateriais += parseSilver(lm.buyMax) * lm.qtdNecessaria;
                //custo dos materiais
            } else if (tabelaReceita != null) {
                for (LinhaMaterial lm : tabelaReceita.getItems())
                    custoMateriais += parseSilver(lm.buyMax) * lm.qtd;
            }

            double custoMateriaisComTaxa = custoMateriais + (custoMateriais * taxaMercado);
            //custo dos materiais com a taxa

            double custoTotal = custoMateriaisComTaxa + taxaDaBarracaDeCraft + (qtdCraftInicial * taxaMercado);
            //custo total


            double precoVendaSalvar = tabelaPrecos.getItems().stream()
                    .mapToDouble(l -> parseSilver(l.sellMin))
                    .max()
                    .orElse(0.0);

            double receitaSalvar = qtdFinalCraftada * precoVendaSalvar;
            double taxaMercSalvar = receitaSalvar * taxaMercado;
            double lucroSalvar = receitaSalvar - custoTotal - taxaMercSalvar;


            String[] melhorCidadeHolder = {"-"};
            double[] melhorVHolder = {0};

            for (LinhaPreco lp : tabelaPrecos.getItems()) {
                double v = parseSilver(lp.sellMin);
                if (v > melhorVHolder[0]) {
                    melhorVHolder[0] = v;
                    melhorCidadeHolder[0] = lp.cidade;
                }
            }
            String nomeCidadeVendaSalvar = BancoDeDadosCraft.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(melhorCidadeHolder[0]))
                    .map(CidadeInfo::getNome)
                    .findFirst()
                    .orElse(melhorCidadeHolder[0]);


            sb.append("  \"calculadora\": {\n");
            sb.append("    \"Quantidade a craftar\": \"").append(fmt(qtdCraftInicial)).append(" un\",\n");
            sb.append("    \"Qtd final craftada\": \"").append(String.format("%.2f un", qtdFinalCraftada)).append("\",\n");
            sb.append("    \"Melhor preco de venda\": \"").append(fmtSilver(precoVendaSalvar)).append("\",\n");
            sb.append("    \"Local\": \"").append(nomeCidadeVendaSalvar).append("\",\n");
            sb.append("    \"Custo dos materiais\": \"").append(fmtSilver(custoMateriaisComTaxa)).append("\",\n");
            sb.append("    \"Local de compra dos materiais\": ").append(cidadesPorMaterialJson()).append(",\n");
            sb.append("    \"Custo total\": \"").append(fmtSilver(custoTotal)).append("\",\n");
            sb.append("    \"Lucro/Prejuizo\": \"").append(lucroSalvar >= 0 ? "+" : "").append(fmtSilver(lucroSalvar)).append("\"\n");
            sb.append("  }\n");
            sb.append("}\n");

            String nomeArquivo = "operacao_" + itemIdCompleto + "_"
                    + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
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

        public LinhaMaterial(String iconeUrl, String nome, String tipo, int qtd, String cidade, String corCidade, String buyMax, String atualizado) {
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
        public final String nome, tipo, cidade, corCidade, buyMax, atualizado;
        public final int qtdNecessaria;

        public LinhaMaterialPreco(String nome, String tipo, int qtdNecessaria, String cidade, String corCidade, String buyMax, String atualizado) {
            this.tipo = tipo;
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

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant, EstadoCraftSelecao estadoSelecao) {
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
        cena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

        palco.setTitle("Albion Market - Craft: " + item.getNome());
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

        Label subtitulo = new Label("Calculadora de Craft - " + item.getNome());
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
        icone.setImage(new Image("https://render.albiononline.com/v1/item/" + itemIdCompleto + ".png", true));

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
        campoSinergiaBarraca = campoCraft("3.0");
        labelItemValue = new Label("-");
        labelItemValue.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 12px; -fx-font-weight: bold;");

        painel.getChildren().addAll(
                label("Quantidade a craftar"), campoQuantidade,
                label("Taxa de retorno (%)"), campoRetorno,
                label("Taxa da barraca (%)"), campoSinergiaBarraca
        );

        painel.getChildren().add(separador());

        // switch inserir preços manualmente
        javafx.scene.canvas.Canvas canvasSwitch = new javafx.scene.canvas.Canvas(44, 22);
        final boolean[] estadoSwitch = {false};

        Runnable desenharSwitch = () -> {
            javafx.scene.canvas.GraphicsContext gc = canvasSwitch.getGraphicsContext2D();
            gc.clearRect(0, 0, 44, 22);
            gc.setFill(estadoSwitch[0] ? javafx.scene.paint.Color.web("#5a8dee") : javafx.scene.paint.Color.web("#555"));
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


        // botão atualizar
        Button btnAtualizar = new Button("Atualizar Valores");
        btnAtualizar.setMaxWidth(Double.MAX_VALUE);
        btnAtualizar.setStyle("-fx-background-color: #5a8dee; -fx-text-fill: white; "
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

        Button btnIniciarOperacao = new Button("Iniciar Operação");
        btnIniciarOperacao.setMaxWidth(Double.MAX_VALUE);
        btnIniciarOperacao.setStyle("-fx-background-color: #3dba6e; -fx-text-fill: white; "
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
        TableColumn<LinhaPreco, String> colCidadePreco = criarColunaCidade(true);
        TableColumn<LinhaPreco, String> colSell = criarColunaPreco("Preço de Venda", 130);
        TableColumn<LinhaPreco, String> colDataPreco = coluna("Última Atualização", 100,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaPrecos.getColumns().addAll(colQual, colCidadePreco, colSell, colDataPreco);

        // tabela de receita
        Label tituloReceita = new Label("Receita de Craft");
        tituloReceita.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloReceita.setPadding(new Insets(12, 0, 6, 0));

        tabelaReceita = new TableView<>();
        tabelaReceita.setStyle("-fx-background-color: #1e1e1e;");
        tabelaReceita.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaReceita.setPlaceholder(new Label("Carregando receita..."));

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
                setStyle(v.equals("Artefato")
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

        // tabela de cálculo
        Label tituloCalculo = new Label("Calculadora de Lucro");
        tituloCalculo.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloCalculo.setPadding(new Insets(12, 0, 6, 0));

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
            LinhaMaterialPreco lm = r.getValue();
            if (lm.tipo != null && lm.tipo.equals("Artefato")) {
                double qtdProduzir = parseDoubleSafe(campoQuantidade, 1.0);
                double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
                double qtdFinal = qtdProduzir / (1.0 - taxaRetorno);
                return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", qtdFinal));
            }
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
                String nome = BancoDeDadosCraft.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(v))
                        .map(CidadeInfo::getNome)
                        .findFirst().orElse(v);
                HBox hb = new HBox(6, ponto, new Label(nome));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });

        TableColumn<LinhaMaterialPreco, String> colMatData = coluna("Ultima Atualizacao", 110,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaMateriais.getColumns().addAll(colMatNome, colMatQtd, colMatBuy, colMatCidade, colMatData);

        VBox area = new VBox(10,
                tituloPrecos, tabelaPrecos,
                tituloReceita, tabelaReceita,
                tituloMateriais, tabelaMateriais,
                tituloCalculo, tabelaCalculo);

        area.setPadding(new Insets(16));
        area.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(tabelaPrecos, Priority.SOMETIMES);
        VBox.setVgrow(tabelaReceita, Priority.SOMETIMES);
        VBox.setVgrow(tabelaMateriais, Priority.SOMETIMES);
        VBox.setVgrow(tabelaCalculo, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(area);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    // logica principal
    private void buscarTudo() {
        List<String> cidades = (estadoSelecao != null && estadoSelecao.cidades != null && !estadoSelecao.cidades.isEmpty())
                ? estadoSelecao.cidades
                : BancoDeDadosCraft.CIDADES.stream().map(CidadeInfo::getApiId).collect(Collectors.toList());

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
                precos = apiService.buscarPrecos(item.getId(), (tier == -1) ? 4 : tier, (enchant == -1) ? 0 : enchant, -1, cidades);
                receita = craftService.buscarReceita(itemIdCompleto);
                itemValue = ItemValues.getValor(itemIdCompleto);

                if (receita != null && !receita.getMateriais().isEmpty()) {
                    List<String> idsMat = receita.getMateriais().stream()
                            .map(ReceitaCraft.MaterialCraft::getUniqueName)
                            .collect(Collectors.toList());
                    precosMateirais = new ArrayList<>();
                    for (String idMat : idsMat) {
                        try {
                            String[] partes = idMat.split("_", 2);
                            int tMat = (partes[0].startsWith("T") && partes[0].length() == 2)
                                    ? Integer.parseInt(partes[0].substring(1)) : 4;
                            String sufixo = partes.length > 1 ? partes[1] : idMat;
                            precosMateirais.addAll(apiService.buscarPrecos(sufixo, tMat, 0, -1, cidades));
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
                labelItemValue.setText(itemValue > 0 ? String.format("%,d", itemValue) : "nao cadastrado");
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
            if (atual == null || (pe.getSellMin() > 0 && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
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
                    pe.getCidade(),
                    corCidade,
                    FormatadorUtil.formatarPreco(pe.getSellMin()),
                    FormatadorUtil.formatarData((pe.getSellDate() != null && !pe.getSellDate().startsWith("0001"))
                            ? pe.getSellDate() : pe.getBuyDate()),
                    fr1, fr2, fr3, fart));
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

        List<LinhaMaterial> linhas = new ArrayList<>();
        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            int enchantAtualR = (enchant == -1) ? 0 : enchant;
            boolean ehArtefato = mat.isArtefato();

            String raw = ehArtefato ? BancoDeDadosCraft.getArtefatoSufixo(itemIdCompleto) : null;
            String sufixoArtefato = raw != null ? raw.split(";;")[0] : null;
            String nomeArtefato = raw != null ? raw.split(";;")[1] : null;

            // ícone usa o sufixoArtefato se disponível, se n usa idMat, q mostra o id do material
            int tAtual = (tier == -1) ? 4 : tier;
            String iconeUrl = ehArtefato
                    ? "https://render.albiononline.com/v1/item/" +
                    (sufixoArtefato != null ? "T" + tAtual + "_" + sufixoArtefato : idMat) + ".png"
                    : enchantAtualR > 0
                    ? "https://render.albiononline.com/v1/item/" + idMat + ".png?quality=" + enchantAtualR
                    : "https://render.albiononline.com/v1/item/" + idMat + ".png";


            String sufixoMat = ehArtefato
                    ? (sufixoArtefato != null ? sufixoArtefato : idMat)
                    : (idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat);

            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;


            // nome: tenta getNomeRecurso, senão usa sufixoArtefato direto como fallback legível
            String nomeRecurso = BancoDeDadosCraft.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : BancoDeDadosCraft.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome).findFirst()
                    .orElse(nomeArtefato != null ? nomeArtefato : idMat);

            String nomeMat2 = (enchantAtualR > 0 && !ehArtefato) ? nomeMat + " ." + enchantAtualR : nomeMat;
            String tipo = mat.isArtefato() ? "Artefato" : "Recurso";

            PriceEntry pe = melhorCompra.get(idMat);
            String buyMax = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String corCidade = pe != null
                    ? BancoDeDadosCraft.CIDADES.stream().filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001")) ? pe.getBuyDate() : pe.getSellDate()) : "-";


            System.out.println("ICONE URL: " + iconeUrl);
            linhas.add(new LinhaMaterial(iconeUrl, nomeMat2, tipo, mat.getCount(), cidade, corCidade, buyMax, data));
        }

        tabelaReceita.setItems(FXCollections.observableArrayList(linhas));
        double alturaLinha = 40.0;
        double alturaHeader = 28.0;
        double alturaCalculada = alturaHeader + (linhas.size() * alturaLinha);
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

        int enchantAtual = (enchant == -1) ? 0 : enchant;
        List<LinhaMaterialPreco> linhas = new ArrayList<>();

        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            boolean ehArtefato = mat.isArtefato();

            String raw = ehArtefato ? BancoDeDadosCraft.getArtefatoSufixo(itemIdCompleto) : null;
            String sufixoArtefato = raw != null ? raw.split(";;")[0] : null;
            String nomeArtefato = raw != null ? raw.split(";;")[1] : null;

            String sufixoMat = ehArtefato
                    ? (sufixoArtefato != null ? sufixoArtefato : idMat)
                    : (idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat);

            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;

            String nomeRecurso = BancoDeDadosCraft.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : BancoDeDadosCraft.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome).findFirst()
                    .orElse(nomeArtefato != null ? nomeArtefato : idMat);
            String nomeExibir = (enchantAtual > 0 && !ehArtefato) ? nomeMat + " ." + enchantAtual : nomeMat;


            PriceEntry pe = melhorCompra.get(idMat);
            String buyMax = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String corCidade = pe != null
                    ? BancoDeDadosCraft.CIDADES.stream().filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001")) ? pe.getBuyDate() : pe.getSellDate()) : "-";

            String tipoMat = mat.isArtefato() ? "Artefato" : "Recurso";
            linhas.add(new LinhaMaterialPreco(nomeExibir, tipoMat, mat.getCount(), cidade, corCidade, buyMax, data));
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
                                ev.getNewValue(), antiga.atualizado,
                                antiga.qtdRecurso1, antiga.qtdRecurso2, antiga.qtdRecurso3, antiga.qtdArtefatos));
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
        if (tabelaCalculo == null) return;

        // parâmetros básicos
        double qtdProduzir = parseDoubleSafe(campoQuantidade, 1.0);
        double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
        double taxaBarraca = parseDoubleSafe(campoSinergiaBarraca, 3.0);
        long iv = itemValue;

        double qtdFinal = qtdProduzir / (1.0 - taxaRetorno);
        double nutricao = (iv * qtdFinal) * 0.1125;
        double taxaCraftTotal = (taxaBarraca * nutricao) / 100.0;

        // taxa de compra e venda dependem do premium
        double taxaCompra = possuiPremium ? 0.03 : 0.05;
        double taxaVenda = possuiPremium ? 0.025 : 0.05;

        // custo dos materiais
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

        double custoMatComTaxa = custoMateriais * qtdProduzir + (qtdProduzir * taxaCompra);

        double custoTotal = (custoMatComTaxa + taxaCraftTotal) + (qtdFinal * taxaCompra);

        // melhor preço de venda
        double melhorVenda = 0;
        String melhorCidadeTemp = "-";
        for (LinhaPreco lp : tabelaPrecos.getItems()) {
            double v = parseSilver(lp.sellMin);
            if (v > melhorVenda) {
                melhorVenda = v;
                melhorCidadeTemp = lp.cidade;
            }
        }
        final String melhorCidade = melhorCidadeTemp;
        String nomeMelhorCidadeVenda = BancoDeDadosCraft.CIDADES.stream()
                .filter(c -> c.getApiId().equals(melhorCidade))
                .map(CidadeInfo::getNome)
                .findFirst().orElse(melhorCidade);

        // receita e lucro
        double receitaTotal = qtdFinal * melhorVenda;
        double taxaMercadoValor = receitaTotal * taxaVenda;
        double lucro = receitaTotal - custoTotal - taxaMercadoValor;

        // recursos e artefatos para colunas dinâmicas
        List<ReceitaCraft.MaterialCraft> recursosCalc = receitaAtual == null
                ? new ArrayList<>()
                : receitaAtual.getMateriais().stream().filter(m -> !m.isArtefato()).collect(Collectors.toList());
        List<ReceitaCraft.MaterialCraft> artefatosCalc = receitaAtual == null
                ? new ArrayList<>()
                : receitaAtual.getMateriais().stream().filter(ReceitaCraft.MaterialCraft::isArtefato).collect(Collectors.toList());

        // mapa nome-material -> cidade (da tabelaMateriais)
        Map<String, String> cidadePorMaterial = new LinkedHashMap<>();
        if (tabelaMateriais != null) {
            for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
                String nomeBase = lm.nome.contains(" .") ? lm.nome.substring(0, lm.nome.lastIndexOf(" .")) : lm.nome;
                cidadePorMaterial.put(lm.nome, lm.cidade);
                cidadePorMaterial.put(nomeBase, lm.cidade);
            }
        }

        java.util.function.Function<ReceitaCraft.MaterialCraft, String> getNomeExibir = mat -> {
            String idMat = mat.getUniqueName();
            String sufixo = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;
            String nomeRec = BancoDeDadosCraft.getNomeRecurso(sufixo, tierMat);
            String nomeMat = nomeRec != null ? nomeRec
                    : BancoDeDadosCraft.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixo))
                    .map(ItemDefinition::getNome).findFirst().orElse(idMat);
            int enchantAtual2 = (enchant == -1) ? 0 : enchant;
            return enchantAtual2 > 0 ? nomeMat + " ." + enchantAtual2 : nomeMat;
        };

        java.util.function.Function<String, String> cidadeParaNome = apiId ->
                BancoDeDadosCraft.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(apiId))
                        .map(CidadeInfo::getNome)
                        .findFirst().orElse(apiId != null ? apiId : "-");

        // monta lista de métricas, parte da tabela
        List<String[]> listaMetricas = new ArrayList<>(Arrays.asList(
                new String[]{"Quantidade a craftar", fmt(qtdProduzir) + " un"},
                new String[]{"Qtd final craftada", String.format("%.2f un", qtdFinal)},
                new String[]{"Melhor preco de venda", fmtSilver(melhorVenda)},
                new String[]{"Local de Venda", nomeMelhorCidadeVenda},
                new String[]{"Custo total dos materiais", fmtSilver(custoMatComTaxa)},
                new String[]{"Taxa da barraca", fmtSilver(taxaCraftTotal)},
                new String[]{"Custo total", fmtSilver(custoTotal)},
                new String[]{"Receita total", fmtSilver(receitaTotal)},
                new String[]{"Lucro/Prejuizo", (lucro >= 0 ? "+" : "") + fmtSilver(lucro)}
        ));

        String[] nomesRecCalc = {"Qtd Recurso 1", "Qtd Recurso 2", "Qtd Recurso 3"};
        String[] nomesLocCalc = {"Local Recurso 1", "Local Recurso 2", "Local Recurso 3"};

        for (int ri = 0; ri < Math.min(recursosCalc.size(), 3); ri++) {
            int qtdR = recursosCalc.get(ri).getCount() * (int) qtdProduzir;
            listaMetricas.add(listaMetricas.size() - 5, new String[]{nomesRecCalc[ri], String.valueOf(qtdR)});
            String nomeMatR = getNomeExibir.apply(recursosCalc.get(ri));
            String cidadeR = cidadePorMaterial.getOrDefault(nomeMatR, "-");
            listaMetricas.add(listaMetricas.size() - 5, new String[]{nomesLocCalc[ri], cidadeParaNome.apply(cidadeR)});
        }
        if (!artefatosCalc.isEmpty()) {
            int qtdArt = artefatosCalc.stream().mapToInt(ReceitaCraft.MaterialCraft::getCount).sum() * (int) qtdFinal;
            listaMetricas.add(listaMetricas.size() - 5, new String[]{"Qtd Artefatos", String.valueOf(qtdArt)});
            String nomeArt = getNomeExibir.apply(artefatosCalc.get(0));
            String cidadeArt = cidadePorMaterial.getOrDefault(nomeArt, "-");
            listaMetricas.add(listaMetricas.size() - 5, new String[]{"Local do Artefato", cidadeParaNome.apply(cidadeArt)});
        }

        String[][] metricas = listaMetricas.toArray(new String[0][]);

        // reconstrói colunas
        tabelaCalculo.getColumns().clear();

        // captura final do lucro para uso nos lambdas das células
        final double lucroFinal = lucro;

        for (String[] metrica : metricas) {
            final String titulo = metrica[0];
            final String valor = metrica[1];

            TableColumn<LinhaCalculo, String> col = new TableColumn<>(titulo);
            col.setMinWidth(140);
            col.setPrefWidth(Region.USE_COMPUTED_SIZE);
            col.setSortable(false);
            col.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().valor));
            col.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    setText(valor);
                    if (titulo.equals("Lucro/Prejuizo")) {
                        setStyle(lucroFinal >= 0
                                ? "-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER;"
                                : "-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER;");
                    } else if (titulo.equals("Custo total") || titulo.equals("Taxa da barraca")) {
                        setStyle("-fx-text-fill: #e05555; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    } else if (titulo.equals("Receita total")) {
                        setStyle("-fx-text-fill: #3dba6e; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    } else if (titulo.startsWith("Local Recurso") || titulo.equals("Local Artefato")) {
                        setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px; -fx-alignment: CENTER;");
                    }
                }
            });
            tabelaCalculo.getColumns().add(col);
        }

        tabelaCalculo.setItems(FXCollections.observableArrayList(new LinhaCalculo("", "")));
        javafx.application.Platform.runLater(() -> {
            tabelaCalculo.requestLayout();
            tabelaCalculo.scrollToColumn(tabelaCalculo.getColumns().get(0));
        });
    }

    // utilitários

    private double parseSilver(String val) {
        if (val == null || val.equals("-")) return 0;
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
        if (Math.abs(v) >= 1_000_000) return String.format("%.2fM de prata", v / 1_000_000);
        if (Math.abs(v) >= 1_000) return String.format("%.1fK de prata", v / 1_000);
        return String.format("%.0f prata", v);
    }

    private TableColumn<LinhaPreco, String> criarColunaCidade(boolean ehPreco) {
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
        col.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("-")) {
                    setText("-");
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
                                              javafx.util.Callback<TableColumn.CellDataFeatures<T, String>, javafx.beans.value.ObservableValue<String>> callback) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(callback);
        return col;
    }

    private TableColumn<LinhaMaterial, String> colunaMat(String titulo, double largura,
                                                         javafx.util.Callback<TableColumn.CellDataFeatures<LinhaMaterial, String>, javafx.beans.value.ObservableValue<String>> callback) {
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
            if (tabelaMateriais != null) tabelaMateriais.refresh();
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


    private String melhorCidadeArtefato() {
        if (tabelaMateriais == null || tabelaMateriais.getItems().isEmpty()) return "-";
        Map<String, Long> contagem = tabelaMateriais.getItems().stream()
                .filter(l -> l.cidade != null && !l.cidade.equals("-"))
                .collect(Collectors.groupingBy(l -> l.cidade, Collectors.counting()));
        return contagem.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> BancoDeDadosCraft.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(e.getKey()))
                        .map(CidadeInfo::getNome).findFirst().orElse(e.getKey()))
                .orElse("-");
    }


    //essa é a funcao pra montar o json das cidades diferentes pra cada material e as quantidades deles
    private String cidadesPorMaterialJson() {
        if (tabelaMateriais == null || tabelaMateriais.getItems().isEmpty()) return "[]";

        StringBuilder sb = new StringBuilder("[");
        boolean primeiro = true;
        for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
            if (!primeiro) sb.append(", ");
            primeiro = false;

            String nomeCidade = BancoDeDadosCraft.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(lm.cidade))
                    .map(CidadeInfo::getNome)
                    .findFirst()
                    .orElse(lm.cidade != null ? lm.cidade : "-");

            int qtdReal = lm.qtdNecessaria * parseIntSafe(campoQuantidade, 1);

            sb.append("{\"material\": \"")
                    .append(lm.nome.replace("\"", "\\\""))
                    .append("\", \"quantidade\": ").append(qtdReal)
                    .append(", \"cidade\": \"").append(nomeCidade)
                    .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }
}

