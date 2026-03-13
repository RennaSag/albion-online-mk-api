package com.albionmarket.service;

import com.albionmarket.model.Categoria;
import com.albionmarket.model.CidadeInfo;
import com.albionmarket.model.ItemDefinition;
import com.albionmarket.model.Subcategoria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * AS SUBCATEGORIAS JA PREENCHIDAS ESTÃO PRONTAS, FALTA AS OUTRAS CATEGORIAS E SUBCATEGORIAS DE ITENS DO JOGO
 *
 * catalogo completo de itens, categorias e cidades do Albion Online.
 * Nomes em portugues br
 * IDs baseados na Albion Online Data API (west.albion-online-data.com).
 */
public class BancoDeDados {

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
                        item("2H_SCYTHE_CRYSTAL", "Foice de cristal", "foice de cristal")
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

                ))
        )));

        // cajados
        lista.add(new Categoria("", "Cajados Arcano", Arrays.asList(
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

                )),

                new Subcategoria("Cajado da Natureza", Arrays.asList(

                )),

                new Subcategoria("Arco", Arrays.asList(

                )),
                new Subcategoria("Besta", Arrays.asList(

                )),
                new Subcategoria("Cajado Metamordo", Arrays.asList(

                ))


        )));


        // elmos, capotes e chapeus
        lista.add(new Categoria("", "Armadura de Capacete", Arrays.asList(

                new Subcategoria("Tecido (Mago)", Arrays.asList(

                )),

                new Subcategoria("Elmos de couro", Arrays.asList(

                )),

                new Subcategoria("Elmos", Arrays.asList(

                ))
        )));

        // peitoral
        lista.add(new Categoria("", "Armaduras de Peitoral", Arrays.asList(

                new Subcategoria("Tecido (Mago)", Arrays.asList(

                )),

                new Subcategoria("Couro (Ranger)", Arrays.asList(

                )),

                new Subcategoria("Placa (Guerreiro)", Arrays.asList(

                ))
        )));

        // botas e sapatos
        lista.add(new Categoria("", "Armadura de Calçado", Arrays.asList(

                new Subcategoria("Tecido (Mago)", Arrays.asList(

                        new ItemDefinition[]{})),

                new Subcategoria("Couro (Ranger)", Arrays.asList(

                )),

                new Subcategoria("Placa (Guerreiro)", Arrays.asList(

                ))
        )));

        // mao secundaria
        lista.add(new Categoria("", "Mão Secundária", Arrays.asList(

                new Subcategoria("Escudo", Arrays.asList(

                )),

                new Subcategoria("Livro/Olho/Tocha/Brumário/Raiz", Arrays.asList(

                )),

                new Subcategoria("Capas", Arrays.asList(
                        item("CAPE", "Capa", "capa cape"),
                        item("CAPEITEM_FW_BRIDGEWATCH", "Capa de Bridgewatch", "capa bridgewatch"),
                        item("CAPEITEM_FW_CAERLEON", "Capa de Caerleon", "capa caerleon"),
                        item("CAPEITEM_FW_LYMHURST", "Capa de Lymhurst", "capa lymhurst"),
                        item("CAPEITEM_FW_MARTLOCK", "Capa de Martlock", "capa martlock"),
                        item("CAPEITEM_FW_THETFORD", "Capa de Thetford", "capa thetford"),
                        item("CAPEITEM_FW_FORTSTERLING", "Capa de Fort Sterling", "capa fort sterling")
                ))
        )));

        // montarias
        lista.add(new Categoria("", "Montarias", Arrays.asList(

                new Subcategoria("Cavalos", Arrays.asList(

                )),

                new Subcategoria("Bois e Mulas", Arrays.asList(

                )),

                new Subcategoria("Cervos e Alces", Arrays.asList(

                )),

                new Subcategoria("Lobos", Arrays.asList(
                        item("MOUNT_DIREWOLF", "Lobo Feroz", "lobo feroz direwolf"),
                        item("MOUNT_DIREWOLF_GHOST", "Lobo Fantasma", "lobo fantasma ghost direwolf"),
                        item("MOUNT_GREYWOLF_FW_CAERLEON", "Lobo Cinza", "lobo cinza greywolf caerleon"),
                        item("MOUNT_GREYWOLF_FW_CAERLEON_ELITE", "Lobo Cinza de Elite", "lobo cinza elite greywolf"),
                        item("MOUNT_COUGAR_KEEPER", "Puma do Guardador", "puma cougar keeper"),
                        item("MOUNT_COUGAR_KEEPER_ELITE", "Puma do Guardador de Elite", "puma cougar keeper elite"),
                        item("MOUNT_RAGECLAW", "Garra-Fúria", "garra furia rageclaw")
                )),

                new Subcategoria("Ursos", Arrays.asList(

                )),

                new Subcategoria("Javalis e Dragões", Arrays.asList(

                )),

                new Subcategoria("Mamutes", Arrays.asList(

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