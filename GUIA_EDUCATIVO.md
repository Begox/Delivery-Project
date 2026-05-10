# 📚 Guia Educativo — Delivery PGE

> Documento técnico-educativo explicando toda a arquitetura, métodos, testes, APIs e uso da aplicação Full Stack de Delivery.

---

## ⚡ Início Rápido — Subindo a Aplicação

> Execute os passos abaixo **em terminais separados**, na ordem indicada.

### Passo 0 — Ativar Homebrew e Node no terminal (obrigatório em todo terminal novo)

```bash
eval "$(/opt/homebrew/bin/brew shellenv)"
export PATH="/Users/bergsonmulato/.nvm/versions/node/v20.20.2/bin:$PATH"
```

---

### Passo 1 — Banco de Dados (PostgreSQL) · Terminal 1

```bash
# Verificar status
brew services list | grep postgresql

# Iniciar (se parado)
brew services start postgresql@16

# Recriar tabelas do zero (opcional)
bash "/Users/bergsonmulato/Projeto Delivery PGE/setup-database.sh"

# Verificar tabelas criadas
psql -U postgres -d delivery_pge -c "\dt"
```

---

### Passo 2 — Backend (Spring Boot) · Terminal 2

```bash
cd "/Users/bergsonmulato/Projeto Delivery PGE/backend"

# Compilar
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn clean compile

# Subir o servidor
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn spring-boot:run
```

✅ Backend em: **http://localhost:8080**  
📖 Swagger UI: **http://localhost:8080/swagger-ui.html**

---

### Passo 3 — Frontend (Angular) · Terminal 3

```bash
export PATH="/Users/bergsonmulato/.nvm/versions/node/v20.20.2/bin:$PATH"

cd "/Users/bergsonmulato/Projeto Delivery PGE/frontend"

# Apenas na primeira execução:
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install

# Iniciar servidor de desenvolvimento
npm start
```

✅ Frontend em: **http://localhost:4200**

---

### Passo 4 — Rodar os Testes do Backend (opcional)

```bash
cd "/Users/bergsonmulato/Projeto Delivery PGE/backend"

JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn test
```

---

## 🗂️ Estrutura Geral do Projeto

```
Projeto Delivery PGE/
├── backend/                   ← Java 17 + Spring Boot 3.2
│   └── src/main/java/com/delivery/pge/
│       ├── config/            ← Configurações (Security, CORS, WebClient, OpenAPI)
│       ├── controller/        ← Endpoints REST (AuthController, UserController, OrderController)
│       ├── dto/               ← Objetos de transferência de dados
│       ├── entity/            ← Entidades JPA (User, Order)
│       ├── exception/         ← Exceções customizadas + GlobalExceptionHandler
│       ├── repository/        ← Interfaces Spring Data JPA
│       ├── security/          ← JWT Provider, Filtro de autenticação, UserDetailsService
│       └── service/           ← Lógica de negócio (Auth, User, Order, Pricing, Geocoding, Route)
├── frontend/                  ← Angular 17 + PrimeNG 17
│   └── src/app/
│       ├── core/              ← Services, Guards, Interceptors, Models
│       ├── pages/             ← Componentes de página (login, register, etc.)
│       └── shared/            ← Header e layout global
├── setup-database.sh          ← Script automático de criação do banco PostgreSQL
└── README.md                  ← Guia de início rápido
```

---

## 🚀 Comandos para Subir a Aplicação

### Pré-requisito — Ativar Homebrew no terminal

```bash
eval "$(/opt/homebrew/bin/brew shellenv)"
```
> Execute isso toda vez que abrir um terminal novo, até adicionar ao `.zprofile`.

---

### 1. Banco de Dados (PostgreSQL)

```bash
# Verificar se o serviço está rodando
brew services list | grep postgresql

# Iniciar se estiver parado
brew services start postgresql@16

# Recriar o banco (se necessário)
bash "/Users/bergsonmulato/Projeto Delivery PGE/setup-database.sh"

# Conectar manualmente e verificar
psql -U postgres -d delivery_pge -c "\dt"
```

