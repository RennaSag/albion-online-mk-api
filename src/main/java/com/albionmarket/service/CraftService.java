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

/**
 * busca receitas de craft na Albion Online Gameinfo API
 * Endpoint: gameinfo.albiononline.com/api/gameinfo/items/{itemId}/data
 */
public class CraftService {

    private static final String BASE = "https://gameinfo.albiononline.com/api/gameinfo/items/";

    private final HttpClient cliente;

    public CraftService() {
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * busca a receita de craft de um item especifico
     *
     * @param itemId ID completo do item, como: "T4_MAIN_SWORD" ou "T5_MAIN_SWORD@2"
     * @return ReceitaCraft com a lista de materiais, ou null se não tiver receita
     */

    public ReceitaCraft buscarReceita(String itemId)
            throws IOException, InterruptedException {

        // tenta com o ID completo primeiro, se falhar usa o ID base sem encantamento
        String url = BASE + itemId + "/data";
        ReceitaCraft resultado = tentarBuscar(url, itemId);
        if (resultado == null && itemId.contains("@")) {
            String idBase = itemId.split("@")[0];
            url = BASE + idBase + "/data";
            resultado = tentarBuscar(url, idBase);
        }
        return resultado;
    }

    private ReceitaCraft tentarBuscar(String url, String itemId)
            throws IOException, InterruptedException {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = cliente.send(req,
                HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) return null;
        return parsearReceita(itemId, resp.body());
    }

    private ReceitaCraft parsearReceita(String itemId, String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            JsonElement craftingEl = root.get("craftingRequirements");
            if (craftingEl == null || craftingEl.isJsonNull()) return null;

            JsonObject crafting = craftingEl.getAsJsonObject();

            double tempo = getDouble(crafting, "time");
            int focus = getInt(crafting, "craftingFocus");

            JsonElement listaEl = crafting.get("craftResourceList");
            if (listaEl == null || listaEl.isJsonNull()) return null;

            JsonArray lista = listaEl.getAsJsonArray();
            List<ReceitaCraft.MaterialCraft> materiais = new ArrayList<>();

            for (JsonElement el : lista) {
                JsonObject mat = el.getAsJsonObject();
                String nome = getStr(mat, "uniqueName");
                int qtd = getInt(mat, "count");
                if (nome != null && qtd > 0) {
                    materiais.add(new ReceitaCraft.MaterialCraft(nome, qtd));
                }
            }

            if (materiais.isEmpty()) return null;

            return new ReceitaCraft(itemId, tempo, focus, materiais);

        } catch (Exception e) {
            return null;
        }
    }

    // utilitarios
    private String getStr(JsonObject obj, String campo) {
        JsonElement el = obj.get(campo);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    private int getInt(JsonObject obj, String campo) {
        JsonElement el = obj.get(campo);
        return (el != null && !el.isJsonNull()) ? el.getAsInt() : 0;
    }

    private double getDouble(JsonObject obj, String campo) {
        JsonElement el = obj.get(campo);
        return (el != null && !el.isJsonNull()) ? el.getAsDouble() : 0.0;
    }
}