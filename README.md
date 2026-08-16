# Albion Market

Projeto pessoal em desenvolvimento.

Albion Market é uma aplicação desktop desenvolvida em Java com JavaFX, inspirada em Albion Online — um MMORPG com economia complexa baseada inteiramente na atividade dos jogadores, funcionando de forma semelhante a um mercado financeiro real.

---

## Sobre o projeto

A proposta central é desenvolver um software capaz de consumir grandes volumes de dados atualizados em tempo real por meio de APIs externas, processá-los e organizá-los de forma estruturada para identificar oportunidades de arbitragem e operações economicamente vantajosas dentro do ecossistema do jogo.

Atualmente, não existe uma ferramenta com esse nível de abordagem, tanto na comunidade nacional quanto internacional. As soluções existentes se limitam a planilhas e recursos isolados, com diversas limitações em atualização, automação e capacidade de análise.

---

## Funcionalidades

### Pesquisa de preços

- Consulta preços de itens em tempo real a partir da API pública mantida pela comunidade
- Compara preços entre as 8 cidades do jogo:
  - Caerleon
  - Bridgewatch
  - Fort Sterling
  - Lymhurst
  - Martlock
  - Thetford
  - Black Market
  - Brecilien
- Permite filtros por:
  - Nome
  - Categoria
  - Subcategoria
  - Tier (T1–T8)
  - Nível de encantamento (.1 a .4)
- Exibe:
  - Preço mínimo de venda
  - Preço máximo de compra
  - Data da última atualização por cidade
- Identifica automaticamente a cidade com o melhor preço de venda

---

### Calculadora de craft

- Busca receitas diretamente da API oficial do jogo
- Exibe materiais necessários com quantidade, preço de compra e cidade de origem
- Resultados carregam de forma progressiva: preço do item, receita e cada material vão aparecendo assim que são encontrados, sem esperar a busca inteira terminar
- Trata corretamente artefatos, cuja quantidade necessária é igual à quantidade final craftada (não retornam pela taxa de retorno)
- Calcula automaticamente:
  - Quantidade final considerando a taxa de retorno (progressão geométrica de reinvestimento)
  - Custo total dos materiais
  - Custo da barraca de craft (baseado em nutrição e game value)
  - Receita total e taxa de mercado (10% sem premium / 5% com premium)
  - Lucro ou prejuízo estimado, incluindo o bônus de diários de fama
- Permite inserção manual de preços para simulações
- Atualiza todos os cálculos em tempo real conforme os parâmetros são alterados
- Identifica melhor cidade para venda e melhor cidade para compra dos materiais
- Permite salvar operações, que ficam disponíveis na tela de Operações Ativas

---

### Calculadora de refino

- Mesma lógica de custo/lucro do craft, aplicada aos recursos refinados (tecido, couro, tábua, barra, blocos de pedra)
- Considera o catalisador do tier anterior na conta, já com o desconto da taxa de retorno do refino

---

### Craft com refino

- Para itens que usam recursos refinados diretamente do refino, calcula as duas etapas encadeadas: quanto de recurso bruto comprar pra refinar, e quanto do refinado é necessário pro craft final
- Mesma lógica de progressão geométrica aplicada nas duas etapas em sequência

---

### Flip de mercado

- Varre o catálogo em busca de oportunidades de comprar em uma cidade e vender em outra com lucro
- Permite ordenar os resultados por qualquer coluna (ex: maior lucro %)
- Operações salvas aparecem em Operações Ativas junto com as de craft e refino

---

### Operações ativas

- Lista todas as operações salvas (craft, refino, craft com refino e flip) com preços, quantidades, custo e lucro
- Permite finalizar/remover uma operação da lista

---

### Interface e organização

- Interface em modo escuro
- Identificação visual das cidades por cores únicas
- Seleção de cidades na tela de busca de item
- Persistência de filtros ao navegar entre telas
- Barra de rolagem adaptada para monitores menores
- Persistência entre sessões via Java Preferences API
- Cache local de ícones: cada item baixado uma vez fica salvo no computador e carrega instantâneo nas próximas vezes, sem depender do servidor de imagens da Albion
- Tela de novidades (changelog) exibida automaticamente após atualizações
- Sistema de login com licença, validada contra um backend próprio (`licencas-server`)

---

## Arquitetura

O projeto segue uma arquitetura em camadas:

- **UI**
  Telas construídas com JavaFX
  (`TelaHome`, `TelaLogin`, `TelaCraftSelecao`, `TelaCraft`, `TelaRefinoSelecao`, `TelaRefino`, `TelaCraftRefinoSelecao`, `TelaCraftRefino`, `TelaFlipSelecao`, `TelaFlip`, `TelaPesquisaPrecos`, `TelaOperacoesAtivas`, `TelaChangelog`, `TelaUpdate`)

- **Service**
  Lógica de negócio e integração com APIs
  (`ApiService`, `CraftService`, `CalculadoraService`, `BancoDeDadosItens`, `BancoDeDadosChangelog`, `ItemValues`, `BuscaService`, `IconeCacheService`, `OperacaoService`)

- **Model**
  Estruturas de dados
  (`ItemDefinition`, `PriceEntry`, `ReceitaCraft`, `CidadeInfo`, `EstadoSelecao`, `VersaoInfo`, entre outros)

- **Util**
  Utilitários de apoio
  (`FormatadorUtil`, `AlbionIdUtil`)

O estado de navegação entre telas é controlado por `EstadoSelecao`, responsável por manter item, tier, encantamento e cidades selecionadas ao voltar para a tela de busca.

`licencas-server/` é um serviço Node.js/Express separado, responsável por emitir e validar as chaves de licença via webhook de compra e banco Postgres — não faz parte do build/runtime do app JavaFX, é implantado à parte.

---

## APIs utilizadas

- Albion Online Data Project
  `west.albion-online-data.com`
  (preços de mercado em tempo real)

- Albion Online GameInfo
  `gameinfo.albiononline.com`
  (receitas e dados de itens)

- Albion Online Render
  `render.albiononline.com`
  (ícones e imagens, com cache local no computador do usuário)

---

## Tecnologias utilizadas

- Java 23
- JavaFX 23
- Maven
- HttpClient (requisições HTTP assíncronas, com controle de concorrência e nova tentativa automática em caso de falha)
- Gson (parsing de JSON)
- Java Preferences API (persistência local)
- Execução em threads separadas para evitar bloqueio da interface

---

## Motivação

Este projeto serve como aplicação prática de conceitos de desenvolvimento de software, incluindo integração com APIs externas, processamento de dados em tempo real, organização em camadas e construção de interfaces desktop.

O contexto do Albion Online é particularmente interessante por possuir uma economia totalmente orientada pelos jogadores, sem interferência direta dos desenvolvedores nos preços. Isso cria um ambiente com dinâmicas reais de oferta, demanda e arbitragem, tornando o problema técnico mais complexo e relevante.

---

## Status do projeto

O projeto está em desenvolvimento ativo.

Próximas evoluções planejadas:

- Expansão do catálogo de artefatos
- Estabilização e melhorias no Flip de Mercado
- Novas funcionalidades de análise e comparação de operações
