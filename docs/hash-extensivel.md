# Hash Extensivel

O indice primario de cada entidade foi implementado com Hash Extensivel persistente.

## Estrutura

Cada indice possui:

- um arquivo de diretorio
- um arquivo de buckets

O diretorio armazena:

- profundidade global
- ponteiros para buckets

Cada bucket armazena:

- profundidade local
- quantidade de entradas
- pares `chave -> endereco`

## Operacao de busca

1. aplica-se a funcao de hash sobre a chave
2. usa-se a profundidade global para localizar a entrada do diretorio
3. acessa-se o bucket correspondente
4. procura-se a chave no bucket

## Operacao de insercao

1. localiza-se o bucket da chave
2. se houver espaco, grava-se a entrada
3. se houver overflow:
   - divide-se o bucket
   - se necessario, duplica-se o diretorio
   - redistribuem-se as entradas

## Vantagem no projeto

Essa estrutura evita varredura completa do arquivo de dados para operacoes por ID, reduzindo o custo de:

- busca
- atualizacao
- exclusao
