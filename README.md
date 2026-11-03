# Mecano

Back-end do sistema integrado de atendimento e execução de serviços de uma oficina mecânica. MVP desenvolvido para
o Tech Challenge da pós-graduação SOAT (fase 1).

> O enunciado do Tech Challenge não define um nome para o sistema. O nome **Mecano** foi dado ao projeto porque ter
> um nome próprio ajuda a idealizar a solução e a se referir a ela durante o desenvolvimento e na documentação.

## Objetivo

Organizar o atendimento da oficina de ponta a ponta: da chegada do veículo à entrega ao cliente, passando por
diagnóstico, orçamento, aprovação, execução e controle de estoque de peças e insumos. A equipe trabalha por uma API
administrativa protegida por JWT, e o cliente acompanha a ordem de serviço e responde ao orçamento por um código de
acompanhamento, sem precisar de login.

## Descrição da solução

Funcionalidades principais:

- **Clientes e veículos**: cadastro com CPF ou CNPJ validados (inclusive CNPJ alfanumérico) e placas nos formatos
  antigo e Mercosul.
- **Catálogo de serviços** (`LaborOperation`): serviços oferecidos, com preço e tempo estimado.
- **Estoque**: peças (unidade) e insumos (quantidade fracionada em L, ML, KG, G ou M), entradas, estoque mínimo,
  reservas, baixas e extrato de movimentações.
- **Ordem de serviço (OS)**: abertura, diagnóstico com inclusão de serviços e peças, envio do orçamento, aprovação
  ou recusa pelo cliente, execução, finalização, entrega e cancelamento pela equipe antes da execução. Cada mudança
  de status fica registrada no histórico.
- **Estoque integrado à OS**: a aprovação do orçamento reserva as peças, o início da execução dá baixa e o
  cancelamento libera a reserva. Sem estoque suficiente, a aprovação é recusada com o motivo.
- **Consultas**: fila de OS em andamento ordenada por prioridade (em execução, aguardando aprovação, em diagnóstico,
  recebida), histórico por cliente e por veículo, listagens paginadas e tempo médio de execução.
- **Acesso da equipe**: API administrativa protegida por JWT. O sistema tem perfis de administrador, atendente,
  mecânico e gestor de estoque (detalhes na [linguagem ubíqua](docs/ddd/linguagem-ubiqua.md)); para usar e testar,
  basta o administrador, que tem acesso a todas as operações.

Ciclo de vida da OS:

```
RECEIVED -> IN_DIAGNOSIS -> AWAITING_APPROVAL -> IN_PROGRESS -> COMPLETED -> DELIVERED
RECEIVED | IN_DIAGNOSIS | AWAITING_APPROVAL -> CANCELED
```

A recusa do orçamento devolve a OS ao diagnóstico, para reorçar. As regras de negócio e o vocabulário estão na
[linguagem ubíqua](docs/ddd/linguagem-ubiqua.md).

## Arquitetura

Monolito modular em camadas, organizado com DDD: **contexto delimitado primeiro, camada depois**.

| Contexto | Pacote | Tipo | Responsabilidade |
|---|---|---|---|
| Ordem de Serviço | `workorder` | Principal | Ciclo de vida da OS, orçamento, execução e entrega |
| Estoque | `inventory` | Suporte | Peças e insumos, reservas, baixas e entradas |
| Catálogo | `catalog` | Suporte | Serviços oferecidos e seus preços |
| Cliente | `customer` | Suporte | Clientes e veículos |
| Autenticação | `auth` | Genérico | Usuários da equipe, login e JWT |
| Compartilhado | `shared` | - | Objetos de valor, erros, paginação e infraestrutura comum |

Cada contexto tem as camadas `presentation` (controllers e DTOs), `application` (um `<Ação>UseCase` por caso de
uso), `domain` (agregados, objetos de valor, eventos e interfaces de repositório, em Java puro) e `infrastructure`
(JPA e segurança).

Decisões principais:

- Agregados se referenciam apenas por ID tipado (`CustomerId`, `VehicleId`, `PartId`...), nunca por objeto.
- O domínio não depende de framework: as entidades JPA ficam em `infrastructure` e são convertidas pelos
  adaptadores de repositório.
- Eventos de domínio publicados após salvar o agregado; as políticas (reserva e baixa de estoque, notificação ao
  cliente) reagem na mesma transação, de modo que uma falha desfaz o comando inteiro.
- O Spring Modulith verifica as dependências entre contextos: cada um publica só os pacotes que os outros podem usar,
  e o build falha se surgir dependência circular.
- Erros no formato ProblemDetail (RFC 9457) com mensagens em português, inclusive 401, 403 e 429.

Diagramas (mapa de contextos, agregados, estados da OS, sequência da aprovação e modelo de dados) em
[docs/ddd/diagramas.md](docs/ddd/diagramas.md).

## Tecnologias

- Java 25 e Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security com OAuth2 Resource Server, Actuator)
- PostgreSQL 17 e Flyway
- Spring Modulith 2.1
- springdoc-openapi 3 (Swagger UI)
- JUnit 5, Mockito, Testcontainers 2 e JaCoCo
- Docker e Docker Compose
- Trivy (análise de vulnerabilidades)

