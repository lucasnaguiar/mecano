# Mecano

Back-end do sistema integrado de atendimento e execução de serviços de uma oficina mecânica. MVP desenvolvido para
o Tech Challenge da pós-graduação SOAT (fase 1).

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
- **Equipe**: perfis `ADMIN`, `ATTENDANT`, `MECHANIC` e `STOCK_MANAGER`, com gestão de usuários pelo administrador.

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
- **Execução local**: JDK 25 e um PostgreSQL acessível. O Maven não precisa estar instalado (o projeto usa o Maven
  Wrapper).
- **Testes**: JDK 25 e Docker rodando (os testes de integração sobem um PostgreSQL com Testcontainers).

## Execução

### Com Docker Compose

```bash
cp .env.example .env
# edite o .env: DB_PASSWORD, JWT_SECRET (mínimo de 32 caracteres) e ADMIN_PASSWORD
docker compose up --build
```

A aplicação sobe em `http://localhost:8080` depois que o banco fica saudável e aplica as migrations na primeira
execução. O serviço `app` tem healthcheck em `/actuator/health/readiness`.

Para popular a base com dados de demonstração, defina `SPRING_PROFILES_ACTIVE=local,demo` no `.env` antes de subir.
Para recomeçar do zero, use `docker compose down -v`.

### Local (sem Docker para a aplicação)

```bash
export $(grep -v '^#' .env | xargs)    # carrega as variáveis do .env
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Variáveis de ambiente (todas descritas no `.env.example`):

| Variável | Uso |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | Conexão com o PostgreSQL |
| `JWT_SECRET`, `JWT_EXPIRATION` | Assinatura e validade do token (padrão `PT1H`) |
| `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador criado na primeira subida |
| `SPRING_PROFILES_ACTIVE` | `local`; acrescente `demo` para os dados de demonstração |
| `DEMO_PASSWORD` | Senha dos usuários de demonstração |
| `RATE_LIMIT_LOGIN`, `RATE_LIMIT_TRACKING` | Requisições por minuto e por IP no login e na consulta do cliente |
| `APP_PORT` | Porta publicada pelo Docker Compose |

### Fluxo completo

O arquivo [docs/api/fluxo-completo.http](docs/api/fluxo-completo.http) percorre o atendimento inteiro (logins,
cadastros, abertura, diagnóstico, orçamento, aprovação pelo cliente, execução, entrega, consultas e erros
esperados) e pode ser executado pelo cliente HTTP do IntelliJ. Ele pressupõe o perfil `demo` e uma base nova; a
senha do administrador vai em `docs/api/http-client.private.env.json` (ignorado pelo git):

```json
{"local": {"adminPassword": "a mesma senha de ADMIN_PASSWORD"}}
```

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

| Usuário | Perfil | Senha |
|---|---|---|
| `ADMIN_EMAIL` do `.env` (padrão `admin@mecano.local`) | `ADMIN` | `ADMIN_PASSWORD` do `.env` |
| `atendente@mecano.local` | `ATTENDANT` | `DEMO_PASSWORD` (padrão `demo-mecano`) |
| `mecanico@mecano.local` | `MECHANIC` | `DEMO_PASSWORD` |
| `estoque@mecano.local` | `STOCK_MANAGER` | `DEMO_PASSWORD` |

Os três últimos só existem com o perfil `demo`, que também cria catálogo, estoque, quatro clientes, seis veículos e
OS recebida, em diagnóstico, aguardando aprovação, em execução, entregue e cancelada. O código de acompanhamento de cada OS aparece no campo `trackingCode` das rotas da equipe.

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
