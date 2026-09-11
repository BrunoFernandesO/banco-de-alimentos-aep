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

## Arquitetura

Aplicação em camadas, orientada a console:

- **Model (domínio)** — hierarquia polimórfica `Alimento` → `AlimentoPerecivel` / `AlimentoNaoPerecivel`; regras de negócio.
- **DAO / Repository** — acesso a dados via JDBC (encapsula todo o SQL).
- **Service** — orquestra regras (ex.: baixa de estoque ao distribuir).
- **View (console)** — menu interativo via terminal.

Diagramas em [`/docs`](docs): `diagrama_classes.png` (UML) e `diagrama_der.png` (DER). Documento completo da 1ª entrega: [`docs/AEP_B1_Banco_de_Alimentos.pdf`](docs/AEP_B1_Banco_de_Alimentos.pdf).

## Tecnologias

- **Java (JDK 17 LTS)** — tipagem forte e pilares de POO.
- **MySQL** — banco relacional (integridade referencial e agregações); alinhado à disciplina de Banco de Dados.
- **JDBC** via **MySQL Connector/J** (`com.mysql.cj.jdbc.Driver`) — acesso a dados sem ORM.

## Estrutura do Repositório

```
/src        Código-fonte Java (2º bimestre)
/docs       Documento PDF da 1ª entrega e diagramas (UML, DER)
/database   Scripts SQL de criação do banco (schema.sql)
```

## Como Executar (2º Bimestre)

> Instruções preliminares — serão finalizadas na entrega do código.

1. Instalar **JDK 17+** e **MySQL 8+**.
2. Criar o banco e as tabelas executando o script (ele já cria o schema `banco_alimentos`):
   ```bash
   mysql -u root -p < database/schema.sql
   ```
3. Baixar o **MySQL Connector/J** (`.jar`) e adicioná-lo ao classpath.
4. Configurar as credenciais de conexão (host, porta 3306, usuário, senha).
5. Compilar e executar a aplicação a partir de `/src`.

## Licença

Projeto acadêmico — UniCesumar 2026.2.
