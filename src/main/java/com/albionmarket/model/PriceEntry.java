package com.albionmarket.model;

/**
 * preço de um item em uma cidade específica, de acordo com o resultado da API
 */
public class PriceEntry {

    private final String itemId;
    private final String cidade;
    private final int qualidade;
    private final long sellMin;     // preço mínimo de venda, oq tem la no mercado pra COMPRAR
    private final long buyMax;      // preço máximo de compra, oq tem la no mercado SENDO VENDIDO
    private final String sellDate;  // data da última atualização do pedido
    private final String buyDate;   // data da última atualização do pedido

    public PriceEntry(String itemId, String cidade, int qualidade,
                      long sellMin, long buyMax,
                      String sellDate, String buyDate) {
        this.itemId = itemId;
        this.cidade = cidade;
        this.qualidade = qualidade;
        this.sellMin = sellMin;
        this.buyMax = buyMax;
        this.sellDate = sellDate;
        this.buyDate = buyDate;
    }

    public String getItemId() {
        return itemId;
    }

    public String getCidade() {
        return cidade;
    }

    public int getQualidade() {
        return qualidade;
    }

    public long getSellMin() {
        return sellMin;
    }

    public long getBuyMax() {
        return buyMax;
    }

    public String getSellDate() {
        return sellDate;
    }

    public String getBuyDate() {
        return buyDate;
    }
}
