# FL Points API

Backend RESTful desenvolvido em **Kotlin + Ktor** para gerenciamento de usuários e registro de “pontos” com autenticação JWT e controle de privilégios administrativos.

O projeto já está funcional e pronto para uso inicial em ambiente controlado (dev/staging), com suporte a SQLite, PostgreSQL e H2, além de containerização com Docker.

Por enquanto, não estou focando >>necessariamente<< em performance, afinal ainda não esbarrei em nenhum gargalo absurdo. Pretendo aferir isso depois que eu implementar mais coisas obrigatórias, como finalizar os testes unitários e algumas funcionalidades-chave.

---

## 📌 Visão Geral

A FL Points API permite:

* Cadastro de usuários (via administrador)
* Autenticação com JWT
* Registro de pontos por usuário
* Restrição de frequência mínima entre pontos (10 segundos)
* Controle de permissões (usuário comum vs administrador)
* Logout com blacklist de tokens
* Consulta de pontos individuais e globais
* Gestão administrativa de usuários

A aplicação é stateless e baseada em JWT, com blacklist persistida para invalidação de tokens.

---

## 🏗️ Stack Tecnológica

| Tecnologia    | Finalidade                      |
| ------------- | ------------------------------- |
| Kotlin 2.3    | Linguagem principal             |
| Ktor 3.4      | Framework HTTP                  |
| Exposed 1.1   | ORM / DSL SQL                   |
| HikariCP      | Pool de conexões                |
| SQLite        | Banco padrão em dev             |
| PostgreSQL    | Banco recomendado para produção |
| H2            | Banco em memória para testes    |
| JWT (HMAC256) | Autenticação                    |
| BCrypt        | Hash seguro de senha            |
| Logback       | Logging                         |
| Docker        | Containerização                 |
| Gradle 8      | Build                           |

---

## 🧠 Regras de Negócio

### Usuários

* Email único
* Senha mínima configurável (default: 4 caracteres)
* Senha armazenada com BCrypt
* Usuários podem ser administradores

### Pontos

* Cada ponto pertence a um único usuário
* Um novo ponto só pode ser criado se o último tiver sido registrado há pelo menos **10 segundos**
* Timestamp baseado em `Clock.System.now()`

### Autenticação

* JWT com:

    * `id`
    * `is_admin`
    * `expiresAt`
* Expiração padrão: **10 minutos**
* Logout adiciona token à blacklist
* Tokens expirados são removidos automaticamente

---

## 🗂️ Modelagem de Dados

### Users

| Campo           | Tipo    | Observações          |
| --------------- | ------- | -------------------- |
| id              | Int     | PK                   |
| name            | String  | Obrigatório          |
| email           | String  | Único                |
| hashed_password | String  | BCrypt               |
| time_zone       | String  | TimeZone serializado |
| is_admin        | Boolean | Default: false       |

---

### Points

| Campo           | Tipo      | Observações         |
| --------------- | --------- | ------------------- |
| id              | Int       | PK                  |
| related_user_id | Int       | FK → Users          |
| instant         | Timestamp | Momento do registro |

---

### JwtBlacklist

| Campo      | Tipo      | Observações |
| ---------- | --------- | ----------- |
| id         | Int       | PK          |
| jwt        | Text      | Único       |
| expires_at | Timestamp | Indexado    |

---

## 🔐 Fluxo de Autenticação

1. Login com email e senha
2. Recebimento de JWT
3. Envio do token via header:

```
Authorization: Bearer <token>
```

4. Middleware valida:

    * Assinatura
    * Expiração
    * Presença na blacklist
    * Claims obrigatórias

---

## 🚀 Endpoints

### Público

#### Health Check

```
GET /health
```

#### Login

```
POST /login
```

Body:

```json
{
  "email": "user@dev.com",
  "plainPassword": "123456"
}
```

Resposta:

```json
{
  "jwt": "token...",
  "userDTO": {
    "id": 2,
    "name": "Dev User",
    "email": "user@dev.com",
    "timeZone": "America/Sao_Paulo",
    "isAdmin": false
  }
}
```

---

## 🔒 Rotas Autenticadas (Usuário)

### Criar ponto

```
POST /user/point
```

Resposta:

```
201 Created
<id do ponto>
```

---

### Listar meus pontos

```
GET /user/points
```

---

### Atualizar senha

```
PATCH /user/update-password
```

Body:

