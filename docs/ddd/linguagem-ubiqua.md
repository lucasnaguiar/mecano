# Linguagem Ubíqua

Sistema Integrado de Atendimento e Execução de Serviços da oficina mecânica.

Este documento define o vocabulário usado por todo o time: nas conversas, no Event Storming, na documentação e no código. Cada termo tem **um único significado dentro do seu contexto delimitado** e **um único nome no código**.

## Convenções

| Onde | Idioma | Exemplo |
|---|---|---|
| Conversas, documentação, Event Storming | Português | Ordem de Serviço, Orçamento Aprovado |
| Pacotes, classes, métodos, variáveis, tabelas | Inglês | `WorkOrder`, `EstimateApproved`, `work_order` |
| Mensagens ao usuário e descrições do Swagger | Português | "Peças só podem ser incluídas durante o diagnóstico" |

Regras de nomenclatura no código:

- **Comandos** são verbos no imperativo: `approveEstimate`, `ApproveEstimateUseCase`.
- **Eventos de domínio** ficam no passado: `EstimateApproved`.
- **Casos de uso** usam o sufixo `UseCase`. A palavra `Service` não é usada no nome de conceitos de negócio nem de classes da camada de aplicação (a anotação `@Service` do Spring é permitida; o nome, não).
- **Status** ficam em maiúsculas: `AWAITING_APPROVAL`.
- **Tabelas e colunas** usam snake_case em inglês: `work_order`, `created_at`.

A tradução de cada termo do negócio para o código está definida neste documento e não deve ser alterada sem atualizá-lo.

---

## Mapa de contextos

| Contexto | Pacote | Tipo de subdomínio | Responsabilidade |
|---|---|---|---|
| Ordem de Serviço | `workorder` | Principal | Ciclo de vida da OS: diagnóstico, orçamento, aprovação, execução e entrega |
| Estoque | `inventory` | Suporte | Peças e insumos, reservas, baixas e entradas |
| Catálogo | `catalog` | Suporte | Serviços oferecidos pela oficina e seus preços |
| Cliente | `customer` | Suporte | Clientes e seus veículos |
| Autenticação | `auth` | Genérico | Usuários do sistema, login e JWT |
| Compartilhado | `shared` | — | Objetos de valor comuns a vários contextos |

Relações entre contextos:

- `workorder` **consulta** `customer`, `catalog` e `inventory` para montar a OS, guardando apenas IDs e cópias de preço.
- `inventory` **reage** aos eventos `EstimateApproved`, `ExecutionStarted` e `EstimateRejected`, publicados por `workorder`.
- Nenhum contexto de negócio depende de `auth`. Quando precisa saber quem executou uma ação, guarda apenas o ID do usuário (`UserId`, que fica em `shared` justamente para isso).

---

## Contexto: Ordem de Serviço (`workorder`)

| Termo | Código | Tipo | Definição |
|---|---|---|---|
| Ordem de Serviço (OS) | `WorkOrder` | Agregado (raiz) | Registro de um atendimento a um veículo, desde a entrada na oficina até a entrega ao cliente. Concentra serviços, peças, orçamento e histórico de status. |
| Número da OS | `WorkOrderNumber` | Objeto de valor | Número sequencial e legível usado pela equipe para se referir à OS (ex.: OS 1532). |
| Código de acompanhamento | `TrackingCode` | Objeto de valor | Código aleatório e difícil de adivinhar que o cliente usa para consultar e aprovar a OS sem acesso administrativo. |
| Descrição do problema | `problemDescription` | Atributo | Relato do cliente sobre o defeito, registrado na abertura da OS. |
| Diagnóstico | `diagnosis` | Etapa | Análise feita pelo mecânico para identificar os serviços e as peças necessários. |
| Item de serviço | `LaborItem` | Entidade (interna) | Serviço do catálogo incluído na OS, com preço e descrição copiados no momento da inclusão. |
| Item de peça | `PartItem` | Entidade (interna) | Peça ou insumo incluído na OS, com quantidade, preço e descrição copiados no momento da inclusão. |
| Orçamento | `Estimate` | Entidade (interna) | Proposta de valor enviada ao cliente, calculada pela soma dos itens da OS. Pode ser aprovado ou rejeitado. |
| Valor total | `totalAmount` | Objeto de valor (`Money`) | Soma dos subtotais de todos os itens da OS. É recalculado sempre que um item é incluído ou removido. |
| Transição de status | `StatusTransition` | Entidade (interna) | Registro de cada mudança de status da OS, com status anterior, novo status, data, hora e usuário. Base para o tempo médio de execução. |
| Tempo médio de execução | `AverageExecutionTime` | Modelo de leitura | Média do tempo entre `IN_PROGRESS` e `COMPLETED` das OS finalizadas. |

