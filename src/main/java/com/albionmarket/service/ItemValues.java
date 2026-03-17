package com.albionmarket.service;

import java.util.HashMap;
import java.util.Map;

/**
 * valores base dos itens para calculo de nutricao no craft
 * o valor real de um item e calculado pela formula:
 * valor = valorBase * 2^(tier - tierBase) * 2^encantamento
 */
public class ItemValues {

    // par de dados: tier minimo do item e valor nesse tier sem encantamento
    public static class ValorBase {
        public final int tierMinimo;
        public final double valor;

        public ValorBase(int tierMinimo, double valor) {
            this.tierMinimo = tierMinimo;
            this.valor = valor;
        }
    }

    // mapa de sufixo do item (igual ao usado no BancoDeDados) -> ValorBase
    private static final Map<String, ValorBase> VALORES = new HashMap<>();

    static {
        // sapatos / botas
        // botas do soldado: t2=8, t4.0=32, t5.0=64(errado)... usando t4.0=128
        // t4.0=128, t5.0=256, t6.0=512, t8.4=32768
        reg("SHOES_PLATE_SET1", 4, 128);
        reg("SHOES_PLATE_SET2", 4, 128);
        reg("SHOES_PLATE_SET3", 4, 128);
        reg("SHOES_PLATE_HELL", 4, 128);
        reg("SHOES_PLATE_MORGANA", 4, 128);
        reg("SHOES_PLATE_AVALON", 4, 128);
        reg("SHOES_PLATE_ROYAL", 4, 128);

        reg("SHOES_LEATHER_SET1", 4, 128);
        reg("SHOES_LEATHER_SET2", 4, 128);
        reg("SHOES_LEATHER_SET3", 4, 128);
        reg("SHOES_LEATHER_HELL", 4, 128);
        reg("SHOES_LEATHER_MORGANA", 4, 128);
        reg("SHOES_LEATHER_AVALON", 4, 128);
        reg("SHOES_LEATHER_ROYAL", 4, 128);

        reg("SHOES_CLOTH_SET1", 4, 128);
        reg("SHOES_CLOTH_SET2", 4, 128);
        reg("SHOES_CLOTH_SET3", 4, 128);
        reg("SHOES_CLOTH_HELL", 4, 128);
        reg("SHOES_CLOTH_MORGANA", 4, 128);
        reg("SHOES_CLOTH_AVALON", 4, 128);
        reg("SHOES_CLOTH_ROYAL", 4, 128);


        //  armaduras (peitoral)
        reg("ARMOR_PLATE_SET1", 4, 128);
        reg("ARMOR_PLATE_SET2", 4, 128);
        reg("ARMOR_PLATE_SET3", 4, 128);
        reg("ARMOR_PLATE_HELL", 4, 128);
        reg("ARMOR_PLATE_MORGANA", 4, 128);
        reg("ARMOR_PLATE_KEEPER", 4, 128);
        reg("ARMOR_PLATE_UNDEAD", 4, 128);
        reg("ARMOR_PLATE_AVALON", 4, 128);
        reg("ARMOR_PLATE_ROYAL", 4, 128);

        reg("ARMOR_LEATHER_SET1", 4, 128);
        reg("ARMOR_LEATHER_SET2", 4, 128);
        reg("ARMOR_LEATHER_SET3", 4, 128);
        reg("ARMOR_LEATHER_HELL", 4, 128);
        reg("ARMOR_LEATHER_MORGANA", 4, 128);
        reg("ARMOR_LEATHER_KEEPER", 4, 128);
        reg("ARMOR_LEATHER_UNDEAD", 4, 128);
        reg("ARMOR_LEATHER_AVALON", 4, 128);
        reg("ARMOR_LEATHER_ROYAL", 4, 128);

        reg("ARMOR_CLOTH_SET1", 4, 128);
        reg("ARMOR_CLOTH_SET2", 4, 128);
        reg("ARMOR_CLOTH_SET3", 4, 128);
        reg("ARMOR_CLOTH_HELL", 4, 128);
        reg("ARMOR_CLOTH_MORGANA", 4, 128);
        reg("ARMOR_CLOTH_KEEPER", 4, 128);
        reg("ARMOR_CLOTH_UNDEAD", 4, 128);
        reg("ARMOR_CLOTH_AVALON", 4, 128);
        reg("ARMOR_CLOTH_ROYAL", 4, 128);


        // capacetes
        reg("HEAD_PLATE_SET1", 4, 128);
        reg("HEAD_PLATE_SET2", 4, 128);
        reg("HEAD_PLATE_SET3", 4, 128);
        reg("HEAD_PLATE_HELL", 4, 128);
        reg("HEAD_PLATE_MORGANA", 4, 128);
        reg("HEAD_PLATE_KEEPER", 4, 128);
        reg("HEAD_PLATE_UNDEAD", 4, 128);
        reg("HEAD_PLATE_AVALON", 4, 128);
        reg("HEAD_PLATE_ROYAL", 4, 128);

        reg("HEAD_LEATHER_SET1", 4, 128);
        reg("HEAD_LEATHER_SET2", 4, 128);
        reg("HEAD_LEATHER_SET3", 4, 128);
        reg("HEAD_LEATHER_HELL", 4, 128);
        reg("HEAD_LEATHER_MORGANA", 4, 128);
        reg("HEAD_LEATHER_KEEPER", 4, 128);
        reg("HEAD_LEATHER_UNDEAD", 4, 128);
        reg("HEAD_LEATHER_AVALON", 4, 128);
        reg("HEAD_LEATHER_ROYAL", 4, 128);

        reg("HEAD_CLOTH_SET1", 4, 128);
        reg("HEAD_CLOTH_SET2", 4, 128);
        reg("HEAD_CLOTH_SET3", 4, 128);
        reg("HEAD_CLOTH_HELL", 4, 128);
        reg("HEAD_CLOTH_MORGANA", 4, 128);
        reg("HEAD_CLOTH_KEEPER", 4, 128);
        reg("HEAD_CLOTH_UNDEAD", 4, 128);
        reg("HEAD_CLOTH_AVALON", 4, 128);
        reg("HEAD_CLOTH_ROYAL", 4, 128);


        // armas 1h
        reg("MAIN_SWORD", 4, 128);
        reg("MAIN_SCIMITAR_MORGANA", 4, 434);
        reg("MAIN_AXE", 3, 192);
        reg("MAIN_MACE", 4, 128);
        reg("MAIN_MACE_HELL", 4, 128);
        reg("MAIN_SPEAR", 4, 128);
        reg("MAIN_DAGGER", 4, 128);
        reg("MAIN_ENIGMATICORB_AVALON", 4, 128);

        // armas 2h
        reg("2H_CLAYMORE", 4, 256);
        reg("2H_DUALSWORD", 4, 256);
        reg("2H_CLEAVER_HELL", 4, 256);
        reg("2H_DUALSCIMITAR_UNDEAD", 4, 256);
        reg("2H_KINGMAKER", 4, 256);
        reg("2H_AXE", 4, 256);
        reg("2H_HALBERD", 4, 256);
        reg("2H_HALBERD_MORGANA", 4, 256);
        reg("2H_SCYTHE_HELL", 4, 256);
        reg("2H_BEARCLAW_AVALON", 4, 256);
        reg("2H_REALMBREAKER_AVALON", 4, 256);
        reg("2H_MACE", 4, 256);
        reg("2H_DUALMACE_AVALON", 4, 256);
        reg("2H_HAMMER_UNDEAD", 4, 562);
        reg("2H_HAMMER", 4, 256);
        reg("2H_POLEHAMMER", 4, 256);
        reg("2H_SPEAR", 4, 256);
        reg("2H_HARPOON_HELL", 4, 256);
        reg("2H_DAGGER", 4, 256);
        reg("2H_CLAWS_HELL", 4, 256);
        reg("2H_QUARTERSTAFF", 4, 256);
        reg("2H_KNUCKLES_SET2", 4, 256);
        reg("2H_KNUCKLES_HELL", 4, 256);
        reg("2H_KNUCKLES_AVALON", 4, 256);
        reg("2H_BOW", 4, 256);
        reg("2H_BOW_AVALON", 4, 256);
        reg("2H_CROSSBOW", 4, 256);
        reg("2H_DUALCROSSBOW_HELL", 4, 256);

        // cajados 1h
        reg("MAIN_ARCANESTAFF", 4, 128);
        reg("MAIN_FIRESTAFF", 4, 128);
        reg("MAIN_FROSTSTAFF", 4, 128);
        reg("MAIN_CURSESTAFF", 4, 128);
        reg("MAIN_HOLYSTAFF", 4, 128);
        reg("MAIN_NATURESTAFF", 4, 128);
        reg("MAIN_LONGBOW_UNDEAD", 4, 128);

        // cajados 2h
        reg("2H_ARCANESTAFF", 4, 256);
        reg("2H_FIRESTAFF", 4, 256);
        reg("2H_FROSTSTAFF", 4, 256);
        reg("2H_HOLYSTAFF", 4, 256);
        reg("2H_NATURESTAFF", 4, 256);


        //bolsa
        reg("BAG", 2, 128);
        reg("BAG_INSIGHT", 4, 256);

        //consumivel
        reg("MEAL_GRILLEDFISH", 1, 20);
        reg("MEAL_SALAD", 1, 5);
        reg("MEAL_SALAD_FISH", 2, 106);
        reg("MEAL_SOUP", 1, 77);
        reg("MEAL_SOUP_FISH", 1, 106);
        reg("MEAL_OMELETTE", 3, 77);
        reg("MEAL_OMELETTE_FISH", 3, 116);
        reg("MEAL_STEW", 4, 91);
        reg("MEAL_STEW_FISH", 3, 78);
        reg("MEAL_PIE", 3, 78);
        reg("MEAL_PIE_FISH", 2, 120);
        reg("MEAL_SANDWICH", 4, 81);
        reg("MEAL_SANDWICH_FISH", 4, 120);
        reg("MEAL_SANDWICH_AVALON", 4, 88);

        /*
        esses itens estao faltando, e os de cima podem estar errados


        reg("2H_SCYTHE_CRYSTAL", 0, 0);
        reg("2H_FLAIL", 0, 0);
        reg("MAIN_ROCKMACE_KEEPER", 0, 0);
        reg("2H_MACE_MORGANA", 0, 0);
        reg("MAIN_MACE_CRYSTAL", 0, 0);
        reg("MAIN_HAMMER", 0, 0);
        reg("2H_DUALHAMMER_HELL", 0, 0);
        reg("2H_RAM_KEEPER", 0, 0);
        reg("2H_HAMMER_AVALON", 0, 0);
        reg("2H_HAMMER_CRYSTAL", 0, 0);
        reg("2H_GLAIVE", 0, 0);
        reg("MAIN_SPEAR_KEEPER", 0, 0);
        reg("2H_TRIDENT_UNDEAD", 0, 0);
        reg("MAIN_SPEAR_LANCE_AVALON", 0, 0);
        reg("2H_GLAIVE_CRYSTAL", 0, 0);
        reg("MAIN_RAPIER_MORGANA", 0, 0);
        reg("2H_DAGGER_KATAR_AVALON", 0, 0);
        reg("2H_DUALSICKLE_UNDEAD", 0, 0);
        reg("MAIN_DAGGER_HELL", 0, 0);
        reg("2H_DAGGERPAIR_CRYSTAL", 0, 0);
        reg("2H_IRONCLADEDSTAFF", 0, 0);
        reg("2H_DOUBLEBLADEDSTAFF", 0, 0);
        reg("2H_COMBATSTAFF_MORGANA", 0, 0);
        reg("2H_TWINSCYTHE_HELL", 0, 0);
        reg("2H_ROCKSTAFF_KEEPER", 0, 0);
        reg("2H_QUARTERSTAFF_AVALON", 0, 0);
        reg("2H_DOUBLEBLADEDSTAFF_CRYSTAL", 0, 0);
        reg("2H_KNUCKLES_SET", 0, 0);
        reg("2H_KNUCKLES_SET3", 0, 0);
        reg("2H_KNUCKLES_KEEPER", 0, 0);
        reg("2H_KNUCKLES_MORGANA", 0, 0);
        reg("2H_KNUCKLES_CRYSTAL", 0, 0);
        reg("2H_ENIGMATICSTAFF", 0, 0);
        reg("MAIN_ARCANESTAFF_UNDEAD", 0, 0);
        reg("2H_ARCANESTAFF_HELL", 0, 0);
        reg("2H_ENIGMATICORB_MORGANA", 0, 0);
        reg("2H_ARCANE_RINGPAIR_AVALON", 0, 0);
        reg("2H_ARCANESTAFF_CRYSTAL", 0, 0);
        reg("2H_INFERNOSTAFF", 0, 0);
        reg("MAIN_FIRESTAFF_KEEPER", 0, 0);
        reg("2H_FIRESTAFF_HELL", 0, 0);
        reg("2H_FIRE_RINGPAIR_AVALON", 0, 0);
        reg("MAIN_FIRESTAFF_CRYSTAL", 0, 0);
        reg("2H_GLACIALSTAFF", 0, 0);
        reg("MAIN_FROSTSTAFF_KEEPER", 0, 0);
        reg("2H_ICEGAUNTLETS_HELL", 0, 0);
        reg("2H_ICECRYSTAL_UNDEAD", 0, 0);
        reg("MAIN_FROSTSTAFF_AVALON", 0, 0);
        reg("2H_FROSTSTAFF_CRYSTAL", 0, 0);
        reg("2H_CURSEDSTAFF", 0, 0);
        reg("2H_DEMONICSTAFF", 0, 0);
        reg("MAIN_CURSEDSTAFF_UNDEAD", 0, 0);
        reg("2H_SKULLORB_HELL", 0, 0);
        reg("2H_CURSEDSTAFF_MORGANA", 0, 0);
        reg("MAIN_CURSEDSTAFF_AVALON", 0, 0);
        reg("MAIN_CURSEDSTAFF_CRYSTAL", 0, 0);
        reg("2H_DIVINESTAFF", 0, 0);
        reg("MAIN_HOLYSTAFF_MORGANA", 0, 0);
        reg("2H_HOLYSTAFF_HELL", 0, 0);
        reg("2H_HOLYSTAFF_UNDEAD", 0, 0);
        reg("MAIN_HOLYSTAFF_AVALON", 0, 0);
        reg("2H_HOLYSTAFF_CRYSTAL", 0, 0);
        reg("2H_WILDSTAFF", 0, 0);
        reg("MAIN_NATURESTAFF_KEEPER", 0, 0);
        reg("2H_NATURESTAFF_HELL", 0, 0);
        reg("2H_NATURESTAFF_KEEPER", 0, 0);
        reg("MAIN_NATURESTAFF_AVALON", 0, 0);
        reg("MAIN_NATURESTAFF_CRYSTAL", 0, 0);
        reg("2H_WARBOW", 0, 0);
        reg("2H_LONGBOW", 0, 0);
        reg("2H_BOW_HELL", 0, 0);
        reg("2H_BOW_KEEPER", 0, 0);
        reg("2H_BOW_CRYSTAL", 0, 0);
        reg("2H_CROSSBOWLARGE", 0, 0);
        reg("MAIN_1HCROSSBOW", 0, 0);
        reg("2H_REPEATINGCROSSBOW_UNDEAD", 0, 0);
        reg("2H_CROSSBOWLARGE_MORGANA", 0, 0);
        reg("2H_CROSSBOW_CANNON_AVALON", 0, 0);
        reg("2H_DUALCROSSBOW_CRYSTAL", 0, 0);
        reg("2H_SHAPESHIFTER_SET1", 0, 0);
        reg("2H_SHAPESHIFTER_SET2", 0, 0);
        reg("2H_SHAPESHIFTER_SET3", 0, 0);
        reg("2H_SHAPESHIFTER_MORGANA", 0, 0);
        reg("2H_SHAPESHIFTER_HELL", 0, 0);
        reg("2H_SHAPESHIFTER_KEEPER", 0, 0);
        reg("2H_SHAPESHIFTER_AVALON", 0, 0);
        reg("2H_SHAPESHIFTER_CRYSTAL", 0, 0);
        */

        /*
        reg("FISHINGBAIT", 0, 0);
        reg("2H_TOOL_FISHINGROD", 0, 0);
        reg("2H_TOOL_FISHINGROD_AVALON", 0, 0);
        reg("HEAD_GATHERER_FISH", 0, 0);
        reg("ARMOR_GATHERER_FISH", 0, 0);
        reg("SHOES_GATHERER_FISH", 0, 0);
        reg("BACKPACK_GATHERER_FISH", 0, 0);
        reg("2H_TOOL_SICKLE", 0, 0);
        reg("2H_TOOL_SICKLE_AVALON", 0, 0);
        reg("HEAD_GATHERER_FIBER", 0, 0);
        reg("ARMOR_GATHERER_FIBER", 0, 0);
        reg("SHOES_GATHERER_FIBER", 0, 0);
        reg("BACKPACK_GATHERER_FIBER", 0, 0);
        reg("2H_TOOL_KNIFE", 0, 0);
        reg("2H_TOOL_KNIFE_AVALON", 0, 0);
        reg("HEAD_GATHERER_HIDE", 0, 0);
        reg("ARMOR_GATHERER_HIDE", 0, 0);
        reg("SHOES_GATHERER_HIDE", 0, 0);
        reg("BACKPACK_GATHERER_HIDE", 0, 0);
        reg("2H_TOOL_PICK", 0, 0);
        reg("2H_TOOL_PICK_AVALON", 0, 0);
        reg("HEAD_GATHERER_ORE", 0, 0);
        reg("ARMOR_GATHERER_ORE", 0, 0);
        reg("SHOES_GATHERER_ORE", 0, 0);
        reg("BACKPACK_GATHERER_ORE", 0, 0);
        reg("H_TOOL_HAMMER", 0, 0);
        reg("H_TOOL_HAMMER_AVALON", 0, 0);
        reg("HEAD_GATHERER_ROCK", 0, 0);
        reg("ARMOR_GATHERER_ROCK", 0, 0);
        reg("SHOES_GATHERER_ROCK", 0, 0);
        reg("BACKPACK_GATHERER_ROCK", 0, 0);
        reg("2H_TOOL_AXE", 0, 0);
        reg("2H_TOOL_AXE_AVALON", 0, 0);
        reg("HEAD_GATHERER_WOOD", 0, 0);
        reg("ARMOR_GATHERER_WOOD", 0, 0);
        reg("SHOES_GATHERER_WOOD", 0, 0);
        reg("BACKPACK_GATHERER_WOOD", 0, 0);
*/

    }

