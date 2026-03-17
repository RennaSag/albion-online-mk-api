```markdown
# Albion Market

Projeto pessoal em desenvolvimento.

Albion Market é uma aplicação desktop desenvolvida em Java com JavaFX, inspirada em Albion Online — um MMO com economia complexa totalmente baseada na atividade dos jogadores, funcionando de forma semelhante a um mercado financeiro real.

---

## Sobre o projeto

A proposta central é desenvolver um software capaz de consumir grandes volumes de dados atualizados em tempo real através de APIs externas, processá-los e organizá-los de forma estruturada para identificar oportunidades de arbitragem e operações economicamente vantajosas dentro do ecossistema do jogo.

Até o momento, não existe nenhuma ferramenta com esse nível de abordagem tanto na comunidade nacional quanto internacional. O que existe atualmente são apenas planilhas e recursos isolados, com diversas limitações em termos de atualização, automação e capacidade de análise.

---

## O que a aplicação faz

### Pesquisa de Preços
- Consulta preços de itens em tempo real a partir da API pública mantida pela comunidade do jogo
- Compara preços entre as 8 cidades do jogo (Caerleon, Bridgewatch, Fort Sterling, Lymhurst, Martlock, Thetford, Black Market e Brecilien)
- Filtra por nome, categoria, subcategoria, tier (T1–T8) e nível de encantamento (.1 a .4)
- Exibe preço mínimo de venda, máximo de compra e data da última atualização por cidade
- Identifica automaticamente a cidade com o melhor preço de venda

### Calculadora de Craft
- Busca a receita de fabricação de qualquer item diretamente da API oficial do jogo
- Exibe os materiais necessários com quantidade, preço de compra e cidade de origem
- Lida corretamente com artefatos únicos por item, cujo comportamento é diferente dos recursos refinados — a quantidade necessária de artefato é igual à quantidade final craftada, não à quantidade base, pois artefatos não retornam no processo de fabricação
- Calcula automaticamente a quantidade final craftada considerando a taxa de retorno configurada
- Calcula custo total dos materiais, custo da barraca de craft (baseado em nutrição e game value do item), receita total e lucro ou prejuízo estimado da operação
- Permite inserir preços manualmente para simular cenários alternativos
- Todos os campos de parâmetros (quantidade, taxa de retorno, taxa da barraca) atualizam os cálculos em tempo real conforme são digitados
- Exibe a cidade com o melhor preço de venda e a cidade mais vantajosa para compra dos materiais
- Permite salvar os dados da operação em um arquivo JSON com timestamp para registro e acompanhamento

### Interface e Organização
- Interface dark com identificação visual das cidades por cores únicas
- Seleção de cidades feita na tela de seleção de item, com os filtros sendo mantidos ao navegar entre telas
- Barra de rolagem na área central para monitores menores
- Persistência dos filtros entre sessões — ao reabrir o programa, o último item e configurações são restaurados automaticamente via Java Preferences API
- Ícones dos itens e materiais carregados diretamente do servidor de render da Albion

---

## Arquitetura

O projeto é organizado em camadas bem definidas:

- **UI** — telas construídas em JavaFX (`TelaHome`, `TelaCraftSelecao`, `TelaCraft`, `TelaPesquisaPrecos`, `TelaLogin`)
- **Service** — lógica de negócio e integração com APIs (`ApiService`, `CraftService`, `BancoDeDadosCraft`, `ItemValues`, `BuscaService`)
- **Model** — modelos de dados (`ItemDefinition`, `PriceEntry`, `ReceitaCraft`, `CidadeInfo`, `EstadoCraftSelecao`, entre outros)
- **Util** — utilitários de formatação (`FormatadorUtil`)

O estado de navegação entre telas é transmitido via `EstadoCraftSelecao`, que carrega item selecionado, tier, encantamento e cidades filtradas, permitindo restaurar os filtros corretamente ao voltar da calculadora.

---

## APIs utilizadas

- **Albion Online Data Project** (`west.albion-online-data.com`) — preços do mercado em tempo real
- **Albion Online GameInfo** (`gameinfo.albiononline.com`) — receitas de craft e dados dos itens
- **Albion Online Render** (`render.albiononline.com`) — ícones e imagens dos itens

---

## Tecnologias utilizadas

- Java 17
- JavaFX 21
- Maven para gerenciamento de dependências
- HttpClient para requisições HTTP assíncronas
- Gson para parsing de JSON
- Java Preferences API para persistência de filtros entre sessões
- Execução de requisições em threads separadas para não bloquear a interface

---

## Motivação

Além do interesse pelo tema, este projeto serve como espaço para aplicar na prática conhecimentos de desenvolvimento de software — desde integração com APIs externas e processamento de dados em tempo real, até organização de uma aplicação completa em camadas e construção de uma interface funcional e utilizável.

O contexto do Albion Online é especialmente interessante porque a economia do jogo é inteiramente movida pelos jogadores, sem interferência dos desenvolvedores nos preços, o que cria um ambiente com dinâmicas reais de oferta, demanda e arbitragem. Isso torna o problema técnico genuinamente complexo e o resultado diretamente aplicável.

---

O projeto ainda está em desenvolvimento ativo. As próximas evoluções planejadas incluem expansão do catálogo de artefatos, refinamento dos cálculos de craft e adição de novas funcionalidades de análise e comparação de operações.
```