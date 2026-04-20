# Persistencia em Arquivos

O sistema nao usa banco de dados. Toda a persistencia e feita em arquivos binarios.

## Cabecalho

Cada arquivo de entidade possui um cabecalho com:

- ultimo ID utilizado
- ponteiro para a lista de espacos livres

## Registro

Cada registro possui:

- lapide
- tamanho do registro
- dados serializados

## Exclusao logica

Ao excluir:

- a lapide passa a indicar remocao
- o espaco entra na lista de reaproveitamento

## Atualizacao

Se o novo registro cabe no mesmo espaco, ele e sobrescrito.

Se nao cabe:

- o registro antigo recebe lapide
- um novo espaco e obtido
- o indice primario e atualizado para o novo endereco

## Persistencia de relacionamentos

Os relacionamentos usam uma lista ligada persistente cujas cabecas sao localizadas por Hash Extensivel.
