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


    public long buscarItemValue(String itemId) throws IOException, InterruptedException {
        String id = itemId.contains("@") ? itemId.split("@")[0] : itemId;
        HttpResponse<String> resp = executarGet(BASE + id + "/data");
        if (resp == null || resp.statusCode() != 200) return 0;
        try {
            return getLong(JsonParser.parseString(resp.body()).getAsJsonObject(), "itemValue");
        } catch (Exception e) { return 0; }
    }

    private ReceitaCraft tentarBuscar(String url, String itemId) throws IOException, InterruptedException {
        HttpResponse<String> resp = executarGet(url);
        if (resp == null || resp.statusCode() != 200) return null;
        return parsearReceita(itemId, resp.body());
    }

    private HttpResponse<String> executarGet(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET().build();
        return cliente.send(req, HttpResponse.BodyHandlers.ofString());
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
            List<ReceitaCraft.MaterialCraft> materiais = new ArrayList<>();
            for (JsonElement el : listaEl.getAsJsonArray()) {
                JsonObject mat = el.getAsJsonObject();
                String nome = getStr(mat, "uniqueName");
                int qtd = getInt(mat, "count");
                if (nome != null && qtd > 0) materiais.add(new ReceitaCraft.MaterialCraft(nome, qtd));
            }
            return materiais.isEmpty() ? null : new ReceitaCraft(itemId, tempo, focus, materiais);
        } catch (Exception e) { return null; }
    }

    private String getStr(JsonObject o, String c) { JsonElement e = o.get(c); return (e!=null&&!e.isJsonNull())?e.getAsString():null; }
    private int getInt(JsonObject o, String c) { JsonElement e = o.get(c); return (e!=null&&!e.isJsonNull())?e.getAsInt():0; }
    private long getLong(JsonObject o, String c) { JsonElement e = o.get(c); return (e!=null&&!e.isJsonNull())?e.getAsLong():0L; }
    private double getDouble(JsonObject o, String c) { JsonElement e = o.get(c); return (e!=null&&!e.isJsonNull())?e.getAsDouble():0.0; }
}