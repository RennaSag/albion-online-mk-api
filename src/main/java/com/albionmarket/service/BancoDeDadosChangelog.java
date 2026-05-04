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


    public static final String APP_VERSAO_ATUAL = "1.1.1";


    public static final List<VersaoInfo> VERSOES = Arrays.asList(


            new VersaoInfo(
                    "1.1.1",
                    "04 de Maio, 2026",
                    "Adição dos diários de ferramenteiro",
                    Arrays.asList(

                            new Mudanca(TipoMudanca.NOVIDADE, "Um bom dia a todos"),
                            new Mudanca(TipoMudanca.NOVIDADE, "Nessa nova versão, foi adicionado os diários de trabalhadores ferramenteiros, que até o momento estava faltando"),
                            new Mudanca(TipoMudanca.MELHORIA, "Todos os diários pra outros itens como armas, armaduras e etc também já estão implementados"),
                            new Mudanca(TipoMudanca.CORRECAO, "Também foram feitas algumas pequenas correções visuais e melhorias na parte de refino e no salvamento das operações ativas"),
                            new Mudanca(TipoMudanca.NOVIDADE, "Espero que você esteja gostando do app e que lhe seja útil"),
                            new Mudanca(TipoMudanca.REMOCAO, "Como sempre, se estiver tendo qualquer problema com o programa, tenha notado um bug ou queira sugerir melhorias, entre em contato com o email de suporte: rennasagcontato@gmail.com")

                    )
            )


    );
}