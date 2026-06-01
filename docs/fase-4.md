# Fase IV - Compressao

## Objetivo

Aplicar compressao aos arquivos de dados do sistema sem alterar o funcionamento das fases anteriores. O CRUD, os indices e os relacionamentos continuam usando os arquivos originais em `storage/dados`; a compressao apenas gera arquivos de backup compactados.

## Arquivos gerados

Os compactados ficam em:

- `backend/src/storage/compactados/dados-huffman.cmp`
- `backend/src/storage/compactados/dados-lzw.cmp`

Cada arquivo compactado contem um pacote unico com todos os arquivos `.db` encontrados em `storage/dados`.

## Como executar

No terminal:

```bash
cd backend/src
javac $(find . -name "*.java")
java eventos.compression.CompressionCli
```

No PowerShell:

```powershell
cd backend/src
javac (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java eventos.compression.CompressionCli
```

Tambem e possivel gerar os arquivos pelo servidor:

```bash
POST http://localhost:8080/compressao/huffman
POST http://localhost:8080/compressao/lzw
```

## Resultado obtido na base atual

Base usada: arquivos `.db` atuais em `backend/src/storage/dados`.

### Huffman

- Tamanho do arquivo original: `649 bytes`
- Tamanho do arquivo comprimido: `1440 bytes`
- Calculo da taxa: `(1 - 1440 / 649) * 100 = -121,88%`
- Interpretacao: a taxa ficou negativa porque os dados atuais sao muito pequenos. O Huffman precisa gravar a tabela de frequencias dos 256 bytes para permitir a leitura futura do pacote; nessa base minima, esse cabecalho ficou maior que o ganho obtido nos dados.

### LZW

- Tamanho do arquivo original: `649 bytes`
- Tamanho do arquivo comprimido: `771 bytes`
- Calculo da taxa: `(1 - 771 / 649) * 100 = -18,80%`
- Interpretacao: a taxa tambem ficou negativa, mas menos que no Huffman, porque o LZW grava menos metadados fixos. Em bases pequenas, ainda ha pouco padrao repetido para compensar os codigos de 16 bits usados no compactado.

## Dificuldades e solucoes

- Gerar um unico compactado para varios arquivos: foi criado um pacote interno que grava caminho relativo, tamanho e bytes de cada `.db` antes de aplicar Huffman ou LZW.
- Preservar as fases anteriores: a compressao nao modifica `storage/dados` nem `storage/indices`; ela apenas le os arquivos e escreve em `storage/compactados`.
- Huffman em arquivos pequenos: a tabela de frequencias aumenta o arquivo final, mas foi mantida porque torna o compactado completo e autocontido.
- LZW com dados binarios: os bytes foram tratados como valores de `0` a `255`, e os codigos gerados sao gravados em 16 bits para manter uma representacao simples e estavel.
