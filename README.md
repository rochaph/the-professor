# the-professor

Laboratório de arquitetura distribuída: um coach de CS2 com IA (o time registra a partida e recebe uma análise
comparada com o próprio playbook) construído para praticar, de ponta a ponta,
**microsserviços, Arquitetura Hexagonal, mensageria, Session, OAuth 2.0/OIDC, Kubernetes, serverless e AWS**.

O domínio é propositalmente enxuto. O foco está em como os serviços se comunicam, falham, se recuperam,
autenticam usuários e são implantados.

## Domínio

- O **time** cadastra seus jogadores, mantém o **playbook** por mapa e registra as partidas (mapa, placar, economia, notas).
- O time **pede a análise** de uma partida, que usa uma **vaga semanal** e um **crédito**.
- O **coach** (IA) compara a partida com o playbook do mapa e devolve pontos fortes, erros recorrentes e o que treinar.
- O time **autoriza um coach externo** a ler o playbook de mapas escolhidos, com escopo limitado e revogável.
- O sistema **avisa no Discord** quando a análise fica pronta ou falha.

A IA roda com um adapter falso por padrão; o real só é ligado com chave no `.env` e limite de gasto.
Os créditos são simulados: não há pagamento de verdade.

Fora do escopo: assistir a partidas ao vivo, ranking de jogadores e cobrança real. A leitura automática de demos é um
extra opcional no Bloco 5.

## Arquitetura

```
                 Browser
                    │
             [Ingress NGINX]
                    │
        ┌───────────▼───────────┐       ┌───────────┐
        │   BFF / Gateway       │◄─────►│ Keycloak  │  OAuth 2.0 / OIDC
        │   sessão em Redis     │       └───────────┘
        └──────┬─────────┬──────┘
          REST │         │ REST (access token)
        ┌──────▼──┐  ┌───▼──────────┐
        │  teams  │  │   reviews    │──► Postgres + Outbox
        └─────────┘  └───┬──────────┘
                         │ eventos (Kafka)
              ┌──────────┼───────────┐
        ┌─────▼─────┐ ┌──▼────────┐  │   Saga: vaga → crédito → análise (CoachPort)
        │   quota   │ │ payments  │  │
        └───────────┘ └───────────┘  │
                                     ▼  review.ready, review.failed
                           SQS (LocalStack → AWS)
                                     │
                          ┌──────────▼──────────┐
                          │  notifications (Go) │  Lambda OU pod k8s
                          └─────────────────────┘
```

| Serviço | Responsabilidade |
|---|---|
| `bff` | API Gateway (roteamento, rate limit, circuit breaker, token relay), sessão (cookie + Redis) e login OAuth 2.0/OIDC; único ponto de entrada do browser |
| `teams` | Times, jogadores, partidas e playbook por mapa; consentimento de acesso para coaches externos |
| `reviews` | Pedido e ciclo de vida da análise; chama a IA pelo `CoachPort` e orquestra a Saga |
| `quota` | Vagas semanais de análise; reserva e liberação |
| `payments` | Carteira de créditos simulada: débito e estorno |
| `notifications` | Avisos no Discord (Go) |

## Stack

| Camada | Tecnologia |
|---|---|
| Serviços principais | Java 25 (LTS), Spring Boot 4 |
| Serviço serverless | Go (AWS Lambda) |
| API Gateway | Spring Cloud Gateway |
| Identidade | Keycloak (local), Amazon Cognito (AWS) |
| Sessão | Spring Session + Redis |
| Mensageria | Kafka, Amazon SQS |
| Persistência | PostgreSQL, DynamoDB |
| Orquestração | Kubernetes (kind), Helm, Argo CD |
| Observabilidade | OpenTelemetry, Prometheus, Grafana, Jaeger |
| Infraestrutura como código | Terraform (LocalStack e AWS) |
| CI | GitHub Actions |

## Onde cada conceito aparece

| Conceito | Onde |
|---|---|
| Microsserviços | 6 serviços, banco por serviço, deploy independente |
| Arquitetura Hexagonal | todos os serviços; `reviews` é a referência (IA falsa e real atrás do mesmo port); `notifications` troca adaptadores (Lambda ↔ HTTP) |
| Mensageria | Outbox, consumidor idempotente, retry + DLQ, Saga da análise com compensação |
| API Gateway | `bff` com Spring Cloud Gateway: roteamento, rate limit, circuit breaker, token relay |
| Session | BFF com cookie de sessão e store no Redis |
| OAuth 2.0 / OIDC | Authorization Code + PKCE, Client Credentials, Token Exchange, validação de JWT, consentimento do time por escopo |
| Kubernetes | kind, Helm, Ingress, HPA, réplicas |
| Serverless / AWS | Lambda de avisos (via SQS) e de resumo semanal (agendada) + DynamoDB, via Terraform |

## Estrutura

```
services/        microsserviços (um diretório por serviço)
infra/           docker-compose, Kubernetes (Helm/Argo CD) e Terraform
docs/adr/        decisões de arquitetura (Architecture Decision Records)
docs/learning/   anotações de estudo de cada conceito
```

## Roteiro

- [ ] **1. Base**: Spring Boot em camadas, refatorado para Arquitetura Hexagonal e multi-módulo; Testcontainers, docker-compose
- [ ] **2. Mensageria e consistência**: Kafka, Outbox, idempotência, retry/DLQ, Saga
- [ ] **3. Session, OAuth 2.0/OIDC e API Gateway**: Keycloak, BFF com Spring Cloud Gateway, Redis, resource servers, Client Credentials, Token Exchange, logout
- [ ] **4. Kubernetes**: kind, Helm, Argo CD, OpenTelemetry, HPA
- [ ] **5. Serverless e AWS**: `notifications` em Go na Lambda, SQS, Terraform (LocalStack → AWS)

## Como rodar

_Será preenchido à medida que os serviços forem implementados._
