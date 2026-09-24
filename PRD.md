# PRD — Encurtador de URL

## 1. Visão Geral

### Produto
**URL Shortener**

### Descrição

Aplicação web para criação e gerenciamento de URLs encurtadas.

O sistema permitirá que usuários criem uma conta, realizem autenticação e gerenciem suas próprias URLs encurtadas.

Cada URL cadastrada pertencerá ao usuário responsável por sua criação. As operações de criação, consulta, alteração e exclusão serão protegidas por autenticação JWT.

O acesso a uma URL curta será público e redirecionará o visitante para a URL original sem exigir autenticação.

### Stack

**Backend**
- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Bean Validation
- API RESTful
- Banco de dados relacional

**Frontend**
- React
- TypeScript
- Vite
- React Router
- Fetch API ou Axios

---

# 2. Objetivo

Construir uma aplicação Full Stack que permita:

- Criar usuários.
- Autenticar usuários.
- Controlar sessões utilizando JWT.
- Renovar tokens de acesso.
- Encurtar URLs.
- Gerenciar URLs pertencentes ao usuário autenticado.
- Redirecionar URLs curtas publicamente.

Exemplo:

```text
Usuário
   ↓
Login
   ↓
Access Token + Refresh Token
   ↓
Cria URL
   ↓
https://example.com/very/long/url
   ↓
http://localhost:8080/Ab3xP9
```

---

# 3. Escopo

A primeira versão deverá permitir:

1. Criar usuário.
2. Realizar login.
3. Renovar Access Token.
4. Encurtar URL.
5. Consultar URL.
6. Listar URLs do usuário autenticado.
7. Alterar URL.
8. Excluir URL.
9. Redirecionar utilizando uma URL curta.

Não fazem parte do escopo inicial:

- Recuperação de senha.
- Confirmação de e-mail.
- Login social.
- Perfis administrativos.
- Planos e pagamentos.
- Domínios personalizados.
- Analytics avançado.
- QR Code.

---

# 4. Entidades

## 4.1 User

Representa um usuário da aplicação.

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador único |
| `name` | String | Nome do usuário |
| `email` | String | E-mail utilizado no login |
| `passwordHash` | String | Hash da senha |
| `createdAt` | LocalDateTime | Data de criação |
| `updatedAt` | LocalDateTime | Última alteração |

O `email` deverá possuir restrição `UNIQUE`.

A senha nunca deverá ser armazenada em texto puro.

---

## 4.2 ShortUrl

Representa uma URL encurtada pertencente a um usuário.

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador |
| `originalUrl` | String | URL original |
| `shortCode` | String | Código curto |
| `userId` | UUID | Usuário proprietário |
| `createdAt` | LocalDateTime | Data de criação |
| `updatedAt` | LocalDateTime | Última alteração |

Relacionamento:

```text
User
 │
 │ 1
 │
 │
 │ N
 ▼
ShortUrl
```

Um usuário poderá possuir várias URLs.

Uma URL deverá pertencer a apenas um usuário.

---

## 4.3 RefreshToken

Representa uma sessão que poderá ser utilizada para gerar novos Access Tokens.

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador |
| `tokenHash` | String | Hash do Refresh Token |
| `userId` | UUID | Usuário proprietário |
| `expiresAt` | LocalDateTime | Expiração |
| `revokedAt` | LocalDateTime? | Data de revogação |
| `createdAt` | LocalDateTime | Data de criação |

Relacionamento:

```text
User
 │
 │ 1
 │
 │
 │ N
 ▼
RefreshToken
```

Isso permite que um usuário possua múltiplas sessões, por exemplo, navegador e dispositivos diferentes.

---

# 5. RF01 — Criar Usuário

O sistema deverá permitir o cadastro de novos usuários.

### Endpoint

```http
POST /api/auth/register
```

### Entrada

```json
{
  "name": "Gustavo",
  "email": "gustavo@example.com",
  "password": "StrongPassword123"
}
```

### Comportamento

