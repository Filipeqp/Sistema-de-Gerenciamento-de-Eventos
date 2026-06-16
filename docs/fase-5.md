# Fase 5 - Criptografia e Apresentacao Final

## Status

- `5a` implementada: login com criptografia XOR

## Objetivo

Adicionar autenticacao simples ao sistema armazenando a senha do participante em formato criptografado com `XOR`.

## Estrutura implementada

Novas classes:

- `eventos.security.XorCipher`
- `eventos.security.AuthService`
- `eventos.controller.AuthController`

Arquivos ajustados:

- `eventos.model.Participante`
- `eventos.controller.ParticipanteController`
- `eventos.server.Main`
- frontend com `LoginPage`

## Como funciona

### Cadastro

Ao cadastrar ou editar um participante, a interface agora exige:

- `nome`
- `email`
- `interesses`
- `senha`

A senha digitada nao e gravada em texto puro. Antes de persistir no arquivo binario, o backend aplica:

1. XOR byte a byte usando uma chave fixa do sistema
2. codificacao `Base64` para armazenamento seguro em string

### Login e primeiro acesso

Endpoint:

- `GET /auth/status`
- `POST /auth/login`
- `POST /auth/primeiro-acesso`
- `POST /auth/register`

Payload:

```json
{
  "email": "usuario@exemplo.com",
  "senha": "1234"
}
```

Se as credenciais estiverem corretas, o backend devolve os dados basicos da sessao:

```json
{
  "id": 1,
  "nome": "Maria",
  "email": "maria@email.com",
  "interesses": "java, dados",
  "autenticado": true
}
```

## Frontend

O frontend agora abre pela tela de `Login`.

Fluxo:

1. frontend consulta `GET /auth/status`
2. a tela inicial oferece dois modos: `Entrar` e `Criar conta`
3. se ainda nao existir usuario com senha, o primeiro cadastro usa `POST /auth/primeiro-acesso`
4. se ja existir usuario, novos cadastros usam `POST /auth/register`
5. o login usa `POST /auth/login`
6. sessao autenticada e salva em `localStorage`
7. aplicacao principal e liberada
8. o botao `Sair` encerra a sessao local

## Compatibilidade com dados existentes

O modelo `Participante` foi ajustado para ler registros antigos que ainda nao tinham o campo `senha`. Nesses casos, a senha fica vazia ate que o participante seja editado e salvo novamente com senha.

## Como testar a fase 5a

### Preparacao

1. Inicie o backend:

```powershell
cd backend/src
javac (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java eventos.server.Main
```

2. Inicie o frontend:

```powershell
cd frontend
npm run dev
```

3. Abra `http://localhost:5173`.

### Teste do primeiro cadastro

1. Se ainda nao existir usuario com senha, a tela deve abrir em `Criar conta`.
2. Preencha:
   - nome
   - email
   - interesses
   - senha
3. Clique em `Criar e entrar`.
4. Verifique que o sistema entra automaticamente.

### Teste de logout e login

1. Clique em `Sair`.
2. Volte para a tela inicial.
3. Entre com o mesmo email e senha.
4. Verifique que o acesso e liberado novamente.

### Teste de novo cadastro na tela inicial

1. Na tela inicial, clique em `Criar conta`.
2. Cadastre outro usuario com email diferente.
3. Verifique que a nova conta tambem entra no sistema sem precisar estar logado antes.

### Teste de erro

1. Tente entrar com senha errada.
2. Verifique a mensagem `Credenciais invalidas`.
3. Tente criar nova conta com email ja existente.
4. Verifique a mensagem de conflito de email.

### Teste pela API

- `GET http://localhost:8080/auth/status`
- `POST http://localhost:8080/auth/login`
- `POST http://localhost:8080/auth/primeiro-acesso`
- `POST http://localhost:8080/auth/register`

Exemplo de payload para cadastro:

```json
{
  "nome": "Maria",
  "email": "maria@email.com",
  "interesses": "java, backend",
  "senha": "1234"
}
```

### Arquivos e classes para mostrar

- `backend/src/eventos/security/XorCipher.java`
- `backend/src/eventos/security/AuthService.java`
- `backend/src/eventos/controller/AuthController.java`
- `backend/src/eventos/model/Participante.java`
- `frontend/src/pages/LoginPage.jsx`
- `frontend/src/api.js`

## O que mostrar no video

1. Abrir o sistema e mostrar a tela inicial com `Entrar` e `Criar conta`.
2. Se a base estiver vazia, criar o primeiro usuario.
3. Mostrar que o sistema entra automaticamente.
4. Clicar em `Sair`.
5. Fazer login com o mesmo email e senha.
6. Voltar para a tela inicial e criar uma segunda conta.
7. Mostrar que nao e necessario estar logado para criar outro acesso.
8. Demonstrar erro de senha incorreta.
9. Demonstrar erro ao tentar cadastrar email repetido.

## Roteiro rapido de video para a fase 5a

1. Mostrar a tela inicial do sistema.
2. Apontar os modos `Entrar` e `Criar conta`.
3. Criar uma conta nova.
4. Mostrar a entrada automatica no sistema.
5. Sair e entrar novamente com o mesmo usuario.
6. Tentar entrar com senha errada para mostrar a validacao.
7. Abrir rapidamente os arquivos:
   - `XorCipher.java`
   - `AuthService.java`
   - `AuthController.java`
   - `LoginPage.jsx`
8. Se quiser reforcar a persistencia, mostrar o arquivo de participantes sendo usado pelo sistema em `backend/src/storage/dados/participantes`.
