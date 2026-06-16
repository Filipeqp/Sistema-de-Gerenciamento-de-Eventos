# Fase 4 - Compactacao e Casamento de Padroes

## Status

- `4a` implementada: compactacao com `Huffman` e `LZW`
- `4b` implementada: pesquisa por casamento de padroes com `Boyer-Moore` e `KMP`

## Parte A - Compactacao

### Objetivo

Aplicar compactacao aos arquivos de dados do sistema sem alterar o funcionamento das fases anteriores. O CRUD, os indices e os relacionamentos continuam usando os arquivos originais em `storage/dados`; a compactacao apenas gera arquivos de backup compactados.

### Arquivos gerados

- `backend/src/storage/compactados/dados-huffman.cmp`
- `backend/src/storage/compactados/dados-lzw.cmp`

Cada arquivo compactado contem um pacote unico com todos os arquivos `.db` encontrados em `storage/dados`.

### Como executar

```powershell
cd backend/src
javac (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java eventos.compression.CompressionCli
```

Tambem e possivel gerar os arquivos pelo servidor:

- `POST /compressao/huffman`
- `POST /compressao/lzw`

## Parte B - Casamento de Padroes

### Objetivo

Permitir a pesquisa textual sobre os registros do sistema usando dois algoritmos classicos:

- `BM` (`Boyer-Moore`)
- `KMP` (`Knuth-Morris-Pratt`)

### Estrutura implementada

Novos pacotes no backend:

- `eventos.pattern.PatternMatchAlgorithm`
- `eventos.pattern.BoyerMooreSearch`
- `eventos.pattern.KmpSearch`
- `eventos.pattern.PatternSearchService`
- `eventos.controller.PesquisaController`

### Como a busca funciona

O backend monta um texto pesquisavel para cada registro e executa o algoritmo escolhido sobre esse texto.

Campos considerados:

- `eventos`: nome, descricao, data e tags
- `palestrantes`: nome, mini curriculo, especialidades e id do evento
- `participantes`: nome, email e interesses
- `inscricoes`: id do evento, id do participante e data da inscricao

### Endpoint

`GET /pesquisa?algoritmo=bm&entidade=eventos&padrao=java`

Parametros:

- `algoritmo`: `bm` ou `kmp`
- `entidade`: `todos`, `eventos`, `palestrantes`, `participantes` ou `inscricoes`
- `padrao`: texto a ser procurado

### Exemplo de resposta

```json
{
  "algoritmo": "bm",
  "entidade": "eventos",
  "padrao": "java",
  "quantidade": 1,
  "resultados": [
    {
      "entidade": "eventos",
      "id": 3,
      "titulo": "Workshop Java",
      "totalOcorrencias": 2,
      "posicoes": [0, 26],
      "trecho": "Workshop Java | Introducao em Java"
    }
  ]
}
```

### Demonstracao pelo frontend

Foi criada a tela `Pesquisa`, onde e possivel:

- escolher `Boyer-Moore` ou `KMP`
- escolher a entidade alvo
- informar o padrao
- visualizar ocorrencias, posicoes e trecho encontrado

## Como testar a fase 4b

### Pelo sistema

1. Inicie o backend em `backend/src` com:

```powershell
javac (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java eventos.server.Main
```

2. Inicie o frontend em `frontend` com:

```powershell
npm run dev
```

3. Cadastre alguns dados que tenham texto facil de localizar:
   - evento: `Workshop Java`
   - descricao: `Introducao a backend com Java`
   - tags: `java, backend, api`
   - participante: interesses com `java`
   - palestrante: especialidade com `backend`
4. Acesse a tela `Pesquisa`.
5. Escolha `Boyer-Moore`, entidade `Todos` e pesquise `java`.
6. Repita com `KMP`.
7. Verifique que os resultados mostram:
   - entidade
   - id
   - quantidade de ocorrencias
   - posicoes
   - trecho encontrado

### Pela API

No navegador ou no Postman:

- `GET http://localhost:8080/pesquisa?algoritmo=bm&entidade=todos&padrao=java`
- `GET http://localhost:8080/pesquisa?algoritmo=kmp&entidade=todos&padrao=java`

### Arquivos e classes para mostrar

- `backend/src/eventos/pattern/BoyerMooreSearch.java`
- `backend/src/eventos/pattern/KmpSearch.java`
- `backend/src/eventos/pattern/PatternSearchService.java`
- `backend/src/eventos/controller/PesquisaController.java`
- `frontend/src/pages/PesquisaPage.jsx`

## O que mostrar no video

### Compactacao

1. Acessar a tela `Compressao`.
2. Gerar `Huffman`.
3. Gerar `LZW`.
4. Mostrar o caminho do arquivo gerado e a taxa de compactacao.

### Casamento de padroes

1. Acessar a tela `Pesquisa`.
2. Pesquisar uma palavra existente usando `Boyer-Moore`.
3. Repetir a mesma pesquisa usando `KMP`.
4. Mostrar que o sistema retorna a entidade, o registro, as posicoes e o trecho encontrado.

### Roteiro rapido de video para a fase 4b

1. Mostrar a tela `Pesquisa`.
2. Explicar que o sistema possui dois algoritmos: `BM` e `KMP`.
3. Pesquisar `java` com `Boyer-Moore`.
4. Mostrar os resultados na interface.
5. Repetir a mesma busca com `KMP`.
6. Abrir rapidamente os arquivos:
   - `BoyerMooreSearch.java`
   - `KmpSearch.java`
   - `PatternSearchService.java`
7. Mostrar o endpoint no navegador ou no Postman para reforcar a implementacao no backend.

## Dificuldades e solucoes

- Pesquisar em varias entidades com formatos diferentes: foi criado um servico unico que transforma cada registro em texto pesquisavel.
- Comparar os algoritmos usando a mesma base: a API usa os mesmos dados de entrada e so troca a estrategia de busca.
- Preservar a arquitetura do projeto: a busca foi adicionada em novas classes de servico e controller, sem acoplar a logica de algoritmo aos DAOs.
