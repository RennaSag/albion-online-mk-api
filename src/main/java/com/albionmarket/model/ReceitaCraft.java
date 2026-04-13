package com.albionmarket.model;

import java.util.List;
import java.util.Map;

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

    public List<MaterialCraft> getMateriais() {
        return materiais;
    }

    // material individual
    public static class MaterialCraft {

        private final String uniqueName; // ex: "T4_PLANKS", "T4_ARTEFACT_SWORD"
        private final int count;
        private final boolean ehArtefato;

        public MaterialCraft(String uniqueName, int count, boolean ehArtefato) {
            this.uniqueName = uniqueName;
            this.count = count;
            this.ehArtefato = ehArtefato;
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