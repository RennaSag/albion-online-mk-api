package com.albionmarket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TelaOperacoesAtivas {

    private final Stage palco;
    private VBox painelCards;

    public TelaOperacoesAtivas(Stage palco) {
        this.palco = palco;
    }


    public void mostrar() {
        Label titulo = new Label("Operações Ativas");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 22));
        titulo.setStyle("-fx-text-fill: #e0e0e0;");

        Button btnVoltar = new Button("Voltar");
        btnVoltar.getStyleClass().add("home-botao");
        btnVoltar.setOnAction(e -> new TelaHome(palco).mostrar());

        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle(
                "-fx-background-color: #5a8dee; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20;");
        btnAtualizar.setOnAction(e -> carregarCards());

        HBox cabecalho = new HBox(16, titulo, new Region(), btnAtualizar, btnVoltar);
        HBox.setHgrow(cabecalho.getChildren().get(1), Priority.ALWAYS);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(16, 20, 16, 20));
        cabecalho.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        painelCards = new VBox(14);
        painelCards.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(painelCards);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");

        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #1e1e1e;");
        raiz.setTop(cabecalho);
        raiz.setCenter(scroll);

        Scene cena = new Scene(raiz, 1280, 800);
        cena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

        palco.setTitle("Albion Market — Operações Ativas");
        palco.setScene(cena);
        palco.setResizable(true);
        palco.setMinWidth(900);
        palco.setMinHeight(600);
        palco.setMaximized(true);

        carregarCards();
    }

    private void carregarCards() {
        painelCards.getChildren().clear();

        try {
            List<Path> arquivos = Files.list(Paths.get("."))
                    .filter(p -> p.getFileName().toString().startsWith("operacao_")
                            && p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();

            if (arquivos.isEmpty()) {
                Label vazio = new Label("Nenhuma operação ativa no momento.");
                vazio.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
                painelCards.getChildren().add(vazio);
                return;
            }

            for (Path arquivo : arquivos) {
                try {
                    String json = Files.readString(arquivo);
                    VBox card = criarCard(json, arquivo);
                    painelCards.getChildren().add(card);
                } catch (Exception ex) {
                    // ignora arquivo corrompido
                }
            }

        } catch (IOException ex) {
            Label erro = new Label("Erro ao ler operações: " + ex.getMessage());
            erro.setStyle("-fx-text-fill: #e05555;");
            painelCards.getChildren().add(erro);
        }
    }

    private VBox criarCard(String json, Path arquivo) {
        String item = extrair(json, "item");
        String tier = extrair(json, "tier");
        String enchant = extrair(json, "encantamento");

        // calculadora — parse linha a linha mais robusto
        Map<String, String> calculadora = new LinkedHashMap<>();
        String blocoCalc = extrairBloco(json, "calculadora");
        if (blocoCalc != null) {
            for (String linha : blocoCalc.split("\n")) {
                linha = linha.trim();
                if (!linha.startsWith("\"")) continue;
                int sepIdx = linha.indexOf("\":");
                if (sepIdx == -1) continue;
                String chave = linha.substring(1, sepIdx).trim();
                String resto = linha.substring(sepIdx + 2).trim();
                // remove vírgula final
                if (resto.endsWith(",")) resto = resto.substring(0, resto.length() - 1).trim();
                // remove aspas
                if (resto.startsWith("\"") && resto.endsWith("\""))
                    resto = resto.substring(1, resto.length() - 1);
                if (!chave.isEmpty()) calculadora.put(chave, resto);
            }
        }

        // cabeçalho do card
        Label nomeItem = new Label(item + "  |  Tier " + tier
                + (!"0".equals(enchant) ? "  ·  Ench. ." + enchant : ""));
        nomeItem.setFont(Font.font("System", FontWeight.BOLD, 14));
        nomeItem.setStyle("-fx-text-fill: #e0e0e0;");

        Label nomeArquivo = new Label(arquivo.getFileName().toString());
        nomeArquivo.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");

        // apenas os campos que interessam, na ordem certa
        List<String> camposExibir = List.of(
                "Quantidade a craftar",
                "Qtd final craftada",
                "Melhor preco de venda",
                "Local",
                "Custo dos materiais",
                "Local de compra dos materiais",
                "Custo total",
                "Lucro/Prejuizo"
        );

        FlowPane boxCalc = new FlowPane();
        boxCalc.setHgap(20);
        boxCalc.setVgap(8);
        for (String campo : camposExibir) {
            if (campo.equals("Local de compra dos materiais")) {
                String blocoLocais = calculadora.getOrDefault(campo, "");
                if (blocoLocais.startsWith("[")) {
                    String[] entradas = blocoLocais.split("\\},\\s*\\{");
                    for (String entrada : entradas) {
                        String mat = extrairCampoInline(entrada, "material");
                        String cid = extrairCampoInline(entrada, "cidade");
                        String qtd = extrairCampoInlineNumero(entrada, "quantidade");
                        if (mat != null && cid != null) {
                            String label = qtd != null ? qtd + " - em " + cid : cid;
                            boxCalc.getChildren().add(miniLabel("Comprar: " + mat, label, "#5a8dee"));
                        }

                    }
                } else {
                    boxCalc.getChildren().add(miniLabel(campo, blocoLocais.isEmpty() ? "—" : blocoLocais, "#5a8dee"));
                }
                continue;
            }

            String valor = calculadora.getOrDefault(campo, "—");
            String cor = "#e0e0e0";
            if (campo.contains("Lucro")) cor = valor.startsWith("+") ? "#3dba6e" : "#e05555";
            else if (campo.equals("Custo dos materiais")) cor = "#e05555";
            else if (campo.equals("Local de compra dos materiais")) cor = "#5a8dee";
            else if (campo.contains("Custo")) cor = "#e05555";
            else if (campo.contains("venda")) cor = "#e05555";
            else if (campo.equals("Local")) cor = "#5a8dee";
            boxCalc.getChildren().add(miniLabel(campo, valor, cor));
        }

        // botão finalizar
        Button btnFinalizar = new Button("Finalizar Operação");
        btnFinalizar.setStyle(
                "-fx-background-color: #e05555; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 7 18;");
        btnFinalizar.setOnAction(ev -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Finalizar Operação");
            confirm.setHeaderText("Deseja finalizar esta operação?");
            confirm.setContentText(item + " — " + arquivo.getFileName());
            confirm.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    try {
                        Files.delete(arquivo);
                        carregarCards();
                    } catch (IOException ex) {
                        new Alert(Alert.AlertType.ERROR,
                                "Erro ao deletar: " + ex.getMessage()).show();
                    }
                }
            });
        });

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #333;");

        VBox card = new VBox(10,
                new HBox(10, nomeItem, new Region(), nomeArquivo),
                sep,
                boxCalc,
                btnFinalizar
        );
        HBox.setHgrow(((HBox) card.getChildren().get(0)).getChildren().get(1), Priority.ALWAYS);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #252525; -fx-background-radius: 8; "
                + "-fx-border-color: #333; -fx-border-radius: 8;");
        return card;
    }


    private VBox miniLabel(String titulo, String valor, String corValor) {
        Label t = new Label(titulo);
        t.setStyle("-fx-text-fill: #777; -fx-font-size: 10px;");
        Label v = new Label(valor != null ? valor : "—");
        v.setStyle("-fx-text-fill: " + corValor + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        return new VBox(2, t, v);
    }

    // extrai valor de "chave": "valor" ou "chave": numero
    private String extrair(String json, String chave) {
        String padrao = "\"" + chave + "\"";
        int idx = json.indexOf(padrao);
        if (idx == -1) return "—";
        int colon = json.indexOf(":", idx);
        if (colon == -1) return "—";
        String resto = json.substring(colon + 1).trim();
        if (resto.startsWith("\"")) {
            int fim = resto.indexOf("\"", 1);
            return fim == -1 ? "—" : resto.substring(1, fim);
        } else {
            // número
            StringBuilder sb = new StringBuilder();
            for (char c : resto.toCharArray()) {
                if (Character.isDigit(c) || c == '.' || c == '-') sb.append(c);
                else break;
            }
            return sb.toString();
        }
    }

    // extrai bloco {...} após uma chave
    private String extrairBloco(String json, String chave) {
        String padrao = "\"" + chave + "\"";
        int idx = json.indexOf(padrao);
        if (idx == -1) return null;
        int abre = json.indexOf("{", idx);
        if (abre == -1) return null;
        int nivel = 0;
        int fecha = -1;
        for (int i = abre; i < json.length(); i++) {
            if (json.charAt(i) == '{') nivel++;
            else if (json.charAt(i) == '}') {
                nivel--;
                if (nivel == 0) {
                    fecha = i;
                    break;
                }
            }
        }
        return fecha == -1 ? null : json.substring(abre + 1, fecha);
    }

    private String extrairCampoInline(String trecho, String chave) {
        String padrao = "\"" + chave + "\": \"";
        int idx = trecho.indexOf(padrao);
        if (idx == -1) return null;
        int inicio = idx + padrao.length();
        int fim = trecho.indexOf("\"", inicio);
        return fim == -1 ? null : trecho.substring(inicio, fim);
    }

    private String extrairCampoInlineNumero(String trecho, String chave) {
        String padrao = "\"" + chave + "\": ";
        int idx = trecho.indexOf(padrao);
        if (idx == -1) return null;
        int inicio = idx + padrao.length();
        StringBuilder sb = new StringBuilder();
        for (int i = inicio; i < trecho.length(); i++) {
            char c = trecho.charAt(i);
            if (Character.isDigit(c)) sb.append(c);
            else if (sb.length() > 0) break;
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}