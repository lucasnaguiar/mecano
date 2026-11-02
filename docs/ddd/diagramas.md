# Diagramas

Diagramas da solução em Mermaid (renderizados pelo GitHub). Os termos seguem a [linguagem ubíqua](linguagem-ubiqua.md).

1. [Mapa de contextos](#1-mapa-de-contextos)
2. [Agregados e referências por ID](#2-agregados-e-referências-por-id)
3. [Ciclo de vida da ordem de serviço](#3-ciclo-de-vida-da-ordem-de-serviço)
4. [Aprovação do orçamento com reserva de estoque](#4-aprovação-do-orçamento-com-reserva-de-estoque)
5. [Modelo de entidades e relacionamentos](#5-modelo-de-entidades-e-relacionamentos)

---

## 1. Mapa de contextos

Dependências entre os módulos, conforme a verificação do Spring Modulith (`ModularityTests`). As setas apontam para
o contexto usado. Não há ciclos: o build falha se surgir algum.

```mermaid
flowchart LR
    subgraph principal[Subdomínio principal]
        workorder["Ordem de Serviço<br/><code>workorder</code>"]
    end
    subgraph suporte[Subdomínios de suporte]
        customer["Cliente<br/><code>customer</code>"]
        catalog["Catálogo<br/><code>catalog</code>"]
        inventory["Estoque<br/><code>inventory</code>"]
    end
    subgraph generico[Subdomínio genérico]
        auth["Autenticação<br/><code>auth</code>"]
    end
    shared["Compartilhado<br/><code>shared</code>"]
    demo["Demonstração<br/><code>demo</code> (perfil demo)"]

    workorder -->|consulta cliente e veículo| customer
    workorder -->|copia preço do serviço| catalog
    workorder -->|reserva, baixa e libera peças| inventory
    workorder --> shared
    customer --> shared
    catalog --> shared
    inventory --> shared
    auth --> shared
    demo -.->|usa os casos de uso| workorder
    demo -.-> customer
    demo -.-> catalog
    demo -.-> inventory
    demo -.-> auth
```

- `workorder` guarda apenas IDs de cliente, veículo, serviço e peça, e copia descrição e preço no momento da inclusão.
- O estoque não conhece a OS (guarda o ID dela como `UUID`): a política de estoque fica em `workorder` e aciona o
  `WorkOrderStockUseCase`.
- Nenhum contexto de negócio depende de `auth`: quem executou cada ação é guardado como `UserId`, de `shared`.
- O diagrama C4 gerado pelo Modulith fica em `target/spring-modulith-docs/components.puml` após `./mvnw test`.

## 2. Agregados e referências por ID

Cada agregado é uma fronteira de consistência. Entre agregados a referência é sempre pelo ID tipado; só as entidades
internas da `WorkOrder` (itens, orçamentos e transições) são gravadas junto com ela.

```mermaid
classDiagram
    direction LR

    class WorkOrder {
        <<Agregado>>
        WorkOrderId id
        Long number
        TrackingCode trackingCode
        WorkOrderStatus status
        String problemDescription
        String diagnosisNotes
        String cancellationReason
        Money totalAmount
    }
    class LaborItem {
        <<Entidade interna>>
        String description
        Money unitPrice
        int quantity
    }
    class PartItem {
        <<Entidade interna>>
        String description
        UnitOfMeasure unitOfMeasure
        Money unitPrice
        Quantity quantity
    }
    class Estimate {
        <<Entidade interna>>
        EstimateStatus status
        Money totalAmount
    }
    class StatusTransition {
        <<Entidade interna>>
        WorkOrderStatus fromStatus
        WorkOrderStatus toStatus
        UserId changedBy
    }
    class Customer {
        <<Agregado>>
        CustomerId id
        TaxId taxId
        String name
    }
    class Vehicle {
        <<Agregado>>
        VehicleId id
        LicensePlate licensePlate
        ModelYear modelYear
    }
    class LaborOperation {
        <<Agregado>>
        LaborOperationId id
        String name
        Money price
    }
    class Part {
        <<Agregado>>
        PartId id
        String code
        PartType type
        UnitOfMeasure unitOfMeasure
        BigDecimal availableQuantity
        BigDecimal reservedQuantity
    }
    class StockMovement {
        <<Entidade>>
        StockMovementType type
        Quantity quantity
    }
    class User {
        <<Agregado>>
        UserId id
        String email
        Role role
        boolean active
    }

    WorkOrder *-- "0..*" LaborItem
    WorkOrder *-- "0..*" PartItem
    WorkOrder *-- "0..*" Estimate
    WorkOrder *-- "1..*" StatusTransition
    WorkOrder ..> Customer : customerId
    WorkOrder ..> Vehicle : vehicleId
    LaborItem ..> LaborOperation : laborOperationId
    PartItem ..> Part : partId
    Vehicle ..> Customer : customerId
    StockMovement ..> Part : partId
    StockMovement ..> WorkOrder : workOrderId (UUID)
    StatusTransition ..> User : changedBy
```

Linha cheia com losango: composição dentro do agregado. Linha tracejada: referência por ID, sem carregar o objeto.

## 3. Ciclo de vida da ordem de serviço

Cada transição gera um `StatusTransition` e um evento de domínio. O status nunca é alterado diretamente.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> RECEIVED : OpenWorkOrder
    RECEIVED --> IN_DIAGNOSIS : StartDiagnosis
    IN_DIAGNOSIS --> AWAITING_APPROVAL : SendEstimate
    AWAITING_APPROVAL --> IN_DIAGNOSIS : RejectEstimate (cliente)
    AWAITING_APPROVAL --> AWAITING_APPROVAL : ApproveEstimate (cliente)<br/>reserva as peças
    AWAITING_APPROVAL --> IN_PROGRESS : StartExecution<br/>orçamento aprovado, baixa as peças
    IN_PROGRESS --> COMPLETED : CompleteWorkOrder
    COMPLETED --> DELIVERED : DeliverVehicle
    RECEIVED --> CANCELED : CancelWorkOrder
    IN_DIAGNOSIS --> CANCELED : CancelWorkOrder
    AWAITING_APPROVAL --> CANCELED : CancelWorkOrder<br/>libera a reserva, se houver
    DELIVERED --> [*]
    CANCELED --> [*]
```

| Status | Rótulo | Na fila de OS em andamento |
|---|---|---|
| `IN_PROGRESS` | Em execução | 1º |
| `AWAITING_APPROVAL` | Aguardando aprovação | 2º |
| `IN_DIAGNOSIS` | Em diagnóstico | 3º |
| `RECEIVED` | Recebida | 4º |
| `COMPLETED` | Finalizada | fora da fila |
| `DELIVERED` | Entregue | fora da fila |
| `CANCELED` | Cancelada | fora da fila |

- Serviços e peças só entram ou saem em `IN_DIAGNOSIS`.
- O cancelamento é feito só pela equipe, com motivo obrigatório; a partir de `IN_PROGRESS` não é mais permitido.
- `DELIVERED` e `CANCELED` são finais. Um veículo só recebe nova OS quando a anterior está em um deles.

## 4. Aprovação do orçamento com reserva de estoque

A política de estoque reage ao evento de forma síncrona, na mesma transação. Se faltar estoque, a transação inteira é
desfeita: o orçamento continua pendente e o cliente recebe o motivo.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant API as WorkOrderCustomerController
    participant UC as ApproveEstimateUseCase
    participant OS as WorkOrder
    participant Store as WorkOrderStore
    participant Pol as WorkOrderStockPolicy
    participant Est as WorkOrderStockUseCase
    participant Peca as Part
    participant BD as PostgreSQL

    Cliente->>API: POST /api/customer/work-orders/{trackingCode}/approval
    API->>UC: execute(trackingCode)
    Note over UC,BD: uma única transação
    UC->>Store: load(trackingCode)
    Store->>BD: SELECT work_order
    UC->>OS: approveEstimate(now)
    OS-->>OS: orçamento PENDING para APPROVED<br/>registra EstimateApproved(peças)
    UC->>Store: save(workOrder)
    Store->>BD: UPDATE work_order, estimate
    Store->>Pol: publica EstimateApproved
    Pol->>Est: reserve(workOrderId, peças)
    loop cada peça do orçamento
        Est->>Peca: reserve(quantidade)
        alt disponível suficiente
            Peca-->>Est: StockMovement RESERVATION
            Est->>BD: UPDATE part, INSERT stock_movement
        else estoque insuficiente
            Peca-->>Est: BusinessRuleException
            Est-->>UC: exceção propagada
            UC-->>API: rollback da transação inteira
            API-->>Cliente: 422 "Estoque insuficiente de ..."
        end
    end
    UC-->>API: OS com orçamento aprovado
    API-->>Cliente: 200 (status continua AWAITING_APPROVAL)
```

O mesmo padrão vale para `ExecutionStarted` (baixa das peças reservadas) e `WorkOrderCanceled` (liberação da
reserva quando o orçamento já estava aprovado).

## 5. Modelo de entidades e relacionamentos

Tabelas criadas pelo Flyway (`src/main/resources/db/migration`). Status e tipos são `VARCHAR` com `CHECK`; valores
monetários são `NUMERIC(12,2)` e quantidades `NUMERIC(12,3)`.

```mermaid
erDiagram
    customer ||--o{ vehicle : possui
    customer ||--o{ work_order : solicita
    vehicle ||--o{ work_order : recebe
    work_order ||--o{ labor_item : contem
    work_order ||--o{ part_item : contem
    work_order ||--o{ estimate : orcamentos
    work_order ||--|{ status_transition : historico
    labor_operation ||--o{ labor_item : origem
    part ||--o{ part_item : origem
    part ||--o{ stock_movement : extrato
    work_order |o--o{ stock_movement : movimenta
    app_user |o--o{ status_transition : altera
    app_user |o--o{ stock_movement : registra

    customer {
        uuid id PK
        varchar name
        varchar tax_id UK "CPF ou CNPJ sem pontuação"
        varchar email
        varchar phone
    }
    vehicle {
        uuid id PK
        uuid customer_id FK
        varchar license_plate UK "antiga ou Mercosul"
        varchar brand
        varchar model
        smallint model_year
    }
    labor_operation {
        uuid id PK
        varchar name UK
        text description
        numeric price
        integer estimated_duration_minutes
        boolean active
    }
    part {
        uuid id PK
        varchar code UK
        varchar name
        varchar type "PART ou SUPPLY"
        varchar unit_of_measure
        numeric unit_price
        numeric available_quantity
        numeric reserved_quantity
        numeric minimum_stock
        boolean active
        bigint version "lock otimista"
    }
    work_order {
        uuid id PK
        bigint number UK "sequence a partir de 1000"
        varchar tracking_code UK
        uuid customer_id FK
        uuid vehicle_id FK
        varchar status
        text problem_description
        text diagnosis_notes
        text cancellation_reason
        numeric total_amount
        bigint version "lock otimista"
    }
    labor_item {
        uuid id PK
        uuid work_order_id FK
        uuid labor_operation_id FK
        varchar description
        numeric unit_price
        integer quantity
    }
    part_item {
        uuid id PK
        uuid work_order_id FK
        uuid part_id FK
        varchar description
        varchar unit_of_measure
        numeric unit_price
        numeric quantity
    }
    estimate {
        uuid id PK
        uuid work_order_id FK
        varchar status "um só PENDING por OS"
        numeric total_amount
        timestamptz sent_at
        timestamptz responded_at
    }
    status_transition {
        uuid id PK
        uuid work_order_id FK
        varchar from_status
        varchar to_status
        uuid changed_by FK "nulo: cliente ou sistema"
        timestamptz occurred_at
    }
    stock_movement {
        uuid id PK
        uuid part_id FK
        uuid work_order_id FK "obrigatório exceto em ENTRY"
        uuid user_id FK
        varchar type "ENTRY, RESERVATION, RELEASE, WITHDRAWAL"
        numeric quantity
        timestamptz occurred_at
    }
    app_user {
        uuid id PK
        varchar name
        varchar email UK
        varchar password_hash
        varchar role "ADMIN, ATTENDANT, MECHANIC, STOCK_MANAGER"
        boolean active
    }
```

Todas as tabelas têm também `created_at` e `updated_at` (exceto as filhas da OS e o extrato, que têm data própria).
A tabela `event_publication` (V2) é do Spring Modulith e não faz parte do modelo de negócio.
