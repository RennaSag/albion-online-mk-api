package com.albionmarket.ui;

import com.albionmarket.model.*;
import com.albionmarket.service.ApiService;
import com.albionmarket.service.BancoDeDados;
import com.albionmarket.service.BuscaService;
import com.albionmarket.util.FormatadorUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * tela principal da aplicação
 */
public class TelaPesquisaPrecos {

    // serviços
    private final ApiService apiService = new ApiService();
    private final BuscaService buscaService = new BuscaService();
    private final List<Categoria> categorias = BancoDeDados.getCategorias();
    private ImageView iconItem;

    // estado de seleção
    private ItemDefinition itemSelecionado = null;

    // controles do painel de filtros
    private TextField campoBusca;
    private ComboBox<Categoria> cbCategoria;
    private ComboBox<Subcategoria> cbSubcategoria;
    private ComboBox<ItemDefinition> cbItem;
    private ComboBox<String> cbTier;
    private ComboBox<String> cbEncantamento;
    private ComboBox<String> cbQualidade;
    private final List<CheckBox> checksCidades = new ArrayList<>();

    // controles da área de resultados
    private TableView<LinhaPreco> tabelaResultados;
    private Label labelStatus;
    private ProgressIndicator progresso;
    private Button btnBuscar;

    // modelo da tabela
    public static class LinhaPreco {
        public final String itemId;
        public final String tier;
        public final String enchant;
        public final String cidade;
        public final String corCidade;
        public final String qualidade;
        public final String sellMin;
        public final String buyMax;
        public final String atualizado;

        public LinhaPreco(String itemId, String tier, String enchant, String cidade,
                          String corCidade, String qualidade,
                          String sellMin, String buyMax, String atualizado) {
            this.itemId = itemId;
            this.tier = tier;
            this.enchant = enchant;
            this.cidade = cidade;
            this.corCidade = corCidade;
            this.qualidade = qualidade;
            this.sellMin = sellMin;
            this.buyMax = buyMax;
            this.atualizado = atualizado;
        }
    }

