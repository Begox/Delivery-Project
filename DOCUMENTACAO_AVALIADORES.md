# 📋 Documentação Técnica — Delivery PGE
### Aplicação Full Stack de Delivery de Comida

> **Para Avaliadores** | Versão 1.0.0 | Maio/2026

---

## 1. Visão Geral da Solução

**Delivery PGE** é uma aplicação Full Stack de delivery inspirada no iFood, desenvolvida com **Java 17 + Spring Boot 3.2** no backend e **Angular 17 + PrimeNG 17** no frontend, integrados por uma API REST com autenticação JWT stateless.

O sistema permite que usuários se cadastrem, façam login, calculem estimativas de entrega com roteamento real via APIs externas (Nominatim + OSRM) e criem pedidos, que ficam registrados e listados de forma paginada.

---

## 2. Tecnologias e Justificativas

### Backend

| Tecnologia | Versão | Por que foi escolhida |
|---|---|---|
| **Java** | 17 LTS | Versão LTS estável, compatível com Spring Boot 3.x, suporta records e pattern matching |
| **Spring Boot** | 3.2.5 | Framework padrão de mercado; auto-configuração, starters e produtividade elevada |
| **Spring Security** | 6.x | Solução robusta e extensível para autenticação/autorização; integração nativa com JWT |
| **Spring Data JPA** | 6.x | Abstração de repositório que elimina boilerplate de SQL; queries derivadas de nome de método |
| **PostgreSQL** | 15+ | Banco relacional robusto, suporte completo a UUID, JSONB e transações ACID |
| **JJWT** | 0.12.5 | Biblioteca padrão para geração e validação de tokens JWT no ecossistema Java |
| **Lombok** | 1.18.32 | Reduz código repetitivo (getters, setters, builders, logging) sem sacrificar legibilidade |
| **WebFlux/WebClient** | 6.x | Client HTTP reativo e não-bloqueante para chamadas às APIs externas (Nominatim, OSRM) |
| **Springdoc OpenAPI** | 2.5.0 | Geração automática de Swagger UI a partir das anotações do código |
| **Bean Validation** | 3.x | Validação declarativa dos campos de entrada com anotações (@NotBlank, @Email, etc.) |
| **JUnit 5 + Mockito** | 5.x / 5.x | Framework padrão de testes unitários; mocking preciso sem necessidade de Spring context |

### Frontend

| Tecnologia | Versão | Por que foi escolhida |
|---|---|---|
| **Angular** | 17 | Framework empresarial com tipagem forte, DI nativa, roteamento e módulos robustos |
| **TypeScript** | 5.x | Tipagem estática que previne erros em tempo de compilação; espelha DTOs do backend |
| **PrimeNG** | 17 | Biblioteca de componentes rica (tabelas, formulários, toasts, menus) com tema Dark premium |
| **PrimeFlex** | 3.x | Sistema de grid e utilitários CSS compatível com PrimeNG |
| **Reactive Forms** | Angular | Validação programática com controle total sobre erros e estado do formulário |
| **RxJS** | 7.x | Programação reativa; `BehaviorSubject` para gerenciamento de estado de autenticação |
| **Jasmine + Karma** | Latest | Suite de testes padrão do Angular CLI; suporte a spies e mocks de serviços |

---

## 3. Arquitetura da Solução

```
┌─────────────────────────────────────────────┐
│              FRONTEND (Angular 17)           │
│  ┌─────────┐ ┌──────────┐ ┌──────────────┐  │
│  │  Pages  │ │ Services │ │ Interceptors │  │
│  └────┬────┘ └────┬─────┘ └──────┬───────┘  │
│       └───────────┴──────────────┘           │
│                    │ HTTP + JWT               │
└────────────────────┼────────────────────────-┘
                     │
┌────────────────────┼─────────────────────────┐
│         BACKEND (Spring Boot 3.2)            │
│  ┌─────────────┐ ┌──────────┐ ┌──────────┐  │
│  │ Controllers │→│ Services │→│   Repos  │  │
│  └──────┬──────┘ └────┬─────┘ └────┬─────┘  │
│         │ Security    │            │         │
│  ┌──────▼──────┐ ┌────▼─────┐ ┌───▼──────┐  │
│  │ JWT Filter  │ │ Pricing/ │ │PostgreSQL│  │
│  │ Auth Manager│ │ Geocoding│ └──────────┘  │
│  └─────────────┘ └────┬─────┘               │
└───────────────────────┼─────────────────────┘
                        │ HTTP (WebClient)
         ┌──────────────┴──────────────┐
         │       APIs Externas         │
         │  Nominatim    OSRM          │
         │ (Geocoding)  (Rotas)        │
         └─────────────────────────────┘
```

