package com.albionmarket.service;

import com.albionmarket.model.Categoria;
import com.albionmarket.model.CidadeInfo;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.model.Subcategoria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * catalogo completo de itens, categorias e cidades do Albion Online.
 * Nomes em portugues br
 * IDs baseados na Albion Online Data API (west.albion-online-data.com).
 */
public class BancoDeDados {

    // cidades
    public static final List<CidadeInfo> CIDADES = Arrays.asList(
            new CidadeInfo("Caerleon",     "Caerleon",      "#c0392b"),
            new CidadeInfo("Bridgewatch",  "Bridgewatch",   "#e67e22"),
            new CidadeInfo("FortSterling", "Fort Sterling", "#7f8c8d"),
            new CidadeInfo("Lymhurst",     "Lymhurst",      "#27ae60"),
            new CidadeInfo("Martlock",     "Martlock",      "#2980b9"),
            new CidadeInfo("Thetford",     "Thetford",      "#8e44ad"),
            new CidadeInfo("BlackMarket",  "Black Market",  "#555555"),
            new CidadeInfo("Brecilien",    "Brecilien",     "#16a085")
    );

    // qualidades
    public static final String[] QUALIDADES = {
            "Todos (mais barato)", "Normal", "Boa", "Notável", "Excelente", "Obra-prima"
    };

