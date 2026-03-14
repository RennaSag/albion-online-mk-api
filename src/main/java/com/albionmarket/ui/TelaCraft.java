package com.albionmarket.ui;

import com.albionmarket.model.*;
import com.albionmarket.model.EstadoCraftSelecao;
import com.albionmarket.service.ApiService;
import com.albionmarket.service.BancoDeDados;
import com.albionmarket.service.CraftService;
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
import java.util.stream.Collectors;

/**
 * Tela de craft: preços do item, receita com preços dos materiais e calculadora.
 */
public class TelaCraft {

    // contexto
    private final Stage          palco;
    private final ItemDefinition item;
    private final int            tier;
    private final int            enchant;
    private final String         itemIdCompleto;

    // serviços
    private final ApiService   apiService   = new ApiService();
    private final CraftService craftService = new CraftService();

    // controles da lateral
    private final List<CheckBox> checksCidades = new ArrayList<>();
    private Label                labelStatus;
    private ProgressIndicator    progresso;

    // campos de cálculo
    private TextField campoQuantidade;
    private TextField campoRetorno;
    private TextField campoTaxaMercado;

    // tabelas
    private TableView<LinhaPreco>     tabelaPrecos;
    private TableView<LinhaMaterial>  tabelaReceita;

    // dados
    private ReceitaCraft receitaAtual;

    // ── Modelo tabela de preços do item ──────────────────────────────────
    public static class LinhaPreco {
        public final String itemId, qualidade, cidade, corCidade;
        public final String sellMin, atualizado;
        // qtd de cada recurso/artefato separado por tipo
        public final int qtdRecurso1, qtdRecurso2, qtdRecurso3, qtdArtefatos;

        public LinhaPreco(String itemId, String qualidade, String cidade,
                          String corCidade, String sellMin, String atualizado,
                          int qtdRecurso1, int qtdRecurso2, int qtdRecurso3, int qtdArtefatos) {
            this.itemId      = itemId;
            this.qualidade   = qualidade;
            this.cidade      = cidade;
            this.corCidade   = corCidade;
            this.sellMin     = sellMin;
            this.atualizado  = atualizado;
            this.qtdRecurso1 = qtdRecurso1;
            this.qtdRecurso2 = qtdRecurso2;
            this.qtdRecurso3 = qtdRecurso3;
            this.qtdArtefatos= qtdArtefatos;
        }
    }

    // ── Modelo tabela de receita (materiais) ─────────────────────────────
    public static class LinhaMaterial {
        public final String iconeUrl, nome, tipo, cidade, corCidade;
        public final String buyMax, atualizado;
        public final int    qtd;

        public LinhaMaterial(String iconeUrl, String nome, String tipo,
                             int qtd, String cidade, String corCidade,
                             String buyMax, String atualizado) {
            this.iconeUrl   = iconeUrl;
            this.nome       = nome;
            this.tipo       = tipo;
            this.qtd        = qtd;
            this.cidade     = cidade;
            this.corCidade  = corCidade;
            this.buyMax     = buyMax;
            this.atualizado = atualizado;
        }
    }

