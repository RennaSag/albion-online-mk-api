package com.albionmarket.service;

import com.albionmarket.model.Categoria;
import com.albionmarket.model.CidadeInfo;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.model.Subcategoria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//sintaxe:
//item("", "", ""),

/**
 *
 * AS SUBCATEGORIAS JA PREENCHIDAS ESTÃO PRONTAS, FALTA AS OUTRAS CATEGORIAS E SUBCATEGORIAS DE ITENS DO JOGO
 * <p>
 * catalogo completo de itens, categorias e cidades do Albion Online.
 * Nomes em portugues br
 * IDs baseados na Albion Online Data API (west.albion-online-data.com).
 */
public class BancoDeDadosCraft {

    // cidades
    public static final List<CidadeInfo> CIDADES = Arrays.asList(
            new CidadeInfo("Caerleon", "Caerleon", "#c0392b"),
            new CidadeInfo("Bridgewatch", "Bridgewatch", "#e67e22"),
            new CidadeInfo("FortSterling", "Fort Sterling", "#7f8c8d"),
            new CidadeInfo("Lymhurst", "Lymhurst", "#27ae60"),
            new CidadeInfo("Martlock", "Martlock", "#2980b9"),
            new CidadeInfo("Thetford", "Thetford", "#8e44ad"),
            new CidadeInfo("BlackMarket", "Black Market", "#555555"),
            new CidadeInfo("Brecilien", "Brecilien", "#16a085")
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
                        item("MAIN_SWORD", "Espada Larga", "espada larga sword"),
                        item("MAIN_SCIMITAR_MORGANA", "Lâmina Aclarada", "lamina aclarada scimitar"),
                        item("2H_DUALSWORD", "Espadas Duplas", "espadas duplas dual"),
                        item("2H_CLAYMORE", "Montante", "montante claymore"),
                        item("2H_CLEAVER_HELL", "Espada Entalhada", "espada entalhada cleaver"),
                        item("2H_DUALSCIMITAR_UNDEAD", "Par de Galatinas", "par galatinas"),
                        item("2H_KINGMAKER", "Cria-Reis", "cria reis kingmaker"),
                        item("MAIN_ENIGMATICORB_AVALON", "Lâmina da Infinidade", "lamina infinidade")
                )),

                new Subcategoria("Machado", Arrays.asList(
                        item("MAIN_AXE", "Machado de Guerra", "machado guerra axe"),
                        item("2H_AXE", "Machadão", "machadão machadao"),
                        item("2H_HALBERD", "Alabarda", "alabarda halberd"),
                        item("2H_HALBERD_MORGANA", "Segadeira Infernal", "segadeira infernal halberd morgana"),
                        item("2H_SCYTHE_HELL", "Chama-Corpos", "chama corpos scythe"),
                        item("2H_BEARCLAW_AVALON", "Patas de Urso", "patas urso bearclaw"),
                        item("2H_REALMBREAKER_AVALON", "Quebra-Reinos", "quebra reinos realmbreaker"),
                        item("2H_SCYTHE_CRYSTAL", "Foice de Cristal", "foice de cristal")
                )),

                new Subcategoria("Maça", Arrays.asList(
                        item("MAIN_MACE", "Maça", "maca mace maça"),
                        item("2H_MACE", "Maça Pesada", "maça pesada mace maca"),
                        item("2H_FLAIL", "Mangual", "mangual"),
                        item("MAIN_ROCKMACE_KEEPER", "Maça Pétrea", "maça petrea maca pétrea"),
                        item("MAIN_MACE_HELL", "Maça de Íncubo", "maca mace maça de incubo íncubo"),
                        item("2H_MACE_MORGANA", "Maça Cambriana", "maca cambriana mace"),
                        item("2H_DUALMACE_AVALON", "Jurador", "jurador"),
                        item("MAIN_MACE_CRYSTAL", "Monarca Tempestuoso", "monarca tempestuoso")
                )),

                new Subcategoria("Martelo", Arrays.asList(
                        item("MAIN_HAMMER", "Martelo", "martelo hammer"),
                        item("2H_POLEHAMMER", "Martelo de Batalha", "martelo de batalha hammer"),
                        item("2H_HAMMER", "Martelo Elevado", "martelo de elevado hammer"),
                        item("2H_HAMMER_UNDEAD", "Martelo Fúnebre", "martelo funebre hammer"),
                        item("2H_DUALHAMMER_HELL", "Martelos de Forja", "martelos de forja hammer"),
                        item("2H_RAM_KEEPER", "Guarda-bosques", "guarda bosques"),
                        item("2H_HAMMER_AVALON", "Mão da Justiça", "mao da justica mão"),
                        item("2H_HAMMER_CRYSTAL", "Martelo Estrondoso", "martelo estrondoso")
                )),

                new Subcategoria("Lança", Arrays.asList(
                        item("MAIN_SPEAR", "Lança", "lanca spear"),
                        item("2H_SPEAR", "Pique", " pique"),
                        item("2H_GLAIVE", "Archa", "archa"),
                        item("MAIN_SPEAR_KEEPER", "Lança Garceira", "lansa garceira lança"),
                        item("2H_HARPOON_HELL", "Caça-espíritos", "caça-espíritos caca espiritos caça"),
                        item("2H_TRIDENT_UNDEAD", "Lança Trina", "lança trina lanca"),
                        item("MAIN_SPEAR_LANCE_AVALON", "Alvorada", "alvorada"),
                        item("2H_GLAIVE_CRYSTAL", "Archa Fraturada", "archa fraturada")
                )),

                new Subcategoria("Adaga", Arrays.asList(
                        item("MAIN_DAGGER", "Adaga", "adaga dagger"),
                        item("MAIN_RAPIER_MORGANA", "Dessangradora", "dessangradora rapier"),
                        item("2H_DAGGER", "Par de Adagas", "par adagas"),
                        item("2H_CLAWS_HELL", "Garras", "garras claws"),
                        item("2H_DAGGER_KATAR_AVALON", "Fúria Contida", "furia contida katar"),
                        item("2H_DUALSICKLE_UNDEAD", "Mortíficos", "mortificos mortíficos"),
                        item("MAIN_DAGGER_HELL", "Presa demoníaca", "presa demoniaca"),
                        item("2H_DAGGERPAIR_CRYSTAL", "Gêmeas Aniquiladoras", "gemeas aniquiladoras")
                )),

                new Subcategoria("Bordão", Arrays.asList(
                        item("2H_QUARTERSTAFF", "Bordão", "bordao quarterstaff"),
                        item("2H_IRONCLADEDSTAFF", "Cajado Férreo", "cajado ferreo férreo"),
                        item("2H_DOUBLEBLADEDSTAFF", "Cajado Bilaminado", "cajado bilaminado"),
                        item("2H_COMBATSTAFF_MORGANA", "Cajado de Monge Negro", "cajado de monge negro"),
                        item("2H_TWINSCYTHE_HELL", "Seganímica", "seganimica seganímica"),
                        item("2H_ROCKSTAFF_KEEPER", "Cajado do Equilíbrio", "cajado do equilibrio equilibrio"),
                        item("2H_QUARTERSTAFF_AVALON", "Buscador do Graal", "buscador do graal"),
                        item("2H_DOUBLEBLADEDSTAFF_CRYSTAL", "Lâminas Gêmeas Fantasmagóricas", "laminas gemeas fantasmagorias")
                )),

                new Subcategoria("Luvas de Guerra", Arrays.asList(
                        item("2H_KNUCKLES_SET", "Luvas de Lutador", "luvas de lutador"),
                        item("2H_KNUCKLES_SET2", "Braçadeiras de Batalha", " bracadeiras de batalha"),
                        item("2H_KNUCKLES_SET3", "Manoplas Claravas", "manoplas cravadas"),
                        item("2H_KNUCKLES_KEEPER", "Luvas Ursinas", "luvas ursinas"),
                        item("2H_KNUCKLES_HELL", "Mãos Infernais", "maos infernais"),
                        item("2H_KNUCKLES_MORGANA", "Cestus Golpeadores", "cestus golpeadores"),
                        item("2H_KNUCKLES_AVALON", "Punhos de Avalon", "punhos de avalon"),
                        item("2H_KNUCKLES_CRYSTAL", "Braçadeiras Pulsantes", "bracadeiras pulsantes")
                ))
        )));


        // cajados
        lista.add(new Categoria("", "Cajados", Arrays.asList(
                new Subcategoria("Cajado Arcano", Arrays.asList(
                        item("MAIN_ARCANESTAFF", "Cajado Arcano", "cajado arcano arcane"),
                        item("2H_ARCANESTAFF", "Cajado Arcano Elevado", "cajado arcano elevado"),
                        item("2H_ENIGMATICSTAFF", "Cajado Enigmático", "cajado enigmatico enigmático"),
                        item("MAIN_ARCANESTAFF_UNDEAD", "Cajado Feiticeiro", "cajado feiticeiro"),
                        item("2H_ARCANESTAFF_HELL", "Cajado Oculto", "cajado oculto"),
                        item("2H_ENIGMATICORB_MORGANA", "Local Malévolo", "local malévolo malevolo"),
                        item("2H_ARCANE_RINGPAIR_AVALON", "Som Equilibrado", "som equilibrado"),
                        item("2H_ARCANESTAFF_CRYSTAL", "Cajado Astral", "cajado astral")
                )),

                new Subcategoria("Cajado de Fogo", Arrays.asList(
                        item("MAIN_FIRESTAFF", "Cajado de Fogo", "cajado fogo fire"),
                        item("2H_FIRESTAFF", "Cajado de Fogo Elevado", "cajado de fogo elevado"),
                        item("2H_INFERNOSTAFF", "Cajado de Fogo Infernal", "cajado de fogo infernal"),
                        item("MAIN_FIRESTAFF_KEEPER", "Cajado Incendiário", "cajado incendiario incendiário"),
                        item("2H_FIRESTAFF_HELL", "Cajado Sulfuroso", "cajado sulfuroso"),
                        item("2H_FIRE_RINGPAIR_AVALON", "Canção da Alvorada", "canaco da alvorada"),
                        item("MAIN_FIRESTAFF_CRYSTAL", "Cajado do Andarilho Flamejante", "cajado do andarilho flamejante")
                )),

                new Subcategoria("Cajado de Gelo", Arrays.asList(
                        item("MAIN_FROSTSTAFF", "Cajado de Gelo", "cajado de gelo"),
                        item("2H_FROSTSTAFF", "Cajado de Gelo Elevado", "cajado de gelo elevado"),
                        item("2H_GLACIALSTAFF", "Cajado Glacial", "cajado glacial"),
                        item("MAIN_FROSTSTAFF_KEEPER", "Cajado Enregelante", "cajado enregelante"),
                        item("2H_ICEGAUNTLETS_HELL", "Cajado de Sincelo", "cajado sincelo"),
                        item("2H_ICECRYSTAL_UNDEAD", "Prisma Geleterno", "primas geleterno"),
                        item("MAIN_FROSTSTAFF_AVALON", "Uivo Frio", "uivo frio"),
                        item("2H_FROSTSTAFF_CRYSTAL", "Cajado Ártico", "cajado artico")
                )),

                new Subcategoria("Cajado Amaldiçoado", Arrays.asList(
                        item("MAIN_CURSESTAFF", "Cajado Amaldiçoado", "cajado amaldicado curse"),
                        item("2H_CURSEDSTAFF", "Cajado Amaldiçoado Elevado", "cajado amaldicoado elevado"),
                        item("2H_DEMONICSTAFF", "Cajado Demoníaco", "cajado demoniaco"),
                        item("MAIN_CURSEDSTAFF_UNDEAD", "Cajado Execrado", "cajado execrado"),
                        item("2H_SKULLORB_HELL", "Caveira Amaldiçoada", "caveira amaldicoada"),
                        item("2H_CURSEDSTAFF_MORGANA", "Cajado da Danação", "cajado da danacao danação"),
                        item("MAIN_CURSEDSTAFF_AVALON", "Chama-Sombra", "chama-sombra chama sombra"),
                        item("MAIN_CURSEDSTAFF_CRYSTAL", "Cajado Pútrido", "cajado pútrido putrido")
                )),

                new Subcategoria("Cajado Sagrado", Arrays.asList(
                        item("MAIN_HOLYSTAFF", "Cajado Sagrado", "cajado sagrado"),
                        item("2H_HOLYSTAFF", "Cajado Sagrado Elevado", "cajado sagrado elevado"),
                        item("2H_DIVINESTAFF", "Cajado Divino", "cajado divino"),
                        item("MAIN_HOLYSTAFF_MORGANA", "Cajado Avivador", "cajado avivador"),
                        item("2H_HOLYSTAFF_HELL", "Cajado Corrompido", "cajado corrompido"),
                        item("2H_HOLYSTAFF_UNDEAD", "Cajado da Redenção", "cajado da redencao"),
                        item("MAIN_HOLYSTAFF_AVALON", "Queda Santa", "queda santa"),
                        item("2H_HOLYSTAFF_CRYSTAL", "Cajado Exaltado", "cajado exaltado")
                )),

                new Subcategoria("Cajado da Natureza", Arrays.asList(
                        item("MAIN_NATURESTAFF", "Cajado da Natureza", "cajado da natureza"),
                        item("2H_NATURESTAFF", "Cajado da Natureza Elevado", "cajado da natureza elevado"),
                        item("2H_WILDSTAFF", "Cajado Selvagem", "cajado selvagem"),
                        item("MAIN_NATURESTAFF_KEEPER", "Cajado Druídico", "cajado druidico"),
                        item("2H_NATURESTAFF_HELL", "Cajado Pustulento", "cajado pustulento"),
                        item("2H_NATURESTAFF_KEEPER", "Cajado Rampante", "cajado rampante"),
                        item("MAIN_NATURESTAFF_AVALON", "Raiz Férrea", "raiz ferrea"),
                        item("MAIN_NATURESTAFF_CRYSTAL", "Cajado de Crosta Forjada", "cajado de crosta forjada")


                )),

                new Subcategoria("Arco", Arrays.asList(
                        item("2H_BOW", "Arco", "arco"),
                        item("2H_WARBOW", "Arco de Guerra", "arco de guerra"),
                        item("2H_LONGBOW", "Arco Longo", "arco longo"),
                        item("2H_LONGBOW_UNDEAD", "Arco Sussurrante", "arco sussurante"),
                        item("2H_BOW_HELL", "Arco Plangente", "arco plangente"),
                        item("2H_BOW_KEEPER", "Arco Badônico", "arco badonico"),
                        item("2H_BOW_AVALON", "Fura-bruma", "fura bruma"),
                        item("2H_BOW_CRYSTAL", "Arco do Andarilho Celeste", "arco do andarilho celeste")

                )),


                new Subcategoria("Besta", Arrays.asList(
                        item("2H_CROSSBOW", "Besta", "besta"),
                        item("2H_CROSSBOWLARGE", "Besta Pesada", "besta pesada"),
                        item("MAIN_1HCROSSBOW", "Besta Leve", "besta leve"),
                        item("2H_REPEATINGCROSSBOW_UNDEAD", "Repetidor Lamentoso", "repetidor lamentoso"),
                        item("2H_DUALCROSSBOW_HELL", "Lança-virotes", "lança virotes"),
                        item("2H_CROSSBOWLARGE_MORGANA", "Arco de Cerco", "arco de certo"),
                        item("2H_CROSSBOW_CANNON_AVALON", "Modelador de Energia", "modelador de energia"),
                        item("2H_DUALCROSSBOW_CRYSTAL", "Detonadores Reluzentes", "detonadores reluzentes")

                )),
                new Subcategoria("Cajado Metamordo", Arrays.asList(
                        item("2H_SHAPESHIFTER_SET1", "Cajado de Predador", "cajado de predador"),
                        item("2H_SHAPESHIFTER_SET2", "Cajado Enraizado", "cajado enraizado"),
                        item("2H_SHAPESHIFTER_SET3", "Cajado Primitivo", "cajado primitivo"),
                        item("2H_SHAPESHIFTER_MORGANA", "Cajado da Lua de Sangue", "cajado da lua de sangue"),
                        item("2H_SHAPESHIFTER_HELL", "Cajado Endemoniado", "cajado endemoniado"),
                        item("2H_SHAPESHIFTER_KEEPER", "Cajado Rúnico da Terra", "cajado runico da terra"),
                        item("2H_SHAPESHIFTER_AVALON", "Cajado Invocador da Luz", "cajado invocador da luz"),
                        item("2H_SHAPESHIFTER_CRYSTAL", "Cajado Petrificante", "cajado petrificante")
                ))
        )));

        // elmos, capotes e capuz
        lista.add(new Categoria("", "Elmos, Capotes e Capuz", Arrays.asList(

                new Subcategoria("Capote de Tecido", Arrays.asList(
                        item("HEAD_CLOTH_SET1", "Capote de Erudito", "capote de erudito"),
                        item("HEAD_CLOTH_SET2", "Capote de Clérigo", "capote de clerigo"),
                        item("HEAD_CLOTH_SET3", "Capote de Mago", "capote de mago"),
                        item("HEAD_CLOTH_KEEPER", "Capote de Druida", "capote de druida"),
                        item("HEAD_CLOTH_HELL", "Capote Malévolo", "capote de malevolo"),
                        item("HEAD_CLOTH_MORGANA", "Capote Sectário", "capote de sectario"),
                        item("HEAD_CLOTH_FEY", "Capote Feérico", "capote feerico"),
                        item("HEAD_CLOTH_AVALON", "Capote da Pureza", "capote da pureza"),
                        item("HEAD_CLOTH_ROYAL", "Capote Real", "capote real")
                )),

                new Subcategoria("Capuz de couro", Arrays.asList(
                        item("HEAD_LEATHER_SET1", "Capuz de Mercenário", "Capuz de mercenario"),
                        item("HEAD_LEATHER_SET2", "Capuz de Caçador", "Capuz de cacador caçador"),
                        item("HEAD_LEATHER_SET3", "Capuz de Assassino", "Capuz de assassino"),
                        item("HEAD_LEATHER_MORGANA", "Capuz de Espreitador", "Capuz de espreitador"),
                        item("HEAD_LEATHER_HELL", "Capuz Inferial", "Capuz inferial"),
                        item("HEAD_LEATHER_UNDEAD", "Capuz Espectral", "Capuz espectral"),
                        item("HEAD_LEATHER_FEY", "Capuz de Andarilho da Névoa", "Capuz de andarilho da nevoa"),
                        item("EAD_LEATHER_AVALON", "Capuz da Tenacidade", "Capuz da tenacidade")
                )),

                new Subcategoria("Elmo de Placa", Arrays.asList(
                        item("HEAD_PLATE_SET1", "Elmo de Soldado", "elmo de soldado"),
                        item("HEAD_PLATE_SET2", "Elmo de Cavaleiro", "elmo de cavaleiro"),
                        item("HEAD_PLATE_SET3", "Elmo de Guardião", "elmo de guardiao"),
                        item("HEAD_PLATE_UNDEAD", "Elmo de Guarda-tumbas", "elmo de guarda tumbas"),
                        item("HEAD_PLATE_HELL", "Elmo Demônio", "elmo demonio"),
                        item("HEAD_PLATE_KEEPER", "Elmo Judicante", "elmo judicante"),
                        item("HEAD_PLATE_FEY", "Elmo de Tecelão do Crepúsculo", "elmo de tecelao do crepusculo"),
                        item("HEAD_PLATE_AVALON", "Elmo da Bravura", "elmo da bravura"),
                        item("HEAD_PLATE_ROYAL", "Elmo Real", "elmo real")
                ))
        )));

        // peitoral
        lista.add(new Categoria("", "Armaduras de Peitoral", Arrays.asList(
                new Subcategoria("Armadura de Tecido", Arrays.asList(
                        item("ARMOR_CLOTH_SET1", "Robe de Erudito", "robe de erudito"),
                        item("ARMOR_CLOTH_SET2", "Robe de Clérigo", "robe de clerigo"),
                        item("ARMOR_CLOTH_SET3", "Robe de Mago", "robe de mago"),
                        item("ARMOR_CLOTH_KEEPER", "Robe de Druida", "robe de druida"),
                        item("ARMOR_CLOTH_HELL", "Robe Malévolo", "robe de malevolo"),
                        item("ARMOR_CLOTH_MORGANA", "Robe Sectário", "robe de sectario"),
                        item("ARMOR_CLOTH_FEY", "Robe Feérico", "robe feerico"),
                        item("ARMOR_CLOTH_AVALON", "Robe da Pureza", "robe da pureza"),
                        item("ARMOR_CLOTH_ROYAL", "Robe Real", "robe real")
                )),

                new Subcategoria("Armadura de Couro", Arrays.asList(
                        item("ARMOR_LEATHER_SET1", "Casaco de Mercenário", "casaco de mercenario"),
                        item("ARMOR_LEATHER_SET2", "Casaco de Caçador", "casaco de cacador caçador"),
                        item("ARMOR_LEATHER_SET3", "Casaco de Assassino", "casaco de assassino"),
                        item("RMOR_LEATHER_MORGANA", "Casaco de Espreitador", "casaco de espreitador"),
                        item("ARMOR_LEATHER_HELL", "Casaco Inferial", "casaco inferial"),
                        item("ARMOR_LEATHER_UNDEAD", "Casaco Espectral", "casaco espectral"),
                        item("ARMOR_LEATHER_FEY", "Casaco de Andarilho da Névoa", "casaco de andarilho da nevoa"),
                        item("", "Casaco da Tenacidade", "casaco da tenacidade")
                )),

                new Subcategoria("Armadura de Placa", Arrays.asList(
                        item("ARMOR_PLATE_SET1", "Armadura de Soldado", "armadura de soldado"),
                        item("ARMOR_PLATE_SET2", "Armadura de Cavaleiro", "armadura de cavaleiro"),
                        item("ARMOR_PLATE_SET3", "Armadura de Guardião", "armadura de guardiao"),
                        item("ARMOR_PLATE_UNDEAD", "Armadura de Guarda-tumbas", "armadura de guarda tumbas"),
                        item("ARMOR_PLATE_HELL", "Armadura Demônio", "armadura demonio"),
                        item("ARMOR_PLATE_KEEPER", "Armadura Judicante", "armadura judicante"),
                        item("ARMOR_PLATE_FEY", "Armadura de Tecelão do Crepúsculo", "armadura de tecelao do crepusculo"),
                        item("ARMOR_PLATE_AVALON", "Armadura da Bravura", "armadura da bravura"),
                        item("ARMOR_PLATE_ROYAL", "Armadura Real", "armadura real")
                ))
        )));


        // botas e sapatos
        lista.add(new Categoria("", "Armadura de Calçado", Arrays.asList(

                new Subcategoria("Sapato de Tecido", Arrays.asList(
                        item("SHOES_CLOTH_SET1", "Sandálias de Erudito", "sandálias de erudito"),
                        item("SHOES_CLOTH_SET2", "Sandálias de Clérigo", "sandálias de clerigo"),
                        item("SHOES_CLOTH_SET3", "Sandálias de Mago", "sandálias de mago"),
                        item("SHOES_CLOTH_KEEPER", "Sandálias de Druida", "sandálias de druida"),
                        item("SHOES_CLOTH_HELL", "Sandálias Malévolo", "sandálias de malevolo"),
                        item("SHOES_CLOTH_MORGANA", "Sandálias Sectário", "sandálias de sectario"),
                        item("SHOES_CLOTH_FEY", "Sandálias Feérico", "sandálias feerico"),
                        item("SHOES_CLOTH_AVALON", "Sandálias da Pureza", "sandálias da pureza"),
                        item("SHOES_CLOTH_ROYAL", "Sandálias Real", "sandálias real")
                )),

                new Subcategoria("Sapato de Couro", Arrays.asList(
                        item("SHOES_LEATHER_SET1", "Sapatos de Mercenário", "sapatos de mercenario"),
                        item("SHOES_LEATHER_SET2", "Sapatos de Caçador", "sapatos de cacador caçador"),
                        item("SHOES_LEATHER_SET3", "Sapatos de Assassino", "sapatos de assassino"),
                        item("SHOES_LEATHER_MORGANA", "Sapatos de Espreitador", "sapatos de espreitador"),
                        item("SHOES_LEATHER_HELL", "Sapatos Inferial", "sapatos inferial"),
                        item("SHOES_LEATHER_UNDEAD", "Sapatos Espectral", "sapatos espectral"),
                        item("SHOES_LEATHER_FEY", "Sapatos de Andarilho da Névoa", "sapatos de andarilho da nevoa"),
                        item("SHOES_LEATHER_AVALON", "Sapatos da Tenacidade", "sapatos da tenacidade")
                )),


                new Subcategoria("Sapato de Placa", Arrays.asList(
                        item("SHOES_PLATE_SET1", "Botas de Soldado", "botas de soldado"),
                        item("SHOES_PLATE_SET2", "Botas de Cavaleiro", "botas de cavaleiro"),
                        item("SHOES_PLATE_SET3", "Botas de Guardião", "botas de guardiao"),
                        item("SHOES_PLATE_UNDEAD", "Botas de Guarda-tumbas", "botas de guarda tumbas"),
                        item("SHOES_PLATE_HELL", "Botas Demônio", "botas demonio"),
                        item("SHOES_PLATE_KEEPER", "Botas Judicante", "botas judicante"),
                        item("SHOES_PLATE_FEY", "Botas de Tecelão do Crepúsculo", "botas de tecelao do crepusculo"),
                        item("SHOES_PLATE_AVALON", "Botas da Bravura", "botas da bravura"),
                        item("SHOES_PLATE_ROYAL", "Botas Real", "botas real")
                ))
        )));


        // mao secundaria
        lista.add(new Categoria("", "Mão Secundária", Arrays.asList(

                new Subcategoria("Guerreiro", Arrays.asList(
                        item("OFF_SHIELD", "Escudo", "escudo"),
                        item("OFF_TOWERSHIELD_UNDEAD", "Sarcófago", "sarcofago"),
                        item("OFF_SHIELD_HELL", "Escudo Vampírico", "escudo vampirico"),
                        item("OFF_SPIKEDSHIELD_MORGANA", "Quebra-rostos", "quebra rostos"),
                        item("OFF_SHIELD_AVALON", "Égide Astral", "egide astra"),
                        item("OFF_SHIELD_CRYSTAL", "Barreira Inquebrável", "barreira inquebravel")
                )),

                new Subcategoria("Mago", Arrays.asList(
                        item("OFF_BOOK", "Tomo de Feitiços", "tomo"),
                        item("OFF_ORB_MORGANA", "Olho dos Segredos", "olho"),
                        item("OFF_DEMONSKULL_HELL", "Muisec", "muisec"),
                        item("OFF_TOTEM_KEEPER", "Raiz Mestra", "raiz"),
                        item("OFF_CENSER_AVALON", "Incensario Celeste", "incensario"),
                        item("OFF_TOME_CRYSTAL", "Grimório Estagnado", "grimorio")
                )),

                new Subcategoria("Caçador", Arrays.asList(
                        item("OFF_TORCH", "Tocha", "tocha"),
                        item("OFF_HORN_KEEPER", "Brumário", "brumario"),
                        item("OFF_TALISMAN_AVALON", "Cetro Sagrado", "cetro sagrado"),
                        item("OFF_LAMP_UNDEA", "Lume Críptico", "lume"),
                        item("OFF_JESTERCANE_HELL", "Bengala Maligna", "bengala"),
                        item("OFF_TORCH_CRYSTAL", "Tocha Chama Azul", "tocha chama azul")
                ))
        )));


        lista.add(new Categoria("", "Capas", Arrays.asList(
                new Subcategoria("Capas", Arrays.asList(
                        item("CAPE", "Capa", "capa cape"),
                        item("CAPEITEM_FW_BRIDGEWATCH", "Capa de Bridgewatch", "capa bridgewatch"),
                        item("CAPEITEM_FW_CAERLEON", "Capa de Caerleon", "capa caerleon"),
                        item("CAPEITEM_FW_LYMHURST", "Capa de Lymhurst", "capa lymhurst"),
                        item("CAPEITEM_FW_MARTLOCK", "Capa de Martlock", "capa martlock"),
                        item("CAPEITEM_FW_THETFORD", "Capa de Thetford", "capa thetford"),
                        item("CAPEITEM_FW_FORTSTERLING", "Capa de Fort Sterling", "capa fort sterling"),
                        item("CAPEITEM_AVALON", "Capa Avaloniana", "capa avaloniana"),
                        item("CAPEITEM_SMUGGLER", "Capa de Contrabandista", "capa de contrabandista"),
                        item("CAPEITEM_HERETIC", "Capa Herege", "capa herege"),
                        item("CAPEITEM_UNDEAD", "Capa Morta-Viva", "capa morta viva"),
                        item("CAPEITEM_KEEPER", "Capa Protetora", "capa protetora"),
                        item("CAPEITEM_MORGANA", "Capa de Morgana", "capa de morgana"),
                        item("CAPEITEM_DEMON", "Capa Demoníaca", "capa demoniaca")
                ))
        )));


        lista.add(new Categoria("", "Bolsas", Arrays.asList(
                new Subcategoria("Bolsas", Arrays.asList(
                        item("BAG", "Bolsa", "bolsa bag"),
                        item("BAG_INSIGHT", "Sacola da Visão", "sacola da visao")
                ))
        )));


        // montarias, não é necessário
        /*
        lista.add(new Categoria("", "Montarias", Arrays.asList(
                new Subcategoria("Montarias Base", Arrays.asList(
                        item("MOUNT_HORSE", "Cavalo", "cavalo"),
                        item("MOUNT_ARMORED_HORSE", "Cavalo Blindado", "cavalo blindado"),
                        item("MOUNT_OX", "Boi", "boi de transporte"),
                        item("MOUNT_RAM_FW_MARTLOCK", "Carneiro", "carneiro"),
                        item("MOUNT_DIREWOLF", "Lobo-Vil", "lobo vil"),
                        item("MOUNT_GREYWOLF_FW_CAERLEON", "Lobo Cinzento", "lobo cinzento"),
                        item("MOUNT_GIANTSTAG", "Veado Gigante", "veado gigante"),
                        item("MOUNT_GIANTSTAG_MOOSE", "Alce", "alce"),
                        item("MOUNT_COUGAR_KEEPER", "Garra-ligeira", "garra ligeira"),
                        item("MOUNT_DIREBOAR_FW_LYMHURST", "Javali", "javali"),
                        item("MOUNT_DIREBOAR_FW_LYMHURST_ELITE", "Javali de elite", "javali de elite"),
                        item("MOUNT_DIREBOAR", "Javali-vil", "javali vil"),
                        item("MOUNT_DIREBEAR_FW_FORTSTERLING", "Urso das Neves", "urso das neves"),
                        item("MOUNT_DIREBEAR", "Urso-Vil", "urso vil"),
                        item("MOUNT_SWAMPDRAGON_FW_THETFORD", "Salamandra do Pantâno", "salamandra do pantano"),
                        item("MOUNT_SWAMPDRAGON", "Dragão do Pântano", "dragao do pantano"),
                        item("MOUNT_MULE", "Mula", "mula"),
                        item("MOUNT_MAMMOTH_TRANSPORT", "Mamute", "mamute"),
                        item("MOUNT_TERRORBIRD_ADC", "Terrorbird", "avemoa terrorbird")
        )));
))*/


        lista.add(new Categoria("", "Consumíveis", Arrays.asList(
                new Subcategoria("Comida", Arrays.asList(
                        item("MEAL_GRILLEDFISH", "Peixe Grelhado", "peixe grelhado"),
                        item("MEAL_SALAD", "Salada", "salada"),
                        item("MEAL_SALAD_FISH", "Salada de Peixe", "salada de"),
                        item("MEAL_SOUP", "Sopa", "sopa"),
                        item("MEAL_SOUP_FISH", "Sopa de Peixe", "sopa de"),
                        item("MEAL_OMELETTE", "Omelete", "omelete"),
                        item("MEAL_OMELETTE_FISH", "Omelete de peixe", "omelete de"),
                        item("MEAL_STEW", "Guisado", "guisado"),
                        item("MEAL_STEW_FISH", "Guisado de Peixe", "guisado de"),

                        item("MEAL_PIE", "Torta", "torna"),
                        item("MEAL_PIE_FISH", "Torta de peixe", "torta de"),
                        item("MEAL_SANDWICH", "Sanduiche", "sanduiche"),
                        item("MEAL_SANDWICH_FISH", "Sanduiche de Peixe", "sanduiche de"),
                        item("MEAL_SANDWICH_AVALON", "Sanduiche de Avaloniano", "sanduiche ava")

                )),

                new Subcategoria("Poçoes", Arrays.asList(
                        item("POTION_HEAL", "Poção de Cura", "pocao de cura"),
                        item("POTION_ENERGY", "Poção de Energia", "pocao de energia"),
                        item("POTION_REVIVE", "Poção de Crescimento", "pocao de crescimento"),
                        item("OTION_STONESKIN", "Poção de Resistência", "pocao de resistencia"),
                        item("POTION_SLOWFIELD", "Poção Pegajosa", "pocao pegajosa"),
                        item("POTION_COOLDOWN", "Poção Venenosa", "pocao venenosa"),
                        item("POTION_CLEANSE", "Poção de Invisibilidade", "pocao de invisibilidade"),
                        item("POTION_MOB_RESET", "Poção Calmante", "pocao calmante"),
                        item("POTION_CLEANSE2", "Poção Purificadora", "pocao purificadora"),
                        item("POTION_ACID", "Poção Ácida", "pocao acida"),
                        item("POTION_BERSERK", "Poção de Fúria", "pocao de furia"),
                        item("POTION_LAVA", "Poção Infernal", "pocao infernal"),
                        item("POTION_GATHER", "Poção de Coleta", "pocao de coleta"),
                        item("POTION_TORNADO", "Tornado Engarrafado", "tornado engarrafado")
                ))
        )));


        lista.add(new Categoria("", "Equipamento de Coleta", Arrays.asList(

                new Subcategoria("Pesca", Arrays.asList(
                        item("FISHINGBAIT", "Ísca", "isca"),
                        item("2H_TOOL_FISHINGROD", "Vara de Pesca", "vara de pesca"),
                        item("2H_TOOL_FISHINGROD_AVALON", "Vara de Pesca Avaloniana", "vara de pesca avaloniana"),
                        item("HEAD_GATHERER_FISH", "Chapéu do Pescador", "chapeu do pescador"),
                        item("ARMOR_GATHERER_FISH", "Traje do Pescador", "traje do pescador"),
                        item("SHOES_GATHERER_FISH", "Botas do Pescador", "botas do pescador"),
                        item("BACKPACK_GATHERER_FISH", "Mochila do Pescador", "mochila do pescador")
                )),

                new Subcategoria("Fibra", Arrays.asList(
                        //infelizmente ele puxa o icone da foice de cristal pra foicezinha normal
                        item("2H_TOOL_SICKLE", "Foice", "foice"),
                        item("2H_TOOL_SICKLE_AVALON", "Foice Avaloniana", "foice avaloniana"),
                        item("HEAD_GATHERER_FIBER", "Chapéu do Ceifeiro", "chapeu do ceifeiro"),
                        item("ARMOR_GATHERER_FIBER", "Traje do Ceifeiro", "traje do ceifeiro"),
                        item("SHOES_GATHERER_FIBER", "Botas do Ceifeiro", "botas do ceifeiro"),
                        item("BACKPACK_GATHERER_FIBER", "Mochila do Ceifeiro", "mochila do ceifeiro")

                )),

                new Subcategoria("Pelego", Arrays.asList(
                        item("2H_TOOL_KNIFE", "Faca de Esfolar", "faca de esfolar"),
                        item("2H_TOOL_KNIFE_AVALON", "Faca de Esfolar Avaloniana", "faca de esfolar avaloniana"),
                        item("HEAD_GATHERER_HIDE", "Chapéu do Esfolador", "chapeu do esforalor"),
                        item("ARMOR_GATHERER_FIBER", "Traje do Esfolador", "traje do esfolador"),
                        item("SHOES_GATHERER_FIBER", "Botas do Esfolador", "botas do esfolador"),
                        item("BACKPACK_GATHERER_FIBER", "Mochila do Esfolador", "mochila do esfolador")
                )),

                new Subcategoria("Minérios", Arrays.asList(
                        item("2H_TOOL_PICK", "Picareta", "picareta"),
                        item("2H_TOOL_PICK_AVALON", "Picareta Avaloniana", "picareta avaloniana"),
                        item("HEAD_GATHERER_ORE", "Chapéu do Minerador", "chapeu do minerador"),
                        item("ARMOR_GATHERER_FIBER", "Traje do Minerador", "traje do minerador"),
                        item("SHOES_GATHERER_FIBER", "Botas do Minerador", "botas do minerador"),
                        item("BACKPACK_GATHERER_FIBER", "Mochila do Minerador", "mochila do minerador")
                )),

                new Subcategoria("Pedra", Arrays.asList(
                        item("H_TOOL_HAMMER", "Martelo de Pedra", "martelo de pedra"),
                        item("H_TOOL_HAMMER_AVALON", "Martelo de Pedra Avaloniano", "martelo de pedra avaloniano"),
                        item("HEAD_GATHERER_ROCK", "Chapéu do Cavouqueiro", ""),
                        item("ARMOR_GATHERER_FIBER", "Traje do Cavouqueiro", "traje do cavouqueiro"),
                        item("SHOES_GATHERER_FIBER", "Botas do Cavouqueiro", "botas do cavouqueiro"),
                        item("BACKPACK_GATHERER_FIBER", "Mochila do Cavouqueiro", "mochila do cavouqueiro")
                )),

                new Subcategoria("Madeira", Arrays.asList(
                        item("2H_TOOL_AXE", "Machado", "machado"),
                        item("2H_TOOL_AXE_AVALON", "Machado Avaloniano", "machado avaloniano"),
                        item("HEAD_GATHERER_WOOD", "Chapéu do Lenhador", "chapeu do lenhador"),
                        item("ARMOR_GATHERER_FIBER", "Traje do Lenhador", "traje do lenhador"),
                        item("SHOES_GATHERER_FIBER", "Botas do Lenhador", "botas do lenhador"),
                        item("BACKPACK_GATHERER_FIBER", "Mochila do Lenhador", "mochila do lenhador")
                ))
        )));



        /*
        lista.add(new Categoria("", "Recursos Brutos", Arrays.asList(

                new Subcategoria("Pesca", Arrays.asList(
                        item("", "", "")
                        ))

        )));


        lista.add(new Categoria("", "Recursos refinados", Arrays.asList(

                new Subcategoria("Pesca", Arrays.asList(
                        item("", "", "")
                ))

        )));*/


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

    // retorna nome do recurso refinado pelo sufixo e tier
    // indices: 2=T2, 3=T3 ... 8=T8
    public static String getNomeRecurso(String sufixo, int tier) {
        String[] nomes = switch (sufixo) {
            case "METALBAR" ->
                    new String[]{"", "", "Barra de Ferro", "Barra de Aço", "Barra de Aço Nobrium", "Barra de Titânio", "Barra de Runite", "Barra de Mithril", "Barra de Adamantium"};
            case "LEATHER" ->
                    new String[]{"", "", "Couro", "Couro Grosso", "Couro Endurecido", "Couro Rígido", "Couro de Elite", "Couro de Sombra", "Couro de Sangue"};
            case "CLOTH" ->
                    new String[]{"", "", "Tecido", "Tecido Grosso", "Seda Encantada", "Seda de Elite", "Seda da Sombra", "Seda do Vazio", "Seda Astral"};
            case "PLANKS" ->
                    new String[]{"", "", "Tábuas", "Tábuas Grossas", "Tábuas de Carvalho", "Tábuas de Teixo", "Tábuas de Álamo Negro", "Tábuas de Freixo", "Tábuas de Cerejeira"};
            case "STONEBLOCK" ->
                    new String[]{"", "", "Bloco de Calcário", "Bloco de Arenito", "Bloco de Travertino", "Bloco de Granito", "Bloco de Basalto", "Bloco de Mármore", "Bloco de Rocha Negra"};
            default -> null;
        };
        if (nomes == null || tier < 0 || tier >= nomes.length) return null;
        return nomes[tier];
    }

}