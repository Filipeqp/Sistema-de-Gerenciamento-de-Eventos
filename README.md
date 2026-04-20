# GestEvent

Sistema academico de gerenciamento de eventos desenvolvido para AED III, com persistencia em arquivos binarios, indice primario por entidade, Hash Extensivel e interface web.

## Funcionalidades

- CRUD completo de eventos
- CRUD completo de participantes
- CRUD completo de palestrantes
- CRUD completo de inscricoes
- Exclusao logica com lapide
- Persistencia entre execucoes
- Indice primario persistente por entidade
- Hash Extensivel para acesso por chave primaria
- Indices relacionais para:
  - `Evento -> Palestrantes`
  - `Evento -> Inscricoes`
  - `Participante -> Inscricoes`
- Integridade referencial entre as entidades

## Arquitetura

O backend foi reorganizado em camadas:

- `eventos.model`: entidades do dominio
- `eventos.persistence`: manipulacao dos arquivos binarios
- `eventos.index`: Hash Extensivel e indices relacionais
- `eventos.dao`: acesso aos dados e integracao com indices
- `eventos.controller`: regras de negocio
- `eventos.server`: servidor HTTP

O frontend permanece em React com Vite, consumindo a API HTTP do backend.

## Estrutura de dados

### Persistencia binaria

Cada entidade possui:

- um arquivo de dados binario
- um cabecalho com ultimo ID e ponteiro para a lista de espacos removidos
- registros com lapide e tamanho

### Indice primario

Cada entidade possui um indice `id -> endereco` persistido em disco.

### Hash Extensivel

O indice primario usa Hash Extensivel com:

- diretorio persistente
- buckets com profundidade local
- duplicacao do diretorio quando necessario
- split de bucket em caso de overflow

### Indices de relacionamento

Os relacionamentos sao atendidos por listas ligadas persistentes com cabeca localizada por Hash Extensivel:

- palestrantes por evento
- inscricoes por evento
- inscricoes por participante

## Regras de negocio

- nao e permitido cadastrar palestrante em evento inexistente
- nao e permitido cadastrar inscricao com evento inexistente
- nao e permitido cadastrar inscricao com participante inexistente
- nao e permitido duplicar inscricao do mesmo participante no mesmo evento
- nao e permitido excluir evento com palestrantes ou inscricoes ativas
- nao e permitido excluir participante com inscricoes ativas
- apenas registros ativos sao listados

## Como executar

## Pre-requisitos

- Java JDK 17 ou superior instalado
- Node.js 20 ou superior instalado
- `npm` disponivel no terminal
- Porta `8080` livre para o backend
- Porta `5173` livre para o frontend

### Backend

```bash
cd backend/src
javac eventos/server/Main.java
java eventos.server.Main
```

Servidor:

```text
http://localhost:8080
```

Teste rapido:

```text
http://localhost:8080/health
```

Resposta esperada:

```json
{"status":"ok"}
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

## Ordem recomendada para rodar

1. Abra um terminal na pasta do projeto
2. Inicie o backend
3. Abra um segundo terminal
4. Inicie o frontend
5. Acesse a aplicacao no navegador

## Estrutura importante para execucao

- O backend principal esta em `backend/src/eventos/server/Main.java`
- Os dados binarios antigos ficam em `backend/src/dados`
- Os novos indices e arquivos auxiliares sao criados em `backend/src/storage` durante a execucao

## Endpoints

- `GET /eventos`
- `GET /eventos/{id}`
- `POST /eventos`
- `PUT /eventos/{id}`
- `DELETE /eventos/{id}`
- `GET /palestrantes`
- `GET /palestrantes?idEvento={idEvento}`
- `GET /participantes`
- `GET /inscricoes`
- `GET /inscricoes?idEvento={idEvento}`
- `GET /inscricoes?idParticipante={idParticipante}`

## Documentacao

Os arquivos em `docs/` incluem:

- `DCU.md`
- `DER.md`
- `arquitetura.md`
- `hash-extensivel.md`
- `persistencia.md`

## Observacoes

- O projeto antigo em classes soltas foi mantido no repositorio como referencia historica.
- A nova implementacao principal esta no pacote `eventos`.