### Padrões Arquiteturais Aplicados

- **Layered Architecture**: Controller → Service → Repository (separação clara de responsabilidades)
- **DTO Pattern**: Entidades JPA nunca expostas diretamente na API
- **Stateless Authentication**: JWT sem sessão no servidor
- **Fail-Fast Validation**: Validações no DTO, no Service e no banco
- **Single Responsibility**: Cada serviço tem uma responsabilidade única e testável

---

## 4. Funcionalidades Implementadas

### Backend
- ✅ Cadastro de usuário com normalização de CPF e validação de duplicidade
- ✅ Login com CPF **ou** email como identificador
- ✅ Geração e validação de tokens JWT (24h de validade)
- ✅ Geocodificação de endereços via Nominatim (OpenStreetMap)
- ✅ Cálculo de rota e distância via OSRM
- ✅ Cálculo de preço com fórmula configurável (taxa base + km)
- ✅ Validação de distância máxima de entrega (30km)
- ✅ Criação de pedido com valor calculado no backend (nunca aceito do frontend)
- ✅ Listagem de pedidos paginada e ordenada por data
- ✅ Atualização de perfil (campos parciais protegidos contra sobrescrita)
- ✅ Tratamento global de erros com respostas padronizadas (RFC 7807)
- ✅ Documentação automática via Swagger UI

### Frontend
- ✅ Tela de login e cadastro com validação reativa
- ✅ Política de senha com regex (maiúscula + minúscula + especial)
- ✅ Autenticação persistida com JWT no localStorage
- ✅ Header dinâmico baseado no estado de autenticação
- ✅ Proteção de rotas com AuthGuard
- ✅ Injeção automática de Bearer token em todas as requisições
- ✅ Logout automático ao receber erro 401
- ✅ Fluxo de pedido em 2 etapas (calcular → confirmar)
- ✅ Listagem de pedidos com tabela paginada e status colorido
- ✅ Tela de edição de perfil com preenchimento automático
- ✅ Loading states e feedback visual durante operações assíncronas
- ✅ Tema escuro (Dark Mode) premium com animações CSS

---

## 5. Segurança

| Aspecto | Implementação |
|---|---|
| **Autenticação** | JWT stateless com HMAC-SHA256 |
| **Autorização** | Spring Security com filtro por rota |
| **Senhas** | BCrypt com salt automático (nunca armazenadas em texto claro) |
| **CPF** | Normalizado antes de persistir; nunca retornado sem necessidade |
| **Valores de pedido** | Sempre recalculados no backend — frontend não determina o preço |
| **CORS** | Configurado para aceitar apenas `http://localhost:4200` |
| **Validação de entrada** | Bean Validation nas DTOs + verificações adicionais nos Services |
| **Expiração de sessão** | Token JWT expira em 24h; frontend faz logout automático em 401 |

---

## 6. Cobertura de Testes

### Testes Unitários Backend — 53 testes, 0 falhas

| Suite | Testes | Cobertura |
|---|---|---|
| `PricingServiceTest` | 14 | Fórmula, boundary testing, configuração dinâmica |
| `AuthServiceTest` | 12 | Registro (normalização CPF, encoding, duplicidade), login (JWT, credenciais) |
| `OrderServiceTest` | 15 | Cadeia de serviços, regras de negócio, paginação |
| `UserServiceTest` | 12 | Contexto de segurança, atualização parcial, mapeamento |

**Técnicas utilizadas:**
- `@ExtendWith(MockitoExtension.class)` — testes rápidos sem contexto Spring
- `ArgumentCaptor` — verificação precisa dos argumentos passados aos mocks
- `@Nested` + `@DisplayName` — organização hierárquica e legível
- `assertThat()` do AssertJ — assertions fluentes e expressivas
- Boundary testing — valores exatamente no limite, acima e abaixo

### Testes Unitários Frontend — Jasmine/Karma

