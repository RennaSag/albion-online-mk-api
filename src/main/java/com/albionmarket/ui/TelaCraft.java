package com.albionmarket.ui;

import com.albionmarket.model.*;
import com.albionmarket.service.*;
import com.albionmarket.util.AlbionIdUtil;
import com.albionmarket.util.FormatadorUtil;
import javafx.application.Platform;
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


//imports pras threads de busca
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.albionmarket.service.CalculadoraService;

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

    // pool da busca em andamento — cancelada antes de comecar uma nova busca
    // ou ao sair da tela, pra nao deixar buscas antigas rodando em paralelo
    private ExecutorService poolBusca;

    // controles da lateral
    // private final List<CheckBox> checksCidades = new ArrayList<>();
    private Label labelStatus;
    private ProgressIndicator progresso;

    // campos de cálculo
    private TextField campoQuantidade;
    private TextField campoRetorno;
    private TextField campoTaxaMercado;
    private TextField campoSinergiaBarraca;
    private TextField campoDiarioVazio;
    private TextField campoDiarioCheio;
    private Label labelItemValue;

    // tabelas
    private TableView<LinhaPreco> tabelaPrecos;
    private TableView<LinhaMaterial> tabelaReceita;
    private TableView<LinhaMaterialPreco> tabelaMateriais;
    private VBox painelCalculo;

    // dados
    private ReceitaCraft receitaAtual;
    private long itemValue = 0;


    private double precoDiarioVazioApi = 0;
    private double precoDiarioCheioApi = 0;


    private double lucroAtual = 0;
    private double custoAtual = 0;
    private double receitaAtual2 = 0;

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
            int t = AlbionIdUtil.tierEfetivo(tier);
            int e = AlbionIdUtil.enchantEfetivo(enchant);

            // pega melhor cidade de venda da tabela
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

            // monta locais de compra dos materiais
            String locaisJson = cidadesPorMaterialJson();

            // monta o JSON usando os valores já calculados pelo atualizarTabelaCalculo()
            double qtdInicial = Double.parseDouble(campoQuantidade.getText().trim());
            double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
            double qtdFinal = qtdInicial / (1.0 - taxaRetorno);

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"item\": \"").append(item.getNome().replace("\"", "\\\"")).append("\",\n");
            sb.append("  \"itemId\": \"").append(itemIdCompleto).append("\",\n");
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

            String nomeArquivo = OperacaoService.salvar(itemIdCompleto, sb.toString());
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
    private final EstadoSelecao estadoSelecao;

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


    /* modelo da tabela de cálculo
    public static class LinhaCalculo {
        public final String nomeColuna, valor;

        public LinhaCalculo(String nomeColuna, String valor) {
            this.nomeColuna = nomeColuna;
            this.valor = valor;
        }
    }*/

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant) {
        this(palco, item, tier, enchant, null);
    }

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant, EstadoSelecao estadoSelecao) {
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

        palco.setTitle("Craft de: " + item.getNome());
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        buscarTudo();
    }

    // cabeçalho
    private HBox criarCabecalho() {
        Label titulo = new Label("Craft");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Calculadora de Craft - " + item.getNome());
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
        btnHome.setOnMouseClicked(e -> {
            if (poolBusca != null) poolBusca.shutdownNow();
            new TelaHome(palco).mostrar();
        });

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

        // ícone
        ImageView icone = new ImageView();
        icone.setFitWidth(160);
        icone.setFitHeight(100);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);
        icone.setImage(IconeCacheService.obterIcone(
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
        campoSinergiaBarraca = campoCraft("3.0");
        campoDiarioVazio = campoCraft("0");
        campoDiarioCheio = campoCraft("0");

        labelItemValue = new Label("-");
        labelItemValue.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 12px; -fx-font-weight: bold;");

        painel.getChildren().addAll(
                label("Quantidade a craftar"), campoQuantidade,
                label("Taxa de retorno (%)"), campoRetorno,
                label("Taxa da barraca"), campoSinergiaBarraca
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

        btnVoltar.setOnAction(v -> {
            if (poolBusca != null) poolBusca.shutdownNow();
            new TelaCraftSelecao(palco, estadoSelecao).mostrar();
        });

        Button btnIniciarOperacao = new Button("Salvar Operação");
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
                Image img = IconeCacheService.obterIcone(url, 32, 32, true, true, true);
                img.errorProperty().addListener((obs, ant, erro) -> {

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
                        : v.equals("Diario")
                        ? "-fx-text-fill: #16a085; -fx-font-weight: bold;"
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

        painelCalculo = new VBox(10);
        painelCalculo.setStyle("-fx-background-color: #1e1e1e;");

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
            // retorna string unica por linha para forcar o updateItem sempre rodar no refresh
            LinhaMaterialPreco lm = r.getValue();
            return new javafx.beans.property.SimpleStringProperty(lm.nome + "|" + lm.tipo);
        });


        colMatQtd.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    return;
                }
                LinhaMaterialPreco lm = getTableView().getItems().get(getIndex());
                String exibir;
                if ("Diario".equals(lm.tipo)) {
                    double[] fameMultiplierPorTier = {0, 0, 1.5, 7.5, 22.5, 90.0, 270.0, 645.0, 1395.0};
                    double[] famaNecessariaPorTier = {0, 0, 0, 0, 3600, 7200, 14400, 28380, 58590};
                    int tierItem = (tier == -1) ? 4 : tier;
                    int enchantItem = (enchant == -1) ? 0 : enchant;
                    double fameMultiplier = (tierItem >= 2 && tierItem <= 8) ? fameMultiplierPorTier[tierItem] : 0;
                    double famaNecessaria = (tierItem >= 2 && tierItem <= 8) ? famaNecessariaPorTier[tierItem] : 0;
                    int qtdMatReceita = receitaAtual == null ? 0 : receitaAtual.getMateriais().stream()
                            .filter(m -> !m.isArtefato()).mapToInt(ReceitaCraft.MaterialCraft::getCount).sum();
                    double qtdP = parseDoubleSafe(campoQuantidade, 1.0);
                    double taxaR = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
                    double qtdFinal = qtdP / (1.0 - taxaR);
                    double famaPorCraft = qtdMatReceita * fameMultiplier * Math.pow(2, enchantItem);
                    int qtdDiarios = (famaNecessaria > 0 && famaPorCraft > 0)
                            ? (int) Math.ceil((famaPorCraft * qtdFinal) / famaNecessaria) : 1;
                    exibir = String.valueOf(qtdDiarios);
                } else if ("Artefato".equals(lm.tipo)) {
                    double qtdP = parseDoubleSafe(campoQuantidade, 1.0);
                    double taxaR = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
                    exibir = String.format("%.2f", lm.qtdNecessaria * (qtdP / (1.0 - taxaR)));
                } else {
                    exibir = String.valueOf(lm.qtdNecessaria * parseIntSafe(campoQuantidade, 1));
                }
                setText(exibir);
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


    private static void log(String msg) {
        System.out.println("[TelaCraft] " + msg);
    }

    // logica principal com busca por threads
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

        int tierEfetivo = (tier == -1) ? 4 : tier;
        int enchantEfetivo = (enchant == -1) ? 0 : enchant;

        // cancela qualquer busca anterior ainda em andamento (ex: clicou em
        // "Atualizar Valores" de novo) pra nao rodar duas em paralelo
        if (poolBusca != null) poolBusca.shutdownNow();

        // pool dedicado pra essa busca — descartado ao terminar
        ExecutorService pool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        poolBusca = pool;

        Task<Void> tarefa = new Task<>() {

            // resultados coletados entre as etapas
            private List<PriceEntry> precos;
            private ReceitaCraft receita;
            private List<PriceEntry> precosMateirais;
            private PriceEntry precoDiarioVazioEntry;
            private PriceEntry precoDiarioCheioEntry;
            private List<PriceEntry> precosDiarioCheioTodos;

            @Override
            protected Void call() throws Exception {

                precosMateirais = Collections.synchronizedList(new ArrayList<>());

                // etapa 1: precos do item e receita em paralelo — nenhum depende do outro.
                // cada um atualiza a interface assim que chega, sem esperar o resto.
                CompletableFuture<List<PriceEntry>> futurePrecos = CompletableFuture.supplyAsync(() -> {
                    try {
                        List<PriceEntry> r = apiService.buscarPrecos(item.getId(), tierEfetivo, enchantEfetivo, -1, cidades);
                        log("preco do item " + itemIdCompleto + ": " + r.size() + " cotacoes encontradas");
                        return r;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }, pool);

                futurePrecos.thenAccept(lista -> {
                    precos = lista;
                    Platform.runLater(() -> {
                        atualizarTabelaPrecos(precos, precosDiarioCheioTodos);
                        atualizarTabelaCalculo();
                    });
                });

                CompletableFuture<ReceitaCraft> futureReceita = CompletableFuture.supplyAsync(() -> {
                    try {
                        ReceitaCraft r = craftService.buscarReceita(itemIdCompleto);
                        log("receita de " + itemIdCompleto + ": " + (r == null || r.getMateriais().isEmpty()
                                ? "nao encontrada" : r.getMateriais().size() + " materiais"));
                        return r;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }, pool);

                futureReceita.thenAccept(r -> {
                    receita = r;
                    Platform.runLater(() -> {
                        receitaAtual = r;
                        atualizarTabelaPrecos(precos != null ? precos : List.of(), precosDiarioCheioTodos);
                        atualizarTabelaReceita(r, precosMateirais, precoDiarioVazioEntry, precoDiarioCheioEntry);
                        atualizarTabelaMateriais(r, precosMateirais, precoDiarioVazioEntry);
                        atualizarTabelaCalculo();
                    });
                });

                CompletableFuture<Long> futureItemValue = CompletableFuture.supplyAsync(
                        () -> ItemValues.getValor(itemIdCompleto), pool);

                futureItemValue.thenAccept(v -> {
                    itemValue = v;
                    Platform.runLater(() -> labelItemValue.setText(
                            itemValue > 0 ? String.format("%,d", itemValue) : "nao cadastrado"));
                });

                // aguarda precos e receita antes de continuar pros materiais
                precos = futurePrecos.get();
                receita = futureReceita.get();
                itemValue = futureItemValue.get();

                if (receita == null || receita.getMateriais().isEmpty()) return null;

                // etapa 2: cada material busca seu preco em paralelo, atualizando a
                // tela assim que cada um chega, sem esperar os outros materiais.
                List<CompletableFuture<Void>> futuresMateriais = new ArrayList<>();

                for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
                    CompletableFuture<Void> fm = CompletableFuture.supplyAsync(() -> {
                        try {
                            String idMat = mat.getUniqueName();
                            String[] partes = idMat.split("_", 2);
                            int tMat = (partes[0].startsWith("T") && partes[0].length() == 2)
                                    ? Integer.parseInt(partes[0].substring(1)) : 4;
                            String sufixo = partes.length > 1 ? partes[1] : idMat;

                            List<PriceEntry> r = (mat.isArtefato() || enchantEfetivo == 0)
                                    ? apiService.buscarPrecos(sufixo, tMat, 0, -1, cidadesSemBM)
                                    : apiService.buscarPrecos(sufixo + "_LEVEL" + enchantEfetivo, tMat, enchantEfetivo, -1, cidadesSemBM);
                            log("preco do material " + idMat + ": " + r.size() + " cotacoes encontradas");
                            return r;
                        } catch (Exception ex) {
                            log("erro ao buscar preco do material " + mat.getUniqueName() + ": " + ex.getMessage());
                            return List.<PriceEntry>of(); // ignora material com erro
                        }
                    }, pool).thenAccept(lista -> {
                        precosMateirais.addAll(lista);
                        List<PriceEntry> snapshot;
                        synchronized (precosMateirais) {
                            snapshot = new ArrayList<>(precosMateirais);
                        }
                        Platform.runLater(() -> {
                            atualizarTabelaReceita(receita, snapshot, precoDiarioVazioEntry, precoDiarioCheioEntry);
                            atualizarTabelaMateriais(receita, snapshot, precoDiarioVazioEntry);
                            atualizarTabelaCalculo();
                        });
                    });
                    futuresMateriais.add(fm);
                }

                // diarios em paralelo com os materiais — tambem atualiza a tela assim que chega
                String sufixoDiario = com.albionmarket.service.BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
                CompletableFuture<Void> futureDiarios = CompletableFuture.completedFuture(null);

                if (sufixoDiario != null && tierEfetivo >= 2) {
                    futureDiarios = CompletableFuture.runAsync(() -> {
                        try {
                            String sufixoDiarioVazio = sufixoDiario + "_EMPTY";
                            String sufixoDiarioCheio = sufixoDiario + "_FULL";

                            CompletableFuture<List<PriceEntry>> fVazio = CompletableFuture.supplyAsync(() -> {
                                try {
                                    return apiService.buscarPrecos(sufixoDiarioVazio, tierEfetivo, 0, 1, cidadesSemBM);
                                } catch (Exception ex) {
                                    return List.<PriceEntry>of();
                                }
                            }, pool);

                            CompletableFuture<List<PriceEntry>> fCheio = CompletableFuture.supplyAsync(() -> {
                                try {
                                    return apiService.buscarPrecos(sufixoDiarioCheio, tierEfetivo, 0, 1, cidadesSemBM);
                                } catch (Exception ex) {
                                    return List.<PriceEntry>of();
                                }
                            }, pool);

                            List<PriceEntry> listaVazio = fVazio.get();
                            List<PriceEntry> listaCheio = fCheio.get();

                            precosDiarioCheioTodos = listaCheio;
                            precoDiarioVazioEntry = listaVazio.stream()
                                    .filter(p -> p.getSellMin() > 0)
                                    .min(java.util.Comparator.comparingLong(PriceEntry::getSellMin))
                                    .orElse(null);
                            precoDiarioCheioEntry = listaCheio.stream()
                                    .filter(p -> p.getBuyMax() > 0)
                                    .max(java.util.Comparator.comparingLong(PriceEntry::getBuyMax))
                                    .orElse(null);

                            log("diario (" + sufixoDiario + "): vazio " + listaVazio.size()
                                    + " cotacoes, cheio " + listaCheio.size() + " cotacoes");

                            precoDiarioVazioApi = precoDiarioVazioEntry != null ? (double) precoDiarioVazioEntry.getSellMin() : 0;
                            precoDiarioCheioApi = precoDiarioCheioEntry != null ? (double) precoDiarioCheioEntry.getBuyMax() : 0;

                            List<PriceEntry> snapshot;
                            synchronized (precosMateirais) {
                                snapshot = new ArrayList<>(precosMateirais);
                            }
                            Platform.runLater(() -> {
                                atualizarTabelaPrecos(precos != null ? precos : List.of(), precosDiarioCheioTodos);
                                atualizarTabelaReceita(receita, snapshot, precoDiarioVazioEntry, precoDiarioCheioEntry);
                                atualizarTabelaMateriais(receita, snapshot, precoDiarioVazioEntry);
                                atualizarTabelaCalculo();
                            });

                        } catch (Exception ex) {
                            log("erro ao buscar precos do diario: " + ex.getMessage());
                        }
                    }, pool);
                }

                // aguarda todos os materiais e os diarios antes de encerrar a task
                CompletableFuture.allOf(
                        futuresMateriais.toArray(new CompletableFuture[0])
                ).get();
                futureDiarios.get();

                return null;
            }

            @Override
            protected void succeeded() {
                // as tabelas ja foram preenchidas progressivamente ao longo do call()
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

        new Thread(tarefa, "thread-craft").start();
    }


    private void atualizarTabelaPrecos(List<PriceEntry> entradas, List<PriceEntry> precosDiarioCheio) {
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
                            ? pe.getSellDate() : pe.getBuyDate()),
                    fr1, fr2, fr3, fart));
        }
        linhas.sort(Comparator.comparing(l -> l.cidade));


        // adiciona preços do diário cheio por cidade
        int tierDiario = (tier == -1) ? 4 : tier;
        String sufixoDiario = com.albionmarket.service.BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
        if (sufixoDiario != null && tierDiario >= 2 && precosDiarioCheio != null) {
            for (PriceEntry pd : precosDiarioCheio) {
                if (pd.getBuyMax() <= 0) continue;
                String corCidade = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(pd.getCidade()))
                        .map(CidadeInfo::getCor).findFirst().orElse("#888");

                String sufixoDiarioNomePreco = com.albionmarket.service.BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
                String nomeDiarioPreco = com.albionmarket.service.BancoDeDadosItens.getNomeDiario(sufixoDiarioNomePreco);
                linhas.add(new LinhaPreco(
                        "Diário Cheio",
                        "Diário de " + nomeDiarioPreco + " (cheio)",
                        pd.getCidade(),
                        corCidade,
                        FormatadorUtil.formatarPreco(pd.getBuyMax()),
                        FormatadorUtil.formatarData(pd.getBuyDate()),
                        0, 0, 0, 0));
            }
        }


        tabelaPrecos.setItems(FXCollections.observableArrayList(linhas));
        double altPrecos = 28.0 + (linhas.size() * 40.0);
        tabelaPrecos.setPrefHeight(altPrecos);
        tabelaPrecos.setMaxHeight(altPrecos);
    }

    private void atualizarTabelaReceita(ReceitaCraft receita, List<PriceEntry> precosMateirais,
                                        PriceEntry diarioVazio, PriceEntry diarioCheio) {
        if (receita == null) {
            tabelaReceita.setPlaceholder(new Label("Receita não disponível para este item."));
            return;
        }

        // agrupa por itemId+cidade, mantendo o menor sellMin (igual TelaPesquisaPrecos)
        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        if (precosMateirais != null) {
            for (PriceEntry pe : precosMateirais) {
                String chave = pe.getItemId() + "|" + pe.getCidade();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null || (pe.getSellMin() > 0 && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }
        // de todos os grupos itemId+cidade, pega o menor sellMin por itemId
        Map<String, PriceEntry> melhorPorItem = new LinkedHashMap<>();
        for (PriceEntry pe : melhorCompra.values()) {
            String chaveItem = pe.getItemId();
            PriceEntry atual = melhorPorItem.get(chaveItem);
            if (atual == null || (pe.getSellMin() > 0 && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                melhorPorItem.put(chaveItem, pe);
            }
        }

        List<LinhaMaterial> linhas = new ArrayList<>();
        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            int enchantAtualR = (enchant == -1) ? 0 : enchant;
            boolean ehArtefato = mat.isArtefato();

            String raw = ehArtefato ? com.albionmarket.service.BancoDeDadosItens.getArtefatoSufixo(itemIdCompleto) : null;
            String sufixoArtefato = raw != null ? raw.split(";;")[0] : null;
            String nomeArtefato = raw != null ? raw.split(";;")[1] : null;

            // ícone usa o sufixoArtefato se disponível, se n usa idMat, q mostra o id do material
            // se não aparecer o nome do material é pq ele n ta cadastrado no BancoDeDadosItens.java
            int tAtual = (tier == -1) ? 4 : tier;
            String iconeUrl = ehArtefato
                    ? "https://render.albiononline.com/v1/item/" +
                    (sufixoArtefato != null ? "T" + tAtual + "_" + sufixoArtefato : idMat) + ".png"
                    : enchantAtualR > 0
                    ? "https://render.albiononline.com/v1/item/" + idMat + "_LEVEL" + enchantAtualR + ".png"
                    // pra recursos usa _LEVEL em vez de @ pro encantamento
                    : "https://render.albiononline.com/v1/item/" + idMat + ".png";


            String sufixoMat = ehArtefato
                    ? (sufixoArtefato != null ? sufixoArtefato : idMat)
                    : (idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat);

            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;


            // nome: tenta getNomeRecurso, senão usa sufixoArtefato direto como fallback legível
            String nomeRecurso = com.albionmarket.service.BancoDeDadosItens.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : com.albionmarket.service.BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome).findFirst()
                    .orElse(nomeArtefato != null ? nomeArtefato : idMat);

            String nomeMat2 = (enchantAtualR > 0 && !ehArtefato) ? nomeMat + " ." + enchantAtualR : nomeMat;
            String tipo = mat.isArtefato() ? "Artefato" : "Recurso";

            // chave que a API retorna: encantado = "T5_METALBAR_LEVEL2@2", normal = "T5_METALBAR"
            String chaveApi = (!ehArtefato && enchantAtualR > 0)
                    ? idMat + "_LEVEL" + enchantAtualR + "@" + enchantAtualR
                    : idMat;
            PriceEntry pe = melhorPorItem.get(chaveApi);
            // exibe o menor preço de venda (sellMin) — o que você paga pra comprar o recurso
            String precoExibir = pe != null ? FormatadorUtil.formatarPreco(pe.getSellMin()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String corCidade = pe != null
                    ? com.albionmarket.service.BancoDeDadosItens.CIDADES.stream().filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getSellDate() != null && !pe.getSellDate().startsWith("0001")) ? pe.getSellDate() : pe.getBuyDate()) : "-";

            linhas.add(new LinhaMaterial(iconeUrl, nomeMat2, tipo, mat.getCount(), cidade, corCidade, precoExibir, data));
        }

        // adiciona linha do diario vazio e cheio se disponivel
        int tierItem = (tier == -1) ? 4 : tier;
        String sufixoDiario = com.albionmarket.service.BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
        if (sufixoDiario != null && tierItem >= 2) {
            String idVazio = "T" + tierItem + "_" + sufixoDiario + "_EMPTY";
            String idCheio = "T" + tierItem + "_" + sufixoDiario + "_FULL";
            String iconeVazio = "https://render.albiononline.com/v1/item/" + idVazio + ".png";
            //String iconeCheio = "https://render.albiononline.com/v1/item/" + idCheio + ".png";

            String precoVazioStr = diarioVazio != null ? FormatadorUtil.formatarPreco(diarioVazio.getSellMin()) : "-";
            String cidadeVazio = diarioVazio != null ? diarioVazio.getCidade() : "-";
            String corVazio = diarioVazio != null
                    ? com.albionmarket.service.BancoDeDadosItens.CIDADES.stream().filter(c -> c.getApiId().equals(cidadeVazio))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String dataVazio = diarioVazio != null ? FormatadorUtil.formatarData(diarioVazio.getSellDate()) : "-";

            //String precoCheioStr = diarioCheio != null ? FormatadorUtil.formatarPreco(diarioCheio.getBuyMax()) : "-";
            String cidadeCheio = diarioCheio != null ? diarioCheio.getCidade() : "-";
            //String corCheio = diarioCheio != null
            //     ? BancoDeDadosItens.CIDADES.stream().filter(c -> c.getApiId().equals(cidadeCheio))
            //      .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            //String dataCheio = diarioCheio != null ? FormatadorUtil.formatarData(diarioCheio.getBuyMax() > 0 ? diarioCheio.getBuyDate() : "-") : "-";

            String sufixoDiarioNome = com.albionmarket.service.BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
            String nomeDiario = com.albionmarket.service.BancoDeDadosItens.getNomeDiario(sufixoDiarioNome);
            linhas.add(new LinhaMaterial(iconeVazio, "Diário de " + nomeDiario + " (vazio)", "Diario", 1, cidadeVazio, corVazio, precoVazioStr, dataVazio));

        }

        tabelaReceita.setItems(FXCollections.observableArrayList(linhas));


        double alturaLinha = 40.0;
        double alturaHeader = 28.0;
        double alturaCalculada = alturaHeader + (linhas.size() * alturaLinha);
        tabelaReceita.setPrefHeight(alturaCalculada);
        tabelaReceita.setMaxHeight(alturaCalculada);


    }

    private void atualizarTabelaMateriais(ReceitaCraft receita,
                                          List<PriceEntry> precosMateirais, PriceEntry precoDiarioVazio) {
        if (tabelaMateriais == null || receita == null) return;

        // agrupa por itemId+cidade, mantendo o menor sellMin (igual TelaPesquisaPrecos)
        Map<String, PriceEntry> melhorCompra = new LinkedHashMap<>();
        if (precosMateirais != null) {
            for (PriceEntry pe : precosMateirais) {
                String chave = pe.getItemId() + "|" + pe.getCidade();
                PriceEntry atual = melhorCompra.get(chave);
                if (atual == null || (pe.getSellMin() > 0 && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                    melhorCompra.put(chave, pe);
                }
            }
        }
        // de todos os grupos itemId+cidade, pega o menor sellMin por itemId
        Map<String, PriceEntry> melhorPorItem = new LinkedHashMap<>();
        for (PriceEntry pe : melhorCompra.values()) {
            String chaveItem = pe.getItemId();
            PriceEntry atual = melhorPorItem.get(chaveItem);
            if (atual == null || (pe.getSellMin() > 0 && (atual.getSellMin() == 0 || pe.getSellMin() < atual.getSellMin()))) {
                melhorPorItem.put(chaveItem, pe);
            }
        }

        int enchantAtual = (enchant == -1) ? 0 : enchant;
        List<LinhaMaterialPreco> linhas = new ArrayList<>();

        for (ReceitaCraft.MaterialCraft mat : receita.getMateriais()) {
            String idMat = mat.getUniqueName();
            boolean ehArtefato = mat.isArtefato();

            String raw = ehArtefato ? com.albionmarket.service.BancoDeDadosItens.getArtefatoSufixo(itemIdCompleto) : null;
            String sufixoArtefato = raw != null ? raw.split(";;")[0] : null;
            String nomeArtefato = raw != null ? raw.split(";;")[1] : null;

            String sufixoMat = ehArtefato
                    ? (sufixoArtefato != null ? sufixoArtefato : idMat)
                    : (idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat);

            int tierMat = (idMat.length() > 1 && idMat.charAt(0) == 'T' && Character.isDigit(idMat.charAt(1)))
                    ? Character.getNumericValue(idMat.charAt(1)) : 4;

            String nomeRecurso = com.albionmarket.service.BancoDeDadosItens.getNomeRecurso(sufixoMat, tierMat);
            String nomeMat = nomeRecurso != null ? nomeRecurso
                    : com.albionmarket.service.BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixoMat))
                    .map(ItemDefinition::getNome).findFirst()
                    .orElse(nomeArtefato != null ? nomeArtefato : idMat);
            String nomeExibir = (enchantAtual > 0 && !ehArtefato) ? nomeMat + " ." + enchantAtual : nomeMat;

            // chave que a API retorna: encantado = "T5_METALBAR_LEVEL2@2", normal = "T5_METALBAR"
            String chaveApi = (!ehArtefato && enchantAtual > 0)
                    ? idMat + "_LEVEL" + enchantAtual + "@" + enchantAtual
                    : idMat;
            PriceEntry pe = melhorPorItem.get(chaveApi);
            // exibe o menor preço de venda (sellMin) — o que você paga pra comprar o recurso
            String precoExibir = pe != null ? FormatadorUtil.formatarPreco(pe.getSellMin()) : "-";
            String cidade = pe != null ? pe.getCidade() : "-";
            String corCidade = pe != null
                    ? com.albionmarket.service.BancoDeDadosItens.CIDADES.stream().filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888") : "#888";
            String data = pe != null ? FormatadorUtil.formatarData(
                    (pe.getSellDate() != null && !pe.getSellDate().startsWith("0001")) ? pe.getSellDate() : pe.getBuyDate()) : "-";

            String tipoMat = mat.isArtefato() ? "Artefato" : "Recurso";
            linhas.add(new LinhaMaterialPreco(nomeExibir, tipoMat, mat.getCount(), cidade, corCidade, precoExibir, data));
        }


        int tierItem2 = (tier == -1) ? 4 : tier;
        String sufixoDiario2 = com.albionmarket.service.BancoDeDadosItens.getDiarioSufixo(itemIdCompleto);
        if (sufixoDiario2 != null && tierItem2 >= 2 && precoDiarioVazio != null) {


            // mesma logica do atualizarTabelaCalculo
            double[] fameMultiplierPorTier2 = {0, 0, 1.5, 7.5, 22.5, 90.0, 270.0, 645.0, 1395.0};
            double[] famaNecessariaPorTier = {0, 0, 0, 0, 3600, 7200, 14400, 28380, 58590};

            double fameMultiplier2 = (tierItem2 >= 2 && tierItem2 <= 8) ? fameMultiplierPorTier2[tierItem2] : 0;
            double famaNecessaria2 = (tierItem2 >= 2 && tierItem2 <= 8) ? famaNecessariaPorTier[tierItem2] : 0;

            int enchantItem2 = (enchant == -1) ? 0 : enchant;

            int qtdMatReceita = 0;
            if (receita != null) {
                qtdMatReceita = receita.getMateriais().stream()
                        .filter(m -> !m.isArtefato())
                        .mapToInt(ReceitaCraft.MaterialCraft::getCount)
                        .sum();
            }

            double qtdProduzir = parseDoubleSafe(campoQuantidade, 1.0);
            double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
            double qtdFinalDiario = qtdProduzir / (1.0 - taxaRetorno);

            double famaPorCraft2 = qtdMatReceita * fameMultiplier2 * Math.pow(2, enchantItem2);

            int qtdDiarios = (famaNecessaria2 > 0 && famaPorCraft2 > 0)
                    ? (int) Math.ceil((famaPorCraft2 * qtdFinalDiario) / famaNecessaria2)
                    : 1;

            String precoVazioStr = FormatadorUtil.formatarPreco(precoDiarioVazio.getSellMin());
            String cidadeVazio = precoDiarioVazio.getCidade();
            String corVazio = com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(cidadeVazio))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888");
            String dataVazio = FormatadorUtil.formatarData(precoDiarioVazio.getSellDate());

            linhas.add(new LinhaMaterialPreco(
                    "Diario Vazio", "Diario", qtdDiarios,
                    cidadeVazio, corVazio, precoVazioStr, dataVazio));
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
        if (painelCalculo == null) return;

        double qtdProduzir = parseDoubleSafe(campoQuantidade, 1.0);
        double taxaRetorno = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
        double taxaBarraca = parseDoubleSafe(campoSinergiaBarraca, 3.0);
        double qtdFinalMateriais = qtdProduzir / (1.0 - taxaRetorno);


        // soma custo dos materiais das tabelas.
        // "Artefato" nunca e devolvido pelo retorno, entao precisa de 1 unidade por
        // item FINAL (qtdFinalMateriais, ja com o retorno aplicado) — os demais tipos
        // (Recurso) escalam pela quantidade inicial digitada, igual ja fazia antes.
        double custoMateriais = 0;
        double precoDiarioVazioTabela = 0;
        int qtdDiariosTabela = 1;

        if (tabelaMateriais != null && !tabelaMateriais.getItems().isEmpty()) {
            for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
                if ("Diario".equals(lm.tipo)) {
                    precoDiarioVazioTabela = FormatadorUtil.parseSilver(lm.buyMax);
                    qtdDiariosTabela = lm.qtdNecessaria;
                } else if ("Artefato".equals(lm.tipo)) {
                    custoMateriais += FormatadorUtil.parseSilver(lm.buyMax) * lm.qtdNecessaria * qtdFinalMateriais;
                } else {
                    custoMateriais += FormatadorUtil.parseSilver(lm.buyMax) * lm.qtdNecessaria * qtdProduzir;
                }
            }
        } else if (tabelaReceita != null) {
            for (LinhaMaterial lm : tabelaReceita.getItems()) {
                if ("Diario".equals(lm.tipo)) continue;
                double qtdEscala = "Artefato".equals(lm.tipo) ? qtdFinalMateriais : qtdProduzir;
                custoMateriais += FormatadorUtil.parseSilver(lm.buyMax) * lm.qtd * qtdEscala;
            }
        }

        // melhor preço de venda
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

        // calculo principal via service
        CalculadoraService.ResultadoCalculo calc = CalculadoraService.calcular(
                qtdProduzir, taxaRetorno, taxaBarraca,
                itemValue, custoMateriais, melhorVenda, possuiPremium);

        double qtdFinal = calc.qtdFinal;
        double custoMatComTaxa = calc.custoMateriais;
        double taxaCraftTotal = calc.taxaBarraca;
        double receitaTotal = calc.receitaTotal;
        double custoTotal = calc.custoTotal;
        double lucro = calc.lucro;

        // calculo dos diarios
        int tierItem = AlbionIdUtil.tierEfetivo(tier);
        int enchantItem = AlbionIdUtil.enchantEfetivo(enchant);

        int qtdMateriaisReceita = 0;
        if (receitaAtual != null) {
            qtdMateriaisReceita = receitaAtual.getMateriais().stream()
                    .filter(m -> !m.isArtefato())
                    .mapToInt(ReceitaCraft.MaterialCraft::getCount)
                    .sum();
        }

        double diariosCompletos = CalculadoraService.calcularDiarios(
                tierItem, enchantItem, qtdMateriaisReceita, qtdFinal);

        // usa o preco da tabela se editado manualmente, senao usa o da api
        double precVazioFinal = precoDiarioVazioTabela > 0 ? precoDiarioVazioTabela : precoDiarioVazioApi;
        double lucroDiarios = CalculadoraService.calcularLucroDiarios(
                diariosCompletos, precVazioFinal, precoDiarioCheioApi);

        double lucroComDiarios = lucro + lucroDiarios;

        lucroAtual = lucroComDiarios;
        custoAtual = custoTotal;
        receitaAtual2 = receitaTotal;


        // funcoes auxiliares de nome e cidade
        java.util.function.Function<ReceitaCraft.MaterialCraft, String> getNomeExibir = mat -> {
            String idMat = mat.getUniqueName();
            String sufixo = idMat.contains("_") ? idMat.substring(idMat.indexOf('_') + 1) : idMat;
            int tierMat = AlbionIdUtil.extrairTier(idMat);
            if (tierMat == -1) tierMat = 4;
            String nomeRec = com.albionmarket.service.BancoDeDadosItens.getNomeRecurso(sufixo, tierMat);
            String nomeMat = nomeRec != null ? nomeRec
                    : com.albionmarket.service.BancoDeDadosItens.getTodosItens().stream()
                    .filter(i -> i.getId().equals(sufixo))
                    .map(ItemDefinition::getNome).findFirst().orElse(idMat);
            int eAtual = AlbionIdUtil.enchantEfetivo(enchant);
            return eAtual > 0 ? nomeMat + " ." + eAtual : nomeMat;
        };

        java.util.function.Function<String, String> cidadeParaNome = apiId ->
                com.albionmarket.service.BancoDeDadosItens.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(apiId))
                        .map(CidadeInfo::getNome)
                        .findFirst().orElse(apiId != null ? apiId : "-");

        Map<String, String> cidadePorMaterial = new LinkedHashMap<>();
        if (tabelaMateriais != null) {
            for (LinhaMaterialPreco lm : tabelaMateriais.getItems()) {
                String nomeBase = lm.nome.contains(" .") ? lm.nome.substring(0, lm.nome.lastIndexOf(" .")) : lm.nome;
                cidadePorMaterial.put(lm.nome, lm.cidade);
                cidadePorMaterial.put(nomeBase, lm.cidade);
            }
        }

        List<ReceitaCraft.MaterialCraft> recursosCalc = receitaAtual == null
                ? new ArrayList<>()
                : receitaAtual.getMateriais().stream().filter(m -> !m.isArtefato()).collect(Collectors.toList());
        List<ReceitaCraft.MaterialCraft> artefatosCalc = receitaAtual == null
                ? new ArrayList<>()
                : receitaAtual.getMateriais().stream().filter(ReceitaCraft.MaterialCraft::isArtefato).collect(Collectors.toList());

        List<String[]> metricas = new ArrayList<>(Arrays.asList(
                new String[]{"Qtd a craftar", FormatadorUtil.fmt(qtdProduzir) + " un"},
                new String[]{"Qtd final craftada", String.format("%.2f un", qtdFinal)},
                new String[]{"Melhor preço de venda", FormatadorUtil.fmtSilver(melhorVenda)},
                new String[]{"Local de venda", nomeCidadeVenda},
                new String[]{"Custo dos materiais", FormatadorUtil.fmtSilver(custoMatComTaxa)},
                new String[]{"Taxa da barraca", FormatadorUtil.fmtSilver(taxaCraftTotal)},
                new String[]{"Diarios completos", String.format("%.2f un", diariosCompletos)},
                new String[]{"Lucro c/ diarios", FormatadorUtil.fmtSilver(lucroDiarios)}
        ));

        String[] nomesRec = {"Qtd Recurso 1", "Qtd Recurso 2", "Qtd Recurso 3"};
        String[] nomesLoc = {"Local Recurso 1", "Local Recurso 2", "Local Recurso 3"};
        for (int ri = 0; ri < Math.min(recursosCalc.size(), 3); ri++) {
            int qtdR = recursosCalc.get(ri).getCount() * (int) qtdProduzir;
            metricas.add(new String[]{nomesRec[ri], String.valueOf(qtdR)});
            String nomeMatR = getNomeExibir.apply(recursosCalc.get(ri));
            String cidadeR = cidadePorMaterial.getOrDefault(nomeMatR, "-");
            metricas.add(new String[]{nomesLoc[ri], cidadeParaNome.apply(cidadeR)});
        }

        if (!artefatosCalc.isEmpty()) {
            int qtdArt = artefatosCalc.stream().mapToInt(ReceitaCraft.MaterialCraft::getCount).sum() * (int) qtdFinal;
            metricas.add(new String[]{"Qtd Artefatos", String.valueOf(qtdArt)});
            String cidadeArt = "-";
            if (tabelaMateriais != null) {
                cidadeArt = tabelaMateriais.getItems().stream()
                        .filter(lm -> "Artefato".equals(lm.tipo))
                        .map(lm -> lm.cidade)
                        .filter(c -> c != null && !c.equals("-"))
                        .findFirst().orElse("-");
            }
            metricas.add(new String[]{"Local do Artefato", cidadeParaNome.apply(cidadeArt)});
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
                lucroComDiarios >= 0 ? "Lucro" : "Prejuízo",
                (lucroComDiarios >= 0 ? "+" : "") + FormatadorUtil.fmtSilver(lucroComDiarios),
                lucroComDiarios >= 0 ? "#5a8dee" : "#e05555");

        HBox.setHgrow(cardCusto, Priority.ALWAYS);
        HBox.setHgrow(cardReceita, Priority.ALWAYS);
        HBox.setHgrow(cardLucro, Priority.ALWAYS);
        linhaDestaque.getChildren().addAll(cardCusto, cardReceita, cardLucro);

        painelCalculo.getChildren().setAll(fluxoNormal, sep, linhaDestaque);
    }


    private double parseDoubleSafe(TextField campo, double padrao) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (Exception e) {
            return padrao;
        }
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

    private <T> TableColumn<T, String> coluna(String titulo, double largura, javafx.util.Callback<TableColumn.CellDataFeatures<T, String>, javafx.beans.value.ObservableValue<String>> callback) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(callback);
        return col;
    }

    private TableColumn<LinhaMaterial, String> colunaMat(String titulo, double largura, javafx.util.Callback<TableColumn.CellDataFeatures<LinhaMaterial, String>, javafx.beans.value.ObservableValue<String>> callback) {
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

    //essa é a funcao pra montar o json das cidades diferentes pra cada material e as quantidades deles
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
                    .findFirst()
                    .orElse(lm.cidade != null ? lm.cidade : "-");

            int qtdReal;
            if ("Diario".equals(lm.tipo)) {
                // replica a mesma lógica da cellFactory da coluna Qtd necessaria
                double[] fameMultiplierPorTier = {0, 0, 1.5, 7.5, 22.5, 90.0, 270.0, 645.0, 1395.0};
                double[] famaNecessariaPorTier = {0, 0, 0, 0, 3600, 7200, 14400, 28380, 58590};
                int tierItem = (tier == -1) ? 4 : tier;
                int enchantItem = (enchant == -1) ? 0 : enchant;
                double fameMultiplier = (tierItem >= 2 && tierItem <= 8) ? fameMultiplierPorTier[tierItem] : 0;
                double famaNecessaria = (tierItem >= 2 && tierItem <= 8) ? famaNecessariaPorTier[tierItem] : 0;
                int qtdMatReceita = receitaAtual == null ? 0 : receitaAtual.getMateriais().stream()
                        .filter(m -> !m.isArtefato()).mapToInt(ReceitaCraft.MaterialCraft::getCount).sum();
                double qtdP = parseDoubleSafe(campoQuantidade, 1.0);
                double taxaR = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
                double qtdFinal = qtdP / (1.0 - taxaR);
                double famaPorCraft = qtdMatReceita * fameMultiplier * Math.pow(2, enchantItem);
                qtdReal = (famaNecessaria > 0 && famaPorCraft > 0)
                        ? (int) Math.ceil((famaPorCraft * qtdFinal) / famaNecessaria) : 1;
            } else if ("Artefato".equals(lm.tipo)) {
                double qtdP = parseDoubleSafe(campoQuantidade, 1.0);
                double taxaR = parseDoubleSafe(campoRetorno, 15.2) / 100.0;
                qtdReal = (int) Math.ceil(lm.qtdNecessaria * (qtdP / (1.0 - taxaR)));
            } else {
                qtdReal = lm.qtdNecessaria * parseIntSafe(campoQuantidade, 1);
            }

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