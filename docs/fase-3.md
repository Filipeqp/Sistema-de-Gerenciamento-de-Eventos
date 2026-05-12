# Fase 3 - Relacionamento N:N e Arvore B+

Esta fase esta implementada pelo relacionamento N:N entre `eventos` e `participantes`.

## Tabela intermediaria

A entidade `Inscricao` e a tabela intermediaria:

- `id`: chave primaria da inscricao
- `idEvento`: chave estrangeira para `Evento`
- `idParticipante`: chave estrangeira para `Participante`
- `dataInscricao`: atributo proprio do relacionamento

Assim, um evento pode ter varios participantes e um participante pode estar em varios eventos.

## Indices do relacionamento

O relacionamento tem indices persistentes em arquivo binario nos dois sentidos:

- `inscricoes_por_evento`: recupera todas as inscricoes de um evento
- `inscricoes_por_participante`: recupera todas as inscricoes de um participante

Esses indices sao implementados por `LinkedEntityListIndex`, com cabecas localizadas por Hash Extensivel.

Endpoints para demonstracao:

- `GET /inscricoes?idEvento=1`
- `GET /inscricoes?idParticipante=1`

## Listagem ordenada com Arvore B+

Todas as entidades possuem indice B+ persistente para listagem ordenada. Na fase 3, a demonstracao principal pode ser feita em:

- `GET /eventos?ordenacao=bplus&direcao=asc`
- `GET /eventos?ordenacao=bplus&direcao=desc`
- `GET /inscricoes?ordenacao=bplus&direcao=asc`
- `GET /inscricoes?idEvento=1&ordenacao=bplus&direcao=asc`

No frontend, as telas de Eventos e Inscricoes possuem seletores de ordenacao marcados com `(B+)`.

## O que mostrar no video

1. Cadastrar pelo menos dois eventos.
2. Cadastrar pelo menos dois participantes.
3. Criar inscricoes ligando participantes a eventos.
4. Filtrar inscricoes por evento e por participante.
5. Mostrar que os arquivos binarios sao criados em `backend/src/storage`.
6. Usar a ordenacao `(B+)` na tela de Eventos ou Inscricoes.