    // registra um item com seu tier minimo e valor base nesse tier
    private static void reg(String sufixo, int tierMinimo, double valorNoTierMinimo) {
        VALORES.put(sufixo, new ValorBase(tierMinimo, valorNoTierMinimo));
    }

    /**
     * retorna o game value de um item dado seu id completo (ex: T5_MAIN_SWORD@2).
     * formula: valorBase * 2^(tier - tierMinimo) * 2^encantamento
     * retorna 0 se o item nao estiver cadastrado.
     */
    public static long getValor(String itemIdCompleto) {
        if (itemIdCompleto == null || itemIdCompleto.isBlank()) return 0;

        // extrai tier, sufixo e encantamento do id completo
        int tier = 4;
        int enchant = 0;
        String sufixo = itemIdCompleto;

        // formato: T4_SUFIXO ou T4_SUFIXO@2
        if (itemIdCompleto.length() > 2 && itemIdCompleto.charAt(0) == 'T'
                && Character.isDigit(itemIdCompleto.charAt(1))) {
            tier = Character.getNumericValue(itemIdCompleto.charAt(1));
            int underline = itemIdCompleto.indexOf('_');
            if (underline >= 0) {
                sufixo = itemIdCompleto.substring(underline + 1);
            }
        }

        // separa encantamento se tiver @
        if (sufixo.contains("@")) {
            String[] partes = sufixo.split("@");
            sufixo = partes[0];
            enchant = Integer.parseInt(partes[1]);
        }

        ValorBase vb = VALORES.get(sufixo);
        if (vb == null) return 0;

        // calcula pela formula
        double valor = vb.valor
                * Math.pow(2, tier - vb.tierMinimo)
                * Math.pow(2, enchant);

        return Math.round(valor);
    }
}