O sistema deverá:

1. Validar os dados.
2. Verificar se o e-mail já está cadastrado.
3. Gerar o hash da senha.
4. Criar o usuário.
5. Nunca retornar a senha ou seu hash.

### Resposta

```json
{
  "id": "49b2a39e-35b0-4e42-b502-a987e790b54f",
  "name": "Gustavo",
  "email": "gustavo@example.com",
  "createdAt": "2026-09-23T12:00:00"
}
```

Status:

`201 Created`

E-mail existente:

`409 Conflict`

Dados inválidos:

`400 Bad Request`

---

# 6. RF02 — Login

O usuário deverá conseguir autenticar utilizando e-mail e senha.

### Endpoint

```http
POST /api/auth/login
```

### Entrada

```json
{
  "email": "gustavo@example.com",
  "password": "StrongPassword123"
}
```

### Fluxo

```text
email + password
       ↓
Buscar usuário
       ↓
Validar senha
       ↓
Gerar Access Token
       +
Gerar Refresh Token
       ↓
Retornar tokens
```

### Resposta

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "df7a882a...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Credenciais inválidas:

`401 Unauthorized`

---

# 7. Access Token

O Access Token será um JWT utilizado para acessar recursos protegidos.

O token deverá possuir tempo de vida curto.

Exemplo conceitual de payload:

```json
{
  "sub": "49b2a39e-35b0-4e42-b502-a987e790b54f",
  "email": "gustavo@example.com",
  "iat": 1790179200,
  "exp": 1790180100
}
```

O `sub` deverá identificar o usuário.

O frontend enviará o token:

```http
Authorization: Bearer <accessToken>
```

O backend deverá validar:

- Assinatura.
- Expiração.
- Estrutura do token.
- Identidade do usuário.

---

# 8. RF03 — Renovação de Token

Quando o Access Token expirar, o frontend poderá solicitar um novo utilizando o Refresh Token.

### Endpoint

```http
POST /api/auth/refresh
```

### Entrada

```json
{
  "refreshToken": "df7a882a..."
}
```

### Fluxo

```text
Refresh Token
      ↓
Calcular hash
      ↓
Buscar token armazenado
      ↓
Existe?
      ↓
Não expirou?
      ↓
Não foi revogado?
      ↓
Gerar novos tokens
```

### Resposta

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

A aplicação deverá utilizar **Refresh Token Rotation**.

Isso significa que, após utilizar um Refresh Token, ele será revogado e um novo Refresh Token será criado.

```text
Refresh Token A
      ↓
POST /refresh
      ↓
Revoga A
      ↓
Access Token B
+
Refresh Token B
```

Um Refresh Token inválido, expirado ou revogado deverá resultar em:

`401 Unauthorized`

---

# 9. RF04 — Encurtar URL

Somente usuários autenticados poderão criar URLs.

### Endpoint

```http
POST /api/urls
Authorization: Bearer <accessToken>
```

### Entrada

```json
{
  "originalUrl": "https://example.com/very/long/url"
}
```

O backend deverá obter o `userId` através do JWT.

O cliente **não deverá enviar o `userId`**.

```text
JWT
 ↓
userId
 ↓
ShortUrl.userId
```

### Resposta

```json
{
  "id": "c514908e-26ca-448d-bd19-e327c8649308",
  "originalUrl": "https://example.com/very/long/url",
  "shortCode": "Ab3xP9",
  "shortUrl": "http://localhost:8080/Ab3xP9",
  "createdAt": "2026-09-23T12:00:00"
}
```

Status:

`201 Created`

---

# 10. RF05 — Consultar URL

```http
GET /api/urls/{id}
Authorization: Bearer <accessToken>
```

O usuário somente poderá consultar URLs pertencentes à sua conta.

Uma estratégia recomendada é buscar diretamente por:

```text
id + userId
```

em vez de buscar apenas pelo `id`.

---

# 11. RF06 — Listar URLs

```http
GET /api/urls
Authorization: Bearer <accessToken>
```

