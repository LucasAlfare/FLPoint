# API de Registro de Ponto com Kotlin Ktor e Exposed

## 1. Definição de Requisitos

- Usuários podem se cadastrar, fazer login e enviar registros de ponto.
- Validações:
  - Restringir envio de registros de ponto dentro de um intervalo de tempo configurado.
  - Limitar registros a uma faixa horária específica.
- Administração:
  - Usuários administrativos podem visualizar, editar e excluir registros de ponto.
- Banco de dados para armazenar informações dos usuários e seus registros de ponto.

## 2. Arquitetura e Stack

- **Backend**: Kotlin com Ktor para as rotas HTTP.
- **Banco de Dados**: PostgreSQL (ou outro) manipulado com Exposed ORM.
- **Autenticação**: JWT (JSON Web Tokens) para autenticação de usuários.
- **Validações**: Custom middleware ou validações dentro dos controllers.
- **Administração**: Definição de permissões para diferentes níveis de usuários (normal vs administrador).

## 3. Banco de Dados (Exposed)

- **Tabelas**:
  - `Users`: tabela para armazenar informações dos usuários (id, nome, email, senha, tipo de usuário).
  - `TimeEntries`: tabela para armazenar registros de ponto (id, user_id, horário do ponto, status).

- **Modelos**:
  - `User`: usuário com nível de permissão (admin ou comum).
  - `TimeEntry`: representação de um registro de ponto, contendo horário e status.

## 4. Autenticação e Autorização

- **Cadastro e Login**:
  - Usuário poderá se cadastrar e fazer login.
  - Geração de JWT token ao fazer login para autenticação.

- **Middleware de Autorização**:
  - Verificar o token JWT nas rotas que precisam de autenticação.
  - Autenticar diferentes permissões de usuários (usuário comum vs administrador).

## 5. Validações de Registro de Ponto

- **Regras**:
  - Verificar se o último registro de ponto foi feito dentro de um intervalo de tempo mínimo (XX minutos).
  - Validar se a solicitação está dentro de um intervalo de horas permitido (por exemplo, das 08h às 18h).

- **Erros**:
  - Caso uma regra seja violada, retornar erro apropriado ao cliente (ex: 400 Bad Request).

## 6. Rotas

- **Autenticação**:
  - POST `/register`: Cadastro de novo usuário.
  - POST `/login`: Login e geração de JWT.

- **Registros de Ponto (Usuários comuns)**:
  - POST `/clock-in`: Registrar ponto (com validações de horário e intervalo).
  - GET `/entries`: Listar todos os registros de ponto do usuário autenticado.

- **Administração (Acesso restrito a administradores)**:
  - GET `/admin/entries`: Listar todos os registros de todos os usuários.
  - PUT `/admin/entries/{id}`: Atualizar um registro de ponto.
  - DELETE `/admin/entries/{id}`: Excluir um registro de ponto.

## 7. Camadas do Projeto (Clean Architecture)

- **Domain**:
  - Entidades: `User`, `TimeEntry`.
  - Regras de negócio: Lógica de validação e gerenciamento de horários, permissões.

- **Use Cases**:
  - Implementação de casos de uso como cadastro, login, registro de ponto, validações de horário e permissões.

- **Infrastructure**:
  - **Ktor**: Definição das rotas HTTP, middlewares, validações de JWT.
  - **Exposed**: Manipulação do banco de dados para armazenar e recuperar informações.

## 8. Testes

- **Testes de Unidade**:
  - Testar as validações de intervalo de tempo e faixa horária.
  - Testar lógica de permissões de usuários.

- **Testes de Integração**:
  - Testar a integração das rotas com o banco de dados.
  - Testar o fluxo de autenticação com JWT.

## 9. Deployment

- Publicar Docker algum lugar.
- Pode ser com "Localtunnel", pra usar debugar externamente sem contratar serviço extra.