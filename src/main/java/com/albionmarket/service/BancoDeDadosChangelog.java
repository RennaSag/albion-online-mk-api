package com.albionmarket.service;

import com.albionmarket.model.VersaoInfo;
import com.albionmarket.model.VersaoInfo.Mudanca;
import com.albionmarket.model.VersaoInfo.TipoMudanca;



import java.util.Arrays;
import java.util.List;

/*
 * Tipos de mudança disponíveis:
 * TipoMudanca.NOVIDADE   →  ✦  (dourado)  — funcionalidade nova
 * TipoMudanca.MELHORIA   →  ↑  (azul)     — melhoria de algo existente
 * TipoMudanca.CORRECAO   →  ✓  (verde)    — bug corrigido
 * TipoMudanca.REMOCAO    →  ✗  (vermelho) — algo removido
 */
public class BancoDeDadosChangelog {


    public static final String APP_VERSAO_ATUAL = "1.0.4";


    public static final List<VersaoInfo> VERSOES = Arrays.asList(


            new VersaoInfo(
                    "1.0.4",
                    "26 de Abril, 2026",
                    "Lançamento Inicial",
                    Arrays.asList(
                            new Mudanca(TipoMudanca.NOVIDADE, "Caro usuário, obrigado por comprar o programa Analisador de Mercado de Albion Online."),
                            new Mudanca(TipoMudanca.NOVIDADE, "Como agradecimento pela compra, foi adiconado +5 dias de acesso à sua licença."),
                            new Mudanca(TipoMudanca.MELHORIA, "Foi adicionado nessa nova versão esta tela de changelog para você acompanhar as atualizações do projeto.")
                            //  new Mudanca(TipoMudanca.NOVIDADE, "Suporte a todos os tiers (T2–T8) e encantamentos"),
                            //  new Mudanca(TipoMudanca.NOVIDADE, "Filtro de cidades para comparar mercados")
                    )
            )


    );
}