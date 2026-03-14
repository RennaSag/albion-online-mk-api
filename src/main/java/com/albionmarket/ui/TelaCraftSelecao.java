package com.albionmarket.ui;

import com.albionmarket.model.Categoria;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.model.Subcategoria;
import com.albionmarket.service.BancoDeDados;
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

import java.util.List;

/**
 * tela de seleção de item para craft
 */
public class TelaCraftSelecao {

    private final Stage palco;
    private final BuscaService buscaService = new BuscaService();
    private final List<Categoria> categorias = BancoDeDados.getCategorias();

    private ItemDefinition itemSelecionado = null;

    private TextField campoBusca;
    private ComboBox<Categoria> cbCategoria;
    private ComboBox<Subcategoria> cbSubcategoria;
    private ComboBox<ItemDefinition> cbItem;
    private ComboBox<String> cbTier;
    private ComboBox<String> cbEncantamento;
    private Label labelItemAtual;
    private ImageView iconItem;

    public TelaCraftSelecao(Stage palco) {
        this.palco = palco;
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
        palco.setMaximized(true);
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
        cbTier.setOnAction(e -> {
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
        cbEncantamento.setOnAction(e -> {
            if (itemSelecionado != null) atualizarIconeItem(montarIdIcone());
        });
        bloco.getChildren().add(cbEncantamento);

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
        iconItem.setFitWidth(120);
        iconItem.setFitHeight(120);
        iconItem.setPreserveRatio(true);
        iconItem.setSmooth(true);

        labelItemAtual = new Label("Nenhum item selecionado.");
        labelItemAtual.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        VBox vb = new VBox(8, iconItem, labelItemAtual);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    // botão selecionar
    private HBox criarBotoesAcao() {
        Button btnSelecionar = new Button("Selecionar");
        btnSelecionar.setPrefWidth(160);
        btnSelecionar.setPrefHeight(42);
        btnSelecionar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnSelecionar.setOnAction(e -> onSelecionar());

        HBox hb = new HBox(btnSelecionar);
        hb.setAlignment(Pos.CENTER);
        return hb;
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

        // por enquanto só printa no console — a próxima tela virá aqui
        System.out.println("Item selecionado para craft: " + item.getId()
                + " | tier=" + tier + " | enchant=" + enchant);
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

    private String montarIdIcone() {
        if (itemSelecionado == null) return null;

        int tier = parseTier(cbTier.getValue());
        int enchant = parseEnchant(cbEncantamento.getValue());

        // se "todos", usa T4 como predefinicao
        int t = (tier == -1) ? 4 : tier;
        int e = (enchant == -1) ? 0 : enchant;

        String base = "T" + t + "_" + itemSelecionado.getId();
        return e > 0 ? base + "@" + e : base;
    }
}