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


    public static final String APP_VERSAO_ATUAL = "1.0.8";


    public static final List<VersaoInfo> VERSOES = Arrays.asList(


            new VersaoInfo(
                    "1.0.8",
                    "02 de Maio, 2026",
                    "Otimizações e correção de bugs",
                    Arrays.asList(
                            new Mudanca(TipoMudanca.CORRECAO, "Nessa nova versão, houve correção de alguns bugs com o cálculo dos diários, que antes estava com alguns problemas."),
                            new Mudanca(TipoMudanca.CORRECAO, "Por conta de um problema no servidor, alguns usuários não estavam recebendo a key de ativação no momento da compra, e isso foi corrigido."),
                            new Mudanca(TipoMudanca.NOVIDADE, "Nas versões futuras, haverá melhorias no sistema de busca de dados pela api, com o proposito de deixar as requisições mais rápidas."),
                            new Mudanca(TipoMudanca.REMOCAO, "Preste atenção no craft com diários! O campo 'Lucro' na tabela de craft não está incluindo o lucro com a venda dos diários de craft."),
                            new Mudanca(TipoMudanca.REMOCAO, "Se você está tendo ou tiver qualquer problema com o aplicativo, entre em contato comigo pelo email de suporte: rennasagcontato@gmail.com. Um forte abraço"),
                            new Mudanca(TipoMudanca.CORRECAO, "Corrigido cálculo de refino, eu tinha mexido no código e ficou errado, mas agora ta resolvido")

                    )
            )


    );
}