| Suite | Testes | Cobertura |
|---|---|---|
| `login.component.spec.ts` | 8 | Validação de formulário, submissão, redirect, erros |
| `register.component.spec.ts` | 9 | Campos obrigatórios, política de senha, conflitos |
| `create-order.component.spec.ts` | 8 | Estimativa, criação de pedido, erros de API |
| `my-orders.component.spec.ts` | 7 | Listagem, paginação, mapeamento de status |

---

## 7. Tratamento de Erros

### Backend — Resposta Padronizada

Todos os erros retornam no formato:
```json
{
  "timestamp": "2026-05-09T19:00:00",
  "status": 409,
  "message": "CPF já cadastrado no sistema"
}
```

### Mapeamento de Exceções (`GlobalExceptionHandler`)

| Exceção | HTTP Status | Cenário |
|---|---|---|
| `DuplicateCpfException` | 409 Conflict | CPF já cadastrado |
| `DuplicateEmailException` | 409 Conflict | Email já cadastrado |
| `UserNotFoundException` | 404 Not Found | Usuário não existe |
| `MaxDistanceExceededException` | 422 Unprocessable | Distância > 30km |
| `ExternalApiException` | 503 Service Unavailable | Nominatim/OSRM indisponível |
| `BusinessException` | 400 Bad Request | Regra de negócio violada |
| `MethodArgumentNotValidException` | 400 Bad Request | Validação de campos falhou |
| `BadCredentialsException` | 401 Unauthorized | Credenciais inválidas |

### Frontend — Tratamento Centralizado

- `AuthInterceptor`: intercepta `401` globalmente → logout automático
- `MessageService` (PrimeNG Toast): feedback visual em todos os erros de formulário e API
- Loading states: botões desabilitados durante requisições (prevenção de double submit)

---

## 8. Qualidade de Código

- **Lombok**: elimina getters/setters/builders repetitivos sem perder legibilidade
- **`@RequiredArgsConstructor`**: injeção de dependência via constructor (best practice Spring)
- **Records Java**: `Coordinates` e `RouteInfo` são records imutáveis — semântica clara
- **BigDecimal**: usado para todos os valores monetários (prevenção de erros de ponto flutuante)
- **`@ConfigurationProperties`**: configurações de negócio isoladas em `DeliveryConfig` — sem valores hard-coded
- **`@Transactional`**: transações explícitas nos métodos de escrita
- **`@Slf4j`**: logging estruturado em todos os serviços
- **Interfaces TypeScript**: modelos do frontend espelham exatamente os DTOs do backend

---

## 9. Como Rodar a Aplicação

### 🪟 Windows

