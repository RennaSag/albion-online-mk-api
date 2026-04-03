package com.albionmarket.ui;

import com.albionmarket.model.*;
import com.albionmarket.service.BancoDeDadosCraft;
import com.albionmarket.service.BuscaService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Tela de seleção do recurso a ser refinado.
 * Filtra apenas os itens da categoria "Recursos" do BancoDeDadosCraft:
 * METALBAR, LEATHER, CLOTH, PLANKS, STONEBLOCK
 * (ORE, HIDE, FIBER, WOOD, ROCK são brutos, não devem aparecer aqui)
 */
public class TelaRefinoSelecao {


    private static final List<String> IDS_REFINADOS = List.of(
            "METALBAR", "LEATHER", "CLOTH", "PLANKS", "STONEBLOCK"
    );


    private static final String PREF_TIER = "refino_tier";
    private static final String PREF_ENCHANT = "refino_enchant";
    private static final String PREF_ITEM_ID = "refino_item_id";
    private static final String PREF_ITEM_NOME = "refino_item_nome";
    private static final String PREF_BUSCA = "refino_busca";
    private static final Preferences PREFS =
            Preferences.userNodeForPackage(TelaRefinoSelecao.class);


    private final Stage palco;
    private final BuscaService buscaService = new BuscaService();
    private final List<CheckBox> checksCidades = new ArrayList<>();
    private ItemDefinition itemSelecionado = null;
    private boolean ignorarBuscaTexto = false;


    private final List<ItemDefinition> itensRefinaveis;


    private TextField campoBusca;
    private ComboBox<Subcategoria> cbSubcategoria;
    private ComboBox<ItemDefinition> cbItem;
    private ComboBox<String> cbTier;
    private ComboBox<String> cbEncantamento;
    private Label labelItemAtual;
    private ImageView iconItem;

    private final EstadoRefinoSelecao estadoAnterior;



    public TelaRefinoSelecao(Stage palco) {
        this.palco = palco;
        this.estadoAnterior = null;
        this.itensRefinaveis = carregarItensRefinaveis();
    }

    public TelaRefinoSelecao(Stage palco, EstadoRefinoSelecao estado) {
        this.palco = palco;
        this.estadoAnterior = estado;
        this.itensRefinaveis = carregarItensRefinaveis();
    }