    //metodo auxiliar dos icones
    private void atualizarIconeItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            iconItem.setImage(null);
            return;
        }
        String url = "https://render.albiononline.com/v1/item/" + itemId + ".png";
        iconItem.setImage(new javafx.scene.image.Image(url, true)); // true = carrega em background
    }


    // ponto de entrada: cria e retorna o layout raiz
    public BorderPane getCriarLayout() {
        BorderPane raiz = new BorderPane();
        raiz.setTop(criarCabecalho());
        raiz.setLeft(criarPainelFiltros());
        raiz.setCenter(criarAreaResultados());
        return raiz;
    }

    // header
    private HBox criarCabecalho() {
        Label titulo = new Label("Albion Market");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("Consulta de preços em tempo real com API");
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);
        HBox cabecalho = new HBox(textos);
        cabecalho.setPadding(new Insets(14, 20, 14, 20));
        cabecalho.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cabecalho;
    }

    // painel de filtros (lado esquerdo)
    private ScrollPane criarPainelFiltros() {
        VBox painel = new VBox(14);
        painel.setPadding(new Insets(16));
        painel.setPrefWidth(300);
        painel.setStyle("-fx-background-color: #252525;");


        //icone do item, controle de tamanho etc
        iconItem = new ImageView();
        iconItem.setFitWidth(120);
        iconItem.setFitHeight(120);
        iconItem.setPreserveRatio(true);
        iconItem.setSmooth(true);

        Label labelIcone = new Label("Item selecionado:");
        labelIcone.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");

        VBox painelIcone = new VBox(6, labelIcone, iconItem);
        painelIcone.setAlignment(Pos.CENTER);
        painelIcone.setMaxWidth(Double.MAX_VALUE);


        // barra de busca por texto
        painel.getChildren().add(criarSecao("Busca por Nome"));
        campoBusca = new TextField();
        campoBusca.setPromptText("Ex: espada larga, cajado sagrado...");
        campoBusca.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        campoBusca.textProperty().addListener((obs, ant, novo) -> onBuscaTexto(novo));
        painel.getChildren().add(campoBusca);

        // categoria > Subcategoria > Item
        painel.getChildren().add(criarSecao("Categoria"));

        cbCategoria = new ComboBox<>();
        cbCategoria.setItems(FXCollections.observableArrayList(categorias));
        cbCategoria.setPromptText("Categoria");
        cbCategoria.setMaxWidth(Double.MAX_VALUE);
        cbCategoria.setOnAction(e -> onCategoriaSelecionada());
        estilizarComboBox(cbCategoria);

        cbSubcategoria = new ComboBox<>();
        cbSubcategoria.setPromptText("Subcategoria");
        cbSubcategoria.setMaxWidth(Double.MAX_VALUE);
        cbSubcategoria.setDisable(true);
        cbSubcategoria.setOnAction(e -> onSubcategoriaSelecionada());
        estilizarComboBox(cbSubcategoria);

        cbItem = new ComboBox<>();
        cbItem.setPromptText("Item");
        cbItem.setMaxWidth(Double.MAX_VALUE);
        cbItem.setDisable(true);
        cbItem.setOnAction(e -> onItemSelecionado());
        estilizarComboBox(cbItem);

        painel.getChildren().addAll(cbCategoria, cbSubcategoria, cbItem);

        // tier
        painel.getChildren().add(criarSecao("Tier"));
        cbTier = new ComboBox<>();
        cbTier.setItems(FXCollections.observableArrayList(
                "Todos", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8"));
        cbTier.setValue("Todos");
        cbTier.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTier);
        painel.getChildren().add(cbTier);

        // encantamento
        painel.getChildren().add(criarSecao("Encantamento"));
        cbEncantamento = new ComboBox<>();
        cbEncantamento.setItems(FXCollections.observableArrayList(
                "Todos", "Sem encantamento", ".1", ".2", ".3", ".4"));
        cbEncantamento.setValue("Todos");
        cbEncantamento.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbEncantamento);
        painel.getChildren().add(cbEncantamento);

        // qualidade
        painel.getChildren().add(criarSecao("Qualidade"));
        cbQualidade = new ComboBox<>();
        cbQualidade.setItems(FXCollections.observableArrayList(BancoDeDados.QUALIDADES));
        cbQualidade.setValue(BancoDeDados.QUALIDADES[0]);
        cbQualidade.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbQualidade);
        painel.getChildren().add(cbQualidade);

        // cidades
        painel.getChildren().add(criarSecao("Cidades"));
        FlowPane gridCidades = new FlowPane(8, 8);
        for (CidadeInfo cidade : BancoDeDados.CIDADES) {
            CheckBox cb = new CheckBox(cidade.getNome());
            cb.setSelected(true);
            cb.setStyle("-fx-text-fill: #ccc;");
            cb.setUserData(cidade.getApiId());
            checksCidades.add(cb);
            gridCidades.getChildren().add(cb);
        }
        painel.getChildren().add(gridCidades);

        // botões
        btnBuscar = new Button("Buscar Preços");
        btnBuscar.setMaxWidth(Double.MAX_VALUE);
        btnBuscar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 0;");
        btnBuscar.setOnAction(e -> executarBusca());

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setMaxWidth(Double.MAX_VALUE);
        btnLimpar.setStyle(
                "-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                        + "-fx-background-radius: 6; -fx-padding: 8 0;");
        btnLimpar.setOnAction(e -> limpar());


        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setMaxWidth(Double.MAX_VALUE);
        btnAtualizar.setStyle(
                "-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                        + "-fx-background-radius: 6; -fx-padding: 8 0;"
        );

        btnAtualizar.setOnAction(e -> executarBusca());


        //espaço pro boltão "voltar" ficar mais afastado dos outros
        Region espaco = new Region();
        //espaco.setMinHeight(10);
        VBox.setVgrow(espaco, Priority.ALWAYS);


        Button btnVoltar = new Button("Voltar");
        btnVoltar.setMaxWidth(Double.MAX_VALUE);
        btnVoltar.getStyleClass().add("home-botao"); //estilo q faz o botão ficar azul qnd passa o mouse

        btnVoltar.setOnAction(e -> {
            Stage palco = (Stage) btnVoltar.getScene().getWindow();
            new TelaHome(palco).mostrar();

            palco.centerOnScreen();
            palco.setMaximized(false);
        });


        painel.getChildren().addAll(btnBuscar, btnLimpar, btnAtualizar, painelIcone, espaco, btnVoltar);

        ScrollPane scroll = new ScrollPane(painel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #252525; -fx-background-color: #252525;");
        return scroll;
    }

    // area de resultados (centro)
    @SuppressWarnings("unchecked")
    private VBox criarAreaResultados() {
        // status e progresso
        labelStatus = new Label("Selecione um item e clique em 'Buscar Preços'.");
        labelStatus.setStyle("-fx-text-fill: #999;");

        progresso = new ProgressIndicator();
        progresso.setVisible(false);
        progresso.setMaxSize(28, 28);

        HBox barraStatus = new HBox(10, progresso, labelStatus);
        barraStatus.setAlignment(Pos.CENTER_LEFT);
        barraStatus.setPadding(new Insets(10, 16, 8, 16));

        // tabela
        tabelaResultados = new TableView<>();
        tabelaResultados.setStyle("-fx-background-color: #1e1e1e;");
        tabelaResultados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaResultados.setPlaceholder(new Label("Nenhum resultado."));

        // colunas
        //parte com as colunas doq vai ser mostrado
        // o id do item não me interessa, eu n quero ver ele, vide linha 246, adicionar "colItem" antes de "colTier" pra ver
        // TableColumn<LinhaPreco, String> colItem   = coluna("Item ID",     180, r -> new javafx.beans.property.SimpleStringProperty(r.getValue().itemId));
        TableColumn<LinhaPreco, String> colTier = coluna("Tier", 55, r -> new javafx.beans.property.SimpleStringProperty(r.getValue().tier));
        TableColumn<LinhaPreco, String> colEnch = coluna("Encantamento", 55, r -> new javafx.beans.property.SimpleStringProperty(r.getValue().enchant));
        TableColumn<LinhaPreco, String> colCidade = criarColunaCidade();
        TableColumn<LinhaPreco, String> colQual = coluna("Qualidade", 100, r -> new javafx.beans.property.SimpleStringProperty(r.getValue().qualidade));
        TableColumn<LinhaPreco, String> colSell = criarColunaPreco("Preço dos Pedidos de Venda", 120, true);
        TableColumn<LinhaPreco, String> colBuy = criarColunaPreco("Preço dos Pedidos de Compra", 120, false);
        TableColumn<LinhaPreco, String> colData = coluna("Última atualização", 85, r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaResultados.getColumns().addAll(
                colTier, colEnch, colCidade, colQual, colSell, colBuy, colData);

        VBox area = new VBox(barraStatus, tabelaResultados);
        VBox.setVgrow(tabelaResultados, Priority.ALWAYS);
        area.setStyle("-fx-background-color: #1e1e1e;");
        return area;
    }

    // logica dos filtros
    private void onBuscaTexto(String texto) {
        itemSelecionado = null;
        List<ItemDefinition> sugestoes = buscaService.buscar(texto, 1);
        if (!sugestoes.isEmpty()) {
            itemSelecionado = sugestoes.get(0);
        }
    }

    private void onCategoriaSelecionada() {
        Categoria cat = cbCategoria.getValue();
        campoBusca.clear();
        itemSelecionado = null;

        cbSubcategoria.setDisable(cat == null);
        cbItem.setDisable(true);
        cbItem.setItems(FXCollections.emptyObservableList());

        if (cat != null) {
            cbSubcategoria.setItems(FXCollections.observableArrayList(cat.getSubcategorias()));
            cbSubcategoria.setValue(null);
        }
    }

    private void onSubcategoriaSelecionada() {
        Subcategoria sub = cbSubcategoria.getValue();
        itemSelecionado = null;

        cbItem.setDisable(sub == null);
        if (sub != null) {
            cbItem.setItems(FXCollections.observableArrayList(sub.getItens()));
            cbItem.setValue(null);
        }
    }

    private void onItemSelecionado() {
        itemSelecionado = cbItem.getValue();
        if (itemSelecionado != null) {
            campoBusca.setText(itemSelecionado.getNome());
            atualizarIconeItem("T4_" + itemSelecionado.getId()); // T4 como preview padrão
        }
    }

    // execucao da busca e controle de casos
    private void executarBusca() {
        // resolve item: via seleção nos combos ou via busca por texto
        ItemDefinition item = itemSelecionado;
        if (item == null && !campoBusca.getText().isBlank()) {
            List<ItemDefinition> res = buscaService.buscar(campoBusca.getText(), 1);
            if (!res.isEmpty()) item = res.get(0);
        }

        if (item == null) {
            labelStatus.setText("Selecione ou busque um item.");
            return;
        }

        List<String> cidades = checksCidades.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.toList());

        if (cidades.isEmpty()) {
            labelStatus.setText("Selecione ao menos uma cidade.");
            return;
        }

        int tier = parseTier(cbTier.getValue());
        int enchant = parseEnchant(cbEncantamento.getValue());
        int quality = parseQuality(cbQualidade.getValue());

        final ItemDefinition itemFinal = item;

        // executa em thread separada para não travar a UI
        Task<List<PriceEntry>> tarefa = new Task<>() {
            @Override
            protected List<PriceEntry> call() throws Exception {
                return apiService.buscarPrecos(itemFinal.getId(), tier, enchant, quality, cidades);
            }
        };

        tarefa.setOnRunning(e -> {
            progresso.setVisible(true);
            btnBuscar.setDisable(true);
            labelStatus.setText("Consultando o mercado…");
            tabelaResultados.setItems(FXCollections.emptyObservableList());
        });

        tarefa.setOnSucceeded(e -> {
            progresso.setVisible(false);
            btnBuscar.setDisable(false);

            List<PriceEntry> entradas = tarefa.getValue();
            ObservableList<LinhaPreco> linhas = processarResultados(entradas, quality, cidades);
            tabelaResultados.setItems(linhas);
            labelStatus.setText(linhas.isEmpty()
                    ? "Nenhum dado disponível para os filtros selecionados."
                    : linhas.size() + " resultados encontrados.");

            if (!linhas.isEmpty()) {
                atualizarIconeItem(linhas.get(0).itemId);
            }
        });

        tarefa.setOnFailed(e -> {
            progresso.setVisible(false);
            btnBuscar.setDisable(false);
            labelStatus.setText("Erro: " + tarefa.getException().getMessage());
        });

        new Thread(tarefa, "thread-api").start();
    }

    /**
     * transforma os PriceEntry brutos da API em linhas para a tabela
     * quando quality == -1 (todos), mantem apenas o preco mais barato por item+cidade.
     */
    private ObservableList<LinhaPreco> processarResultados(
            List<PriceEntry> entradas, int quality, List<String> cidades) {

        // agrupa por itemId+cidade mantendo o menor sell_price_min
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

        // converte para LinhaPreco
        List<LinhaPreco> linhas = new ArrayList<>();

        for (PriceEntry pe : melhor.values()) {
            // ignora linhas sem nenhum dado
            if (pe.getSellMin() == 0 && pe.getBuyMax() == 0) continue;

            // extrai tier e encantamento do item ID
            String[] partes = pe.getItemId().split("_", 2); // ["T5", "MAIN_SWORD@2"]
            String tierStr = partes.length > 0 ? partes[0] : "?";
            String enchStr = pe.getItemId().contains("@")
                    ? "." + pe.getItemId().split("@")[1]  //parte do q vai aparecer nas colunas, ex: encantamento .x (.1, .2, .3)
                    : "0";

            // cores das cidades
            String corCidade = BancoDeDados.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor)
                    .findFirst()
                    .orElse("#888");

            String qualLabel = quality == -1
                    ? FormatadorUtil.nomeQualidade(pe.getQualidade()) + " *"
                    : FormatadorUtil.nomeQualidade(pe.getQualidade());


            //formatacao pra aparecer na tela bonitinho
            linhas.add(new LinhaPreco(
                    pe.getItemId(),
                    tierStr,
                    enchStr,
                    pe.getCidade(),
                    corCidade,
                    qualLabel,
                    FormatadorUtil.formatarPreco(pe.getSellMin()),
                    FormatadorUtil.formatarPreco(pe.getBuyMax()),

                    FormatadorUtil.formatarData(
                            (pe.getSellDate() != null && !pe.getSellDate().startsWith("0001"))
                                    ? pe.getSellDate() : pe.getBuyDate()
                    )

            ));
        }

        // ordena por tier > encantamento > cidade
        linhas.sort(Comparator
                .comparing((LinhaPreco l) -> l.tier)
                .thenComparing(l -> l.enchant)
                .thenComparing(l -> l.cidade));

        return FXCollections.observableArrayList(linhas);
    }

    // limpeza do menu

    private void limpar() {
        campoBusca.clear();
        cbCategoria.setValue(null);
        cbSubcategoria.setItems(FXCollections.emptyObservableList());
        cbSubcategoria.setDisable(true);
        cbItem.setItems(FXCollections.emptyObservableList());
        cbItem.setDisable(true);
        cbTier.setValue("Todos");
        cbEncantamento.setValue("Todos");
        cbQualidade.setValue(BancoDeDados.QUALIDADES[0]);
        checksCidades.forEach(cb -> cb.setSelected(true));
        tabelaResultados.setItems(FXCollections.emptyObservableList());
        labelStatus.setText("Selecione um item e clique em Buscar Preços.");
        itemSelecionado = null;
    }

    // utilitarios

    private int parseTier(String val) {
        if (val == null || val.equals("Todos")) return -1;
        return Integer.parseInt(val.replace("T", ""));
    }

    private int parseEnchant(String val) {
        if (val == null || val.equals("Todos")) return -1;
        if (val.equals("Sem encantamento")) return 0;
        return Integer.parseInt(val.replace(".", ""));
    }


    private int parseQuality(String val) {
        if (val == null) return -1;
        return switch (val) {
            case "Normal" -> 1;
            case "Boa" -> 2;
            case "Notável" -> 3;
            case "Excelente" -> 4;
            case "Obra-prima" -> 5;
            default -> -1;
        };
    }

    /**
     * cria um label de secao estilizado.
     */
    private Label criarSecao(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
        return lbl;
    }

    /**
     * aplica estilo escuro padrao a um ComboBox.
     */
    private void estilizarComboBox(ComboBox<?> cb) {
        cb.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
    }

    /**
     * cria uma coluna de tabela String.
     */
    private TableColumn<LinhaPreco, String> coluna(
            String titulo, double largura,
            javafx.util.Callback<TableColumn.CellDataFeatures<LinhaPreco, String>,
                    javafx.beans.value.ObservableValue<String>> callback) {

        TableColumn<LinhaPreco, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(callback);
        return col;
    }

    /**
     * coluna de cidade com marcador colorido.
     */
    private TableColumn<LinhaPreco, String> criarColunaCidade() {
        TableColumn<LinhaPreco, String> col = new TableColumn<>("Cidade");
        col.setPrefWidth(130);
        col.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                LinhaPreco linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));

                // Nome amigável da cidade
                String nomeCidade = BancoDeDados.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(item))
                        .map(CidadeInfo::getNome)
                        .findFirst()
                        .orElse(item);

                HBox hb = new HBox(6, ponto, new Label(nomeCidade));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
                setText(null);
            }
        });
        return col;
    }

    /**
     * coluna de preço com cor vermelha (sell) ou verde (buy).
     */
    @SuppressWarnings("unchecked")
    private TableColumn<LinhaPreco, String> criarColunaPreco(
            String titulo, double largura, boolean ehVenda) {

        TableColumn<LinhaPreco, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(largura);
        col.setCellValueFactory(r -> new javafx.beans.property.SimpleStringProperty(
                ehVenda ? r.getValue().sellMin : r.getValue().buyMax));

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.equals("—")) {
                    setText("—");
                    setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(item);
                    setStyle(ehVenda
                            ? "-fx-text-fill: #e05555; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;"
                            : "-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });
        return col;
    }
}