    // categorias
    public static List<Categoria> getCategorias() {
        List<Categoria> lista = new ArrayList<>();

        // armas melee
        lista.add(new Categoria("", "Armas Corpo a Corpo", Arrays.asList(

                new Subcategoria("Espadas", Arrays.asList(
                        item("MAIN_SWORD",                "Espada Larga",              "espada larga sword"),
                        item("MAIN_SCIMITAR_MORGANA",     "Lâmina Aclarada",           "lamina aclarada scimitar"),
                        item("2H_DUALSWORD",              "Espadas Duplas",            "espadas duplas dual"),
                        item("2H_CLAYMORE",               "Montante",                  "montante claymore"),
                        item("2H_CLEAVER_HELL",           "Espada Entalhada",          "espada entalhada cleaver"),
                        item("2H_DUALSCIMITAR_UNDEAD",    "Par de Galatinas",          "par galatinas"),
                        item("2H_KINGMAKER",              "Cria-Reis",                 "cria reis kingmaker"),
                        item("MAIN_ENIGMATICORB_AVALON",  "Lâmina da Infinidade",      "lamina infinidade")
                )),

                new Subcategoria("Machado", Arrays.asList(
                        item("MAIN_AXE",                  "Machado de Guerra",         "machado guerra axe"),
                        item("MAIN_ROCKCUTTER_AVALON",    "Machado de Pedra",          "machado pedra rockcutter"),
                        item("2H_AXE",                    "Machado Elevado",           "machado elevado great"),
                        item("2H_HALBERD",                "Alabarda",                  "alabarda halberd"),
                        item("2H_HALBERD_MORGANA",        "Segadeira Infernal",        "segadeira infernal halberd morgana"),
                        item("2H_SCYTHE_HELL",            "Chama-Corpos",              "chama corpos scythe"),
                        item("2H_BEARCLAW_AVALON",        "Patas de Urso",             "patas urso bearclaw"),
                        item("2H_REALMBREAKER_AVALON",    "Quebra-Reinos",             "quebra reinos realmbreaker")
                )),

                new Subcategoria("Maça", Arrays.asList(
                        item("MAIN_MACE",                 "Maça",                      "maca mace"),
                        item("MAIN_ROCKTEETH_AVALON",     "Maça Pétrea",               "maca petrea rockteeth"),
                        item("MAIN_MACE_HELL",            "Maça Íncubo",               "maca incubo"),
                        item("2H_MACE",                   "Maça Pesada",               "maca pesada heavy"),
                        item("2H_DUALMACE_AVALON",        "Maça Cambriana",            "maca cambriana dualmace"),
                        item("2H_FLAIL_HELL",             "Mangual",                   "mangual flail"),
                        item("2H_HAMMER_UNDEAD",          "Jurador",                   "jurador hammer undead")
                )),

                new Subcategoria("Martelo", Arrays.asList(
                        item("2H_HAMMER",                 "Martelo",                   "martelo hammer"),
                        item("2H_POLEHAMMER",             "Martelo de Batalha",        "martelo batalha polehammer"),
                        item("2H_HAMMER_HELL",            "Martelo Fúnebre",           "martelo funebre"),
                        item("2H_DUALHAMMER_AVALON",      "Martelos de Forja",         "martelos forja dualhammer"),
                        item("2H_POLEHAMMER_UNDEAD",      "Martelo Elevado",           "martelo elevado"),
                        item("2H_HAMMER_MORGANA",         "Guarda-Bosques",            "guarda bosques"),
                        item("2H_HANDOFJUSTICE_AVALON",   "Mão da Justiça",            "mao justica")
                )),

                new Subcategoria("Lança", Arrays.asList(
                        item("MAIN_SPEAR",                "Lança",                     "lanca spear"),
                        item("MAIN_SPEAR_MORGANA",        "Lança de Morgana",          "lanca morgana"),
                        item("2H_SPEAR",                  "Lucerna",                   "lucerna glaive"),
                        item("2H_HARPOON_HELL",           "Arpão do Inferno",          "arpao inferno harpoon"),
                        item("2H_GLAIVE_HELL",            "Glaive Cortante",           "glaive cortante"),
                        item("2H_JOUSTING_LANCE_AVALON",  "Lança de Justa",            "lanca justa jousting"),
                        item("2H_SPIRITHUNTER",           "Caçador de Espíritos",      "cacador espiritos spirithunter")
                )),

                new Subcategoria("Adaga", Arrays.asList(
                        item("MAIN_DAGGER",               "Adaga",                     "adaga dagger"),
                        item("MAIN_RAPIER_MORGANA",       "Florete de Morgana",        "florete morgana rapier"),
                        item("2H_DAGGER",                 "Par de Adagas",             "par adagas"),
                        item("2H_CLAWS_HELL",             "Garras Infernais",          "garras infernais claws"),
                        item("2H_DUALDAGGER_UNDEAD",      "Adagas Espirituais",        "adagas espirituais"),
                        item("2H_SOULSCYTHE_AVALON",      "Foice das Almas",           "foice almas soulscythe")
                )),

                new Subcategoria("Bordão", Arrays.asList(
                        item("2H_QUARTERSTAFF",                "Bordão",                   "bordao quarterstaff"),
                        item("2H_IRONCLADEDSTAFF_HELL",        "Bastão de Ferro",          "bastao ferro ironclad"),
                        item("2H_DOUBLEBLADEDSTAFF_UNDEAD",    "Bastão de Duas Lâminas",   "bastao duas laminas double"),
                        item("2H_TWINSCYTHE_AVALON",           "Gadanho Gêmeo",            "gadanho gemeo twinscythe"),
                        item("2H_TRANSFORMATIONSTAFF_UNDEAD",  "Bordão da Transformação",  "bordao transformacao")
                )),

                new Subcategoria("Luvas de Guerra", Arrays.asList(
                        item("MAIN_KNUCKLES_SET1",        "Luvas de Lutador",          "luvas lutador knuckles"),
                        item("MAIN_KNUCKLES_SET2",        "Braçadeira de Batalha",     "bracadeira batalha"),
                        item("2H_KNUCKLES_SET1",          "Cravadas",                  "cravadas"),
                        item("2H_KNUCKLES_SET2",          "Ursinas",                   "ursinas"),
                        item("2H_KNUCKLES_HELL",          "Mãos Infernais",            "maos infernais"),
                        item("2H_CESTUS_AVALON",          "Cestus Golpeadores",        "cestus golpeadores"),
                        item("2H_KNUCKLES_AVALON",        "Punhos de Avalon",          "punhos avalon")
                ))
        )));

        // cajados
        lista.add(new Categoria("", "Cajados", Arrays.asList(

                new Subcategoria("Cajado Arcano", Arrays.asList(
                        item("MAIN_ARCANESTAFF",          "Cajado Arcano",             "cajado arcano arcane"),
                        item("MAIN_GRACEFULSTAFF_AVALON", "Cajado Gracioso",           "cajado gracioso graceful"),
                        item("2H_ARCANESTAFF",            "Cajado Arcano Elevado",     "cajado arcano elevado"),
                        item("2H_ENIGMATICSTAFF_UNDEAD",  "Cajado Enigmático",         "cajado enigmatico"),
                        item("2H_WITCHWORKSTAFF_MORGANA", "Cajado Bruxo",              "cajado bruxo witchwork"),
                        item("2H_OCCULTSTAFF_UNDEAD",     "Cajado Oculto",             "cajado oculto occult"),
                        item("2H_RUNESTAFF_AVALON",       "Cajado de Runas",           "cajado runas")
                )),

                new Subcategoria("Cajado de Fogo", Arrays.asList(
                        item("MAIN_FIRESTAFF",            "Cajado de Fogo",            "cajado fogo fire"),
                        item("MAIN_INFERNOSTAFF_HELL",    "Cajado Infernal",           "cajado infernal inferno"),
                        item("2H_FIRESTAFF",              "Cajado de Fogo Elevado",    "cajado fogo elevado"),
                        item("2H_INFERNOSTAFF_HELL",      "Cajado Infernal Elevado",   "cajado infernal elevado"),
                        item("2H_WILDFIRE_HELL",          "Cajado Fogo Selvagem",      "cajado fogo selvagem wildfire"),
                        item("2H_ROOTOFLIFE_AVALON",      "Raiz da Vida",              "raiz vida rootoflife"),
                        item("2H_BRIMSTONESTAFF_AVALON",  "Cajado de Enxofre",         "cajado enxofre brimstone")
                )),

                new Subcategoria("Cajado de Gelo", Arrays.asList(
                        item("MAIN_FROSTSTAFF",           "Cajado de Gelo",            "cajado gelo frost"),
                        item("MAIN_ICETEMPEST_UNDEAD",    "Cajado Tempestade de Gelo", "cajado tempestade gelo icetempest"),
                        item("2H_FROSTSTAFF",             "Cajado de Gelo Elevado",    "cajado gelo elevado"),
                        item("2H_GLACIALSTAFF_UNDEAD",    "Cajado Glacial",            "cajado glacial"),
                        item("2H_ICECRYSTALSTAFF_AVALON", "Cajado de Cristal de Gelo", "cajado cristal gelo"),
                        item("2H_HOARFROSTSTAFF_UNDEAD",  "Cajado de Gelo Vivo",       "cajado gelo vivo hoarfrost")
                )),

                new Subcategoria("Cajado Amaldiçoado", Arrays.asList(
                        item("MAIN_CURSESTAFF",           "Cajado Amaldiçoado",        "cajado amaldicado curse"),
                        item("MAIN_DEMONICSTAFF_HELL",    "Cajado Demoníaco",          "cajado demoniaco demonic"),
                        item("2H_CURSESTAFF",             "Cajado Amaldiçoado Elevado","cajado amaldicado elevado"),
                        item("2H_SKULLJESTER_UNDEAD",     "Bobo do Crânio",            "bobo cranio skulljester"),
                        item("2H_SHADOWCALLER_UNDEAD",    "Chamador das Sombras",      "chamador sombras shadowcaller"),
                        item("2H_LIFECURSE_UNDEAD",       "Maldição Vital",            "maldicao vital lifecurse"),
                        item("2H_CURSEDPELL_AVALON",      "Cajado Encantado",          "cajado encantado cursedpell")
                )),

                new Subcategoria("Cajado Sagrado", Arrays.asList(
                        item("MAIN_HOLYSTAFF",            "Cajado Sagrado",            "cajado sagrado holy heal"),
                        item("MAIN_DIVINESTAFF_MORGANA",  "Cajado Divino",             "cajado divino divine"),
                        item("2H_HOLYSTAFF",              "Cajado Sagrado Elevado",    "cajado sagrado elevado"),
                        item("2H_LIFETOUCH_UNDEAD",       "Toque de Vida",             "toque vida lifetouch"),
                        item("2H_HOLYORB_MORGANA",        "Orbe Sagrado",              "orbe sagrado holyorb"),
                        item("2H_HALLOWFALL_AVALON",      "Queda Sagrada",             "queda sagrada hallowfall"),
                        item("2H_REDEMPTIONSTAFF_AVALON", "Cajado da Redenção",        "cajado redencao redemption")
                )),

                new Subcategoria("Cajado da Natureza", Arrays.asList(
                        item("MAIN_NATURESTAFF",          "Cajado da Natureza",        "cajado natureza nature heal"),
                        item("MAIN_WILDSTAFF_HELL",       "Cajado Selvagem",           "cajado selvagem wild"),
                        item("2H_NATURESTAFF",            "Cajado da Natureza Elevado","cajado natureza elevado"),
                        item("2H_IRONROOT_UNDEAD",        "Cajado Pustulento",         "cajado pustulento ironroot"),
                        item("2H_DRUIDICSTAFF_AVALON",    "Cajado Druídico",           "cajado druidico druid"),
                        item("2H_ROTVINE_UNDEAD",         "Cajado Rampante",           "cajado rampante rotvine"),
                        item("2H_BLIGHT_HELL",            "Praga",                     "praga blight")
                ))
        )));

        // armas ranged
        lista.add(new Categoria("", "Armas à Distância", Arrays.asList(

                new Subcategoria("Arco", Arrays.asList(
                        item("MAIN_BOW",                        "Arco",                  "arco bow"),
                        item("MAIN_LONGBOW_UNDEAD",             "Arco do Morto-Vivo",    "arco morto vivo longbow"),
                        item("2H_BOW",                          "Arco Longo",            "arco longo long"),
                        item("2H_WARBOW_UNDEAD",                "Badônico",              "badonico warbow"),
                        item("2H_LONGBOW_MORGANA",              "Arco de Morgana",       "arco morgana"),
                        item("2H_WHISPERBOW_HELL",              "Arco Sussurrante",      "arco sussurrante whisperbow"),
                        item("2H_BOW_AVALON",                   "Arco Avaloniano",       "arco avaloniano")
                )),

                new Subcategoria("Besta", Arrays.asList(
                        item("MAIN_CROSSBOW",                   "Besta Leve",            "besta leve crossbow"),
                        item("MAIN_CROSSBOW_MORGANA",           "Besta de Morgana",      "besta morgana"),
                        item("2H_CROSSBOW",                     "Besta Pesada",          "besta pesada heavy"),
                        item("2H_CROSSBOW_MORGANA",             "Arco de Cerco",         "arco cerco siege crossbow"),
                        item("2H_DUALCROSSBOW_HELL",            "Bestas Leves",          "bestas leves dual"),
                        item("2H_REPEATINGCROSSBOW_AVALON",     "Besta Avaloniana",      "besta avaloniana repeating"),
                        item("2H_BOLTCASTERS_UNDEAD",           "Lançadores de Dardos",  "lancadores dardos boltcasters")
                ))
        )));

        // elmos, capotes e chapeus
        lista.add(new Categoria("", "Capacetes", Arrays.asList(

                new Subcategoria("Tecido (Mago)", Arrays.asList(
                        item("HEAD_CLOTH_SET1",      "Chapéu do Aprendiz",             "chapeu aprendiz cloth pano"),
                        item("HEAD_CLOTH_SET2",      "Chapéu do Feiticeiro",           "chapeu feiticeiro cloth"),
                        item("HEAD_CLOTH_SET3",      "Chapéu do Mago",                 "chapeu mago cloth"),
                        item("HEAD_CLOTH_HELL",      "Elmo Demônio (Pano)",            "elmo demonio cloth hell"),
                        item("HEAD_CLOTH_MORGANA",   "Elmo de Guarda-tumbas (Pano)",   "elmo guarda tumbas cloth morgana"),
                        item("HEAD_CLOTH_KEEPER",    "Elmo do Guardador (Pano)",       "elmo guardador cloth keeper"),
                        item("HEAD_CLOTH_UNDEAD",    "Elmo Tecelão do Crepúsculo (Pano)","elmo tecelao cloth undead"),
                        item("HEAD_CLOTH_AVALON",    "Elmo da Bravura (Pano)",         "elmo bravura cloth avalon"),
                        item("HEAD_CLOTH_ROYAL",     "Elmo Real (Pano)",               "elmo real cloth royal")
                )),

                new Subcategoria("Elmos de couro", Arrays.asList(
                        item("HEAD_LEATHER_SET1",    "Chapéu do Caçador",              "chapeu cacador leather couro"),
                        item("HEAD_LEATHER_SET2",    "Chapéu do Stalker",              "chapeu stalker leather"),
                        item("HEAD_LEATHER_SET3",    "Chapéu do Assassino",            "chapeu assassino leather"),
                        item("HEAD_LEATHER_HELL",    "Elmo Demônio (Couro)",           "elmo demonio leather hell"),
                        item("HEAD_LEATHER_MORGANA", "Elmo de Guarda-tumbas (Couro)",  "elmo guarda tumbas leather morgana"),
                        item("HEAD_LEATHER_KEEPER",  "Elmo do Guardador (Couro)",      "elmo guardador leather keeper"),
                        item("HEAD_LEATHER_UNDEAD",  "Elmo Tecelão do Crepúsculo (Couro)","elmo tecelao leather undead"),
                        item("HEAD_LEATHER_AVALON",  "Elmo da Bravura (Couro)",        "elmo bravura leather avalon"),
                        item("HEAD_LEATHER_ROYAL",   "Elmo Real (Couro)",              "elmo real leather royal")
                )),

                new Subcategoria("Elmos", Arrays.asList(
                        item("HEAD_PLATE_SET1",      "Elmo do Soldado",                "elmo soldado plate placa"),
                        item("HEAD_PLATE_SET2",      "Elmo do Cavaleiro",              "elmo cavaleiro plate"),
                        item("HEAD_PLATE_SET3",      "Elmo do Guardião",               "elmo guardiao plate"),
                        item("HEAD_PLATE_HELL",      "Elmo Demônio (Placa)",           "elmo demonio plate hell"),
                        item("HEAD_PLATE_MORGANA",   "Elmo de Guarda-tumbas (Placa)",  "elmo guarda tumbas plate morgana"),
                        item("HEAD_PLATE_KEEPER",    "Elmo do Guardador (Placa)",      "elmo guardador plate keeper"),
                        item("HEAD_PLATE_UNDEAD",    "Elmo Tecelão do Crepúsculo (Placa)","elmo tecelao plate undead"),
                        item("HEAD_PLATE_AVALON",    "Elmo da Bravura (Placa)",        "elmo bravura plate avalon"),
                        item("HEAD_PLATE_ROYAL",     "Elmo Real (Placa)",              "elmo real plate royal")
                ))
        )));

        // peitoral
        lista.add(new Categoria("", "Armaduras", Arrays.asList(

                new Subcategoria("Tecido (Mago)", Arrays.asList(
                        item("ARMOR_CLOTH_SET1",     "Vestes do Aprendiz",             "vestes aprendiz cloth pano"),
                        item("ARMOR_CLOTH_SET2",     "Vestes do Feiticeiro",           "vestes feiticeiro cloth"),
                        item("ARMOR_CLOTH_SET3",     "Vestes do Mago",                 "vestes mago cloth"),
                        item("ARMOR_CLOTH_HELL",     "Armadura Demônia (Pano)",        "armadura demonia cloth hell"),
                        item("ARMOR_CLOTH_MORGANA",  "Armadura de Guarda-tumbas (Pano)","armadura guarda tumbas cloth morgana"),
                        item("ARMOR_CLOTH_KEEPER",   "Armadura do Guardador (Pano)",   "armadura guardador cloth keeper"),
                        item("ARMOR_CLOTH_UNDEAD",   "Armadura Tecelão (Pano)",        "armadura tecelao cloth undead"),
                        item("ARMOR_CLOTH_AVALON",   "Armadura da Bravura (Pano)",     "armadura bravura cloth avalon"),
                        item("ARMOR_CLOTH_ROYAL",    "Armadura Real (Pano)",           "armadura real cloth royal")
                )),

                new Subcategoria("Couro (Ranger)", Arrays.asList(
                        item("ARMOR_LEATHER_SET1",   "Jaqueta do Caçador",             "jaqueta cacador leather couro"),
                        item("ARMOR_LEATHER_SET2",   "Jaqueta do Stalker",             "jaqueta stalker leather"),
                        item("ARMOR_LEATHER_SET3",   "Jaqueta do Assassino",           "jaqueta assassino leather"),
                        item("ARMOR_LEATHER_HELL",   "Armadura Demônia (Couro)",       "armadura demonia leather hell"),
                        item("ARMOR_LEATHER_MORGANA","Armadura de Guarda-tumbas (Couro)","armadura guarda tumbas leather morgana"),
                        item("ARMOR_LEATHER_KEEPER", "Armadura do Guardador (Couro)",  "armadura guardador leather keeper"),
                        item("ARMOR_LEATHER_UNDEAD", "Armadura Tecelão (Couro)",       "armadura tecelao leather undead"),
                        item("ARMOR_LEATHER_AVALON", "Armadura da Bravura (Couro)",    "armadura bravura leather avalon"),
                        item("ARMOR_LEATHER_ROYAL",  "Armadura Real (Couro)",          "armadura real leather royal")
                )),

                new Subcategoria("Placa (Guerreiro)", Arrays.asList(
                        item("ARMOR_PLATE_SET1",     "Armadura do Soldado",            "armadura soldado plate placa"),
                        item("ARMOR_PLATE_SET2",     "Armadura do Cavaleiro",          "armadura cavaleiro plate"),
                        item("ARMOR_PLATE_SET3",     "Armadura do Guardião",           "armadura guardiao plate"),
                        item("ARMOR_PLATE_HELL",     "Armadura Demônia (Placa)",       "armadura demonia plate hell"),
                        item("ARMOR_PLATE_MORGANA",  "Armadura de Guarda-tumbas (Placa)","armadura guarda tumbas plate morgana"),
                        item("ARMOR_PLATE_KEEPER",   "Armadura do Guardador (Placa)",  "armadura guardador plate keeper"),
                        item("ARMOR_PLATE_UNDEAD",   "Armadura Tecelão (Placa)",       "armadura tecelao plate undead"),
                        item("ARMOR_PLATE_AVALON",   "Armadura da Bravura (Placa)",    "armadura bravura plate avalon"),
                        item("ARMOR_PLATE_ROYAL",    "Armadura Real (Placa)",          "armadura real plate royal")
                ))
        )));

        // botas e sapatos
        lista.add(new Categoria("", "Sapatos/Botas", Arrays.asList(

                new Subcategoria("Tecido (Mago)", Arrays.asList(
                        item("SHOES_CLOTH_SET1",     "Sandálias do Aprendiz",          "sandalia aprendiz cloth pano"),
                        item("SHOES_CLOTH_SET2",     "Sandálias do Feiticeiro",        "sandalia feiticeiro cloth"),
                        item("SHOES_CLOTH_SET3",     "Sandálias do Mago",              "sandalia mago cloth"),
                        item("SHOES_CLOTH_HELL",     "Sapatos Demoníacos (Pano)",      "sapatos demoniacos cloth hell"),
                        item("SHOES_CLOTH_MORGANA",  "Sapatos de Guarda-tumbas (Pano)","sapatos guarda tumbas cloth morgana"),
                        item("SHOES_CLOTH_AVALON",   "Sandálias da Bravura",           "sandalias bravura cloth avalon"),
                        item("SHOES_CLOTH_ROYAL",    "Sapatos Reais (Pano)",           "sapatos reais cloth royal")
                )),

                new Subcategoria("Couro (Ranger)", Arrays.asList(
                        item("SHOES_LEATHER_SET1",   "Botas do Caçador",               "botas cacador leather couro"),
                        item("SHOES_LEATHER_SET2",   "Botas do Stalker",               "botas stalker leather"),
                        item("SHOES_LEATHER_SET3",   "Botas do Assassino",             "botas assassino leather"),
                        item("SHOES_LEATHER_HELL",   "Botas Demoníacas (Couro)",       "botas demoniacas leather hell"),
                        item("SHOES_LEATHER_MORGANA","Botas de Guarda-tumbas (Couro)", "botas guarda tumbas leather morgana"),
                        item("SHOES_LEATHER_AVALON", "Botas da Bravura (Couro)",       "botas bravura leather avalon"),
                        item("SHOES_LEATHER_ROYAL",  "Botas Reais (Couro)",            "botas reais leather royal")
                )),

                new Subcategoria("Placa (Guerreiro)", Arrays.asList(
                        item("SHOES_PLATE_SET1",     "Botas do Soldado",               "botas soldado plate placa"),
                        item("SHOES_PLATE_SET2",     "Botas do Cavaleiro",             "botas cavaleiro plate"),
                        item("SHOES_PLATE_SET3",     "Botas do Guardião",              "botas guardiao plate"),
                        item("SHOES_PLATE_HELL",     "Botas Demoníacas (Placa)",       "botas demoniacas plate hell"),
                        item("SHOES_PLATE_MORGANA",  "Botas de Guarda-tumbas (Placa)", "botas guarda tumbas plate morgana"),
                        item("SHOES_PLATE_AVALON",   "Botas da Bravura (Placa)",       "botas bravura plate avalon"),
                        item("SHOES_PLATE_ROYAL",    "Botas Reais (Placa)",            "botas reais plate royal"),
                        item("SHOES_PLATE_JUDICATOR","Botas Judicantes",               "botas judicantes plate judicator")
                ))
        )));

        // mao secundaria
        lista.add(new Categoria("", "Mão Secundária", Arrays.asList(

                new Subcategoria("Escudo", Arrays.asList(
                        item("OFF_SHIELD_SET1",      "Escudo",                          "escudo shield"),
                        item("OFF_SHIELD_SET2",      "Escudo Reforçado",                "escudo reforcado"),
                        item("OFF_SHIELD_SET3",      "Escudo do Cruzado",               "escudo cruzado"),
                        item("OFF_SHIELD_UNDEAD",    "Escudo do Morto-Vivo",            "escudo morto vivo undead"),
                        item("OFF_SHIELD_MORGANA",   "Escudo de Morgana",               "escudo morgana"),
                        item("OFF_SHIELD_KEEPER",    "Escudo do Guardador",             "escudo guardador keeper")
                )),

                new Subcategoria("Livro/Olho/Tocha/Brumário/Raiz", Arrays.asList(
                        item("OFF_BOOK_SET1",        "Livro da Conspiração",            "livro conspiracao book"),
                        item("OFF_BOOK_SET2",        "Livro do Feitiço",                "livro feiticio book"),
                        item("OFF_TORCH_SET1",       "Tocha",                           "tocha torch"),
                        item("OFF_TORCH_SET2",       "Tocha Encantada",                 "tocha encantada"),
                        item("OFF_ORB_SET1",         "Olho Arcano",                     "olho arcano orb"),
                        item("OFF_ORB_SET2",         "Foco Arcano",                     "foco arcano orb"),
                        item("OFF_HORN_SET1",        "Brumário",                        "brumario horn"),
                        item("OFF_HORN_SET2",        "Brumário Elevado",                "brumario elevado horn"),
                        item("OFF_TOTEM_SET1",       "Raiz",                            "raiz totem"),
                        item("OFF_TOTEM_SET2",       "Raiz Mística",                    "raiz mistica totem")
                )),

                new Subcategoria("Capas", Arrays.asList(
                        item("CAPE",                          "Capa",                    "capa cape"),
                        item("CAPEITEM_FW_BRIDGEWATCH",       "Capa de Bridgewatch",     "capa bridgewatch"),
                        item("CAPEITEM_FW_CAERLEON",          "Capa de Caerleon",        "capa caerleon"),
                        item("CAPEITEM_FW_LYMHURST",          "Capa de Lymhurst",        "capa lymhurst"),
                        item("CAPEITEM_FW_MARTLOCK",          "Capa de Martlock",        "capa martlock"),
                        item("CAPEITEM_FW_THETFORD",          "Capa de Thetford",        "capa thetford"),
                        item("CAPEITEM_FW_FORTSTERLING",      "Capa de Fort Sterling",   "capa fort sterling")
                ))
        )));

        // montarias
        lista.add(new Categoria("", "Montarias", Arrays.asList(

                new Subcategoria("Cavalos", Arrays.asList(
                        item("MOUNT_HORSE",              "Cavalo de Montaria",          "cavalo horse"),
                        item("MOUNT_HORSE_ARMORED",      "Cavalo Blindado",             "cavalo blindado armored"),
                        item("MOUNT_HORSE_UNDEAD",       "Cavalo Espectral",            "cavalo espectral undead")
                )),

                new Subcategoria("Bois e Mulas", Arrays.asList(
                        item("MOUNT_OX",                 "Boi de Carga",                "boi carga ox"),
                        item("MOUNT_MULE",               "Mula de Combate Herege",      "mula combate herege mule")
                )),

                new Subcategoria("Cervos e Alces", Arrays.asList(
                        item("MOUNT_GIANTSTAG",          "Cervo Gigante",               "cervo gigante stag"),
                        item("MOUNT_GIANTSTAG_MOOSE",    "Alce",                        "alce moose")
                )),

                new Subcategoria("Lobos", Arrays.asList(
                        item("MOUNT_DIREWOLF",           "Lobo Feroz",                  "lobo feroz direwolf"),
                        item("MOUNT_DIREWOLF_GHOST",     "Lobo Fantasma",               "lobo fantasma ghost direwolf"),
                        item("MOUNT_GREYWOLF_FW_CAERLEON","Lobo Cinza",                 "lobo cinza greywolf caerleon"),
                        item("MOUNT_GREYWOLF_FW_CAERLEON_ELITE","Lobo Cinza de Elite",  "lobo cinza elite greywolf"),
                        item("MOUNT_COUGAR_KEEPER",      "Puma do Guardador",           "puma cougar keeper"),
                        item("MOUNT_COUGAR_KEEPER_ELITE","Puma do Guardador de Elite",  "puma cougar keeper elite"),
                        item("MOUNT_RAGECLAW",           "Garra-Fúria",                 "garra furia rageclaw")
                )),

                new Subcategoria("Ursos", Arrays.asList(
                        item("MOUNT_DIREBEAR",           "Urso Colossal Selado",        "urso colossal selado direbear"),
                        item("MOUNT_DIREBEAR_FW_FORTSTERLING","Urso Invernal",          "urso invernal direbear fortsterling"),
                        item("MOUNT_DIREBEAR_FW_FORTSTERLING_ELITE","Urso Invernal de Elite","urso invernal elite direbear"),
                        item("MOUNT_GRIZZLYBEAR",        "Urso Pardo",                  "urso pardo grizzly bear")
                )),

                new Subcategoria("Javalis e Dragões", Arrays.asList(
                        item("MOUNT_DIREBOAR",           "Javali Feroz Selado",         "javali feroz selado direboar"),
                        item("MOUNT_DIREBOAR_FW_LYMHURST","Javali Selvagem",            "javali selvagem direboar lymhurst"),
                        item("MOUNT_DIREBOAR_FW_LYMHURST_ELITE","Javali Selvagem de Elite","javali selvagem elite direboar"),
                        item("MOUNT_SWAMPDRAGON",        "Dragão do Pântano Selado",    "dragao pantano selado swampdragon"),
                        item("MOUNT_SWAMPDRAGON_FW_THETFORD","Salamandra do Pântano",   "salamandra pantano swampdragon thetford"),
                        item("MOUNT_SWAMPDRAGON_FW_THETFORD_ELITE","Salamandra do Pântano de Elite","salamandra pantano elite"),
                        item("MOUNT_ARMORED_SWAMPDRAGON_BATTLE","Dragão do Pântano Blindado de Batalha","dragao blindado batalha swampdragon")
                )),

                new Subcategoria("Mamutes", Arrays.asList(
                        item("MOUNT_MAMMOTH_TRANSPORT",  "Mamute de Transporte",        "mamute transporte mammoth"),
                        item("MOUNT_MAMMOTH_BATTLE",     "Mamute de Batalha",           "mamute batalha mammoth"),
                        item("MOUNT_MAMMOTH_BATTLE_ELITE","Mamute de Batalha de Elite", "mamute batalha elite mammoth")
                )),

                new Subcategoria("Aves e Exóticos", Arrays.asList(
                        item("MOUNT_MOABIRD_FW_BRIDGEWATCH","Moa",                       "moa moabird bridgewatch"),
                        item("MOUNT_MOABIRD_FW_BRIDGEWATCH_ELITE","Moa de Elite",        "moa elite moabird"),
                        item("MOUNT_RAM_FW_MARTLOCK",    "Carneiro-Grande",             "carneiro grande ram martlock"),
                        item("MOUNT_RAM_FW_MARTLOCK_ELITE","Carneiro-Grande de Elite",  "carneiro grande elite ram"),
                        item("MOUNT_TERRORBIRD",         "Pássaro do Terror Selado",    "passaro terror terrorbird"),
                        item("MOUNT_TERRORBIRD_ELITE",   "Pássaro do Terror de Elite",  "passaro terror elite terrorbird"),
                        item("MOUNT_RAVENSOUL",          "Corvo de Morgana",            "corvo morgana raven"),
                        item("MOUNT_BLACKPANTHER",       "Pantera Negra",               "pantera negra black panther"),
                        item("MOUNT_OWL",                "Coruja Divina",               "coruja divina owl divine"),
                        item("MOUNT_OWL_ELITE",          "Coruja Mística de Elite",     "coruja mistica elite owl"),
                        item("MOUNT_BASILISK_FLAME",     "Basilisco em Chamas",         "basilisco chamas flame basilisk"),
                        item("MOUNT_BASILISK_VENOM",     "Basilisco Venenoso",          "basilisco venenoso venom basilisk"),
                        item("MOUNT_AVALON_BASILISK",    "Basilisco Avaloniano",        "basilisco avaloniano avalon"),
                        item("MOUNT_PESTLIZARD",         "Lagarto Pestilento",          "lagarto pestilento pestlizard"),
                        item("MOUNT_HUSKY",              "Husky de Neve",               "husky neve snow"),
                        item("MOUNT_FROSTRAM",           "Carneiro de Gelo",            "carneiro gelo frost ram"),
                        item("MOUNT_SIEGE_BALLISTA",     "Balista de Cerco",            "balista cerco siege ballista"),
                        item("MOUNT_SPECTRAL_BONEHORSE", "Cavalo de Ossos Espectral",   "cavalo ossos espectral bonehorse")
                ))
        )));

        // recursos coletaveis
        lista.add(new Categoria("", "Recursos", Arrays.asList(

                new Subcategoria("Recursos Brutos", Arrays.asList(
                        item("FIBER",      "Algodão",              "algodao fibra fiber"),
                        item("HIDE",       "Pelego",               "pelego couro hide"),
                        item("ORE",        "Minério",              "minerio ore"),
                        item("ROCK",       "Pedra",                "pedra rock"),
                        item("WOOD",       "Troncos",              "troncos madeira wood")
                )),

                new Subcategoria("Recursos Refinados", Arrays.asList(
                        item("METALBAR",    "Barra de Metal",       "barra metal bar"),
                        item("LEATHER",     "Couro",                "couro leather"),
                        item("CLOTH",       "Tecido",               "tecido cloth"),
                        item("PLANKS",      "Tábuas",               "tabua planks"),
                        item("STONEBLOCK",  "Bloco de Calcário",    "bloco calcario stoneblock")
                ))
        )));

        // consumiveis comidas e pocoes
        lista.add(new Categoria("🧪", "Consumíveis", Arrays.asList(

                new Subcategoria("Poções", Arrays.asList(
                        item("POTION_HEAL",             "Poção de Cura",                "pocao cura heal"),
                        item("POTION_ENERGY",           "Poção de Energia",             "pocao energia mana"),
                        item("POTION_INVULNERABILITY",  "Poção de Invulnerabilidade",   "pocao invulnerabilidade"),
                        item("POTION_RESISTANCE",       "Poção de Resistência",         "pocao resistencia")
                )),

                new Subcategoria("Comidas", Arrays.asList(
                        item("MEAL_STEW",               "Ensopado",                     "ensopado stew"),
                        item("MEAL_SALAD",              "Salada",                       "salada"),
                        item("MEAL_OMELETTE",           "Omelete",                      "omelete"),
                        item("MEAL_ROAST",              "Assado",                       "assado roast"),
                        item("MEAL_SANDWICH",           "Sanduíche",                    "sanduiche"),
                        item("MEAL_SOUP",               "Sopa",                         "sopa soup"),
                        item("FISH_FRESHWATER_STEW",    "Ensopado de Peixe",            "ensopado peixe fish stew")
                ))
        )));

        // bolsa
        lista.add(new Categoria("", "Acessórios", Arrays.asList(

                new Subcategoria("Bolsas", Arrays.asList(
                        item("BAG", "Bolsa", "bolsa mochila bag")
                )),

                new Subcategoria("Capas", Arrays.asList(
                        item("CAPE",                     "Capa",                    "capa cape"),
                        item("CAPEITEM_FW_BRIDGEWATCH",  "Capa de Bridgewatch",     "capa bridgewatch"),
                        item("CAPEITEM_FW_CAERLEON",     "Capa de Caerleon",        "capa caerleon"),
                        item("CAPEITEM_FW_LYMHURST",     "Capa de Lymhurst",        "capa lymhurst"),
                        item("CAPEITEM_FW_MARTLOCK",     "Capa de Martlock",        "capa martlock"),
                        item("CAPEITEM_FW_THETFORD",     "Capa de Thetford",        "capa thetford"),
                        item("CAPEITEM_FW_FORTSTERLING", "Capa de Fort Sterling",   "capa fort sterling")
                ))
        )));

        return lista;
    }

    // utilitario: cria um ItemDefinition de forma mais concisa
    private static ItemDefinition item(String id, String nome, String keywords) {
        return new ItemDefinition(id, nome, keywords);
    }

    // retorna todos os itens em lista plana para busca por texto
    public static List<ItemDefinition> getTodosItens() {
        List<ItemDefinition> todos = new ArrayList<>();
        for (Categoria cat : getCategorias()) {
            for (Subcategoria sub : cat.getSubcategorias()) {
                todos.addAll(sub.getItens());
            }
        }
        return todos;
    }
}