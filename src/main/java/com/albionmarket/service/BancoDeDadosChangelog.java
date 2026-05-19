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


    public static final String APP_VERSAO_ATUAL = "1.1.2";


    public static final List<VersaoInfo> VERSOES = Arrays.asList(


            new VersaoInfo(
                    "1.1.2",
                    "19 de Maio, 2026",
                    "Craft com refino",
                    Arrays.asList(

                            new Mudanca(TipoMudanca.NOVIDADE, "Olá a todos!"),
                            new Mudanca(TipoMudanca.MELHORIA, "Nessa nova versão, foi implementada a feature para craft a partir de um refino, como havia sido pedido por algumas pessoas"),
                            new Mudanca(TipoMudanca.CORRECAO, "A nova ferramenta de craft com refino ainda não foi testada nem validada, então recomendo tomarem cuidado por enquanto. Quando tudo estiver correto, vou gravar um vídeo mostrando. Ainda será melhorada para que fique tudo perfeito"),
                            new Mudanca(TipoMudanca.REMOCAO, "Além disso, estou trabalhando na feature de flip de mercado, mas ainda sem sucesso. Estou com alguns problemas na requisição dos dados do flip pela API do Albion Data, mas futuramente tudo será resolvido"),
                            new Mudanca(TipoMudanca.MELHORIA, "Se tiver qualquer problema, entre em contato com o suporte: rennasagcontato@gmail.com")
                            )
            )


    );
}