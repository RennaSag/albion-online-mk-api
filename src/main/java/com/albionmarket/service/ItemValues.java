package com.albionmarket.service;

import java.util.HashMap;
import java.util.Map;

/**
 * valores base dos itens para calculo de nutricao no craft.
 * o valor real de um item e calculado pela formula:
 *   valor = valorBase * 2^(tier - tierBase) * 2^encantamento
 *
 * exemplo com botas de soldado (SHOES_PLATE_SET1):
 *   tier base = 2, valor base = 8
 *   t4.0 = 8 * 2^(4-2) * 2^0 = 8 * 4 * 1 = 32
 *   t4.1 = 8 * 2^(4-2) * 2^1 = 8 * 4 * 2 = 64... espera, no exemplo dado t4.0=128
 *   ajuste: tier base = 1, valor base = 4
 *   t4.0 = 4 * 2^(4-1) = 4 * 8 = 32 ok
 *   t5.0 = 4 * 2^(5-1) = 4 * 16 = 64... mas no exemplo t5.0 = 256
 *   ajuste final: o valor base cadastrado ja e para t2 (tier minimo real do item)
 *   e a formula usa tier - 2 como expoente
 *
 * forma mais simples: cada item tem valorT2 (valor no tier 2 sem encantamento).
 * valor final = valorT2 * 2^(tier-2) * 2^encantamento
 *
 * para itens que comecam no t4 (armas, armaduras pesadas etc):
 * cadastramos valorT4 e usamos tier - 4 como base.
 */
public class ItemValues {

    // par de dados: tier minimo do item e valor nesse tier sem encantamento
    public static class ValorBase {
        public final int    tierMinimo;
        public final double valor;

        public ValorBase(int tierMinimo, double valor) {
            this.tierMinimo = tierMinimo;
            this.valor      = valor;
        }
    }

    // mapa de sufixo do item (igual ao usado no BancoDeDados) -> ValorBase
    private static final Map<String, ValorBase> VALORES = new HashMap<>();

