# Arquitetura

O projeto foi organizado segundo MVC com DAO.

## Model

As classes em `backend/src/eventos/model` representam o dominio:

- `Evento`
- `Palestrante`
- `Participante`
- `Inscricao`

Todas implementam a interface `Record`, usada pela camada de persistencia.

## View

A interface esta em `frontend/`, usando React e CSS para consumo da API.

## Controller

Os controllers ficam em `backend/src/eventos/controller` e concentram:

- validacoes
- integridade referencial
- regras de exclusao
- serializacao das respostas

## DAO

Os DAOs ficam em `backend/src/eventos/dao` e cuidam de:

- operacoes CRUD
- integracao com o arquivo binario
- indice primario
- indices relacionais

## Infraestrutura

As classes em `eventos.persistence` e `eventos.index` compoem a infraestrutura de armazenamento:

- `BinaryRecordFile`
- `ExtensibleHashIndex`
- `LinkedEntityListIndex`