---

### 2. Backend (Spring Boot)

```bash
cd "/Users/bergsonmulato/Projeto Delivery PGE/backend"

# Compilar o projeto
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn clean compile

# Rodar a aplicação
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn spring-boot:run

# Rodar os testes
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home" \
  mvn test
```

✅ Backend disponível em: **http://localhost:8080**
📖 Swagger UI: **http://localhost:8080/swagger-ui.html**

---

### 3. Frontend (Angular)

```bash
# Adicionar Node.js ao PATH (se necessário)
export PATH="/Users/bergsonmulato/.nvm/versions/node/v20.20.2/bin:$PATH"

cd "/Users/bergsonmulato/Projeto Delivery PGE/frontend"

# Instalar dependências (apenas na primeira vez)
npm install primeng@17 primeicons primeflex --legacy-peer-deps
npm install

# Rodar em modo desenvolvimento
npm start
```

✅ Frontend disponível em: **http://localhost:4200**

---

## 🏗️ Backend — Entidades JPA

### `User.java`
Representa o usuário do sistema. Implementa `UserDetails` para integração com Spring Security.

```
id            → UUID gerado automaticamente (@GeneratedValue UUID)
fullName      → Nome completo
cpf           → CPF normalizado (sem pontos/traços), único no banco
email         → Email único, usado como username
password      → Senha codificada com BCrypt (nunca exposta na API)
phone         → Telefone principal
secondaryPhone→ Telefone secundário (opcional)
cep           → CEP do endereço de entrega
address       → Endereço completo
referencePoint→ Ponto de referência
role          → Enum UserRole: USER (padrão)
createdAt     → Preenchido automaticamente pelo @PrePersist
```

**Por que implementar `UserDetails`?**
Permite que o Spring Security use a entidade `User` diretamente no contexto de autenticação. O método `getUsername()` retorna o email, que é o identificador usado no JWT.

---

### `Order.java`
Representa um pedido de entrega.

```
id                   → UUID gerado automaticamente
user                 → Relacionamento @ManyToOne (muitos pedidos por usuário)
pickupAddress        → Endereço de coleta dos itens
deliveryAddress      → Endereço de destino da entrega
itemDescription      → O que está sendo entregue
distanceKm           → Distância calculada pelo OSRM (em km)
estimatedTimeMinutes → Tempo estimado em minutos
estimatedValue       → Valor calculado pelo PricingService (em R$)
status               → Enum: PENDING | IN_PROGRESS | DELIVERED | CANCELED
createdAt            → Data/hora da criação (@PrePersist)
```

**Por que `@Builder.Default` no status?**
O Lombok `@Builder` ignora valores padrão de campos. `@Builder.Default` garante que `status = PENDING` seja definido mesmo quando o objeto é construído via builder pattern.

---

## 📦 DTOs (Data Transfer Objects)

DTOs isolam as entidades JPA da API REST, evitando exposição de dados sensíveis e criando um contrato claro entre frontend e backend.

| DTO | Direção | Campos Principais |
|-----|---------|-------------------|
| `RegisterRequestDTO` | Cliente → API | fullName, cpf, email, password, phone, address |
| `LoginRequestDTO` | Cliente → API | identifier (CPF ou email), password |
| `LoginResponseDTO` | API → Cliente | token (JWT), userId, fullName, email, cpf |
| `UserResponseDTO` | API → Cliente | id, fullName, cpf, email, phone, address, createdAt |
| `UpdateUserDTO` | Cliente → API | phone, secondaryPhone, cep, address, referencePoint |
| `EstimateRequestDTO` | Cliente → API | pickupAddress, deliveryAddress |
| `EstimateResponseDTO` | API → Cliente | distanceKm, estimatedTimeMinutes, estimatedValue |
| `OrderRequestDTO` | Cliente → API | pickupAddress, deliveryAddress, itemDescription |
| `OrderResponseDTO` | API → Cliente | id, userId, status, estimatedValue, createdAt |

