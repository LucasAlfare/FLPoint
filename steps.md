# Cronograma de Desenvolvimento da API de Registro de Ponto

## **Parte 1: Configuração Inicial**

- Instalar e configurar ambiente de desenvolvimento:
  - Kotlin, Ktor e Exposed.
  - Banco de dados PostgreSQL ou outro de sua escolha.
  - Ferramentas auxiliares (IntelliJ IDEA, Postman, etc.).
- Criar estrutura básica do projeto:
  - Inicializar projeto Kotlin com Ktor.
  - Configurar dependências do projeto (Ktor, Exposed, JWT).

## **Parte 2: Modelagem e Banco de Dados**

- Definir e criar as tabelas no banco de dados:
  - `Users` (id, nome, email, senha, tipo de usuário).
  - `TimeEntries` (id, user_id, horário do ponto, status).
- Modelar as entidades no código com Exposed:
  - `User` e `TimeEntry`.
- Testar conexão e migrações do banco de dados:
  - Verificar se as tabelas estão sendo criadas corretamente.

## **Parte 3: Autenticação (Cadastro e Login)**

- Criar rotas de autenticação:
  - POST `/register`: Cadastro de novo usuário.
  - POST `/login`: Login e geração de JWT.
- Implementar JWT para autenticação:
  - Geração e validação de tokens.
- Testar a funcionalidade de autenticação.

## **Parte 4: Middleware de Autorização**

- Criar middleware para verificar JWT nas rotas protegidas.
- Testar permissões básicas:
  - Acesso restrito a usuários autenticados.
  - Testar permissões diferentes (usuário normal vs administrador).

## **Parte 5: Implementação de Registro de Ponto**

- Criar rota para registrar ponto:
  - POST `/clock-in`: Registrar ponto para usuários autenticados.
- Implementar lógica básica para salvar o registro de ponto no banco de dados.

## **Parte 6: Validações de Registro de Ponto**

- Implementar validações:
  - Validação de intervalo de tempo mínimo entre registros.
  - Validação de faixa horária permitida para registros.
- Testar diferentes cenários de validações:
  - Registro válido e inválido (ex: fora do horário permitido, dentro do intervalo mínimo).

## **Parte 7: Listagem de Registros**

- Criar rota para listar registros do próprio usuário:
  - GET `/entries`: Listar todos os registros de ponto do usuário autenticado.
- Testar funcionalidade de listagem.

## **Parte 8: Rotas Administrativas**

- Criar rotas administrativas para CRUD de registros:
  - GET `/admin/entries`: Listar todos os registros de ponto.
  - PUT `/admin/entries/{id}`: Atualizar registro de ponto.
  - DELETE `/admin/entries/{id}`: Excluir registro de ponto.
- Testar CRUD de registros:
  - Testar se apenas usuários administradores têm acesso a essas rotas.

## **Parte 9: Testes de Unidade e Integração**

- Escrever testes de unidade:
  - Testar validações de intervalo e faixa horária.
  - Testar permissões de acesso (usuário normal vs admin).
- Escrever testes de integração:
  - Testar rotas de autenticação, registro de ponto e CRUD de registros.

## **Parte 10: Revisão e Deployment**

- Revisar o código, refatorar e melhorar a organização se necessário.
- Configurar CI/CD para deployment:
  - Escolher uma plataforma de hospedagem (Heroku, AWS, etc.).
  - Configurar integração contínua para facilitar o envio de atualizações.
- Testar a API em ambiente de produção.

## **Extras (se houver tempo)**

- Adicionar logs e monitoramento.
- Implementar notificações ou alertas de erros.
- Melhorar a segurança (ex: hashing de senhas, rate limiting).