    static {
        // sapatos / botas
        // botas do soldado: t2=8, t4.0=32, t5.0=64(errado)... usando t4.0=128
        // revisando: t4.0=128, t5.0=256, t6.0=512, t8.4=32768
        // t4.0=128 -> t5.0 deveria ser 256 (dobra) ok
        // t6.0=512 ok, t8.0=2048, t8.4=2048*16=32768 ok
        // entao valor base no t4 = 128
        reg("SHOES_PLATE_SET1",    4, 128);
        reg("SHOES_PLATE_SET2",    4, 128);
        reg("SHOES_PLATE_SET3",    4, 128);
        reg("SHOES_PLATE_HELL",    4, 128);
        reg("SHOES_PLATE_MORGANA", 4, 128);
        reg("SHOES_PLATE_AVALON",  4, 128);
        reg("SHOES_PLATE_ROYAL",   4, 128);

        reg("SHOES_LEATHER_SET1",    4, 128);
        reg("SHOES_LEATHER_SET2",    4, 128);
        reg("SHOES_LEATHER_SET3",    4, 128);
        reg("SHOES_LEATHER_HELL",    4, 128);
        reg("SHOES_LEATHER_MORGANA", 4, 128);
        reg("SHOES_LEATHER_AVALON",  4, 128);
        reg("SHOES_LEATHER_ROYAL",   4, 128);

        reg("SHOES_CLOTH_SET1",    4, 128);
        reg("SHOES_CLOTH_SET2",    4, 128);
        reg("SHOES_CLOTH_SET3",    4, 128);
        reg("SHOES_CLOTH_HELL",    4, 128);
        reg("SHOES_CLOTH_MORGANA", 4, 128);
        reg("SHOES_CLOTH_AVALON",  4, 128);
        reg("SHOES_CLOTH_ROYAL",   4, 128);

        //  armaduras (peitoral)
        reg("ARMOR_PLATE_SET1",    4, 128);
        reg("ARMOR_PLATE_SET2",    4, 128);
        reg("ARMOR_PLATE_SET3",    4, 128);
        reg("ARMOR_PLATE_HELL",    4, 128);
        reg("ARMOR_PLATE_MORGANA", 4, 128);
        reg("ARMOR_PLATE_KEEPER",  4, 128);
        reg("ARMOR_PLATE_UNDEAD",  4, 128);
        reg("ARMOR_PLATE_AVALON",  4, 128);
        reg("ARMOR_PLATE_ROYAL",   4, 128);

        reg("ARMOR_LEATHER_SET1",    4, 128);
        reg("ARMOR_LEATHER_SET2",    4, 128);
        reg("ARMOR_LEATHER_SET3",    4, 128);
        reg("ARMOR_LEATHER_HELL",    4, 128);
        reg("ARMOR_LEATHER_MORGANA", 4, 128);
        reg("ARMOR_LEATHER_KEEPER",  4, 128);
        reg("ARMOR_LEATHER_UNDEAD",  4, 128);
        reg("ARMOR_LEATHER_AVALON",  4, 128);
        reg("ARMOR_LEATHER_ROYAL",   4, 128);

        reg("ARMOR_CLOTH_SET1",    4, 128);
        reg("ARMOR_CLOTH_SET2",    4, 128);
        reg("ARMOR_CLOTH_SET3",    4, 128);
        reg("ARMOR_CLOTH_HELL",    4, 128);
        reg("ARMOR_CLOTH_MORGANA", 4, 128);
        reg("ARMOR_CLOTH_KEEPER",  4, 128);
        reg("ARMOR_CLOTH_UNDEAD",  4, 128);
        reg("ARMOR_CLOTH_AVALON",  4, 128);
        reg("ARMOR_CLOTH_ROYAL",   4, 128);

        // capacetes
        reg("HEAD_PLATE_SET1",    4, 128);
        reg("HEAD_PLATE_SET2",    4, 128);
        reg("HEAD_PLATE_SET3",    4, 128);
        reg("HEAD_PLATE_HELL",    4, 128);
        reg("HEAD_PLATE_MORGANA", 4, 128);
        reg("HEAD_PLATE_KEEPER",  4, 128);
        reg("HEAD_PLATE_UNDEAD",  4, 128);
        reg("HEAD_PLATE_AVALON",  4, 128);
        reg("HEAD_PLATE_ROYAL",   4, 128);

        reg("HEAD_LEATHER_SET1",    4, 128);
        reg("HEAD_LEATHER_SET2",    4, 128);
        reg("HEAD_LEATHER_SET3",    4, 128);
        reg("HEAD_LEATHER_HELL",    4, 128);
        reg("HEAD_LEATHER_MORGANA", 4, 128);
        reg("HEAD_LEATHER_KEEPER",  4, 128);
        reg("HEAD_LEATHER_UNDEAD",  4, 128);
        reg("HEAD_LEATHER_AVALON",  4, 128);
        reg("HEAD_LEATHER_ROYAL",   4, 128);

        reg("HEAD_CLOTH_SET1",    4, 128);
        reg("HEAD_CLOTH_SET2",    4, 128);
        reg("HEAD_CLOTH_SET3",    4, 128);
        reg("HEAD_CLOTH_HELL",    4, 128);
        reg("HEAD_CLOTH_MORGANA", 4, 128);
        reg("HEAD_CLOTH_KEEPER",  4, 128);
        reg("HEAD_CLOTH_UNDEAD",  4, 128);
        reg("HEAD_CLOTH_AVALON",  4, 128);
        reg("HEAD_CLOTH_ROYAL",   4, 128);

        //  armas 1h (espada, machado, maca etc)
        reg("MAIN_SWORD",               4, 128);
        reg("MAIN_SCIMITAR_MORGANA",    4, 128);
        reg("MAIN_AXE",                 4, 128);
        reg("MAIN_ROCKCUTTER_AVALON",   4, 128);
        reg("MAIN_MACE",                4, 128);
        reg("MAIN_ROCKTEETH_AVALON",    4, 128);
        reg("MAIN_MACE_HELL",           4, 128);
        reg("MAIN_SPEAR",               4, 128);
        reg("MAIN_SPEAR_MORGANA",       4, 128);
        reg("MAIN_DAGGER",              4, 128);
        reg("MAIN_RAPIER_MORGANA",      4, 128);
        reg("MAIN_ENIGMATICORB_AVALON", 4, 128);
        reg("MAIN_KNUCKLES_SET1",       4, 128);
        reg("MAIN_KNUCKLES_SET2",       4, 128);

        // armas 2h
        reg("2H_CLAYMORE",              4, 256);
        reg("2H_DUALSWORD",             4, 256);
        reg("2H_CLEAVER_HELL",          4, 256);
        reg("2H_DUALSCIMITAR_UNDEAD",   4, 256);
        reg("2H_KINGMAKER",             4, 256);
        reg("2H_AXE",                   4, 256);
        reg("2H_HALBERD",               4, 256);
        reg("2H_HALBERD_MORGANA",       4, 256);
        reg("2H_SCYTHE_HELL",           4, 256);
        reg("2H_BEARCLAW_AVALON",       4, 256);
        reg("2H_REALMBREAKER_AVALON",   4, 256);
        reg("2H_MACE",                  4, 256);
        reg("2H_DUALMACE_AVALON",       4, 256);
        reg("2H_FLAIL_HELL",            4, 256);
        reg("2H_HAMMER_UNDEAD",         4, 256);
        reg("2H_HAMMER",                4, 256);
        reg("2H_POLEHAMMER",            4, 256);
        reg("2H_HAMMER_HELL",           4, 256);
        reg("2H_DUALHAMMER_AVALON",     4, 256);
        reg("2H_POLEHAMMER_UNDEAD",     4, 256);
        reg("2H_HAMMER_MORGANA",        4, 256);
        reg("2H_HANDOFJUSTICE_AVALON",  4, 256);
        reg("2H_SPEAR",                 4, 256);
        reg("2H_HARPOON_HELL",          4, 256);
        reg("2H_GLAIVE_HELL",           4, 256);
        reg("2H_JOUSTING_LANCE_AVALON", 4, 256);
        reg("2H_SPIRITHUNTER",          4, 256);
        reg("2H_DAGGER",                4, 256);
        reg("2H_CLAWS_HELL",            4, 256);
        reg("2H_DUALDAGGER_UNDEAD",     4, 256);
        reg("2H_SOULSCYTHE_AVALON",     4, 256);
        reg("2H_QUARTERSTAFF",          4, 256);
        reg("2H_IRONCLADEDSTAFF_HELL",  4, 256);
        reg("2H_DOUBLEBLADEDSTAFF_UNDEAD", 4, 256);
        reg("2H_TWINSCYTHE_AVALON",     4, 256);
        reg("2H_TRANSFORMATIONSTAFF_UNDEAD", 4, 256);
        reg("2H_KNUCKLES_SET1",         4, 256);
        reg("2H_KNUCKLES_SET2",         4, 256);
        reg("2H_KNUCKLES_HELL",         4, 256);
        reg("2H_CESTUS_AVALON",         4, 256);
        reg("2H_KNUCKLES_AVALON",       4, 256);
        reg("2H_BOW",                   4, 256);
        reg("2H_WARBOW_UNDEAD",         4, 256);
        reg("2H_LONGBOW_MORGANA",       4, 256);
        reg("2H_WHISPERBOW_HELL",       4, 256);
        reg("2H_BOW_AVALON",            4, 256);
        reg("2H_CROSSBOW",              4, 256);
        reg("2H_CROSSBOW_MORGANA",      4, 256);
        reg("2H_DUALCROSSBOW_HELL",     4, 256);
        reg("2H_REPEATINGCROSSBOW_AVALON", 4, 256);
        reg("2H_BOLTCASTERS_UNDEAD",    4, 256);

        // cajados 1h
        reg("MAIN_ARCANESTAFF",         4, 128);
        reg("MAIN_GRACEFULSTAFF_AVALON",4, 128);
        reg("MAIN_FIRESTAFF",           4, 128);
        reg("MAIN_INFERNOSTAFF_HELL",   4, 128);
        reg("MAIN_FROSTSTAFF",          4, 128);
        reg("MAIN_ICETEMPEST_UNDEAD",   4, 128);
        reg("MAIN_CURSESTAFF",          4, 128);
        reg("MAIN_DEMONICSTAFF_HELL",   4, 128);
        reg("MAIN_HOLYSTAFF",           4, 128);
        reg("MAIN_DIVINESTAFF_MORGANA", 4, 128);
        reg("MAIN_NATURESTAFF",         4, 128);
        reg("MAIN_WILDSTAFF_HELL",      4, 128);
        reg("MAIN_BOW",                 4, 128);
        reg("MAIN_LONGBOW_UNDEAD",      4, 128);
        reg("MAIN_CROSSBOW",            4, 128);
        reg("MAIN_CROSSBOW_MORGANA",    4, 128);

        // cajados 2h
        reg("2H_ARCANESTAFF",           4, 256);
        reg("2H_ENIGMATICSTAFF_UNDEAD", 4, 256);
        reg("2H_WITCHWORKSTAFF_MORGANA",4, 256);
        reg("2H_OCCULTSTAFF_UNDEAD",    4, 256);
        reg("2H_RUNESTAFF_AVALON",      4, 256);
        reg("2H_FIRESTAFF",             4, 256);
        reg("2H_INFERNOSTAFF_HELL",     4, 256);
        reg("2H_WILDFIRE_HELL",         4, 256);
        reg("2H_ROOTOFLIFE_AVALON",     4, 256);
        reg("2H_BRIMSTONESTAFF_AVALON", 4, 256);
        reg("2H_FROSTSTAFF",            4, 256);
        reg("2H_GLACIALSTAFF_UNDEAD",   4, 256);
        reg("2H_ICECRYSTALSTAFF_AVALON",4, 256);
        reg("2H_HOARFROSTSTAFF_UNDEAD", 4, 256);
        reg("2H_CURSESTAFF",            4, 256);
        reg("2H_SKULLJESTER_UNDEAD",    4, 256);
        reg("2H_SHADOWCALLER_UNDEAD",   4, 256);
        reg("2H_LIFECURSE_UNDEAD",      4, 256);
        reg("2H_CURSEDPELL_AVALON",     4, 256);
        reg("2H_HOLYSTAFF",             4, 256);
        reg("2H_LIFETOUCH_UNDEAD",      4, 256);
        reg("2H_HOLYORB_MORGANA",       4, 256);
        reg("2H_HALLOWFALL_AVALON",     4, 256);
        reg("2H_REDEMPTIONSTAFF_AVALON",4, 256);
        reg("2H_NATURESTAFF",           4, 256);
        reg("2H_IRONROOT_UNDEAD",       4, 256);
        reg("2H_DRUIDICSTAFF_AVALON",   4, 256);
        reg("2H_ROTVINE_UNDEAD",        4, 256);
        reg("2H_BLIGHT_HELL",           4, 256);

        // mao secundaria
        reg("OFF_SHIELD_SET1",    4, 128);
        reg("OFF_SHIELD_SET2",    4, 128);
        reg("OFF_SHIELD_SET3",    4, 128);
        reg("OFF_SHIELD_UNDEAD",  4, 128);
        reg("OFF_SHIELD_MORGANA", 4, 128);
        reg("OFF_SHIELD_KEEPER",  4, 128);
        reg("OFF_BOOK_SET1",      4, 128);
        reg("OFF_BOOK_SET2",      4, 128);
        reg("OFF_TORCH_SET1",     4, 128);
        reg("OFF_TORCH_SET2",     4, 128);
        reg("OFF_ORB_SET1",       4, 128);
        reg("OFF_ORB_SET2",       4, 128);
        reg("OFF_HORN_SET1",      4, 128);
        reg("OFF_HORN_SET2",      4, 128);
        reg("OFF_TOTEM_SET1",     4, 128);
        reg("OFF_TOTEM_SET2",     4, 128);
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
            sufixo  = partes[0];
            enchant = Integer.parseInt(partes[1]);
        }

        ValorBase vb = VALORES.get(sufixo);
        if (vb == null) return 0;

        // calcula pela formula
        double valor = vb.valor
                * Math.pow(2, tier    - vb.tierMinimo)
                * Math.pow(2, enchant);

        return Math.round(valor);
    }
}