O endpoint deverá retornar somente URLs pertencentes ao usuário autenticado.

Exemplo:

```json
[
  {
    "id": "c514908e-26ca-448d-bd19-e327c8649308",
    "originalUrl": "https://google.com",
    "shortCode": "aBc123",
    "shortUrl": "http://localhost:8080/aBc123"
  }
]
```

Posteriormente poderá ser adicionada paginação:

```http
GET /api/urls?page=0&size=20
```

---

# 12. RF07 — Alterar URL

```http
PUT /api/urls/{id}
Authorization: Bearer <accessToken>
```

Entrada:

```json
{
  "originalUrl": "https://new-example.com"
}
```

Somente o proprietário da URL poderá alterá-la.

O `shortCode` deverá permanecer o mesmo.

---

# 13. RF08 — Excluir URL

```http
DELETE /api/urls/{id}
Authorization: Bearer <accessToken>
```

Somente o proprietário poderá excluir a URL.

Resposta:

`204 No Content`

---

# 14. RF09 — Redirecionar

O redirecionamento será **público**.

Não será necessário possuir JWT.

### Endpoint

```http
GET /{shortCode}
```

Exemplo:

```text
GET /Ab3xP9
     ↓
Buscar ShortUrl
     ↓
https://example.com
     ↓
HTTP 302 Found
```

Resposta:

```http
HTTP/1.1 302 Found
Location: https://example.com
```

---

# 15. Autorização das Rotas

A configuração do Spring Security deverá seguir:

| Endpoint | Autenticação |
|---|---|
| `POST /api/auth/register` | Pública |
| `POST /api/auth/login` | Pública |
| `POST /api/auth/refresh` | Pública* |
| `GET /{shortCode}` | Pública |
| `POST /api/urls` | JWT |
| `GET /api/urls` | JWT |
| `GET /api/urls/{id}` | JWT |
| `PUT /api/urls/{id}` | JWT |
| `DELETE /api/urls/{id}` | JWT |

`/refresh` não exige Access Token, mas exige um Refresh Token válido.

---

# 16. Endpoints da API

```text
AUTH

POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh


URLS

POST   /api/urls
GET    /api/urls
GET    /api/urls/{id}
PUT    /api/urls/{id}
DELETE /api/urls/{id}


REDIRECT

GET    /{shortCode}
```

---

# 17. Arquitetura Backend

Estrutura sugerida:

```text
src/main/java/com/example/urlshortener/

├── controller/
│   ├── AuthController.java
│   ├── UrlController.java
│   └── RedirectController.java
│
├── service/
│   ├── AuthService.java
│   ├── TokenService.java
│   └── UrlService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── RefreshTokenRepository.java
│   └── UrlRepository.java
│
├── entity/
│   ├── User.java
│   ├── RefreshToken.java
│   └── ShortUrl.java
│
├── dto/
│   ├── auth/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── AuthResponse.java
│   │
│   └── url/
│       ├── CreateUrlRequest.java
│       ├── UpdateUrlRequest.java
│       └── UrlResponse.java
│
├── security/
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UserNotFoundException.java
│   └── UrlNotFoundException.java
│
└── UrlShortenerApplication.java
```

---

# 18. Modelo de Dados

```text
┌─────────────────────┐
│        User         │
├─────────────────────┤
│ id                  │
│ name                │
│ email               │
│ passwordHash        │
│ createdAt           │
│ updatedAt           │
└──────────┬──────────┘
           │
           │ 1
           │
     ┌─────┴───────────────┐
     │                     │
     │ N                   │ N
     ▼                     ▼
┌──────────────────┐ ┌───────────────────┐
│     ShortUrl     │ │   RefreshToken    │
├──────────────────┤ ├───────────────────┤
│ id               │ │ id                │
│ originalUrl      │ │ tokenHash         │
│ shortCode        │ │ userId            │
│ userId           │ │ expiresAt         │
│ createdAt        │ │ revokedAt         │
│ updatedAt        │ │ createdAt         │
└──────────────────┘ └───────────────────┘
```

