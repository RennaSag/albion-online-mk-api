# Albion Market

Projeto pessoal em desenvolvimento.

Albion Market é uma aplicação desktop desenvolvida em Java com JavaFX, inspirada em Albion Online — um MMORGP com economia complexa baseada inteiramente na atividade dos jogadores, funcionando de forma semelhante a um mercado financeiro real.

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
- Exibe materiais necessários com:
  - Quantidade
  - Preço de compra
  - Cidade de origem
- Trata corretamente artefatos, cuja quantidade necessária é igual à quantidade final craftada (não retornam no processo)
- Calcula automaticamente:
  - Quantidade final considerando taxa de retorno
  - Custo total dos materiais
  - Custo da barraca de craft (baseado em nutrição e game value)
  - Receita total
  - Lucro ou prejuízo estimado
- Permite inserção manual de preços para simulações
- Atualiza todos os cálculos em tempo real conforme os parâmetros são alterados
- Identifica:
  - Melhor cidade para venda
  - Melhor cidade para compra dos materiais
- Permite salvar operações em arquivo JSON com timestamp

---

### Interface e organização

- Interface em modo escuro
- Identificação visual das cidades por cores únicas
- Seleção de cidades na tela de busca de item
- Persistência de filtros ao navegar entre telas
- Barra de rolagem adaptada para monitores menores
- Persistência entre sessões via Java Preferences API
- Carregamento de ícones diretamente do servidor de render do Albion

---

## Arquitetura

O projeto segue uma arquitetura em camadas:

- **UI**  
  Telas construídas com JavaFX  
  (`TelaHome`, `TelaCraftSelecao`, `TelaCraft`, `TelaPesquisaPrecos`, `TelaLogin`)

- **Service**  
  Lógica de negócio e integração com APIs  
  (`ApiService`, `CraftService`, `BancoDeDadosCraft`, `ItemValues`, `BuscaService`)

- **Model**  
  Estruturas de dados  
  (`ItemDefinition`, `PriceEntry`, `ReceitaCraft`, `CidadeInfo`, `EstadoCraftSelecao`, entre outros)

- **Util**  
  Utilitários de apoio  
  (`FormatadorUtil`)

O estado de navegação entre telas é controlado por `EstadoCraftSelecao`, responsável por manter item, tier, encantamento e cidades selecionadas.

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
  (ícones e imagens)

---

## Tecnologias utilizadas

- Java 17
- JavaFX 21
- Maven
- HttpClient (requisições HTTP assíncronas)
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
- Refinamento dos cálculos de craft
- Novas funcionalidades de análise e comparação de operações