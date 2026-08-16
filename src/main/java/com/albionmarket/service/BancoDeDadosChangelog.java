package com.albionmarket.service;

import com.albionmarket.model.VersaoInfo;
import com.albionmarket.model.VersaoInfo.Mudanca;
import com.albionmarket.model.VersaoInfo.TipoMudanca;


import java.util.Arrays;
import java.util.List;

/*
 * Tipos de mudança disponíveis:
 * TipoMudanca.NOVIDADE   →  ✦  (dourado)  - funcionalidade nova
 * TipoMudanca.MELHORIA   →  ↑  (azul)     - melhoria de algo existente
 * TipoMudanca.CORRECAO   →  ✓  (verde)    - bug corrigido
 * TipoMudanca.REMOCAO    →  ✗  (vermelho) - algo removido
 */
public class BancoDeDadosChangelog {


    public static final String APP_VERSAO_ATUAL = "1.1.3";


    public static final List<VersaoInfo> VERSOES = Arrays.asList(


            new VersaoInfo(
                    "1.1.3",
                    "15 de Agosto de 2026",
                    "Telas mais rápidas e otimizações",
                    Arrays.asList(

                            new Mudanca(TipoMudanca.NOVIDADE, "Craft, Refino e Craft com Refino agora carregam bem mais rápido"),
                            new Mudanca(TipoMudanca.CORRECAO, "Corrigidos vários itens com o diário de craft."),
                            new Mudanca(TipoMudanca.CORRECAO, "Revisado o cálculo de lucro do Craft."),
                            new Mudanca(TipoMudanca.CORRECAO, "Corrigidas linhas vazias que às vezes sobravam no final das tabelas de preços."),
                            new Mudanca(TipoMudanca.MELHORIA, "Sair de uma tela ou clicar em Atualizar Valores agora cancela buscas antigas em andamento, evitando resultado atrasado aparecendo por cima do novo."),
                            new Mudanca(TipoMudanca.NOVIDADE, "Home renovada: ícones nos botões e aviso de validade da licença mais visível quando estiver perto de vencer."),
                            new Mudanca(TipoMudanca.MELHORIA, "Qualquer problema, chama no suporte: rennasagcontato@gmail.com")
                    )
            )
    );
}