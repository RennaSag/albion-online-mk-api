package com.albionmarket.service;

import com.albionmarket.model.ReceitaCraft;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


//classe que vai me trazer a receita dos itens, os artefatos ainda não estão com o nome
//cadastrados, preciso arrumar isso
public class CraftService {

    private static final String BASE = "https://gameinfo.albiononline.com/api/gameinfo/items/";
    private final HttpClient cliente;

    public CraftService() {
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public ReceitaCraft buscarReceita(String itemId) throws IOException, InterruptedException {
        ReceitaCraft r = tentarBuscar(BASE + itemId + "/data", itemId);
        if (r == null && itemId.contains("@")) {
            String base = itemId.split("@")[0];
            r = tentarBuscar(BASE + base + "/data", base);
        }
        return r;
    }

/* Funcao do Valor do item, na api parece q não tem essa informação, mas ta tudo cadastrado em ItemValues.java
    public long buscarItemValue(String itemId) throws IOException, InterruptedException {
        String id = itemId.contains("@") ? itemId.split("@")[0] : itemId;
        HttpResponse<String> resp = executarGet(BASE + id + "/data");
        if (resp == null || resp.statusCode() != 200) return 0;
        try {
            return getLong(JsonParser.parseString(resp.body()).getAsJsonObject(), "itemValue");
        } catch (Exception e) { return 0; }
    }

 */

    //busca na url da api o id do item que quero
    private ReceitaCraft tentarBuscar(String url, String itemId) throws IOException, InterruptedException {
        HttpResponse<String> resp = executarGet(url);
        if (resp == null || resp.statusCode() != 200) return null;
        return parsearReceita(itemId, resp.body());
    }

    //execucao da requisicao http da url pra eu trazer esses dados do json da api
    private HttpResponse<String> executarGet(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET().build();
        return cliente.send(req, HttpResponse.BodyHandlers.ofString());
    }

    //aqui ele me traz as informações da receira do elemento rotulado como "craftingRequirements"
    private ReceitaCraft parsearReceita(String itemId, String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonElement craftingEl = root.get("craftingRequirements");
            if (craftingEl == null || craftingEl.isJsonNull()) return null;

            JsonObject crafting = craftingEl.getAsJsonObject();
            double tempo = getDouble(crafting, "time");

            int focus = getInt(crafting, "craftingFocus");
            JsonElement listaEl = crafting.get("craftResourceList");
            /* interessante que ele traz pra mim o custo de foco pra aquele item, mas
            isso no jogo envolve questões de spec, que eu ainda tenho que aprender a fazer
            esse calculo pra usar esse campo "craftingFocus", que ainda não sei usar
            e não ta implementado
             */

            if (listaEl == null || listaEl.isJsonNull()) return null;
            List<ReceitaCraft.MaterialCraft> materiais = new ArrayList<>();
            /* lista dos materias (a receita" pro meu item, que pode
            ser um item só, ou 1 item 1 artefato, ou 2 itens 1 artefato etc
             */

            for (JsonElement el : listaEl.getAsJsonArray()) {
                JsonObject mat = el.getAsJsonObject();
                String nome = getStr(mat, "uniqueName");
                int qtd = getInt(mat, "count");
                if (nome != null && qtd > 0) materiais.add(new ReceitaCraft.MaterialCraft(nome, qtd));
                /* aqui ele me traz a quantidade de tal material pra receita

                 */
            }
            return materiais.isEmpty() ? null : new ReceitaCraft(itemId, tempo, focus, materiais);
        } catch (Exception e) {
            return null;
        }
    }


    //esses getters aqui é só pra eu trazer as infos de antes
    private String getStr(JsonObject o, String c) {
        JsonElement e = o.get(c);
        return (e != null && !e.isJsonNull()) ? e.getAsString() : null;
    }

    private int getInt(JsonObject o, String c) {
        JsonElement e = o.get(c);
        return (e != null && !e.isJsonNull()) ? e.getAsInt() : 0;
    }

    private long getLong(JsonObject o, String c) {
        JsonElement e = o.get(c);
        return (e != null && !e.isJsonNull()) ? e.getAsLong() : 0L;
    }

    private double getDouble(JsonObject o, String c) {
        JsonElement e = o.get(c);
        return (e != null && !e.isJsonNull()) ? e.getAsDouble() : 0.0;
    }
}