```json
{
  "currentPlainPassword": "123456",
  "newPlainPassword": "654321"
}
```

---

### Logout

```
POST /user/logout
```

---

## 👑 Rotas Administrativas

Requer `isAdmin = true`.

### Registrar usuário

```
POST /admin/register
```

### Listar usuários

```
GET /admin/users
```

### Deletar usuário

```
DELETE /admin/users/{id}
```

### Listar todos os pontos

```
GET /admin/points
```

---

## ⚙️ Execução Local (Dev)

Por padrão:

* Banco: SQLite local
* Porta: 7171
* JWT Secret: `dev-secret`

### Usuários criados automaticamente:

| Tipo  | Email                                 | Senha  |
| ----- | ------------------------------------- | ------ |
| Admin | [admin@dev.com](mailto:admin@dev.com) | 123456 |
| User  | [user@dev.com](mailto:user@dev.com)   | 123456 |

Rodar com Gradle:

```
./gradlew run
```

Ou gerar JAR:

```
./gradlew assemble
java -jar server.jar
```

---

## 🐳 Execução com Docker

Build:

```
docker build -t fl-points .
```

Run:

```
docker run -p 7171:7171 fl-points
```

Ou com docker-compose:

```
docker compose up --build
```

---

## 🌎 Variáveis de Ambiente (Produção)

| Variável                  | Obrigatória |
| ------------------------- | ----------- |
| APP_ENV=prod              | Sim         |
| DATABASE_JDBC_URL         | Sim         |
| DATABASE_JDBC_CLASS_NAME  | Sim         |
| DATABASE_USERNAME         | Sim         |
| DATABASE_PASSWORD         | Sim         |
| WEBSERVER_PORT            | Sim         |
| JWT_ALGORITHM_SIGN_SECRET | Sim         |

---

## 🧱 Arquitetura

* Camada HTTP (Ktor)
* Camada de Usecases
* Camada de Persistência (DataCRUD)
* Implementação via Exposed
* Banco desacoplado via interface
* JWT stateless + blacklist persistida
* Tratamento centralizado de exceções (StatusPages)

---

## 🛡️ Segurança Atual

✔ BCrypt
✔ JWT com expiração
✔ Blacklist persistida
✔ Controle de privilégio admin
✔ Tratamento centralizado de erro
✔ Pool de conexões
✔ CORS configurado

---

# 📌 O que falta para uso real em produção

Embora funcional, para uso real corporativo seriam necessários:

---

## 1️⃣ Infraestrutura

* Banco dedicado (PostgreSQL recomendado)
* Backup automatizado
* Migração versionada de schema (Flyway ou Liquibase)
* Observabilidade (Prometheus + Grafana)
* Logs estruturados (JSON)
* Rate limiting
* Reverse proxy (NGINX)
* HTTPS obrigatório

---

## 2️⃣ Segurança

* Refresh tokens
* Rotação de secret JWT
* Política de senha forte
* Confirmação de email
* Recuperação de senha
* Proteção contra brute force
* Auditoria de ações administrativas
* Criptografia em repouso (se exigido)

---

## 3️⃣ Compliance & Burocracias

Dependendo do contexto real:

* Adequação à LGPD
* Política de privacidade
* Termos de uso
* Consentimento explícito de dados
* Controle de retenção de dados
* Exportação de dados do usuário
* Direito ao esquecimento
* Registro formal de controlador/processador
* Logs de auditoria imutáveis

---

## 4️⃣ Funcionalidades Interessantes

* Solicitação de ponto esquecido (com justificativa)
* Aprovação de ponto manual por admin
* Histórico auditável de alterações
* Relatórios por período
* Dashboard administrativo
* Cache de consultas
* Paginação
* Filtros por data
* Webhooks
* Sistema de notificações
* Multi-organização (multi-tenant)
* Controle de fuso horário por usuário no relatório

---

## 5️⃣ Melhorias Técnicas

* Separar arquivo gigante em módulos
* Introduzir testes automatizados
* Introduzir camada de service explícita
* DTOs separados do domínio
* Introduzir Result/Either ao invés de exceptions
* Documentação OpenAPI/Swagger
* Versionamento de API (/v1)

---

# 📈 Status do Projeto

✔ MVP funcional
✔ Autenticação implementada
✔ Controle de privilégio
✔ Persistência configurável
✔ Docker-ready

**Pronto para ambiente controlado / laboratório / protótipo real.**

# [LICENSE](LICENSE)

Check the linked file.