package com.albionmarket.util;

/**
 * Utilitários para montar, parsear e interpretar IDs de itens do Albion Online.
 * Centraliza a lógica que estava repetida em TelaCraft, TelaRefino,
 * TelaCraftSelecao e TelaRefinoSelecao.
 */
public class AlbionIdUtil {

    private AlbionIdUtil() {}  // classe utilitária, não instanciar

    /**
     * Converte o valor do ComboBox de tier para inteiro.
     * "Todos" → -1, "T4" → 4
     */
    public static int parseTier(String val) {
        if (val == null || val.equals("Todos")) return -1;
        return Integer.parseInt(val.replace("T", ""));
    }

    /**
     * Converte o valor do ComboBox de encantamento para inteiro.
     * "Todos" → -1, "Sem encantamento" → 0, ".2" → 2
     */
    public static int parseEnchant(String val) {
        if (val == null || val.equals("Todos")) return -1;
        if (val.equals("Sem encantamento")) return 0;
        return Integer.parseInt(val.replace(".", ""));
    }

    /**
     * Extrai o tier de um uniqueName como "T5_METALBAR".
     * Retorna -1 se não conseguir.
     */
    public static int extrairTier(String uniqueName) {
        if (uniqueName != null
                && uniqueName.startsWith("T")
                && uniqueName.length() > 1
                && Character.isDigit(uniqueName.charAt(1))) {
            return Character.getNumericValue(uniqueName.charAt(1));
        }
        return -1;
    }

    /**
     * Monta o ID completo do item para a API.
     * Exemplo: tier=5, infixo="MAIN_SWORD", enchant=2 → "T5_MAIN_SWORD@2"
     */
    public static String buildApiId(String infixo, int tier, int enchant) {
        String base = "T" + tier + "_" + infixo;
        return enchant > 0 ? base + "@" + enchant : base;
    }

    /**
     * Resolve o tier e enchant com fallback para o padrão do jogo.
     * tier -1 vira 4, enchant -1 vira 0.
     */
    public static int tierEfetivo(int tier)   { return tier   == -1 ? 4 : tier; }
    public static int enchantEfetivo(int enc) { return enc    == -1 ? 0 : enc;  }
}