**Pré-requisitos:**
- [Java 17 JDK](https://adoptium.net/) — marcar "Add to PATH" na instalação
- [Maven 3.8+](https://maven.apache.org/download.cgi) — extrair e adicionar ao PATH
- [Node.js 18+](https://nodejs.org/) — versão LTS
- [PostgreSQL 15+](https://www.postgresql.org/download/windows/) — instalar com pgAdmin

**1. Banco de Dados (via pgAdmin ou CMD):**
```cmd
psql -U postgres
CREATE DATABASE delivery_pge;
\q
```

**2. Backend:**
```cmd
cd "Projeto Delivery PGE\backend"
mvn clean spring-boot:run
```

**3. Frontend:**
```cmd
cd "Projeto Delivery PGE\frontend"
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install
npm start
```

---

### 🐧 Linux (Ubuntu/Debian)

**Pré-requisitos:**
```bash
# Java 17
sudo apt update
sudo apt install openjdk-17-jdk -y

# Maven
sudo apt install maven -y

# Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# PostgreSQL
sudo apt install postgresql postgresql-contrib -y
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**1. Banco de Dados:**
```bash
sudo -u postgres psql -c "CREATE DATABASE delivery_pge;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

**2. Backend:**
```bash
cd "Projeto Delivery PGE/backend"
mvn clean spring-boot:run
```

**3. Frontend:**
```bash
cd "Projeto Delivery PGE/frontend"
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install
npm start
```

---

### 🍎 macOS

**Pré-requisitos (via Homebrew):**
```bash
# Instalar Homebrew (se não tiver)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Adicionar ao PATH
eval "$(/opt/homebrew/bin/brew shellenv)"

# Java 17, Maven, PostgreSQL, Node.js
brew install openjdk@17 maven postgresql@16
brew services start postgresql@16

# Adicionar Java 17 ao PATH permanentemente
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zprofile
source ~/.zprofile
```

**1. Banco de Dados:**
```bash
psql postgres -c "CREATE DATABASE delivery_pge;"
psql postgres -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

**2. Backend:**
```bash
cd "Projeto Delivery PGE/backend"
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn spring-boot:run
```

**3. Frontend (usando NVM):**
```bash
# Instalar NVM se não tiver
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18

cd "Projeto Delivery PGE/frontend"
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install
npm start
```

---

### ✅ Verificação (todos os sistemas)

Após subir backend e frontend:

| Serviço | URL | Verificação |
|---|---|---|
| Backend | http://localhost:8080 | Deve retornar erro 401 (sinal que está no ar) |
| Swagger UI | http://localhost:8080/swagger-ui.html | Interface completa dos endpoints |
| Frontend | http://localhost:4200 | Tela de login do Delivery PGE |

**Teste rápido via curl:**
```bash
# Cadastrar usuário de teste
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Avaliador PGE",
    "cpf": "111.222.333-44",
    "email": "avaliador@pge.com",
    "password": "Avalia@123",
    "phone": "11999999999",
    "cep": "01310-100",
    "address": "Av. Paulista, 1000, São Paulo - SP",
    "referencePoint": "Teste"
  }'
```

---

### Rodar os Testes

**Backend (53 testes JUnit/Mockito):**
```bash
# Windows/Linux
mvn test

# macOS (com Java 17 explícito)
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" mvn test
```

Resultado esperado:
```
Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Frontend (Jasmine/Karma):**
```bash
# Modo headless (CI/CD)
npm test -- --watch=false --browsers=ChromeHeadless

# Modo interativo
npm test
```

---

## 10. Configurações da Aplicação

### `application.properties` (backend)

```properties
# Banco de Dados
spring.datasource.url=jdbc:postgresql://localhost:5432/delivery_pge
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA — cria tabelas automaticamente
spring.jpa.hibernate.ddl-auto=update

# Regras de Negócio (configuráveis sem recompilar)
delivery.base-fee=5.00
delivery.price-per-km=2.50
delivery.max-distance-km=30.0

# JWT
jwt.secret=chave-secreta-minima-256-bits
jwt.expiration=86400000

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### `environment.ts` (frontend)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

---

## 11. Endpoints da API (Resumo)

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/api/v1/auth/register` | ❌ | Cadastrar usuário |
| POST | `/api/v1/auth/login` | ❌ | Login (retorna JWT) |
| GET | `/api/v1/users/me` | ✅ | Ver perfil |
| PUT | `/api/v1/users/me` | ✅ | Atualizar perfil |
| POST | `/api/v1/orders/calculate-estimate` | ✅ | Calcular estimativa |
| POST | `/api/v1/orders` | ✅ | Criar pedido |
| GET | `/api/v1/orders/my-orders` | ✅ | Listar pedidos (paginado) |
| GET | `/api/v1/orders/user/{userId}` | ✅ | Pedidos por usuário |

---

## 12. Critérios de Avaliação — Atendimento

| Critério | Como foi atendido |
|---|---|
| **Corretude funcional** | Todos os fluxos (cadastro, login, estimativa, pedido, listagem, perfil) implementados e funcionais |
| **Qualidade de código** | Lombok, Records, BigDecimal, @ConfigurationProperties, sem valores hard-coded, sem duplicação |
| **Arquitetura** | Camadas Controller→Service→Repository, DTOs, separação de responsabilidades, padrões REST |
| **Segurança** | JWT stateless, BCrypt, CORS restrito, validação em múltiplas camadas, valor recalculado no backend |
| **Cobertura de testes** | 53 testes unitários backend (JUnit/Mockito) + 32 testes frontend (Jasmine); AAA pattern |
| **Organização do projeto** | Pacotes por domínio, nomenclatura clara, README e documentação completa |
| **Integração frontend/backend** | AuthInterceptor injetando JWT, modelos TypeScript espelhando DTOs, tratamento de 401 |
| **Tratamento de erros** | GlobalExceptionHandler, exceções customizadas, respostas padronizadas, feedback visual |
| **Experiência do usuário** | Dark mode premium, loading states, toasts de feedback, fluxo de 2 etapas, tabela paginada |
| **Boas práticas Java/Spring** | @Transactional, @Slf4j, injeção por constructor, Bean Validation, WebClient reativo |
| **Boas práticas Angular/PrimeNG** | Reactive Forms, AuthGuard, Interceptor, BehaviorSubject, componentes PrimeNG semânticos |
