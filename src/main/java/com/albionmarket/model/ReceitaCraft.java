package com.albionmarket.model;

import java.util.List;

/**
 * receita de craft dos itens retornada pela gameinfo API, api pra receita
 */
public class ReceitaCraft {

    private final String itemId;
    private final double tempoSegundos;
    private final int craftingFocus;
    private final List<MaterialCraft> materiais;

    public ReceitaCraft(String itemId, double tempoSegundos,
                        int craftingFocus, List<MaterialCraft> materiais) {
        this.itemId = itemId;
        this.tempoSegundos = tempoSegundos;
        this.craftingFocus = craftingFocus;
        this.materiais = materiais;
    }

    public String getItemId() {
        return itemId;
    }

    public double getTempoSegundos() {
        return tempoSegundos;
    }

    public int getCraftingFocus() {
        return craftingFocus;
    }

    public List<MaterialCraft> getMateriais() {
        return materiais;
    }

    // material individual
    public static class MaterialCraft {

        private final String uniqueName; // ex: "T4_PLANKS", "T4_ARTEFACT_SWORD"
        private final int count;
        private final boolean ehArtefato;

        public MaterialCraft(String uniqueName, int count) {
            this.uniqueName = uniqueName;
            this.count = count;
            // artefatos têm "ARTEFACT" ou "RUNE"/"SOUL"/"RELIC" no nome
            this.ehArtefato = uniqueName.contains("ARTEFACT")
                    || uniqueName.contains("RUNE")
                    || uniqueName.contains("SOUL")
                    || uniqueName.contains("RELIC")
                    || uniqueName.contains("AVALON");
        }

        public String getUniqueName() {
            return uniqueName;
        }

        public int getCount() {
            return count;
        }

        public boolean isArtefato() {
            return ehArtefato;
        }

        /**
         * retorna o tier do material como inteiro, ou -1 se não tiver.
         */
        public int getTier() {
            if (uniqueName.startsWith("T") && uniqueName.length() > 1
                    && Character.isDigit(uniqueName.charAt(1))) {
                return Character.getNumericValue(uniqueName.charAt(1));
            }
            return -1;
        }
    }
}