---

## ⚙️ Serviços — Lógica de Negócio

### `PricingService`

Calcula o valor da entrega com base na distância.

**Fórmula:**
```
estimatedValue = baseFee + (distanceKm × pricePerKm)

Exemplo: 5.00 + (5.2 × 2.50) = R$ 18,00
```

**Configuração em `application.properties`:**
```properties
delivery.base-fee=5.00
delivery.price-per-km=2.50
delivery.max-distance-km=30.0
```

**Regra de negócio:** Lança `MaxDistanceExceededException` se `distanceKm > 30`.

---

### `GeocodingService`

Converte endereços em coordenadas geográficas usando a API **Nominatim** (OpenStreetMap).

```
Entrada:  "Av. Paulista, 1000, São Paulo - SP"
Saída:    Coordinates(lat=-23.5613, lon=-46.6564)

Fluxo:
1. GET https://nominatim.openstreetmap.org/search?q={endereço}&format=json&limit=1
2. Extrai lat/lon do primeiro resultado
3. Lança ExternalApiException se nenhum resultado for encontrado
```

---

### `RouteService`

Calcula a rota entre dois pontos usando **OSRM** (Open Source Routing Machine).

```
Entrada:  Coordinates(origem) + Coordinates(destino)
Saída:    RouteInfo(distanceKm=5.2, estimatedTimeMinutes=20)

Fluxo:
1. GET https://router.project-osrm.org/route/v1/driving/{lon,lat;lon,lat}
2. Extrai distance (metros) → divide por 1000 → km
3. Extrai duration (segundos) → divide por 60 com Math.ceil → minutos
```

---

### `OrderService`

Orquestra o fluxo completo de pedido.

**`calculateEstimate()`:**
```
1. Geocodifica endereço de coleta  → Coordinates (via GeocodingService)
2. Geocodifica endereço de entrega → Coordinates (via GeocodingService)
3. Calcula rota entre coordenadas  → RouteInfo (via RouteService)
4. Calcula preço pela distância    → BigDecimal (via PricingService)
5. Retorna EstimateResponseDTO com distância + tempo + valor
```

**`createOrder()`:**
```
1. Obtém usuário autenticado do SecurityContext (via UserService)
2. Chama calculateEstimate() internamente para validar endereços
3. Rejeita se valor estimado = zero (BusinessException)
4. Cria e persiste o Order com status PENDING
⚠️  O valor NUNCA vem do frontend — sempre recalculado no backend
```

---

### `AuthService`

Gerencia registro e autenticação.

**`register()`:**
```
1. Normaliza CPF: "123.456.789-09" → "12345678909"
2. Verifica CPF duplicado → DuplicateCpfException
3. Verifica Email duplicado → DuplicateEmailException
4. Codifica senha com BCrypt
5. Persiste User e retorna UserResponseDTO
```

**`login()`:**
```
1. Delega ao AuthenticationManager do Spring Security
2. Spring Security chama UserDetailsServiceImpl.loadUserByUsername()
   (aceita CPF ou email como identificador)
3. Gera token JWT com JwtTokenProvider
4. Retorna LoginResponseDTO com token + dados do usuário
```

---

### `UserService`

Gerencia o perfil do usuário autenticado.

**`getCurrentUser()`:** Lê o usuário diretamente do `SecurityContextHolder` — já disponível após o filtro JWT processar o token.

**`updateCurrentUser()`:** Atualiza apenas campos com valor `!= null && !isBlank()`. Campos como CPF e email **não podem ser alterados** por segurança.

---

## 🔒 Segurança JWT

### Fluxo Completo

```
REGISTRO/LOGIN:
  Cliente → POST /auth/login → AuthService → AuthenticationManager
  → UserDetailsServiceImpl.loadUserByUsername() → BCrypt.matches()
  → JwtTokenProvider.generateToken() → retorna token

REQUISIÇÃO AUTENTICADA:
  Cliente → GET /users/me (com header Authorization: Bearer {token})
  → JwtAuthenticationFilter intercepta
  → JwtTokenProvider valida token (assinatura + expiração)
  → UserDetailsServiceImpl carrega User pelo email
  → SecurityContextHolder.setAuthentication(...)
  → Controller recebe requisição autenticada
```