    // estado dos filtros da tela anterior (para restaurar ao clicar Voltar)
    private final EstadoCraftSelecao estadoSelecao;

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant) {
        this(palco, item, tier, enchant, null);
    }

    public TelaCraft(Stage palco, ItemDefinition item, int tier, int enchant,
                     EstadoCraftSelecao estadoSelecao) {
        this.palco         = palco;
        this.item          = item;
        this.tier          = tier;
        this.enchant       = enchant;
        this.estadoSelecao = estadoSelecao;
        int t = (tier    == -1) ? 4 : tier;
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

    // ── Cabeçalho ────────────────────────────────────────────────────────
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

    // ── Lateral esquerda ─────────────────────────────────────────────────
    private ScrollPane criarLateral() {
        VBox painel = new VBox(14);
        painel.setPadding(new Insets(16));
        painel.setPrefWidth(280);
        painel.setStyle("-fx-background-color: #252525;");

        // ícone
        ImageView icone = new ImageView();
        icone.setFitWidth(100);
        icone.setFitHeight(100);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);
        icone.setImage(new Image(
                "https://render.albiononline.com/v1/item/" + itemIdCompleto + ".png", true));

        Label nomeItem = new Label(item.getNome());
        nomeItem.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 13px;");
        nomeItem.setWrapText(true);

        int t = (tier    == -1) ? 4 : tier;
        int e = (enchant == -1) ? 0 : enchant;
        Label infoItem = new Label("Tier " + t + (e > 0 ? "  ·  Ench. ." + e : ""));
        infoItem.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        VBox boxIcone = new VBox(6, icone, nomeItem, infoItem);
        boxIcone.setAlignment(Pos.CENTER);
        boxIcone.setPadding(new Insets(0, 0, 10, 0));
        painel.getChildren().addAll(boxIcone, separador());

        // cidades
        painel.getChildren().add(secao("Cidades"));
        FlowPane gridCidades = new FlowPane(8, 8);
        for (CidadeInfo cidade : BancoDeDados.CIDADES) {
            CheckBox cb = new CheckBox(cidade.getNome());
            cb.setSelected(true);
            cb.setStyle("-fx-text-fill: #ccc;");
            cb.setUserData(cidade.getApiId());
            checksCidades.add(cb);
            gridCidades.getChildren().add(cb);
        }
        painel.getChildren().addAll(gridCidades, separador());

        // parâmetros
        painel.getChildren().add(secao("Parâmetros de Craft"));
        campoQuantidade  = campoCraft("1");
        campoRetorno     = campoCraft("15.2");
        campoTaxaMercado = campoCraft("3.0");

        painel.getChildren().addAll(
                label("Quantidade a craftar"),  campoQuantidade,
                label("Taxa de retorno (%)"),   campoRetorno,
                label("Taxa do mercado (%)"),   campoTaxaMercado
        );
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

        painel.getChildren().addAll(btnAtualizar, espaco, btnVoltar);

        ScrollPane scroll = new ScrollPane(painel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #252525; -fx-background-color: #252525;");
        return scroll;
    }

    // ── Área central ─────────────────────────────────────────────────────
    private VBox criarAreaCentral() {
        // -- tabela de preços do item --
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


        // -- tabela de receita (materiais) --
        Label tituloReceita = new Label("Receita de Craft");
        tituloReceita.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");
        tituloReceita.setPadding(new Insets(12, 0, 6, 0));

        tabelaReceita = new TableView<>();
        tabelaReceita.setStyle("-fx-background-color: #1e1e1e;");
        tabelaReceita.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaReceita.setPlaceholder(new Label("Carregando receita..."));

        // coluna ícone
        TableColumn<LinhaMaterial, String> colIcone = new TableColumn<>("  ");
        colIcone.setPrefWidth(50);
        colIcone.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().iconeUrl));
        colIcone.setCellFactory(tc -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(32); iv.setFitHeight(32); iv.setPreserveRatio(true); }
            @Override protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) { setGraphic(null); return; }
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
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
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
            @Override protected void updateItem(String v, boolean empty) {
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
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—"); setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
                } else {
                    setText(v);
                    setStyle("-fx-text-fill: #3dba6e; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        TableColumn<LinhaMaterial, String> colDataMat = colunaMat("Última Atualização", 110,
                r -> new javafx.beans.property.SimpleStringProperty(r.getValue().atualizado));

        tabelaReceita.getColumns().addAll(
                colIcone, colNomeMat, colTipoMat, colQtd1,
                colCidadeMat, colBuyMat, colDataMat);

        VBox area = new VBox(10,
                tituloPrecos,  tabelaPrecos,
                tituloReceita, tabelaReceita
        );
        area.setPadding(new Insets(16));
        area.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(tabelaPrecos,  Priority.SOMETIMES);
        VBox.setVgrow(tabelaReceita, Priority.ALWAYS);
        return area;
    }

    // ── Lógica principal ─────────────────────────────────────────────────
    private void buscarTudo() {
        List<String> cidades = checksCidades.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.toList());

        if (cidades.isEmpty()) {
            labelStatus.setText("Selecione ao menos uma cidade.");
            return;
        }

        progresso.setVisible(true);
        labelStatus.setText("Buscando preços e receita...");
        tabelaPrecos.setItems(FXCollections.emptyObservableList());
        tabelaReceita.setItems(FXCollections.emptyObservableList());

        Task<Void> tarefa = new Task<>() {
            private List<PriceEntry> precos;
            private ReceitaCraft     receita;
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

    /** Monta colunas dinâmicas de recursos/artefatos conforme a receita atual. */
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
                @Override protected void updateItem(String v, boolean empty) {
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
                @Override protected void updateItem(String v, boolean empty) {
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
            String corCidade = BancoDeDados.CIDADES.stream()
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
        atualizarColunasRecursos();
        tabelaPrecos.setItems(FXCollections.observableArrayList(linhas));
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
            String idMat   = mat.getUniqueName();
            String iconeUrl = "https://render.albiononline.com/v1/item/" + idMat + ".png";

            // nome amigável: tenta mapear pelo BancoDeDados, senão usa o ID
            String nomeMat = BancoDeDados.getTodosItens().stream()
                    .filter(i -> idMat.contains(i.getId()))
                    .map(ItemDefinition::getNome)
                    .findFirst().orElse(idMat);

            String tipo = mat.isArtefato() ? "Artefato" : "Recurso";

            // melhor preço de compra
            PriceEntry pe = melhorCompra.get(idMat);
            String buyMax    = pe != null ? FormatadorUtil.formatarPreco(pe.getBuyMax()) : "—";
            String cidade    = pe != null ? pe.getCidade() : "—";
            String corCidade = pe != null
                    ? BancoDeDados.CIDADES.stream()
                    .filter(c -> c.getApiId().equals(pe.getCidade()))
                    .map(CidadeInfo::getCor).findFirst().orElse("#888")
                    : "#888";
            String data = pe != null
                    ? FormatadorUtil.formatarData(
                    (pe.getBuyDate() != null && !pe.getBuyDate().startsWith("0001"))
                            ? pe.getBuyDate() : pe.getSellDate())
                    : "—";

            linhas.add(new LinhaMaterial(
                    iconeUrl, nomeMat, tipo, mat.getCount(),
                    cidade, corCidade, buyMax, data));
        }

        tabelaReceita.setItems(FXCollections.observableArrayList(linhas));
    }

    // ── Utilitários de UI ─────────────────────────────────────────────────
    private TableColumn<LinhaPreco, String> criarColunaCidade(boolean ehPreco) {
        TableColumn<LinhaPreco, String> col = new TableColumn<>("Cidade");
        col.setPrefWidth(130);
        col.setCellValueFactory(r ->
                new javafx.beans.property.SimpleStringProperty(r.getValue().cidade));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                LinhaPreco linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));
                String nome = BancoDeDados.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(v))
                        .map(CidadeInfo::getNome).findFirst().orElse(v);
                HBox hb = new HBox(6, ponto, new Label(nome));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb); setText(null);
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
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—"); setGraphic(null); return;
                }
                LinhaMaterial linha = getTableView().getItems().get(getIndex());
                Circle ponto = new Circle(5, Color.web(linha.corCidade));
                String nome = BancoDeDados.CIDADES.stream()
                        .filter(c -> c.getApiId().equals(v))
                        .map(CidadeInfo::getNome).findFirst().orElse(v);
                HBox hb = new HBox(6, ponto, new Label(nome));
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb); setText(null);
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
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.equals("—")) {
                    setText("—"); setStyle("-fx-text-fill: #666; -fx-alignment: CENTER-RIGHT;");
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
        });
        return tf;
    }

    private int parseIntSafe(TextField campo, int padrao) {
        try { return Math.max(1, Integer.parseInt(campo.getText().trim())); }
        catch (Exception ex) { return padrao; }
    }
}