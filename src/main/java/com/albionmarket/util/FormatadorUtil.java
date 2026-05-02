package com.albionmarket.util;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class FormatadorUtil {

    private static final NumberFormat FMT_NUMERO =
        NumberFormat.getNumberInstance(new Locale("pt", "BR"));

    /** formata um valor de prata (ex: 1234567 → "1.234.567"), retorna "—" se zero. */
    public static String formatarPreco(long valor) {
        if (valor <= 0) return "—";
        return FMT_NUMERO.format(valor);
    }

    /**
     * converte uma data ISO 8601 em texto relativo legível,
     * como: "agora", "3min", "2h", "5d"
     * isso aqui na coluna "ultima atualizacao da tabela de pesquisa de precos */
    public static String formatarData(String isoStr) {
        if (isoStr == null || isoStr.isBlank() || isoStr.startsWith("0001")) return "—";

        try {
            Instant data;
            try {
                data = Instant.parse(isoStr);
            } catch (Exception ex) {
                data = java.time.LocalDateTime.parse(isoStr)
                        .toInstant(java.time.ZoneOffset.UTC);
            }
            long minutos = ChronoUnit.MINUTES.between(data, Instant.now());
            if (minutos <  2)    return "agora";
            if (minutos < 60)    return minutos + "min";
            if (minutos < 1440)  return (minutos / 60) + "h";
            return (minutos / 1440) + "d";

        } catch (Exception e) {
            return "—";
        }
    }


    /** nome em portugues das qualidades (1=Normal … 5=Obra-prima). */
    public static String nomeQualidade(int qualidade) {
        return switch (qualidade) {
            case 1 -> "Normal";
            case 2 -> "Boa";
            case 3 -> "Notável";
            case 4 -> "Excelente";
            case 5 -> "Obra-prima";
            default -> "?";
        };
    }


    /**
     * Converte uma string de preço formatada em pt-BR de volta para double.
     * Ex: "1.234.567" → 1234567.0, "—" → 0.0
     */
    public static double parseSilver(String val) {
        if (val == null || val.equals("-") || val.equals("—")) return 0;
        try {
            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            return fmt.parse(val).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Formata um double sem casas decimais.
     * Ex: 1.0 → "1"
     */
    public static String fmt(double v) {
        return String.format("%.0f", v);
    }

    /**
     * Formata um valor em prata de forma legível.
     * Ex: 1500000.0 → "1.50M de prata", 1500.0 → "1.5K de prata"
     */
    public static String fmtSilver(double v) {
        if (Math.abs(v) >= 1_000_000) return String.format("%.2fM de prata", v / 1_000_000);
        if (Math.abs(v) >= 1_000)     return String.format("%.1fK de prata", v / 1_000);
        return String.format("%.0f prata", v);
    }
}