### `JwtTokenProvider`
- Usa **HMAC-SHA256** para assinar o token
- Token contém: username (email), data de emissão, data de expiração
- Validade padrão: **24 horas**

### `SecurityConfig` — Rotas públicas vs. privadas

```java
// PÚBLICAS (sem token):
POST /api/v1/auth/register
POST /api/v1/auth/login

// PRIVADAS (requerem Bearer token):
GET  /api/v1/users/me
PUT  /api/v1/users/me
POST /api/v1/orders/calculate-estimate
POST /api/v1/orders
GET  /api/v1/orders/my-orders
GET  /api/v1/orders/user/{userId}
```

---

## 📡 API REST — Exemplos de Uso

### Cadastrar usuário
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "João Silva",
    "cpf": "123.456.789-09",
    "email": "joao@email.com",
    "password": "Senha@123",
    "phone": "11999999999",
    "cep": "01310-100",
    "address": "Av. Paulista, 1000, São Paulo - SP",
    "referencePoint": "Próximo ao MASP"
  }'
```

### Fazer login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "joao@email.com", "password": "Senha@123"}'

# Resposta:
# { "token": "eyJhbGci...", "userId": "uuid", "fullName": "João Silva" }
```

### Calcular estimativa
```bash
curl -X POST http://localhost:8080/api/v1/orders/calculate-estimate \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pickupAddress": "Av. Paulista, 1000, São Paulo - SP",
    "deliveryAddress": "Rua Augusta, 500, São Paulo - SP"
  }'

# Resposta:
# { "distanceKm": 5.20, "estimatedTimeMinutes": 20, "estimatedValue": 18.00 }
```

### Criar pedido
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pickupAddress": "Av. Paulista, 1000, São Paulo - SP",
    "deliveryAddress": "Rua Augusta, 500, São Paulo - SP",
    "itemDescription": "2x Pizza Margherita"
  }'
```

### Listar meus pedidos (paginado)
```bash
curl "http://localhost:8080/api/v1/orders/my-orders?page=0&size=10" \
  -H "Authorization: Bearer SEU_TOKEN"
```

### Atualizar perfil
```bash
curl -X PUT http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"phone": "11988887777", "address": "Nova Rua, 100"}'
```

---

## 🧪 Testes JUnit/Mockito

### Padrão AAA (Arrange, Act, Assert)

```java
@Test
void shouldCalculateCorrectPrice() {
    // Arrange — configurar mocks
    when(deliveryConfig.getBaseFee()).thenReturn(new BigDecimal("5.00"));
    when(deliveryConfig.getPricePerKm()).thenReturn(new BigDecimal("2.50"));

    // Act — executar
    BigDecimal result = pricingService.calculateDeliveryPrice(5.2);

    // Assert — verificar
    assertThat(result).isEqualByComparingTo("18.00");
}
```

### Resumo das 4 suites de teste

**`PricingServiceTest` — 14 testes**
- Fórmula de cálculo com valores reais
- Boundary testing: exatamente 30km (OK), 30.01km (exceção)
- Configuração dinâmica via mock de `DeliveryConfig`

**`AuthServiceTest` — 12 testes**
- Normalização de CPF antes de salvar (`ArgumentCaptor`)
- Encoding de senha com `PasswordEncoder`
- CPF/Email duplicado → exceção correta, sem salvar
- Geração de JWT no login
- `BadCredentialsException` para senha errada

**`OrderServiceTest` — 15 testes**
- Cadeia Geocoding → Route → Pricing
- Associação do pedido ao usuário autenticado
- `BusinessException` para valor zero (não salva)
- Paginação com `ArgumentCaptor<PageRequest>`

**`UserServiceTest` — 12 testes**
- Mapeamento completo de todos os campos
- Campos nulos/brancos não sobrescrevem dados
- `UserNotFoundException` com SecurityContext inválido

### Técnicas de Mockito utilizadas

```java
// Simular retorno de método
when(repository.findById(id)).thenReturn(Optional.of(user));

