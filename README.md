<h1 align="center">🛵 Delivery PGE</h1>

<p align="center">
  <strong>Aplicação Full Stack de Delivery — Java 17 · Spring Boot 3.2 · Angular 17 · PostgreSQL</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%20LTS-orange?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-17-DD0031?logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15%2B-4169E1?logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Testes%20Backend-53%20%E2%9C%85-success" />
  <img src="https://img.shields.io/badge/Testes%20Frontend-32%20%E2%9C%85-success" />
</p>

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Como Executar](#-como-executar)
  - [macOS](#-macos)
  - [Windows](#-windows)
  - [Linux](#-linux)
- [Executar os Testes](#-executar-os-testes)
- [Endpoints da API](#-endpoints-da-api)
- [Exemplos de Request/Response](#-exemplos-de-requestresponse)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Regras de Negócio](#-regras-de-negócio)
- [Segurança](#-segurança)
- [APIs Externas](#-apis-externas)
- [Configuração](#-configuração)
- [Documentação Adicional](#-documentação-adicional)

---

## 🎯 Visão Geral

**Delivery PGE** é uma plataforma completa de delivery inspirada no iFood, desenvolvida como projeto Full Stack de ponta a ponta.

O sistema permite que usuários se cadastrem, façam login com **CPF ou e-mail**, calculem estimativas de entrega com **roteamento geográfico real** (via APIs OpenStreetMap) e criem pedidos que ficam registrados com paginação. Toda a comunicação entre frontend e backend é protegida por **JWT stateless**.

```
Frontend (Angular 17)  ←→  API REST (Spring Boot 3.2)  ←→  PostgreSQL
                                       ↕
                          Nominatim (Geocoding) + OSRM (Rotas)
```

---

## ✅ Funcionalidades

### Backend
| # | Funcionalidade |
|---|---|
| 1 | Cadastro de usuário com normalização de CPF e validação de duplicidade |
| 2 | Login com **CPF ou e-mail** como identificador |
| 3 | Geração e validação de tokens JWT (24h de validade, HMAC-SHA256) |
| 4 | Geocodificação de endereços via **Nominatim** (OpenStreetMap) |
| 5 | Cálculo de rota e distância real via **OSRM** |
| 6 | Cálculo de preço com fórmula configurável (taxa base + R$/km) |
| 7 | Validação de distância máxima (30 km) |
| 8 | Criação de pedido com valor **sempre recalculado no backend** |
| 9 | Listagem de pedidos paginada e ordenada por data |
| 10 | Atualização parcial de perfil (campos nulos não sobrescrevem dados) |
| 11 | Tratamento global de erros com respostas padronizadas |
| 12 | Documentação interativa via **Swagger UI** |

### Frontend
| # | Funcionalidade |
|---|---|
| 1 | Tela de login e cadastro com validação reativa (Reactive Forms) |
| 2 | Política de senha com regex (maiúscula + minúscula + especial) |
| 3 | Autenticação persistida com JWT no `localStorage` |
| 4 | Header dinâmico baseado no estado de autenticação |
| 5 | Proteção de rotas com **AuthGuard** |
| 6 | Injeção automática de Bearer token via **AuthInterceptor** |
| 7 | Logout automático ao receber erro `401` |
| 8 | Fluxo de pedido em **2 etapas** (calcular estimativa → confirmar) |
| 9 | Listagem de pedidos com tabela paginada e status colorido |
| 10 | Tela de edição de perfil com preenchimento automático |
| 11 | Loading states e feedback visual (PrimeNG Toast) |
| 12 | **Dark Mode** premium com animações CSS |

---

## 🏛️ Arquitetura

```
┌─────────────────────────────────────────────────┐
│               FRONTEND (Angular 17)              │
│  ┌──────────┐  ┌──────────┐  ┌───────────────┐  │
│  │  Pages   │  │ Services │  │  Interceptors │  │
│  └─────┬────┘  └─────┬────┘  └───────┬───────┘  │
│        └─────────────┴───────────────┘           │
│                      │ HTTP + JWT Bearer          │
└──────────────────────┼──────────────────────────-┘
                       │
┌──────────────────────┼───────────────────────────┐
│          BACKEND (Spring Boot 3.2)               │
│  ┌──────────────┐  ┌──────────┐  ┌───────────┐  │
│  │  Controllers │→ │ Services │→ │   Repos   │  │
│  └──────┬───────┘  └────┬─────┘  └─────┬─────┘  │
│         │               │              │         │
│  ┌──────▼───────┐  ┌────▼──────┐  ┌───▼──────┐  │
│  │  JWT Filter  │  │ Pricing/  │  │PostgreSQL│  │
│  │ Auth Manager │  │ Geocoding │  └──────────┘  │
│  └──────────────┘  └─────┬─────┘               │
└────────────────────────── ┼────────────────────-┘
                             │ HTTP (WebClient reativo)
              ┌──────────────┴──────────────┐
              │        APIs Externas         │
              │  Nominatim      OSRM         │
              │ (Geocoding)   (Rotas)        │
              └──────────────────────────────┘
```

### Padrões Aplicados
- **Layered Architecture** — Controller → Service → Repository
- **DTO Pattern** — Entidades JPA nunca expostas diretamente na API
- **Stateless Authentication** — JWT sem sessão no servidor
- **Fail-Fast Validation** — Validações no DTO, no Service e no banco
- **Single Responsibility** — Cada serviço tem uma única responsabilidade testável

---

## 🛠️ Tecnologias

### Backend
| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | **17 LTS** | Linguagem principal |
| Spring Boot | **3.2.5** | Framework base (auto-configuração, starters) |
| Spring Security | 6.x | Autenticação e autorização por rota |
| Spring Data JPA | 6.x | Persistência com PostgreSQL |
| JJWT | 0.12.5 | Geração e validação de tokens JWT |
| PostgreSQL | **15+** | Banco relacional principal |
| Lombok | 1.18.32 | Redução de boilerplate (getters, builders, logs) |
| WebClient (WebFlux) | 6.x | Cliente HTTP reativo para APIs externas |
| Springdoc OpenAPI | 2.5.0 | Swagger UI automático |
| Bean Validation | 3.x | Validação declarativa de campos (`@NotBlank`, `@Email`) |
| JUnit 5 + Mockito | 5.x | Testes unitários sem contexto Spring |
| Maven | **3.8+** | Gerenciamento de dependências e build |

### Frontend
| Tecnologia | Versão | Finalidade |
|---|---|---|
| Angular | **17** | Framework SPA com DI e roteamento |
| TypeScript | 5.x | Tipagem estática espelhando DTOs do backend |
| PrimeNG | **17** | Componentes visuais (tabelas, formulários, toasts) |
| PrimeFlex | 3.x | Grid e utilitários CSS |
| RxJS | 7.x | `Observable`, `BehaviorSubject` para estado reativo |
| Reactive Forms | Angular | Validação programática de formulários |
| Jasmine + Karma | Latest | Testes unitários de componentes Angular |

---

## ⚙️ Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java JDK | **17** |
| Maven | **3.8+** |
| Node.js | **18+** |
| npm | **9+** |
| PostgreSQL | **15+** |

---

## 🚀 Como Executar

> Execute **banco → backend → frontend** nessa ordem, cada um em um terminal separado.

---

### 🍎 macOS

#### Passo 0 — Ativar Homebrew e Node (obrigatório em cada terminal novo)
```bash
eval "$(/opt/homebrew/bin/brew shellenv)"
export PATH="/Users/bergsonmulato/.nvm/versions/node/v20.20.2/bin:$PATH"
```

#### Passo 1 — Banco de Dados (Terminal 1)
```bash
# Iniciar PostgreSQL
brew services start postgresql@16

# Criar banco (apenas na primeira vez)
bash "/Users/bergsonmulato/Projeto Delivery PGE/setup-database.sh"

# Verificar tabelas
psql -U postgres -d delivery_pge -c "\dt"
```

#### Passo 2 — Backend (Terminal 2)
```bash
cd "/Users/bergsonmulato/Projeto Delivery PGE/backend"

# Compilar
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn clean compile

# Subir o servidor
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn spring-boot:run
```

#### Passo 3 — Frontend (Terminal 3)
```bash
cd "/Users/bergsonmulato/Projeto Delivery PGE/frontend"

# Instalar dependências (apenas na primeira vez)
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install

# Iniciar servidor de desenvolvimento
npm start
```

---

### 🪟 Windows

**Pré-requisitos:** [Java 17](https://adoptium.net/) · [Maven](https://maven.apache.org/) · [Node.js 18+](https://nodejs.org/) · [PostgreSQL 15+](https://www.postgresql.org/download/windows/)

```cmd
REM 1. Banco de Dados (pgAdmin ou CMD)
psql -U postgres -c "CREATE DATABASE delivery_pge;"

REM 2. Backend
cd "Projeto Delivery PGE\backend"
mvn clean spring-boot:run

REM 3. Frontend
cd "Projeto Delivery PGE\frontend"
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install
npm start
```

---

### 🐧 Linux (Ubuntu/Debian)

```bash
# Instalar dependências do sistema
sudo apt update
sudo apt install openjdk-17-jdk maven postgresql postgresql-contrib -y
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# 1. Banco de Dados
sudo systemctl start postgresql
sudo -u postgres psql -c "CREATE DATABASE delivery_pge;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"

# 2. Backend
cd "Projeto Delivery PGE/backend"
mvn clean spring-boot:run

# 3. Frontend
cd "Projeto Delivery PGE/frontend"
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install
npm start
```

---

### ✅ Verificação (todos os sistemas)

| Serviço | URL | Sinal de sucesso |
|---|---|---|
| **Backend** | http://localhost:8080 | Retorna `401` (está no ar, aguardando token) |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Interface completa dos endpoints |
| **Frontend** | http://localhost:4200 | Tela de login do Delivery PGE |

```bash
# Teste rápido — cadastrar usuário via curl
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
    "referencePoint": "Próximo ao MASP"
  }'
```

---

## 🧪 Executar os Testes

### Backend — 53 testes JUnit/Mockito (0 falhas)

```bash
# Linux / Windows
cd backend
mvn test

# macOS (Java 17 explícito)
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn test
```

Resultado esperado:
```
Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Suite | Testes | O que cobre |
|---|---|---|
| `PricingServiceTest` | 14 | Fórmula, boundary testing, configuração dinâmica |
| `AuthServiceTest` | 12 | Normalização CPF, BCrypt, duplicidade, JWT |
| `OrderServiceTest` | 15 | Cadeia Geocoding→Route→Pricing, paginação |
| `UserServiceTest` | 12 | SecurityContext, atualização parcial, mapeamento |

### Frontend — 32 testes Jasmine/Karma

```bash
cd frontend

# Modo headless (CI/CD)
npm test -- --watch=false --browsers=ChromeHeadless

# Modo interativo
npm test
```

| Suite | Testes |
|---|---|
| `login.component.spec.ts` | 8 |
| `register.component.spec.ts` | 9 |
| `create-order.component.spec.ts` | 8 |
| `my-orders.component.spec.ts` | 7 |

---

## 📡 Endpoints da API

### Autenticação (Público — sem token)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Cadastrar novo usuário |
| `POST` | `/api/v1/auth/login` | Login com CPF ou e-mail + senha |

### Usuário (🔒 Requer Bearer token)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/users/me` | Obter perfil do usuário autenticado |
| `PUT` | `/api/v1/users/me` | Atualizar dados cadastrais |

### Pedidos (🔒 Requer Bearer token)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/orders/calculate-estimate` | Calcular estimativa (distância, tempo, valor) |
| `POST` | `/api/v1/orders` | Criar pedido confirmado |
| `GET` | `/api/v1/orders/my-orders?page=0&size=10` | Listar pedidos paginado |
| `GET` | `/api/v1/orders/user/{userId}` | Listar pedidos por usuário |

---

## 📝 Exemplos de Request/Response

### Registro
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "João Silva",
  "cpf": "123.456.789-09",
  "email": "joao@email.com",
  "password": "Senha@123",
  "phone": "11999999999",
  "cep": "01310-100",
  "address": "Av. Paulista, 1000, São Paulo - SP",
  "referencePoint": "Próximo ao MASP"
}
```
```json
// 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva",
  "cpf": "12345678909",
  "email": "joao@email.com",
  "createdAt": "2026-05-09T12:00:00"
}
```

### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{ "identifier": "joao@email.com", "password": "Senha@123" }
```
```json
// 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "cpf": "12345678909"
}
```

### Calcular Estimativa
```bash
POST /api/v1/orders/calculate-estimate
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "pickupAddress": "Av. Paulista, 1000, São Paulo - SP",
  "deliveryAddress": "Rua Augusta, 500, São Paulo - SP"
}
```
```json
// 200 OK
{
  "distanceKm": 5.20,
  "estimatedTimeMinutes": 20,
  "estimatedValue": 18.00
}
// Fórmula: R$ 5,00 + (5,20 km × R$ 2,50) = R$ 18,00
```

### Criar Pedido
```bash
POST /api/v1/orders
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "pickupAddress": "Av. Paulista, 1000, São Paulo - SP",
  "deliveryAddress": "Rua Augusta, 500, São Paulo - SP",
  "itemDescription": "2x Pizza Margherita, 1x Refrigerante 2L"
}
```
```json
// 201 Created
{
  "id": "7f3e5c2a-...",
  "status": "PENDING",
  "distanceKm": 5.20,
  "estimatedTimeMinutes": 20,
  "estimatedValue": 18.00,
  "createdAt": "2026-05-09T14:30:00"
}
```

### Erro Padrão
```json
{
  "timestamp": "2026-05-09T12:00:00",
  "status": 409,
  "message": "CPF já cadastrado no sistema"
}
```

---

## 📁 Estrutura do Projeto

```
Projeto Delivery PGE/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/delivery/pge/
│       │   ├── config/        ← SecurityConfig, DeliveryConfig, WebClientConfig, OpenApiConfig
│       │   ├── controller/    ← AuthController, UserController, OrderController
│       │   ├── dto/           ← DTOs de request e response
│       │   ├── entity/        ← User, Order, UserRole (enum), OrderStatus (enum)
│       │   ├── exception/     ← GlobalExceptionHandler + exceções customizadas
│       │   ├── repository/    ← UserRepository, OrderRepository
│       │   ├── security/      ← JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
│       │   └── service/       ← AuthService, UserService, OrderService,
│       │                         GeocodingService, RouteService, PricingService
│       └── test/java/com/delivery/pge/service/
│           ├── AuthServiceTest.java      ← 12 testes
│           ├── OrderServiceTest.java     ← 15 testes
│           ├── PricingServiceTest.java   ← 14 testes
│           └── UserServiceTest.java      ← 12 testes
│
└── frontend/
    └── src/app/
        ├── core/
        │   ├── guards/        ← AuthGuard (protege rotas privadas)
        │   ├── interceptors/  ← AuthInterceptor (injeta JWT + trata 401)
        │   ├── models/        ← models.ts (interfaces TypeScript ↔ DTOs)
        │   └── services/      ← AuthService, UserService, OrderService
        ├── pages/
        │   ├── login/         ← Tela de login + spec
        │   ├── register/      ← Tela de cadastro + spec
        │   ├── user-area/     ← Dashboard do usuário
        │   ├── my-orders/     ← Listagem de pedidos + spec
        │   ├── create-order/  ← Criação de pedido em 2 etapas + spec
        │   └── update-user/   ← Atualização de perfil
        └── shared/layout/
            └── header/        ← Header global com menu dinâmico
```

---

## 📐 Regras de Negócio

| Regra | Valor | Onde configurar |
|---|---|---|
| Taxa base de entrega | **R$ 5,00** | `application.properties` |
| Preço por quilômetro | **R$ 2,50** | `application.properties` |
| Distância máxima | **30 km** | `application.properties` |
| Validade do JWT | **24 horas** | `application.properties` |
| Identificador de login | CPF **ou** e-mail | — |

**Fórmula:** `valorEntrega = taxaBase + (distanciaKm × valorPorKm)`

> ⚠️ O valor da entrega é **sempre calculado pelo backend**. O frontend nunca determina o preço.

---

## 🔒 Segurança

| Aspecto | Implementação |
|---|---|
| **Autenticação** | JWT stateless — HMAC-SHA256 |
| **Autorização** | Spring Security com filtro por rota |
| **Senhas** | BCrypt com salt automático (nunca armazenadas em texto claro) |
| **CPF** | Normalizado antes de persistir (`123.456.789-09` → `12345678909`) |
| **Valores** | Preços sempre recalculados no backend |
| **CORS** | Restrito a `http://localhost:4200` |
| **Validação** | Bean Validation nas DTOs + verificações adicionais nos Services |
| **Expiração** | Token expira em 24h; frontend faz logout automático em `401` |

### Rotas públicas vs. privadas
```
# Públicas (sem token):
POST /api/v1/auth/register
POST /api/v1/auth/login

# Privadas (exigem Bearer token):
GET  /api/v1/users/me
PUT  /api/v1/users/me
POST /api/v1/orders/calculate-estimate
POST /api/v1/orders
GET  /api/v1/orders/my-orders
GET  /api/v1/orders/user/{userId}
```

---

## 🌐 APIs Externas

| Serviço | URL | Finalidade |
|---|---|---|
| **Nominatim** | nominatim.openstreetmap.org | Geocodificação de endereços (texto → lat/lon) |
| **OSRM** | router.project-osrm.org | Cálculo de rota real e distância em km |

> ✅ Ambas são **gratuitas**, sem chave de API, baseadas em OpenStreetMap.  
> ⚠️ Respeite o limite do Nominatim: máximo de 1 requisição por segundo.

---

## ⚙️ Configuração

### `backend/src/main/resources/application.properties`
```properties
# Banco de Dados
spring.datasource.url=jdbc:postgresql://localhost:5432/delivery_pge
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA — cria/atualiza tabelas automaticamente
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

### `frontend/src/environments/environment.ts`
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

### Política de senha
```
Regex: ^(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$

✅ Mínimo 6 caracteres
✅ 1 letra maiúscula
✅ 1 letra minúscula
✅ 1 caractere especial (@$!%*?&)

Exemplos válidos: Senha@123 · Admin#456
```

---

## 📖 Documentação Adicional

| Documento | Descrição |
|---|---|
| [`GUIA_EDUCATIVO.md`](./GUIA_EDUCATIVO.md) | Explicação técnica detalhada de toda a arquitetura, serviços, JWT, DTOs, testes e Angular |
| [`DOCUMENTACAO_AVALIADORES.md`](./DOCUMENTACAO_AVALIADORES.md) | Documentação formal para avaliadores — justificativas técnicas, critérios de avaliação atendidos |
| [Swagger UI](http://localhost:8080/swagger-ui.html) | Documentação interativa da API (requer backend rodando) |

---

<p align="center">
  Desenvolvido como projeto Full Stack · Java 17 + Spring Boot 3.2 + Angular 17 · Maio/2026
</p>