    private List<ItemDefinition> carregarItensRefinaveis() {
        return BancoDeDadosCraft.getCategorias().stream()
                .filter(c -> c.getNome().equals("Recursos"))
                .flatMap(c -> c.getSubcategorias().stream())
                .flatMap(s -> s.getItens().stream())
                .filter(i -> IDS_REFINADOS.contains(i.getId()))
                .collect(Collectors.toList());
    }



    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarConteudo());

        palco.setTitle("Albion Online - Seleção de Refino");
        palco.getScene().setRoot(raiz);
        palco.setMinWidth(1280);
        palco.setMinHeight(720);

        if (estadoAnterior != null) restaurarEstado();
        else carregarPreferencias();
    }



    private void restaurarEstado() {
        cbTier.setValue(estadoAnterior.tier == -1 ? "Todos" : "T" + estadoAnterior.tier);

        String enchStr;
        if (estadoAnterior.enchant == -1) enchStr = "Todos";
        else if (estadoAnterior.enchant == 0) enchStr = "Sem encantamento";
        else enchStr = "." + estadoAnterior.enchant;
        cbEncantamento.setValue(enchStr);

        if (estadoAnterior.item != null) {
            itemSelecionado = estadoAnterior.item;
            campoBusca.setText(estadoAnterior.textoBusca != null
                    ? estadoAnterior.textoBusca : estadoAnterior.item.getNome());
            marcarSelecionado(estadoAnterior.item);
            atualizarIconeItem(montarIdIcone());
        }

        if (estadoAnterior.cidades != null) {
            checksCidades.forEach(cb -> {
                String apiId = (String) cb.getUserData();
                cb.setSelected(estadoAnterior.cidades.contains(apiId));
            });
        }
    }



    private HBox criarCabecalho() {
        Label titulo = new Label("Seleção de Recurso para Refino");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Label subtitulo = new Label("O que vamos refinar hoje?");
        subtitulo.setStyle("-fx-text-fill: #999;");

        VBox textos = new VBox(2, titulo, subtitulo);
        textos.setAlignment(Pos.CENTER);

        HBox cab = new HBox(textos);
        cab.setAlignment(Pos.CENTER);
        cab.setPadding(new Insets(14, 20, 14, 20));
        cab.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        return cab;
    }

    private VBox criarConteudo() {
        VBox conteudo = new VBox(10);
        conteudo.setPadding(new Insets(15, 60, 15, 60));
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.setStyle("-fx-background-color: #1e1e1e;");
        conteudo.getChildren().addAll(
                criarBlocoFiltros(),
                criarLabelItemAtual(),
                criarBotoesAcao());
        return conteudo;
    }

    private VBox criarBlocoFiltros() {
        VBox bloco = new VBox(6);
        bloco.setMaxWidth(500);


        bloco.getChildren().add(criarSecao("Busca por Nome"));
        campoBusca = new TextField();
        campoBusca.setPromptText("Ex.: tábua, couro, tecido, barra, bloco...");
        campoBusca.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #e0e0e0; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        campoBusca.textProperty().addListener((obs, ant, novo) -> onBuscaTexto(novo));
        bloco.getChildren().add(campoBusca);


        bloco.getChildren().add(criarSecao("Tipo de Recurso"));

        // Monta subcategorias filtradas: só as que tenham itens refináveis
        Categoria catRecursos = BancoDeDadosCraft.getCategorias().stream()
                .filter(c -> c.getNome().equals("Recursos"))
                .findFirst().orElse(null);

        List<Subcategoria> subcatsRefinaveis = new ArrayList<>();
        if (catRecursos != null) {
            for (Subcategoria sub : catRecursos.getSubcategorias()) {
                List<ItemDefinition> refinaveis = sub.getItens().stream()
                        .filter(i -> IDS_REFINADOS.contains(i.getId()))
                        .collect(Collectors.toList());
                if (!refinaveis.isEmpty()) {
                    // cria subcategoria filtrada com apenas os refinados
                    subcatsRefinaveis.add(new Subcategoria(sub.getNome(), refinaveis));
                }
            }
        }

        cbSubcategoria = new ComboBox<>();
        cbSubcategoria.setPromptText("Tipo de recurso");
        cbSubcategoria.setMaxWidth(Double.MAX_VALUE);
        cbSubcategoria.setItems(FXCollections.observableArrayList(subcatsRefinaveis));
        cbSubcategoria.setOnAction(e -> onSubcategoriaSelecionada());
        estilizarComboBox(cbSubcategoria);

        cbItem = new ComboBox<>();
        cbItem.setPromptText("Recurso refinado");
        cbItem.setMaxWidth(Double.MAX_VALUE);
        cbItem.setDisable(true);
        cbItem.setOnAction(e -> onItemSelecionado());
        estilizarComboBox(cbItem);

        bloco.getChildren().addAll(cbSubcategoria, cbItem);


        bloco.getChildren().add(criarSecao("Tier"));
        cbTier = new ComboBox<>();
        cbTier.setItems(FXCollections.observableArrayList(
                "Todos", "T2", "T3", "T4", "T5", "T6", "T7", "T8"));
        cbTier.setValue("Todos");
        cbTier.setMaxWidth(Double.MAX_VALUE);
        estilizarComboBox(cbTier);
        cbTier.setOnAction(ev -> {
            if (itemSelecionado != null) atualizarIconeItem(montarIdIcone());
        });
        bloco.getChildren().add(cbTier);


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
        for (CidadeInfo cidade : BancoDeDadosCraft.CIDADES) {
            CheckBox cb = new CheckBox(cidade.getNome());
            cb.setSelected(true);
            cb.setStyle("-fx-text-fill: #ccc;");
            cb.setUserData(cidade.getApiId());
            checksCidades.add(cb);
            gridCidades.getChildren().add(cb);
        }
        bloco.getChildren().add(gridCidades);

        HBox wrapper = new HBox(bloco);
        wrapper.setAlignment(Pos.CENTER);
        VBox container = new VBox(wrapper);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private VBox criarLabelItemAtual() {
        iconItem = new ImageView();
        iconItem.setFitWidth(80);
        iconItem.setFitHeight(80);
        iconItem.setPreserveRatio(true);
        iconItem.setSmooth(true);

        labelItemAtual = new Label("Nenhum recurso selecionado.");
        labelItemAtual.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        VBox vb = new VBox(8, iconItem, labelItemAtual);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    private VBox criarBotoesAcao() {
        Button btnSelecionar = new Button("Selecionar");
        btnSelecionar.setPrefWidth(160);
        btnSelecionar.setPrefHeight(40);
        btnSelecionar.setStyle("-fx-background-color: #5a8dee; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnSelecionar.setOnAction(e -> onSelecionar());

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setPrefWidth(160);
        btnLimpar.setPrefHeight(40);
        btnLimpar.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnLimpar.setOnAction(e -> limpar());

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setPrefWidth(160);
        btnVoltar.setPrefHeight(40);
        btnVoltar.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #ccc; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
        btnVoltar.setOnAction(e -> new TelaHome(palco).mostrar());

        VBox vb = new VBox(8, btnSelecionar, btnLimpar, btnVoltar);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    private void onBuscaTexto(String texto) {
        if (ignorarBuscaTexto || texto == null || texto.isBlank()) return;
        String textoBaixo = texto.toLowerCase();
        ItemDefinition encontrado = itensRefinaveis.stream()
                .filter(i -> i.getNome().toLowerCase().contains(textoBaixo)
                        || i.getKeywords().toLowerCase().contains(textoBaixo))
                .findFirst().orElse(null);
        itemSelecionado = encontrado;
        if (encontrado != null) {
            marcarSelecionado(encontrado);
            atualizarIconeItem(montarIdIcone());
        }
    }

    private void onSubcategoriaSelecionada() {
        Subcategoria sub = cbSubcategoria.getValue();
        campoBusca.clear();
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
            ignorarBuscaTexto = true;
            campoBusca.setText(itemSelecionado.getNome());
            ignorarBuscaTexto = false;
            marcarSelecionado(itemSelecionado);
            atualizarIconeItem(montarIdIcone());
        }
    }

    private void onSelecionar() {
        ItemDefinition item = itemSelecionado;

        if (item == null && !campoBusca.getText().isBlank()) {
            String textoBaixo = campoBusca.getText().toLowerCase();
            item = itensRefinaveis.stream()
                    .filter(i -> i.getNome().toLowerCase().contains(textoBaixo)
                            || i.getKeywords().toLowerCase().contains(textoBaixo))
                    .findFirst().orElse(null);
        }

        if (item == null) {
            labelItemAtual.setText("Selecione um recurso refinado antes de continuar.");
            labelItemAtual.setStyle("-fx-text-fill: #e05555; -fx-font-size: 13px;");
            return;
        }

        int tier = parseTier(cbTier.getValue());
        int enchant = parseEnchant(cbEncantamento.getValue());
        List<String> cidades = checksCidades.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.toList());

        EstadoRefinoSelecao estado = new EstadoRefinoSelecao(
                item, tier, enchant, campoBusca.getText(), cidades);
        salvarPreferencias(item, tier, enchant, campoBusca.getText());
        new TelaRefino(palco, item, tier, enchant, estado).mostrar();
    }

    private void limpar() {
        campoBusca.clear();
        cbSubcategoria.setValue(null);
        cbItem.setItems(FXCollections.emptyObservableList());
        cbItem.setDisable(true);
        cbTier.setValue("Todos");
        cbEncantamento.setValue("Todos");
        itemSelecionado = null;
        atualizarLabelSemSelecao();
    }


    private void marcarSelecionado(ItemDefinition item) {
        labelItemAtual.setText("Selecionado: " + item.getNome());
        labelItemAtual.setStyle("-fx-text-fill: #5a8dee; -fx-font-size: 13px;");
    }

    private void atualizarLabelSemSelecao() {
        labelItemAtual.setText("Nenhum recurso selecionado.");
        labelItemAtual.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
        if (iconItem != null) iconItem.setImage(null);
    }

    private void atualizarIconeItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            iconItem.setImage(null);
            return;
        }

        String url = "https://render.albiononline.com/v1/item/" + itemId + ".png";

        javafx.scene.image.Image img = new javafx.scene.image.Image(url, true);

        img.errorProperty().addListener((obs, oldVal, erro) -> {
            if (erro) {
                // 🔥 fallback automático SEM encantamento
                String semEnchant = itemId.contains("@")
                        ? itemId.split("@")[0]
                        : itemId;

                String fallbackUrl = "https://render.albiononline.com/v1/item/" + semEnchant + ".png";
                iconItem.setImage(new javafx.scene.image.Image(fallbackUrl, true));
            }
        });

        iconItem.setImage(img);
    }

    private String montarIdIcone() {
        if (itemSelecionado == null) return null;

        int t = parseTier(cbTier.getValue());
        int e = parseEnchant(cbEncantamento.getValue());

        t = (t == -1) ? 4 : t;
        e = (e == -1) ? 0 : e;

        String base = "T" + t + "_" + itemSelecionado.getId();

        return e > 0
                ? base + "_LEVEL" + e
                : base;
    }


    private void salvarPreferencias(ItemDefinition it, int tier, int enchant, String busca) {
        PREFS.put(PREF_TIER, tier == -1 ? "Todos" : "T" + tier);
        PREFS.put(PREF_ENCHANT, enchant == -1 ? "Todos"
                : enchant == 0 ? "Sem encantamento" : "." + enchant);
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
        if (itemId.isBlank()) return;
        if (!tierStr.isBlank()) cbTier.setValue(tierStr);
        if (!enchStr.isBlank()) cbEncantamento.setValue(enchStr);
        ItemDefinition found = itensRefinaveis.stream()
                .filter(i -> i.getId().equals(itemId)).findFirst().orElse(null);
        if (found != null) {
            itemSelecionado = found;
            campoBusca.setText(busca.isBlank() ? itemNome : busca);
            marcarSelecionado(found);
            atualizarIconeItem(montarIdIcone());
        }
    }


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