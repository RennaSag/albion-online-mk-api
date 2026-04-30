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


    public static final String APP_VERSAO_ATUAL = "1.0.6";


    public static final List<VersaoInfo> VERSOES = Arrays.asList(


            new VersaoInfo(
                    "1.0.6",
                    "30 de Abril, 2026",
                    "Implementação dos diários de Craft",
                    Arrays.asList(
                            //new Mudanca(TipoMudanca.CORRECAO, "Houve uma correção no sistema de envio automático de emails com as keys, se você teve ou está tendo qualquer problema em acessar o programa mesmo após a compra, entre em contato com o suporte (rennasagcontato@gmail.com"),
                            new Mudanca(TipoMudanca.NOVIDADE, "Nessa nova versão, houve finalmente a implementação dos cálculos de diários de craft.")
                            //new Mudanca(TipoMudanca.NOVIDADE, "Como agradecimento pela compra, foi adiconado +5 dias de acesso à sua licença."),
                            //new Mudanca(TipoMudanca.MELHORIA, "Foi adicionado nessa nova versão esta tela de changelog para você acompanhar as atualizações do projeto.")

                    )
            )


    );
}