### Status da Ordem de Serviço

| Status | Código | Significado | Entra neste status quando |
|---|---|---|---|
| Recebida | `RECEIVED` | O veículo chegou e a OS foi aberta | A OS é aberta (`OpenWorkOrder`) |
| Em diagnóstico | `IN_DIAGNOSIS` | O mecânico está avaliando o veículo e montando o orçamento | O diagnóstico é iniciado (`StartDiagnosis`) |
| Aguardando aprovação | `AWAITING_APPROVAL` | O orçamento foi enviado e aguarda a resposta do cliente | O orçamento é enviado (`SendEstimate`) |
| Em execução | `IN_PROGRESS` | Os serviços aprovados estão sendo realizados | A execução é iniciada (`StartExecution`) |
| Finalizada | `COMPLETED` | Os serviços foram concluídos e o veículo está pronto | A OS é finalizada (`CompleteWorkOrder`) |
| Entregue | `DELIVERED` | O veículo foi devolvido ao cliente | O veículo é entregue (`DeliverVehicle`) |

Regras de transição:

```
RECEIVED → IN_DIAGNOSIS → AWAITING_APPROVAL → IN_PROGRESS → COMPLETED → DELIVERED
```

- O status nunca é alterado diretamente. Ele muda apenas como consequência de um comando do negócio.
- Serviços e peças só podem ser incluídos em `IN_DIAGNOSIS`.
- Uma OS em `DELIVERED` não pode voltar a nenhum status anterior.
- O destino da OS após a rejeição do orçamento está em aberto (ver [Decisões em aberto](#decisões-em-aberto)).

### Situação do orçamento

| Situação | Código | Significado |
|---|---|---|
| Pendente | `PENDING` | Enviado ao cliente, sem resposta |
| Aprovado | `APPROVED` | O cliente autorizou a execução |
| Rejeitado | `REJECTED` | O cliente recusou o orçamento |

---

## Contexto: Estoque (`inventory`)

| Termo | Código | Tipo | Definição |
|---|---|---|---|
| Peça | `Part` (tipo `PART`) | Agregado (raiz) | Item vendido por unidade e instalado no veículo. Ex.: filtro de óleo, pastilha de freio. |
| Insumo | `Part` (tipo `SUPPLY`) | Agregado (raiz) | Item consumido em quantidade, que pode ser fracionada. Ex.: óleo de motor (litros), graxa (gramas). |
| Tipo de item | `PartType` | Enum | Distingue peça (`PART`) de insumo (`SUPPLY`). |
| Unidade de medida | `unitOfMeasure` | Atributo | Unidade em que o item é controlado: unidade, litro, quilo etc. |
| Quantidade disponível | `availableQuantity` | Atributo | Quantidade em estoque que ainda pode ser reservada. |
| Quantidade reservada | `reservedQuantity` | Atributo | Quantidade separada para OS com orçamento aprovado, ainda não baixada. |
| Estoque mínimo | `minimumStock` | Atributo | Quantidade abaixo da qual a oficina deve repor o item. |
| Reserva | `reserve` | Operação | Separação de peças para uma OS aprovada. Diminui a disponível e aumenta a reservada. |
| Baixa | `decrease` | Operação | Saída definitiva das peças reservadas, quando a execução começa. |
| Liberação de reserva | `releaseReservation` | Operação | Devolução das peças reservadas à quantidade disponível. |
| Entrada | `registerEntry` | Operação | Recebimento de novas unidades de um item no estoque. |
| Movimentação de estoque | `StockMovement` | Entidade | Registro de cada alteração no estoque, como um extrato. |
| Tipo de movimentação | `StockMovementType` | Enum | `ENTRY`, `RESERVATION`, `RELEASE` ou `WITHDRAWAL`. |

---

## Contexto: Catálogo (`catalog`)

| Termo | Código | Tipo | Definição |
|---|---|---|---|
| Serviço (do catálogo) | `LaborOperation` | Agregado (raiz) | Trabalho que a oficina oferece, com nome, descrição, preço e tempo estimado. Ex.: troca de óleo, alinhamento. |
| Preço do serviço | `price` | Objeto de valor (`Money`) | Valor atual do serviço. Alterá-lo não afeta OS já orçadas, porque o preço foi copiado para o `LaborItem`. |
| Tempo estimado | `estimatedDuration` | Atributo | Duração esperada para executar o serviço. |
| Ativo | `active` | Atributo | Indica se o serviço ainda pode ser incluído em novas OS. |

---

## Contexto: Cliente (`customer`)

| Termo | Código | Tipo | Definição |
|---|---|---|---|
| Cliente | `Customer` | Agregado (raiz) | Pessoa física ou jurídica que traz o veículo para atendimento. Identificado por CPF ou CNPJ. |
| Veículo | `Vehicle` | Agregado (raiz) | Carro atendido pela oficina. Pertence a um cliente e é referenciado pelo ID dele. |
| Documento do cliente | `TaxId` | Objeto de valor (compartilhado) | Identifica o cliente. Único no sistema. |
| Placa do veículo | `LicensePlate` | Objeto de valor (compartilhado) | Identifica o veículo. Única no sistema. |
| Ano do modelo | `ModelYear` | Objeto de valor (compartilhado) | Ano do veículo. |

> As regras de formato e validação de `TaxId`, `LicensePlate` e `ModelYear` estão em [Objetos de valor compartilhados](#objetos-de-valor-compartilhados-shared).

---

## Contexto: Autenticação (`auth`)

| Termo | Código | Tipo | Definição |
|---|---|---|---|
| Usuário | `User` | Agregado (raiz) | Pessoa da equipe da oficina que acessa as APIs administrativas. **Não confundir com Cliente.** |
| E-mail | `email` | Atributo | Identifica o usuário no login. Único no sistema e armazenado em minúsculas. |
| Perfil | `Role` | Enum | Conjunto de permissões do usuário: `ADMIN`, `ATTENDANT`, `MECHANIC` ou `STOCK_MANAGER` (ver [Atores](#atores)). |
| Login | `LoginUseCase` | Caso de uso | Confere e-mail e senha e emite um token de acesso. |
| Token de acesso | `AccessToken` (JWT) | Objeto de valor | Credencial emitida no login e exigida nas APIs administrativas. Carrega o ID e o perfil do usuário e tem validade definida. |
| Emissor de token | `TokenProvider` | Porta (aplicação) | Contrato para emitir o token de acesso. A implementação com JWT fica na infraestrutura. |
| Administrador inicial | — | Conceito | Primeiro usuário `ADMIN`, criado na subida da aplicação a partir de `ADMIN_EMAIL` e `ADMIN_PASSWORD`. |

Regras:

- Não existe cadastro público de usuário. Apenas `ADMIN` cadastra usuários da equipe.
- Credenciais inválidas sempre geram a mesma mensagem ("E-mail ou senha inválidos."), sem indicar qual dos dois está errado.

---

## Objetos de valor compartilhados (`shared`)

| Termo | Código | Definição |
|---|---|---|
| Dinheiro | `Money` | Valor monetário em reais (BRL). Nunca usa ponto flutuante. Permite somar e multiplicar por quantidade. |
| Quantidade | `Quantity` | Quantidade positiva, inteira ou fracionada, conforme a unidade de medida. |
| Documento | `TaxId` | CPF ou CNPJ com dígitos verificadores válidos. |
| Placa | `LicensePlate` | Placa nos formatos antigo ou Mercosul. |
| Ano do modelo | `ModelYear` | Ano do veículo dentro de um intervalo válido. |
| Identificador de usuário | `UserId` | ID do usuário da equipe, usado por outros contextos para registrar quem executou uma ação sem depender de `auth`. |

---

## Atores

| Ator | Código (perfil) | Responsabilidades |
|---|---|---|
| Atendente | `ATTENDANT` | Identifica o cliente, cadastra veículos, abre a OS, envia o orçamento e entrega o veículo |
| Mecânico | `MECHANIC` | Faz o diagnóstico, inclui serviços e peças, executa e finaliza a OS |
| Gestor de estoque | `STOCK_MANAGER` | Cadastra peças e insumos, registra entradas e altera preços |
| Administração | `ADMIN` | Gerencia usuários e catálogo e acompanha indicadores, como o tempo médio de execução |
| Cliente | — | Acompanha a OS e aprova ou rejeita o orçamento pelo código de acompanhamento |

> Os perfis acima são uma proposta. Se atendente e gestor de estoque forem a mesma pessoa na oficina, os perfis podem ser unificados.

---

## Comandos e eventos

| Comando | Código | Ator | Agregado | Evento gerado |
|---|---|---|---|---|
| Fazer login | `Login` | Equipe | `User` | — |
| Cadastrar usuário | `RegisterUser` | Administração | `User` | — |
| Cadastrar cliente | `RegisterCustomer` | Atendente | `Customer` | Cliente Cadastrado (`CustomerRegistered`) |
| Cadastrar veículo | `RegisterVehicle` | Atendente | `Vehicle` | Veículo Cadastrado (`VehicleRegistered`) |
| Abrir ordem de serviço | `OpenWorkOrder` | Atendente | `WorkOrder` | Ordem de Serviço Aberta (`WorkOrderOpened`) |
| Iniciar diagnóstico | `StartDiagnosis` | Mecânico | `WorkOrder` | Diagnóstico Iniciado (`DiagnosisStarted`) |
| Incluir serviço | `AddLabor` | Mecânico | `WorkOrder` | Serviço Incluído (`LaborAdded`) |
| Incluir peça ou insumo | `AddPart` | Mecânico | `WorkOrder` | Peça Incluída (`PartAdded`) |
| Enviar orçamento | `SendEstimate` | Atendente | `WorkOrder` | Orçamento Enviado (`EstimateSent`) |
| Aprovar orçamento | `ApproveEstimate` | Cliente | `WorkOrder` | Orçamento Aprovado (`EstimateApproved`) |
| Rejeitar orçamento | `RejectEstimate` | Cliente | `WorkOrder` | Orçamento Rejeitado (`EstimateRejected`) |
| Iniciar execução | `StartExecution` | Mecânico | `WorkOrder` | Execução Iniciada (`ExecutionStarted`) |
| Finalizar ordem de serviço | `CompleteWorkOrder` | Mecânico | `WorkOrder` | OS Finalizada (`WorkOrderCompleted`) |
| Entregar veículo | `DeliverVehicle` | Atendente | `WorkOrder` | Veículo Entregue (`VehicleDelivered`) |
| Cadastrar peça ou insumo | `RegisterPart` | Gestor de estoque | `Part` | Peça Cadastrada (`PartRegistered`) |
| Registrar entrada | `RegisterStockEntry` | Gestor de estoque | `Part` | Entrada Registrada (`StockEntryRegistered`) |
| Alterar preço | `ChangePartPrice` | Gestor de estoque | `Part` | Preço Alterado (`PartPriceChanged`) |
| Reservar peças | `ReserveParts` | Política | `Part` | Peças Reservadas (`PartsReserved`) |
| Baixar estoque | `DecreaseStock` | Política | `Part` | Estoque Baixado (`StockDecreased`) |
| Liberar reserva | `ReleaseReservation` | Política | `Part` | Reserva Liberada (`ReservationReleased`) |
| Verificar estoque mínimo | `CheckMinimumStock` | Política | `Part` | Estoque Mínimo Atingido (`MinimumStockReached`) |

**Eventos pivotais**, que marcam as grandes fases do atendimento: Ordem de Serviço Aberta, Orçamento Enviado, Orçamento Aprovado, OS Finalizada e Veículo Entregue.

## Políticas

| Política | Quando | Então |
|---|---|---|
| Notificar orçamento | `EstimateSent` | Enviar o orçamento ao cliente por e-mail |
| Reservar peças | `EstimateApproved` | Reservar no estoque as peças do orçamento |
| Liberar reserva | `EstimateRejected` | Devolver ao estoque as peças reservadas, se houver |
| Baixar peças | `ExecutionStarted` | Dar baixa nas peças reservadas |
| Alertar estoque mínimo | `StockDecreased` com quantidade abaixo do mínimo | Alertar o gestor de estoque |
| Avisar veículo pronto | `WorkOrderCompleted` | Avisar o cliente que o veículo pode ser retirado |
| Registrar transição | Qualquer mudança de status da OS | Gravar um `StatusTransition` com data, hora e usuário |

---

## Termos que exigem cuidado

| Evite | Use | Motivo |
|---|---|---|
| "Serviço" sozinho | **Serviço do catálogo** (`LaborOperation`) ou **item de serviço** (`LaborItem`) | "Serviço" também nomeia a OS e a camada de aplicação. No código, `Service` não é usado para conceitos do negócio. |
| `Budget` | **Orçamento** (`Estimate`) | Em inglês, *budget* significa verba planejada, não proposta ao cliente. |
| `setStatus`, "mudar status" | O comando do negócio (`approveEstimate`, `complete`...) | O status é consequência de uma ação, nunca alterado diretamente. |
| "Cliente" para quem faz login | **Usuário** (`User`) | Cliente é quem traz o veículo. Usuário é a equipe da oficina. |
| "Peça" para tudo do estoque | **Peça** ou **insumo**, conforme o tipo | Insumos têm quantidade fracionada e unidade de medida própria. |
| "Cancelar orçamento" | **Rejeitar orçamento** | Rejeição é uma decisão do cliente. Cancelamento de OS ainda não foi definido. |
| "Fechar OS" | **Finalizar** (`COMPLETED`) ou **entregar** (`DELIVERED`) | São dois momentos diferentes do atendimento. |

---

## Relação com o código

Caminhos relativos a `src/main/java/br/dev/lucasaguiar/mecano/`.

| Termo | Onde está | Situação |
|---|---|---|
| Ordem de Serviço | `workorder/domain/WorkOrder.java` · tabela `work_order` | Esqueleto |
| Item de serviço / Item de peça | `workorder/domain/LaborItem.java`, `PartItem.java` · tabelas `labor_item`, `part_item` | Esqueleto |
| Orçamento | `workorder/domain/Estimate.java` · tabela `estimate` | Esqueleto |
| Transição de status | `workorder/domain/StatusTransition.java` · tabela `status_transition` | Esqueleto |
| Status da OS / Situação do orçamento | `workorder/domain/WorkOrderStatus.java`, `EstimateStatus.java` | Implementado |
| Número da OS / Código de acompanhamento | `WorkOrderNumber`, `TrackingCode` | Planejado |
| Tempo médio de execução | `workorder/application/GetAverageExecutionTimeUseCase.java` | Esqueleto |
| Eventos da OS | `workorder/domain/event/` | Esqueleto |
| Casos de uso da OS | `workorder/application/*UseCase.java` | Esqueleto |
| Peça / Insumo | `inventory/domain/Part.java`, `PartType.java` · tabela `part` | Esqueleto |
| Movimentação de estoque | `inventory/domain/StockMovement.java`, `StockMovementType.java` · tabela `stock_movement` | Esqueleto |
| Eventos do estoque | `inventory/domain/event/` | Esqueleto |
| Serviço do catálogo | `catalog/domain/LaborOperation.java` · tabela `labor_operation` | Esqueleto |
| Cliente | `customer/domain/customer/Customer.java` · tabela `customer` | Esqueleto |
| Veículo | `customer/domain/vehicle/Vehicle.java` · tabela `vehicle` | Esqueleto |
| Eventos de cliente e veículo | `customer/domain/customer/event/`, `customer/domain/vehicle/event/` | Esqueleto |
| Usuário | `auth/domain/User.java` · tabela `app_user` | Implementado |
| Login e cadastro de usuário | `auth/application/LoginUseCase.java`, `RegisterUserUseCase.java` | Implementado |
| Token de acesso | `auth/application/AccessToken.java`, `auth/infrastructure/security/JwtTokenProvider.java` | Implementado |
| Objetos de valor | `shared/domain/` | Esqueleto (sem validação) |

Legenda: **Implementado**, com regras e testes; **Esqueleto**, a classe existe, mas sem regras; **Planejado**, ainda não existe no código.

> Mantenha esta tabela atualizada a cada novo conceito. Ela é a evidência de que a documentação corresponde à implementação.

---

## Decisões em aberto

Pontos levantados no Event Storming que precisam de decisão do time. Ao decidir, registre a escolha aqui e ajuste os termos acima.

- [ ] Para onde vai a OS após a rejeição do orçamento: cancelada, volta ao diagnóstico ou é entregue sem serviço?
- [ ] O orçamento é recalculado a cada item ou gerado apenas no envio?
- [ ] Quem envia o orçamento: o atendente ou o sistema, ao fim do diagnóstico?
- [ ] A execução começa automaticamente na aprovação ou depende do mecânico?
- [ ] Existe cancelamento de OS? Se sim, qual o termo e o status?
- [ ] Falta de estoque na aprovação: bloqueia a aprovação ou permite e sinaliza?
- [ ] Reserva e baixa ocorrem na mesma transação da OS ou por eventos entre contextos?
- [ ] Peça e insumo continuam no mesmo agregado (`Part` com tipo) ou viram agregados separados?
- [ ] O cliente se identifica pelo código de acompanhamento ou pelo número da OS com o documento?
