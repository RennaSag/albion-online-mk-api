package com.albionmarket.service;

import com.albionmarket.model.PriceEntry;
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
 * Serviço responsável por consultar preços na Albion Online Data API.
 * Endpoint: west.albion-online-data.com para servidor americano
 */
public class ApiService {

    private static final String API_BASE   = "https://west.albion-online-data.com";
    private static final int    BATCH_SIZE = 40; // máximo de IDs por requisição, de acordo com a documentação albion-data

    private final HttpClient cliente;

    public ApiService() {
        this.cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    /**
     * busca preços de um item para múltiplas combinações de tier x encantamento x qualidade.
     *
     * @param sufixoId  sufixo do item "MAIN_SWORD", "2H_CLAYMORE"
     * @param tier      número 1-8 ou -1 para todos
     * @param enchant   número 0-4 ou -1 para todos
     * @param quality   número 1-5 ou -1 para todos
     * @param cidades   lista de IDs de cidades
     * @return lista de PriceEntry com os dados recebidos
     */
    public List<PriceEntry> buscarPrecos(String sufixoId, int tier, int enchant,
                                         int quality, List<String> cidades)
            throws IOException, InterruptedException {

        // gera lista de tiers e encantamentos a buscar
        int[] tiers    = tier    == -1 ? new int[]{1,2,3,4,5,6,7,8} : new int[]{tier};
        int[] enchants = enchant == -1 ? new int[]{0,1,2,3,4}        : new int[]{enchant};
        int[] quals    = quality == -1 ? new int[]{1,2,3,4,5}         : new int[]{quality};

        // monta lista de item IDs
        List<String> itemIds = new ArrayList<>();
        for (int t : tiers) {
            for (int e : enchants) {
                String base = "T" + t + "_" + sufixoId;
                itemIds.add(e > 0 ? base + "@" + e : base);
            }
        }

        // monta parametro de qualidades
        StringBuilder qualParam = new StringBuilder();
        for (int i = 0; i < quals.length; i++) {
            if (i > 0) qualParam.append(",");
            qualParam.append(quals[i]);
        }

        // monta parametro de cidades
        String cidadesParam = String.join(",", cidades);

        // busca em lotes para não exceder o limite da URL
        List<PriceEntry> resultado = new ArrayList<>();
        for (int i = 0; i < itemIds.size(); i += BATCH_SIZE) {
            List<String> lote = itemIds.subList(i, Math.min(i + BATCH_SIZE, itemIds.size()));
            String idsParam   = String.join(",", lote);
            String url        = API_BASE + "/api/v2/stats/prices/" + idsParam
                              + ".json?locations=" + cidadesParam
                              + "&qualities=" + qualParam;

            resultado.addAll(executarRequisicao(url));
        }

        return resultado;
    }

    /** executa uma requisição HTTP e parseia o array JSON retornado. */
    private List<PriceEntry> executarRequisicao(String url)
            throws IOException, InterruptedException {

        HttpRequest requisicao = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> resposta = cliente.send(requisicao,
            HttpResponse.BodyHandlers.ofString());

        if (resposta.statusCode() != 200) {
            throw new IOException("Erro HTTP " + resposta.statusCode() + " para URL: " + url);
        }

        return parsearResposta(resposta.body());
    }

    /** converte o array JSON da API em objetos PriceEntry. */
    private List<PriceEntry> parsearResposta(String json) {
        List<PriceEntry> lista = new ArrayList<>();

        JsonArray array = JsonParser.parseString(json).getAsJsonArray();

        for (JsonElement el : array) {
            JsonObject obj = el.getAsJsonObject();

            String itemId   = getStr(obj, "item_id");
            String cidade   = getStr(obj, "city");
            int    quality  = getInt(obj, "quality");
            long   sellMin  = getLong(obj, "sell_price_min");
            long   buyMax   = getLong(obj, "buy_price_max");
            String sellDate = getStr(obj, "sell_price_min_date");
            String buyDate  = getStr(obj, "buy_price_max_date");

            if (itemId != null && cidade != null) {
                lista.add(new PriceEntry(itemId, cidade, quality,
                                         sellMin, buyMax, sellDate, buyDate));
            }
        }

        return lista;
    }

    // utilitarios de leitura segura de JSON

    private String getStr(JsonObject obj, String campo) {
        JsonElement el = obj.get(campo);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    private int getInt(JsonObject obj, String campo) {
        JsonElement el = obj.get(campo);
        return (el != null && !el.isJsonNull()) ? el.getAsInt() : 0;
    }

    private long getLong(JsonObject obj, String campo) {
        JsonElement el = obj.get(campo);
        return (el != null && !el.isJsonNull()) ? el.getAsLong() : 0L;
    }
}
