# Sistema de Gestão de Banco de Alimentos

Aplicação de console (Java + MySQL) para controle de estoque de um banco de alimentos, com **priorização de distribuição por proximidade de vencimento** e **registro rastreável das entregas**, reduzindo o desperdício de alimentos.

**Atividade de Estudo Programada (AEP) — 2026.2 · Engenharia de Software · UniCesumar (Maringá-PR)**

Alinhado aos Objetivos de Desenvolvimento Sustentável da ONU:
- **ODS 2 — Fome Zero** (meta 2.1)
- **ODS 12 — Consumo e Produção Responsáveis** (meta 12.3)

## Equipe

| Integrante | RA |
|---|---|
| Bruno André Fernandes Ortega | 25291321-2 |
| Giovanna Cristina Martins | 25001909-2 |

## Requisitos Funcionais (Escopo)

| ID | Requisito |
|----|-----------|
| RF01 | O sistema deve permitir o cadastro de doadores, contendo nome, tipo (pessoa física, mercado ou indústria) e contato. |
| RF02 | O sistema deve permitir registrar a entrada de um alimento no estoque — perecível ou não perecível — vinculado a um doador, contendo nome, quantidade, data de recebimento e, quando perecível, data de validade. |
| RF03 | O sistema deve listar os alimentos em estoque ordenados por prioridade de distribuição, calculada de forma distinta conforme o tipo de alimento. |
| RF04 | O sistema deve permitir o cadastro de beneficiários, contendo nome e contato. |
| RF05 | O sistema deve permitir registrar a distribuição de uma quantidade de um alimento a um beneficiário, dando baixa automática no estoque. |
| RF06 | O sistema deve alertar, ao iniciar, quais alimentos perecíveis estão a poucos dias do vencimento. |
| RF07 | O sistema deve gerar um relatório de desperdício, listando os alimentos vencidos que não foram distribuídos. |

## Cronograma / Backlog (2º Bimestre)

| Sprint / Data | Épico | User Story | Responsável |
|---|---|---|---|
| Sprint 1 · 15/09–28/09 | Modelagem e Base | Estruturar o DER, o script SQL de criação das tabelas e a conexão JDBC. | Bruno |
| Sprint 2 · 29/09–12/10 | Cadastros Base | Cadastrar doadores e registrar a entrada de alimentos (perecíveis ou não). | Giovanna |
| Sprint 3 · 13/10–26/10 | Núcleo Polimórfico | Listagem do estoque ordenada por prioridade + CRUD completo de Alimento. | Bruno |
| Sprint 4 · 27/10–09/11 | Beneficiários e Distribuição | Cadastro de beneficiários e registro de distribuições com baixa de estoque. | Giovanna |
| Sprint 5 · 10/11–14/11 | Alertas, Relatórios e Entrega | Alertas de vencimento, relatório de desperdício, menu integrado e README. | Ambos |

> A data final do 2º bimestre é provisória (meados de novembro) e será ajustada ao calendário oficial. Alterações de escopo serão registradas aqui, preservando a coerência da solução.

### Andamento

| Sprint | Situação | Entregue |
|---|---|---|
| Sprint 1 · Modelagem e Base | Concluída | DER; `schema.sql` reexecutável; `dados_exemplo.sql`; conexão JDBC (Singleton) configurada por `db.properties`; verificação das tabelas na inicialização; build Maven. |
| Sprint 2 · Cadastros Base | Pendente | — |
| Sprint 3 · Núcleo Polimórfico | Pendente | — |
| Sprint 4 · Beneficiários e Distribuição | Pendente | — |
| Sprint 5 · Alertas, Relatórios e Entrega | Pendente | — |

## Arquitetura

Aplicação em camadas, orientada a console. Pacote raiz: `br.unicesumar.bancoalimentos`.

| Camada | Pacote | Responsabilidade |
|---|---|---|
| **Model (domínio)** | `model` | Hierarquia polimórfica `Alimento` → `AlimentoPerecivel` / `AlimentoNaoPerecivel`; regras de negócio. |
| **DAO / Repository** | `dao` | Acesso a dados via JDBC (encapsula todo o SQL). A conexão fica em `dao.conexao`. |
| **Service** | `service` | Orquestra regras (ex.: baixa de estoque ao distribuir). |
| **View (console)** | `view` | Menu interativo via terminal. |

A conexão com o MySQL é única e compartilhada (padrão **Singleton**, `ConexaoBanco`), adequada a uma aplicação de console com um operador por vez. As credenciais ficam fora do código, em `db.properties`, que não é versionado.

Diagramas em [`/docs`](docs): `diagrama_classes.png` (UML) e `diagrama_der.png` (DER). Documento completo da 1ª entrega: [`docs/AEP_B1_Banco_de_Alimentos.pdf`](docs/AEP_B1_Banco_de_Alimentos.pdf).

## Decisões de Implementação e Aderência ao Planejamento

Pontos em que a implementação detalha ou ajusta o planejado na 1ª entrega, cada um com a sua justificativa:

