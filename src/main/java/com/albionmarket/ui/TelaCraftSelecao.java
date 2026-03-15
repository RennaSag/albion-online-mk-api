package com.albionmarket.ui;

import com.albionmarket.model.Categoria;
import com.albionmarket.model.EstadoCraftSelecao;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.model.Subcategoria;
import com.albionmarket.service.BancoDeDadosCraft;
import com.albionmarket.service.BuscaService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;

import java.util.List;
import java.util.prefs.Preferences;

/**
 * Tela de seleção de item para craft.
 * Possui filtro de busca idêntico ao de TelaPesquisaPrecos,
 * sem o seletor de qualidade.
 */
public class TelaCraftSelecao {

    private static final String PREF_TIER = "craft_tier";
    private static final String PREF_ENCHANT = "craft_enchant";
    private static final String PREF_ITEM_ID = "craft_item_id";
    private static final String PREF_ITEM_NOME = "craft_item_nome";
    private static final String PREF_BUSCA = "craft_busca";
    private static final Preferences PREFS =
            Preferences.userNodeForPackage(TelaCraftSelecao.class);

    private final Stage palco;
    private final BuscaService buscaService = new BuscaService();
    private final List<Categoria> categorias = BancoDeDadosCraft.getCategorias();

    private final List<CheckBox> checksCidades = new ArrayList<>();

    private ItemDefinition itemSelecionado = null;

    private TextField campoBusca;
    private ComboBox<Categoria> cbCategoria;
    private ComboBox<Subcategoria> cbSubcategoria;
    private ComboBox<ItemDefinition> cbItem;
    private ComboBox<String> cbTier;
    private ComboBox<String> cbEncantamento;
    private Label labelItemAtual;
    private ImageView iconItem;

    // estado anterior (pode ser null se for abertura limpa)
    private final EstadoCraftSelecao estadoAnterior;

    public TelaCraftSelecao(Stage palco) {
        this.palco = palco;
        this.estadoAnterior = null;
    }

    public TelaCraftSelecao(Stage palco, EstadoCraftSelecao estado) {
        this.palco = palco;
        this.estadoAnterior = estado;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarConteudo());
        raiz.setBottom(criarRodape());

        Scene cena = new Scene(raiz, 1100, 750);
        cena.getStylesheets().add(
                getClass().getResource("/estilos.css").toExternalForm()
        );

        palco.setTitle("Albion Market — Craft");
        palco.setScene(cena);
        palco.setResizable(true);
        palco.setMinWidth(800);
        palco.setMinHeight(600);
        palco.centerOnScreen();

