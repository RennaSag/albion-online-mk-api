package com.albionmarket.service;

/**
 * Centraliza todos os cálculos de lucro, custo e nutrição
 * que antes estavam espalhados dentro de TelaCraft e TelaRefino.
 */
public class CalculadoraService {

    private CalculadoraService() {
    }

    /**
     * Resultado completo de um cálculo de craft ou refino.
     */
    public static class ResultadoCalculo {
        public final double qtdFinal;
        public final double custoMateriais;
        public final double taxaBarraca;
        public final double taxaMercado;
        public final double custoTotal;
        public final double receitaTotal;
        public final double lucro;

        public ResultadoCalculo(double qtdFinal, double custoMateriais,
                                double taxaBarraca, double taxaMercado,
                                double custoTotal, double receitaTotal, double lucro) {
            this.qtdFinal = qtdFinal;
            this.custoMateriais = custoMateriais;
            this.taxaBarraca = taxaBarraca;
            this.taxaMercado = taxaMercado;
            this.custoTotal = custoTotal;
            this.receitaTotal = receitaTotal;
            this.lucro = lucro;
        }
    }

    /**
     * Calcula o resultado de um craft ou refino.
     *
     * @param qtdProduzir    quantidade que o usuário quer produzir
     * @param taxaRetorno    taxa de retorno em decimal (ex: 0.152)
     * @param taxaBarracaPct taxa da barraca em percentual (ex: 3.0)
     * @param itemValue      game value do item (para calcular nutrição)
     * @param custoMateriais custo total dos materiais já somado
     * @param melhorVenda    melhor preço de venda encontrado
     * @param possuiPremium  se o usuário tem premium
     */
    public static ResultadoCalculo calcular(
            double qtdProduzir,
            double taxaRetorno,
            double taxaBarracaPct,
            long itemValue,
            double custoMateriais,
            double melhorVenda,
            boolean possuiPremium) {

        double qtdFinal = qtdProduzir / (1.0 - taxaRetorno);
        double nutricao = (itemValue * qtdFinal) * 0.1125;
        double taxaBarraca = (taxaBarracaPct * nutricao) / 100.0;

        double taxaCompra = possuiPremium ? 0.03 : 0.05;
        double taxaVenda = possuiPremium ? 0.025 : 0.05;

        double custoMatComTaxa = custoMateriais * (1.0 + taxaCompra);
        double custoTotal = custoMatComTaxa + taxaBarraca;

        double receitaTotal = qtdFinal * melhorVenda;
        double taxaMercado = receitaTotal * taxaVenda;
        double lucro = receitaTotal - custoTotal - taxaMercado;

        return new ResultadoCalculo(
                qtdFinal, custoMatComTaxa, taxaBarraca,
                taxaMercado, custoTotal, receitaTotal, lucro);
    }

    /**
     * Calcula quantos diários completos são gerados ao craftar.
     *
     * @param tier                    tier do item (2-8)
     * @param enchant                 encantamento do item (0-4)
     * @param qtdMateriaisNaoArtefato quantidade de materiais não-artefato na receita
     * @param qtdFinal                quantidade final produzida (já com retorno)
     */
    public static double calcularDiarios(int tier, int enchant,
                                         int qtdMateriaisNaoArtefato, double qtdFinal) {
        double[] fameMultiplier = {0, 0, 1.5, 7.5, 22.5, 90.0, 270.0, 645.0, 1395.0};
        double[] famaNecessaria = {0, 0, 0, 0, 3600, 7200, 14400, 28380, 58590};

        if (tier < 2 || tier > 8) return 0;

        double famaPorCraft = qtdMateriaisNaoArtefato
                * fameMultiplier[tier]
                * Math.pow(2, enchant);

        if (famaNecessaria[tier] <= 0 || famaPorCraft <= 0) return 0;

        return (famaPorCraft * qtdFinal) / famaNecessaria[tier];
    }

    /**
     * Calcula o lucro adicional dos diários.
     * Lucro = (preço do cheio * qtd diários) - (preço do vazio * qtd diários)
     */
    public static double calcularLucroDiarios(double diariosCompletos,
                                              double precoDiarioVazio,
                                              double precoDiarioCheio) {
        return (precoDiarioCheio * diariosCompletos) - (precoDiarioVazio * diariosCompletos);
    }
}