| Tema | Decisão | Justificativa |
|---|---|---|
| `Doador.cadastrar()`, `Beneficiario.cadastrar()` e `Distribuicao.registrar()` (diagrama de classes) | Implementados na camada de serviço: `DoadorService.cadastrar`, `BeneficiarioService.cadastrar` e `DistribuicaoService.registrar`, que usam os DAOs. | A arquitetura documentada na 1ª entrega (seção 3) isola todo o SQL no DAO e deixa o domínio sem JDBC. O diagrama mostra as operações no nível conceitual; colocá-las nas entidades (Active Record) misturaria SQL ao modelo. Além disso, `registrar` altera duas tabelas em uma transação, o que pertence ao serviço. |
| Prioridade do não perecível | Cresce devagar com o tempo em estoque (`10 + 0,1 × dias`, teto 40), em vez de ser fixa. | Mantém o planejado ("mais baixa" que a de qualquer perecível, que fica entre 50 e 100) e acrescenta o critério FIFO para desempatar não perecíveis. |
| Fórmula do perecível | `50 + 50 / (diasParaVencer + 1)`: 100 no dia do vencimento, 75 na véspera. | Cresce conforme a validade se aproxima (RF03) e nunca chega à faixa dos não perecíveis. Documentada em `AlimentoPerecivel.calcularPrioridade()`. |
| Classes auxiliares fora do diagrama | `TipoAlimento` (discriminador da coluna `tipo`), `RegraNegocioException`, `Formatos`, `ItemDesperdicio` e `RelatorioDesperdicio` (RF07) e a hierarquia de menus `MenuConsole` (view). | Detalhes de implementação; entidades, atributos e relacionamentos do diagrama não mudaram. |
| RF07 — o que é "não distribuído" | O relatório lista todo alimento vencido que ainda tinha saldo e indica à parte os que nunca tiveram distribuição. | O saldo que venceu é desperdício mesmo quando parte do item foi entregue; mostrar os dois casos mede a perda real (meta 12.3). |
| RF06 — "poucos dias" | Perecíveis com saldo que vencem de hoje até 3 dias à frente (constante `RelatorioService.DIAS_ALERTA_VENCIMENTO`). | Valor único e fácil de ajustar. |
| Alteração de alimento | Nome, saldo e datas podem ser alterados; tipo e doador não. | Tipo e doador definem a natureza e a origem da doação; um cadastro errado sem distribuições pode ser excluído e refeito. |
| Exclusão de alimento | Recusada se o alimento possui distribuições. | Preserva a rastreabilidade das entregas; as chaves estrangeiras não usam `ON DELETE CASCADE` (ver `schema.sql`). |
| Distribuição | Registrada com a data do dia, escolhendo o alimento na lista por prioridade (RF03); não pode ser alterada depois. | É um registro histórico; vencidos e quantidades acima do saldo são recusados. |

## Tecnologias

- **Java (JDK 17 LTS)** — tipagem forte e pilares de POO.
- **MySQL** — banco relacional (integridade referencial e agregações); alinhado à disciplina de Banco de Dados.
- **JDBC** via **MySQL Connector/J** (`com.mysql.cj.jdbc.Driver`) — acesso a dados sem ORM.
- **Maven** (com Maven Wrapper) — compilação e download automático do Connector/J; dispensa instalar o Maven.

## Estrutura do Repositório

```
/src/main/java   Código-fonte Java (pacotes model, dao, service, view)
/docs            Documento PDF da 1ª entrega e diagramas (UML, DER)
/database        schema.sql (criação do banco) e dados_exemplo.sql (carga de teste)
pom.xml          Configuração do build Maven (Java 17 + MySQL Connector/J)
mvnw, mvnw.cmd   Maven Wrapper (Linux/macOS e Windows)
db.properties.example   Modelo da configuração de conexão
```

## Como Executar

**Pré-requisitos:** **JDK 17 ou superior** e **MySQL 8** em execução (porta 3306). Não é preciso instalar o Maven nem baixar o driver: o Maven Wrapper faz isso na primeira execução (requer internet; pode levar alguns minutos).

Todos os comandos abaixo são executados na **raiz do repositório**.

**1. Criar o banco e as tabelas.** O script cria o schema `banco_alimentos` e pode ser executado mais de uma vez sem apagar dados:

```bash
mysql -u root -p -e "source database/schema.sql"
```

Opcional — carregar dados de exemplo (doadores, alimentos vencidos, perto do vencimento e não perecíveis, beneficiários e distribuições). **Apaga os registros existentes**:

```bash
mysql -u root -p -e "source database/dados_exemplo.sql"
```

> Os comandos funcionam no Prompt de Comando, no PowerShell e no terminal do Linux/macOS. Se `mysql` não for reconhecido no Windows, abra os arquivos no **MySQL Workbench** (*File → Open SQL Script*) e execute-os, ou adicione `C:\Program Files\MySQL\MySQL Server 8.x\bin` ao `PATH`.

**2. Configurar a conexão.** Copie o modelo e informe usuário e senha do seu MySQL no arquivo `db.properties`:

```bash
copy db.properties.example db.properties     # Windows
cp db.properties.example db.properties       # Linux/macOS
```

**3. Compilar e executar:**

```bash
.\mvnw.cmd -q compile exec:java              # Windows (Prompt de Comando ou PowerShell)
sh ./mvnw -q compile exec:java               # Linux/macOS
```

Também é possível abrir a pasta como **projeto Maven** em qualquer IDE (IntelliJ IDEA, Eclipse, NetBeans, VS Code) e executar a classe `br.unicesumar.bancoalimentos.Main`.

**Saída esperada** (estado atual, Sprint 1):

```
==================================================
  SISTEMA DE GESTAO DE BANCO DE ALIMENTOS
  AEP 2026.2 - Engenharia de Software - UniCesumar
==================================================
Conectado: MySQL 8.x.x (banco banco_alimentos)
Tabelas verificadas: doadores, alimentos, beneficiarios, distribuicoes.
Base pronta. O menu principal sera disponibilizado nas proximas sprints.
```

Se algo estiver errado (MySQL parado, senha incorreta, banco não criado, `db.properties` ausente), o sistema exibe uma mensagem `ERRO:` indicando exatamente o que corrigir.

## Licença

Projeto acadêmico — UniCesumar 2026.2.