        // restaura filtros: estado anterior (voltou da TelaCraft) ou preferências salvas
        if (estadoAnterior != null) {
            restaurarEstado();
        } else {
            carregarPreferencias();
        }
    }

    private void restaurarEstado() {
        // restaura tier
        String tierStr = estadoAnterior.tier == -1 ? "Todos" : "T" + estadoAnterior.tier;
        cbTier.setValue(tierStr);

        // restaura encantamento
        String enchStr;
        if (estadoAnterior.enchant == -1) enchStr = "Todos";
        else if (estadoAnterior.enchant == 0) enchStr = "Sem encantamento";
        else enchStr = "." + estadoAnterior.enchant;
        cbEncantamento.setValue(enchStr);

        // restaura item selecionado
        if (estadoAnterior.item != null) {
            itemSelecionado = estadoAnterior.item;
            campoBusca.setText(estadoAnterior.textoBusca != null
                    ? estadoAnterior.textoBusca : estadoAnterior.item.getNome());
            labelItemAtual.setText("Selecionado: " + estadoAnterior.item.getNome());
            labelItemAtual.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 13px;");
            atualizarIconeItem(montarIdIcone());
        }
    }

    // header igual ao de TelaPesquisaPrecos
    private HBox criarCabecalho() {
        Label titulo = new Label("Albion Market");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("O que vamos craftar hoje?");
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);
        textos.setAlignment(Pos.CENTER);
        HBox cabecalho = new HBox(textos);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(14, 20, 14, 20));
        cabecalho.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cabecalho;
    }

    // conteudo central com o filtro
    private VBox criarConteudo() {
        VBox conteudo = new VBox(20);
        conteudo.setPadding(new Insets(30, 60, 30, 60));
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.setStyle("-fx-background-color: #1e1e1e;");

        conteudo.getChildren().addAll(
                criarBlocoFiltros(),
                criarLabelItemAtual(),
                criarBotoesAcao()
        );

        return conteudo;
    }

    // bloco de filtros centralizado
    private VBox criarBlocoFiltros() {
        VBox bloco = new VBox(12);
        bloco.setMaxWidth(500);

        // busca por texto
        bloco.getChildren().add(criarSecao("Busca por Nome"));
        campoBusca = new TextField();
        campoBusca.setPromptText("Ex: espada larga, cajado sagrado...");
        campoBusca.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        campoBusca.textProperty().addListener((obs, ant, novo) -> onBuscaTexto(novo));
        bloco.getChildren().add(campoBusca);

        // categoria > subcategoria > item
        bloco.getChildren().add(criarSecao("Categoria"));

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

        bloco.getChildren().addAll(cbCategoria, cbSubcategoria, cbItem);

        // tier
        bloco.getChildren().add(criarSecao("Tier"));
        cbTier = new ComboBox<>();
        cbTier.setItems(FXCollections.observableArrayList(
                "Todos", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8"));
        cbTier.setValue("Todos");
        cbTier.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTier);
        cbTier.setOnAction(ev -> {
            if (itemSelecionado != null) atualizarIconeItem(montarIdIcone());
        });
        bloco.getChildren().add(cbTier);

        // encantamento
        bloco.getChildren().add(criarSecao("Encantamento"));
        cbEncantamento = new ComboBox<>();
        cbEncantamento.setItems(FXCollections.observableArrayList(
                "Todos", "Sem encantamento", ".1", ".2", ".3", ".4"));
        cbEncantamento.setValue("Todos");
        cbEncantamento.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbEncantamento);
        cbEncantamento.setOnAction(ev -> {
            if (itemSelecionado != null) atualizarIconeItem(montarIdIcone());
        });
        bloco.getChildren().add(cbEncantamento);

        bloco.getChildren().add(criarSecao("Cidades"));
        FlowPane gridCidades = new FlowPane(8, 8);
        for (com.albionmarket.model.CidadeInfo cidade : BancoDeDadosCraft.CIDADES) {
            CheckBox cb = new CheckBox(cidade.getNome());
            cb.setSelected(true);
            cb.setStyle("-fx-text-fill: #ccc;");
            cb.setUserData(cidade.getApiId());
            checksCidades.add(cb);
            gridCidades.getChildren().add(cb);
        }
        bloco.getChildren().add(gridCidades);

        // centraliza o bloco
        HBox wrapper = new HBox(bloco);
        wrapper.setAlignment(Pos.CENTER);

        VBox container = new VBox(wrapper);
        container.setAlignment(Pos.CENTER);



        return container;
    }

    // label e icone que mostram o item selecionado
    private VBox criarLabelItemAtual() {
        iconItem = new ImageView();
        iconItem.setFitWidth(80);
        iconItem.setFitHeight(80);
        iconItem.setPreserveRatio(true);
        iconItem.setSmooth(true);

        labelItemAtual = new Label("Nenhum item selecionado.");
        labelItemAtual.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        VBox vb = new VBox(8, iconItem, labelItemAtual);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    // botoes
    private VBox criarBotoesAcao() {
        Button btnSelecionar = new Button("Selecionar");
        btnSelecionar.setPrefWidth(160);
        btnSelecionar.setPrefHeight(42);
        btnSelecionar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnSelecionar.setOnAction(e -> onSelecionar());

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setPrefWidth(160);
        btnLimpar.setPrefHeight(42);
        btnLimpar.setStyle(
                "-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnLimpar.setOnAction(e -> limpar());

        VBox vb = new VBox(10, btnSelecionar, btnLimpar);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    private void limpar() {
        campoBusca.clear();
        cbCategoria.setValue(null);
        cbSubcategoria.setItems(FXCollections.emptyObservableList());
        cbSubcategoria.setDisable(true);
        cbItem.setItems(FXCollections.emptyObservableList());
        cbItem.setDisable(true);
        cbTier.setValue("Todos");
        cbEncantamento.setValue("Todos");
        itemSelecionado = null;
        atualizarLabelSemSelecao();
    }

    // botão voltar centralizado no rodapé
    private HBox criarRodape() {
        Button btnVoltar = new Button("Voltar");
        btnVoltar.setPrefWidth(120);
        btnVoltar.setPrefHeight(42);
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setOnAction(e -> new TelaHome(palco).mostrar());

        HBox hb = new HBox(btnVoltar);
        hb.setAlignment(Pos.CENTER);
        hb.setPadding(new Insets(0, 0, 24, 0));
        hb.setStyle("-fx-background-color: #1e1e1e;");
        return hb;
    }

    // icone do item selecionado
    private void atualizarIconeItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            iconItem.setImage(null);
            return;
        }
        String url = "https://render.albiononline.com/v1/item/" + itemId + ".png";
        iconItem.setImage(new javafx.scene.image.Image(url, true));
    }

    // logica dos filtros (igual a TelaPesquisaPrecos)
    private void onBuscaTexto(String texto) {
        itemSelecionado = null;
        List<ItemDefinition> sugestoes = buscaService.buscar(texto, 1);
        if (!sugestoes.isEmpty()) {
            itemSelecionado = sugestoes.get(0);
            labelItemAtual.setText("Selecionado: " + itemSelecionado.getNome());
            labelItemAtual.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 13px;");
            atualizarIconeItem(montarIdIcone());
        }
    }

    private void onCategoriaSelecionada() {
        Categoria cat = cbCategoria.getValue();
        campoBusca.clear();
        itemSelecionado = null;
        atualizarLabelSemSelecao();

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
        atualizarLabelSemSelecao();

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
            labelItemAtual.setText("Selecionado: " + itemSelecionado.getNome());
            labelItemAtual.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 13px;");
            atualizarIconeItem(montarIdIcone());
        }
    }

    private void atualizarLabelSemSelecao() {
        labelItemAtual.setText("Nenhum item selecionado.");
        labelItemAtual.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
        if (iconItem != null) iconItem.setImage(null);
    }

    private void onSelecionar() {
        // resolve item: combo ou busca por texto
        ItemDefinition item = itemSelecionado;
        if (item == null && !campoBusca.getText().isBlank()) {
            List<ItemDefinition> res = buscaService.buscar(campoBusca.getText(), 1);
            if (!res.isEmpty()) item = res.get(0);
        }

        if (item == null) {
            labelItemAtual.setText("Selecione um item antes de continuar.");
            labelItemAtual.setStyle("-fx-text-fill: #e05555; -fx-font-size: 13px;");
            return;
        }

        int tier = parseTier(cbTier.getValue());
        int enchant = parseEnchant(cbEncantamento.getValue());

        List<String> cidadesSelecionadas = checksCidades.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(java.util.stream.Collectors.toList());
        EstadoCraftSelecao estado = new EstadoCraftSelecao(
                item, tier, enchant, campoBusca.getText(), cidadesSelecionadas);
        salvarPreferencias(item, tier, enchant, campoBusca.getText());
        new TelaCraft(palco, item, tier, enchant, estado).mostrar();
    }

    // persistência de filtros
    private void salvarPreferencias(ItemDefinition it, int tier, int enchant, String busca) {
        PREFS.put(PREF_TIER, tier == -1 ? "Todos" : "T" + tier);
        PREFS.put(PREF_ENCHANT, enchant == -1 ? "Todos"
                : enchant == 0 ? "Sem encantamento"
                : "." + enchant);
        PREFS.put(PREF_ITEM_ID, it.getId());
        PREFS.put(PREF_ITEM_NOME, it.getNome());
        PREFS.put(PREF_BUSCA, busca != null ? busca : it.getNome());
    }

    private void carregarPreferencias() {
        String tierStr = PREFS.get(PREF_TIER, "");
        String enchStr = PREFS.get(PREF_ENCHANT, "");
        String itemId = PREFS.get(PREF_ITEM_ID, "");
        String itemNome = PREFS.get(PREF_ITEM_NOME, "");
        String busca = PREFS.get(PREF_BUSCA, "");

        if (itemId.isBlank()) return; // nada salvo ainda

        if (!tierStr.isBlank()) cbTier.setValue(tierStr);
        if (!enchStr.isBlank()) cbEncantamento.setValue(enchStr);

        if (!itemId.isBlank()) {
            // tenta encontrar o item no BancoDeDados
            ItemDefinition found = BancoDeDadosCraft.getTodosItens().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst().orElse(null);
            if (found != null) {
                itemSelecionado = found;
                campoBusca.setText(busca.isBlank() ? itemNome : busca);
                labelItemAtual.setText("Selecionado: " + found.getNome());
                labelItemAtual.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 13px;");
                atualizarIconeItem(montarIdIcone());
            }
        }
    }

    private String montarIdIcone() {
        if (itemSelecionado == null) return null;
        int t = parseTier(cbTier.getValue());
        int e = parseEnchant(cbEncantamento.getValue());
        t = (t == -1) ? 4 : t;
        e = (e == -1) ? 0 : e;
        String base = "T" + t + "_" + itemSelecionado.getId();
        return e > 0 ? base + "@" + e : base;
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

    private Label criarSecao(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-font-weight: bold;");
        return lbl;
    }

    private void estilizarComboBox(ComboBox<?> cb) {
        cb.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
    }
}