---

# 19. Frontend

O frontend passa a possuir as seguintes páginas:

```text
/login
/register
/urls
```

## Register

Campos:

- Nome
- E-mail
- Senha
- Confirmar senha

Após cadastro bem-sucedido, o usuário poderá ser direcionado para o login.

## Login

Campos:

- E-mail
- Senha

Fluxo:

```text
Login
 ↓
POST /api/auth/login
 ↓
Access Token
+
Refresh Token
 ↓
Sessão autenticada
 ↓
/urls
```

## Gerenciamento de URLs

A página `/urls` será protegida e permitirá:

- Encurtar URL.
- Listar URLs.
- Copiar URL curta.
- Editar URL.
- Excluir URL.

---

# 20. Fluxo Geral de Autenticação

```text
              ┌──────────────┐
              │   Register   │
              └──────┬───────┘
                     │
                     ▼
                   User
                     │
                     ▼
              ┌──────────────┐
              │    Login     │
              └──────┬───────┘
                     │
              email + password
                     │
                     ▼
              AuthService
                     │
                     ▼
          ┌─────────────────────┐
          │ Access Token (JWT)  │
          │ Refresh Token       │
          └──────────┬──────────┘
                     │
                     ▼
              Protected API
                     │
              JWT validation
                     │
                     ▼
                  userId
                     │
                     ▼
               User's URLs
```

---

# 21. Segurança

A implementação deverá seguir algumas regras fundamentais:

- Senhas nunca poderão ser armazenadas em texto puro.
- Utilizar algoritmo adequado de password hashing, como BCrypt.
- A chave utilizada para assinatura do JWT deverá ficar fora do código-fonte.
- Access Tokens deverão possuir duração curta.
- Refresh Tokens deverão possuir duração maior.
- Refresh Tokens deverão ser revogáveis.
- Refresh Tokens armazenados no banco deverão ser protegidos por hash.
- O backend nunca deverá confiar em um `userId` enviado pelo frontend para determinar propriedade de recursos.
- O usuário autenticado deverá ser identificado através do JWT.
- Um usuário não poderá consultar, editar ou excluir URLs pertencentes a outro usuário.

---

# 22. Critérios de Aceite

O MVP será considerado concluído quando:

- Um usuário puder criar uma conta.
- Não for possível cadastrar dois usuários com o mesmo e-mail.
- A senha for armazenada utilizando hash.
- O usuário puder realizar login.
- Credenciais inválidas retornarem `401`.
- Login válido gerar Access Token e Refresh Token.
- O Access Token permitir acessar endpoints protegidos.
- Requisições sem autenticação para endpoints protegidos retornarem `401`.
- O Refresh Token permitir gerar um novo Access Token.
- Refresh Tokens expirados ou revogados forem rejeitados.
- Refresh Token Rotation estiver implementado.
- Cada URL pertencer a um usuário.
- O usuário visualizar somente suas próprias URLs.
- O usuário não conseguir editar ou excluir URLs de outro usuário.
- Uma URL válida puder ser encurtada.
- O `shortCode` for único.
- URLs puderem ser consultadas, listadas, alteradas e excluídas.
- O redirecionamento funcionar sem autenticação.
- URLs inexistentes retornarem `404`.

---

# 23. Ordem de Implementação

Para manter o desenvolvimento incremental:

```text
1. User
   ↓
2. UserRepository
   ↓
3. Cadastro
   ↓
4. Login
   ↓
5. JWT
   ↓
6. Spring Security
   ↓
7. RefreshToken
   ↓
8. Renovação de token
   ↓
9. ShortUrl
   ↓
10. CRUD de URLs
   ↓
11. Vincular URL → User
   ↓
12. Redirecionamento
   ↓
13. Frontend de autenticação
   ↓
14. Frontend de gerenciamento
```

Dessa maneira, autenticação e autorização já estarão funcionando antes da implementação do gerenciamento das URLs.