// Verificar que método foi chamado
verify(repository, times(1)).save(any(User.class));

// Verificar que método NUNCA foi chamado
verify(repository, never()).save(any());

// Capturar argumento passado para um mock
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(repository).save(captor.capture());
assertThat(captor.getValue().getCpf()).isEqualTo("12345678909");

// Simular lançamento de exceção
when(repository.existsByCpf("123")).thenReturn(true);
assertThatThrownBy(() -> service.register(dto))
    .isInstanceOf(DuplicateCpfException.class);
```

---

## 🖥️ Frontend Angular

### Fluxo de Autenticação

```
1. LoginComponent → AuthService.login() → POST /auth/login
2. Resposta: { token, userId, fullName }
3. Token salvo: localStorage['auth_token']
4. isAuthenticated$ emite true → Header atualiza menus
5. Usuário redirecionado para /user-area

Em cada requisição:
  AuthInterceptor adiciona: Authorization: Bearer {token}

Se API retornar 401:
  AuthInterceptor → AuthService.logout() → redireciona para /login
```

### AuthGuard — Proteção de Rotas

```typescript
// Rotas protegidas em app-routing.module.ts:
{ path: 'user-area',    component: UserAreaComponent,    canActivate: [AuthGuard] },
{ path: 'create-order', component: CreateOrderComponent, canActivate: [AuthGuard] },
{ path: 'my-orders',    component: MyOrdersComponent,    canActivate: [AuthGuard] },
{ path: 'update-user',  component: UpdateUserComponent,  canActivate: [AuthGuard] },
```

### Fluxo de Criação de Pedido (2 etapas)

```
Etapa 1 — Estimar:
  Preencher pickupAddress + deliveryAddress → clicar "Calcular"
  → OrderService.calculateEstimate() → POST /orders/calculate-estimate
  → Exibe distância, tempo estimado, valor

Etapa 2 — Confirmar:
  Preencher itemDescription → clicar "Confirmar Pedido"
  → OrderService.createOrder() → POST /orders
  → Backend recalcula o valor internamente
  → Pedido criado com status PENDING
```

---

## 🌐 APIs Externas

| API | URL | Função |
|-----|-----|--------|
| Nominatim | nominatim.openstreetmap.org | Geocodificação de endereços |
| OSRM | router.project-osrm.org | Cálculo de rotas e distâncias |

Ambas são **gratuitas**, sem chave de API, baseadas em OpenStreetMap.

---

## 🔐 Política de Senha

Regex de validação:
```
^(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$
```

Requisitos:
- ✅ Mínimo 6 caracteres
- ✅ 1 letra maiúscula
- ✅ 1 letra minúscula
- ✅ 1 caractere especial

Exemplos válidos: `Senha@123`, `Admin#456`

---

## ❗ Erros Comuns e Soluções

| Erro | Causa | Solução |
|------|-------|---------|
| `command not found: mvn` | Maven fora do PATH | `eval "$(/opt/homebrew/bin/brew shellenv)"` |
| `command not found: npm` | Node fora do PATH | `export PATH="/Users/bergsonmulato/.nvm/versions/node/v20.20.2/bin:$PATH"` |
| `cannot find symbol` no Maven | Lombok incompatível com Java 25 | Usar `JAVA_HOME` com openjdk@17 |
| `401 Unauthorized` | Token expirado | Fazer login novamente |
| `Distância excede máximo` | Rota > 30km | Usar endereços mais próximos |
| `CPF já cadastrado` | CPF duplicado | Usar outro CPF ou fazer login |
| `Endereço não encontrado` | API Nominatim não geocodificou | Endereço mais detalhado (cidade + estado) |