## Pré-requisitos

- **Com Docker** (recomendado): Docker com o plugin Compose.
- **Aplicação pelo Maven**: JDK 25 e um PostgreSQL acessível (o do `docker compose up -d postgres` serve). O Maven
  não precisa estar instalado: o projeto usa o Maven Wrapper.
- **Testes**: JDK 25 e Docker rodando (os testes de integração sobem um PostgreSQL com Testcontainers).

## Execução

O projeto roda localmente **sem nenhuma configuração**: o `docker-compose.yml` e o perfil `local` já trazem valores
padrão para banco, segredo do JWT e administrador. Esses valores servem só para a máquina local (ver
[Variáveis de ambiente](#variáveis-de-ambiente)).

### Opção 1: tudo com Docker Compose (recomendado)

Requer apenas o Docker.

```bash
docker compose up --build
```

- Sobe o PostgreSQL e a aplicação; na primeira execução, o Flyway cria as tabelas, o administrador é criado
  e o perfil `demo` cadastra dados de exemplo (catálogo, estoque, clientes, veículos e OS).
- A aplicação fica pronta em `http://localhost:8080` quando o serviço `app` aparece como `healthy` em
  `docker compose ps` (health check em `/actuator/health/readiness`).
- Swagger: `http://localhost:8080/swagger-ui.html`. Usuários em
  [Credenciais de demonstração](#credenciais-de-demonstração).
- Para parar: `docker compose down`. Para recomeçar com a base vazia: `docker compose down -v`.
- Se as portas 8080 ou 5432 já estiverem em uso, troque com `APP_PORT=8081 DB_PORT=5433 docker compose up --build`.

### Opção 2: aplicação pelo Maven, banco pelo Docker

Requer JDK 25 e Docker. Útil para desenvolver e depurar pela IDE.

```bash
docker compose up -d postgres                                        # só o banco, em localhost:5432
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,demo         # aplicação em localhost:8080
```

O perfil `local` usa os mesmos valores padrão do compose, então não é preciso exportar variáveis. No IntelliJ, basta
rodar a classe `MecanoApplication` com os perfis ativos `local,demo`.

Se o JDK padrão da máquina não for o 25, aponte o `JAVA_HOME` antes, por exemplo
`export JAVA_HOME=~/.sdkman/candidates/java/25.0.3-amzn`.

### Variáveis de ambiente

Todas são opcionais na execução local. Para alterar alguma, copie o `.env.example` para `.env` (lido
automaticamente pelo Docker Compose) ou exporte a variável antes do `./mvnw`.

| Variável | Padrão local | Uso |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local,demo` | Perfis ativos; sem `demo`, a base sobe vazia, só com o administrador |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | `localhost`, `5432`, `mecano` | Endereço do PostgreSQL (no compose, a aplicação usa o host `postgres`) |
| `DB_USERNAME`, `DB_PASSWORD` | `mecano`, `mecano` | Usuário e senha do PostgreSQL |
| `JWT_SECRET` | segredo de desenvolvimento | Assinatura dos tokens (mínimo de 32 caracteres) |
| `JWT_EXPIRATION` | `PT1H` | Validade do token |
| `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | `Administrador`, `admin@mecano.local`, `admin-mecano` | Administrador criado na primeira subida |
| `RATE_LIMIT_LOGIN`, `RATE_LIMIT_TRACKING` | `10`, `30` | Requisições por minuto e por IP no login e na consulta do cliente |
| `APP_PORT` | `8080` | Porta da aplicação publicada pelo compose |

Os padrões existem só no `docker-compose.yml` e no perfil `local`. Fora dele (em um servidor, por exemplo), banco,
`JWT_SECRET` e senha do administrador precisam ser informados, com valores próprios.

### Fluxo completo

O arquivo [docs/api/fluxo-completo.http](docs/api/fluxo-completo.http) percorre o atendimento inteiro com o
administrador (login, cadastros, abertura, diagnóstico, orçamento, aprovação pelo cliente, execução, entrega,
consultas e erros esperados) e pode ser executado pelo cliente HTTP do IntelliJ, com o ambiente `local`. Ele
pressupõe uma base nova (`docker compose down -v` antes), porque usa CPF e placa fixos. Se você trocou o `ADMIN_PASSWORD`, informe a nova senha em
`docs/api/http-client.private.env.json` (ignorado pelo git): `{"local": {"adminPassword": "..."}}`.

## Testes

```bash
./mvnw verify                            # testes unitários e de integração, relatório e checagem de cobertura
./mvnw test -Dtest=WorkOrderTest         # uma classe
./mvnw test -Dtest='WorkOrderTest#cancelAfterApprovalReleasesReservedParts'   # um método
```

- **Unitários**: regras dos agregados e objetos de valor, casos de uso com Mockito.
- **Persistência**: repositórios contra PostgreSQL real (Testcontainers + Flyway).
- **API**: fluxos completos via MockMvc com segurança, incluindo o atendimento de ponta a ponta, reservas de estoque,
  cancelamento, paginação, limites de requisições e a documentação OpenAPI.
- **Arquitetura**: `ModularityTests` verifica os módulos com o Spring Modulith.

Cobertura: o JaCoCo bloqueia o build se os domínios críticos (`workorder.domain`, `inventory.domain` e
`shared.domain`) ficarem abaixo de 80% de linhas. Na versão atual, os pacotes de domínio estão entre 99% e 100%, e
o projeto inteiro em 93%. Relatório em `target/site/jacoco/index.html`.

Análise de vulnerabilidades: feita com o Trivy nas dependências, na imagem Docker, no Dockerfile e em busca de
segredos. Resultado, correções e comandos para reproduzir em
[docs/seguranca/analise-vulnerabilidades.md](docs/seguranca/analise-vulnerabilidades.md).

## Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI, separado em dois grupos:
  - **Equipe** (`/v3/api-docs/equipe`): login e rotas `/api/admin/**`, protegidas por JWT;
  - **Cliente** (`/v3/api-docs/cliente`): rotas públicas `/api/customer/**`, pelo código de acompanhamento.

Para chamar as rotas da equipe, faça `POST /api/auth/login`, copie o `accessToken` e use o botão **Authorize**. Todas
as operações documentam as respostas de erro com exemplos.

## Banco de dados (decisão e justificativa)

Foi escolhido o **PostgreSQL** (relacional), pelos seguintes motivos:

- **Consistência transacional**: aprovar um orçamento e reservar as peças acontece em uma única transação ACID; se
  faltar estoque, nada é gravado. O lock otimista (`version`) evita reservas concorrentes sobre o mesmo item.
- **Dados naturalmente relacionais**: cliente, veículo, OS, itens, orçamentos, histórico e extrato de estoque têm
  relacionamentos e chaves estrangeiras bem definidos.
- **Regras garantidas também no banco**: `CHECK` para status e tipos, valores e quantidades não negativos, documentos e
  placas únicos, índice único parcial que permite um só orçamento pendente por OS e sequence para o número legível
  da OS.
- **Precisão numérica**: `NUMERIC` para dinheiro (2 casas) e quantidades fracionadas (3 casas), sem erros de ponto
  flutuante.
- **Maturidade e custo**: software livre, amplamente suportado, com o mesmo banco no Docker, nos testes
  (Testcontainers) e em produção.

O schema é versionado pelo Flyway (`src/main/resources/db/migration`) e o Hibernate apenas valida o mapeamento.

## Credenciais de demonstração

| Usuário | Senha |
|---|---|
| `admin@mecano.local` | `admin-mecano` |

É o administrador criado na primeira subida, com os valores padrão locais (`ADMIN_EMAIL` e `ADMIN_PASSWORD`); se
você alterou algum no `.env`, use o valor alterado. Com ele, faça `POST /api/auth/login` e use o `accessToken` nas
rotas `/api/admin/**`. O perfil `demo` também cria usuários dos demais perfis (`atendente`, `mecanico` e
`estoque@mecano.local`, senha `demo-mecano`), que não são necessários para testar o sistema.

O perfil `demo` cadastra catálogo, estoque, quatro clientes, seis veículos e OS recebida, em diagnóstico, aguardando
aprovação, em execução, entregue e cancelada. O código de acompanhamento de cada OS aparece no campo `trackingCode`
das rotas da equipe e é o que o cliente usa nas rotas `/api/customer/**`, sem login.

## Estrutura de diretórios

```
.
├── docs/
│   ├── api/                        fluxo completo para o cliente HTTP do IntelliJ
│   ├── ddd/                        linguagem ubíqua, diagramas e event storming
│   └── seguranca/                  análise de vulnerabilidades
├── src/main/java/br/dev/lucasaguiar/mecano/
│   ├── workorder/                  Ordem de Serviço (subdomínio principal)
│   │   ├── domain/                 WorkOrder, itens, orçamento, status e eventos
│   │   ├── application/            casos de uso e políticas
│   │   ├── infrastructure/         persistência JPA
│   │   └── presentation/           API da equipe (admin) e do cliente (customer)
│   ├── inventory/                  Estoque
│   ├── catalog/                    Catálogo de serviços
│   ├── customer/                   Clientes e veículos
│   ├── auth/                       Usuários, login e JWT
│   ├── shared/                     objetos de valor, erros, paginação, OpenAPI e limites de requisição
│   └── demo/                       dados de demonstração (perfil demo)
├── src/main/resources/
│   ├── application.yml             configuração e perfis (local, demo, test)
│   └── db/migration/               migrations do Flyway (V1 a V10)
├── src/test/java/...               testes unitários, de persistência, de API e de arquitetura
├── Dockerfile
├── docker-compose.yml
└── .env.example
```

## Documentação DDD

- [Linguagem ubíqua](docs/ddd/linguagem-ubiqua.md): termos, status, comandos, eventos, políticas e decisões.
- [Diagramas](docs/ddd/diagramas.md): mapa de contextos, agregados, estados da OS, sequência da aprovação e modelo de
  entidades e relacionamentos.
- [Event Storming](docs/ddd/event-storming.md): board do Event Storming.

## Vídeo

Demonstração da solução: link a ser incluído.
