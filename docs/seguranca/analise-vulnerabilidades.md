# Análise de vulnerabilidades

Resultado da análise de segurança do MVP: dependências da aplicação, imagem Docker, configuração do Dockerfile e
segredos no repositório.

| Item | Valor |
|---|---|
| Ferramenta | [Trivy](https://trivy.dev) 0.69.3 (imagem `aquasec/trivy:0.69.3`) |
| Base de vulnerabilidades | Trivy DB v2, atualizada em 02/11/2026 |
| Data da análise | 02/11/2026 |
| Alvos | código do repositório (`pom.xml`, `Dockerfile`, todos os arquivos versionados) e a imagem gerada pelo `Dockerfile` |
| Varreduras | vulnerabilidades conhecidas (CVE), configuração insegura (misconfig) e segredos expostos |

Relatórios completos da análise final:

- [trivy-imagem.txt](trivy-imagem.txt): imagem Docker (sistema operacional e dependências Java do `app.jar`);
- [trivy-repositorio.txt](trivy-repositorio.txt): repositório (dependências Maven, Dockerfile e segredos).

## Resumo

| Alvo | Antes | Depois | Situação |
|---|---|---|---|
| Dependências Java (`app.jar`) | 4 críticas, 10 altas, 4 médias | **nenhuma** | Corrigido |
| Dependências Maven (`pom.xml`) | as mesmas 18 | **nenhuma** | Corrigido |
| Configuração do Dockerfile | 1 baixa (sem `HEALTHCHECK`) | **nenhuma** | Corrigido |
| Segredos no repositório | nenhum | nenhum | Sem achados |
| Sistema operacional da imagem (Ubuntu 26.04 de `eclipse-temurin:25-jre`) | 52 médias, 4 baixas | 52 médias, 4 baixas | Aceito: nenhuma tem correção publicada (ver abaixo) |

Resultado final: **nenhuma vulnerabilidade crítica ou alta**, nenhum segredo exposto e nenhuma configuração insegura
no Dockerfile.

## Vulnerabilidades encontradas na primeira análise

Todas vinham de dependências gerenciadas pelo Spring Boot 4.1.1: o Tomcat embarcado e o Jackson (a versão 3, usada
pela aplicação, e a 2, trazida pelo springdoc/swagger).

| Severidade | CVE | Pacote e versão | Corrigida em |
|---|---|---|---|
| CRITICAL | CVE-2026-65182 | `org.apache.tomcat.embed:tomcat-embed-core` 11.0.24 | 11.0.25, 10.1.58, 9.0.121 |
| CRITICAL | CVE-2026-65637 | `org.apache.tomcat.embed:tomcat-embed-core` 11.0.24 | 11.0.25, 10.1.59, 9.0.121 |
| CRITICAL | CVE-2026-65905 | `org.apache.tomcat.embed:tomcat-embed-core` 11.0.24 | 11.0.25, 10.1.58, 9.0.121 |
| CRITICAL | CVE-2026-68525 | `org.apache.tomcat.embed:tomcat-embed-core` 11.0.24 | 11.0.25, 10.1.58, 9.0.121 |
| HIGH | CVE-2026-89407 | `com.fasterxml.jackson.core:jackson-core` 2.21.5 | 2.18.11, 2.21.7, 2.22.3 |
| HIGH | CVE-2026-89425 | `com.fasterxml.jackson.core:jackson-core` 2.21.5 | 2.21.7, 2.22.3, 2.18.11 |
| HIGH | CVE-2026-68497 | `com.fasterxml.jackson.core:jackson-databind` 2.21.5 | 2.18.10, 2.21.6, 2.22.2 |
| HIGH | CVE-2026-91776 | `com.fasterxml.jackson.core:jackson-databind` 2.21.5 | 2.18.11, 2.21.7, 2.22.3 |
| HIGH | CVE-2026-91777 | `com.fasterxml.jackson.core:jackson-databind` 2.21.5 | 2.21.7, 2.18.11, 2.22.3 |
| HIGH | CVE-2026-89407 | `tools.jackson.core:jackson-core` 3.1.5 | 3.1.7, 3.2.2 |
| HIGH | CVE-2026-89425 | `tools.jackson.core:jackson-core` 3.1.5 | 3.1.7, 3.2.3 |
| HIGH | CVE-2026-68497 | `tools.jackson.core:jackson-databind` 3.1.5 | 3.2.2, 3.1.6 |
| HIGH | CVE-2026-91776 | `tools.jackson.core:jackson-databind` 3.1.5 | 3.1.7, 3.2.3 |
| HIGH | CVE-2026-91777 | `tools.jackson.core:jackson-databind` 3.1.5 | 3.1.7, 3.2.3 |
| MEDIUM | CVE-2026-19032 | `com.fasterxml.jackson.core:jackson-databind` 2.21.5 | 2.18.10, 2.21.6, 2.22.2 |
| MEDIUM | CVE-2026-83557 | `com.fasterxml.jackson.core:jackson-databind` 2.21.5 | 2.18.10, 2.21.6, 2.22.2 |
| MEDIUM | CVE-2026-19032 | `tools.jackson.core:jackson-databind` 3.1.5 | 3.1.6, 3.2.2 |
| MEDIUM | CVE-2026-83557 | `tools.jackson.core:jackson-databind` 3.1.5 | 3.1.6, 3.2.2 |

As críticas do Tomcat incluem desvio de restrições de segurança e de autenticação; as altas do Jackson permitem
negação de serviço com JSON malicioso. As duas bibliotecas recebem requisições de qualquer cliente, então foram
tratadas como prioridade.

## Correções aplicadas

1. **Spring Boot 4.1.1 para 4.1.2** (versão de correção, `pom.xml`). O BOM da 4.1.2 traz as versões corrigidas sem
   nenhuma sobrescrita manual:

   | Biblioteca | Antes | Depois |
   |---|---|---|
   | `tomcat-embed-core` | 11.0.24 | 11.0.27 |
   | `tools.jackson.core` (Jackson 3) | 3.1.5 | 3.1.7 |
   | `com.fasterxml.jackson.core` (Jackson 2) | 2.21.5 | 2.21.7 |

   Todos os testes (`./mvnw verify`, 149 testes e verificação de cobertura) passaram após a atualização.

2. **`HEALTHCHECK` no Dockerfile**, consultando `/actuator/health/readiness` com o `curl` já instalado na imagem.
   A imagem passa a informar a própria saúde também fora do docker-compose.

## Pendências aceitas: pacotes do sistema operacional

As 56 vulnerabilidades restantes (52 médias e 4 baixas) estão em pacotes do Ubuntu 26.04 da imagem base
`eclipse-temurin:25-jre`, principalmente `rust-coreutils`, `libpcre2-8-0`, `libgnutls30t64` e `libc6`. Na data da
análise, **nenhuma tem correção publicada** (status `affected` no Trivy), então não há versão para atualizar.

Mitigações:

- nenhuma é crítica ou alta;
- a aplicação roda com usuário sem privilégios (`app`), e o contêiner só expõe a porta 8080;
- os pacotes afetados são utilitários do sistema, não usados pela aplicação para processar requisições;
- a análise é repetida antes de cada versão entregue (comandos abaixo); se alguma delas passar a ser crítica ou alta
  com correção disponível, basta reconstruir a imagem com a base atualizada.

## Critério de aprovação

Uma versão só é entregue sem vulnerabilidade crítica ou alta que já tenha correção publicada, e sem segredo exposto
ou configuração insegura de severidade crítica ou alta. Achados médios e baixos ficam registrados no relatório. O
critério é verificado com `--severity HIGH,CRITICAL --ignore-unfixed --exit-code 1` (ver "Como reproduzir"): o
comando termina com erro quando o critério não é atendido.

O critério foi conferido: a imagem da versão anterior (Spring Boot 4.1.1) é reprovada, com 4 críticas e 10 altas; a
versão corrigida é aprovada.

## Como reproduzir

Com o Docker rodando, na raiz do projeto:

```bash
# imagem da aplicação
docker build -t mecano:scan .
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:0.69.3 \
  image --scanners vuln mecano:scan

# repositório: dependências Maven, Dockerfile e segredos
docker run --rm -v "$PWD":/src:ro aquasec/trivy:0.69.3 \
  fs --scanners vuln,misconfig,secret /src
```

Para verificar o critério de aprovação, acrescente `--severity HIGH,CRITICAL --ignore-unfixed --exit-code 1` aos
dois comandos.
