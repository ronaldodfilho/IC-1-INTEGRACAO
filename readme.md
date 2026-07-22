# Integração com Dados Abertos da ANS

Projeto em Java desenvolvido para a etapa de integração com os Dados Abertos da Agência Nacional de Saúde Suplementar (ANS).

O programa identifica os arquivos dos três trimestres mais recentes, realiza o download e a extração dos arquivos ZIP, processa os dados contábeis e gera um CSV consolidado com as despesas das operadoras.

## Sobre o projeto

Este projeto faz parte de um teste técnico dividido em diferentes etapas. Este repositório corresponde somente à etapa de **integração com os Dados Abertos da ANS**.

O objetivo é automatizar parte do processo de obtenção e consolidação das demonstrações contábeis disponibilizadas pela ANS.

No estado atual, o projeto:

* Acessa a área pública de demonstrações contábeis da ANS;
* Identifica os anos disponíveis;
* Localiza os arquivos ZIP de cada ano;
* Seleciona os arquivos pertencentes aos três trimestres mais recentes;
* Faz o download e a extração dos arquivos;
* Lê um cadastro local de operadoras;
* Filtra contas contábeis relacionadas a despesas com eventos e sinistros;
* Agrupa os valores por CNPJ, ano e trimestre;
* Gera um arquivo CSV consolidado;
* Compacta o resultado em um arquivo ZIP.

O CSV gerado possui as colunas:

```text
CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas
```

## Funcionalidades implementadas

* Acesso às páginas públicas da ANS por meio de requisições HTTP;
* Leitura do HTML das páginas utilizando Jsoup;
* Identificação dos diretórios de anos disponíveis;
* Identificação de links para arquivos com extensão `.zip`;
* Extração do número do trimestre a partir do nome do arquivo;
* Ordenação dos arquivos por ano e trimestre;
* Seleção dos três trimestres mais recentes;
* Inclusão de mais de um arquivo quando eles pertencem ao mesmo trimestre;
* Download dos arquivos ZIP para a pasta `downloads`;
* Extração automática dos arquivos ZIP;
* Criação das pastas necessárias durante a execução;
* Leitura do cadastro local de operadoras;
* Associação entre Registro ANS, CNPJ e razão social;
* Identificação das colunas pelo nome do cabeçalho;
* Detecção simples de arquivos separados por ponto e vírgula ou vírgula;
* Leitura dos arquivos contábeis linha por linha;
* Filtro de contas contábeis iniciadas por `411`;
* Conversão de valores numéricos;
* Identificação do trimestre a partir do mês informado na data;
* Soma das despesas por CNPJ, ano e trimestre;
* Geração do arquivo `downloads/consolidado.csv`;
* Geração do arquivo `consolidado_despesas.zip`.



## Estrutura do projeto

```text
.
├── Main.java
├── AnsService.java
├── ArquivoTrimestre.java
├── ProcessadorService.java
├── OperadoraCadastro.java
├── ConsolidaDTO.java
├── ZipService.java
├── recursos/
│   └── Relatorio_cadop.csv
└── downloads/
    ├── arquivos ZIP baixados
    ├── extraídos/
    └── consolidado.csv
```

A pasta `downloads` e suas subpastas são criadas durante a execução quando necessário.

### `Main.java`

Contém o ponto de entrada do programa e coordena as etapas:

1. Busca dos arquivos;
2. Download;
3. Extração;
4. Carregamento do cadastro;
5. Processamento dos dados;
6. Geração do CSV;
7. Compactação do resultado.

### `AnsService.java`

Responsável pela comunicação com o site da ANS.

A classe contém métodos para:

* Buscar o HTML de uma página;
* Listar os links encontrados;
* Identificar os anos disponíveis;
* Listar os arquivos ZIP de um ano;
* Selecionar os três trimestres mais recentes;
* Fazer o download dos arquivos.

### `ArquivoTrimestre.java`

Representa um arquivo trimestral encontrado no site da ANS.

Cada objeto armazena:

* Ano;
* Trimestre;
* Nome do arquivo;
* URL de download.

### `ZipService.java`

Responsável por extrair o conteúdo dos arquivos ZIP para uma pasta de destino.

### `ProcessadorService.java`

Responsável pela leitura, transformação, agrupamento e exportação dos dados.

A classe mantém em memória:

* Um mapa com o cadastro das operadoras;
* Um mapa com os valores consolidados.

### `OperadoraCadastro.java`

Representa uma operadora carregada do cadastro local.

Armazena:

* CNPJ;
* Razão social.

### `ConsolidaDTO.java`

Representa uma linha do resultado consolidado.

Armazena:

* CNPJ;
* Razão social;
* Trimestre;
* Ano;
* Valor total das despesas.

## Como executar

### 1. Pré-requisitos

É necessário ter:

* JDK 11 ou superior;
* Uma IDE com suporte a Java, como IntelliJ IDEA, Eclipse ou Visual Studio Code;
* A biblioteca Jsoup disponível no projeto;
* Acesso à internet para consultar e baixar os arquivos da ANS;
* O arquivo de cadastro das operadoras no caminho esperado.

O arquivo de cadastro deve estar localizado em:

```text
recursos/Relatorio_cadop.csv
```

O arquivo precisa possuir as seguintes colunas:

```text
REGISTRO_OPERADORA
CNPJ
Razao_Social
```

### 2. Abrir o projeto

Abra a pasta do projeto em uma IDE Java e configure um JDK compatível.

### 3. Executar

Execute o método `main` da classe:

```java
Main
```


### 4. Arquivos gerados

Durante a execução, os arquivos baixados são salvos em:

```text
downloads/
```

Os arquivos extraídos são salvos em:

```text
downloads/extraídos/
```

O CSV consolidado é salvo em:

```text
downloads/consolidado.csv
```

O resultado compactado é salvo na raiz do projeto:

```text
consolidado_despesas.zip
```

## Fluxo de funcionamento

1. O programa acessa a página de demonstrações contábeis da ANS.
2. Os links presentes no HTML são lidos com o Jsoup.
3. O programa identifica quais links representam diretórios de anos.
4. Para cada ano, são procurados links de arquivos com extensão `.zip`.
5. O ano e o trimestre de cada arquivo são armazenados em objetos `ArquivoTrimestre`.
6. Os arquivos são ordenados do trimestre mais recente para o mais antigo.
7. O programa seleciona todos os arquivos pertencentes aos três trimestres mais recentes.
8. Os arquivos selecionados são baixados para a pasta `downloads`.
9. Cada ZIP é extraído para uma pasta própria.
10. O cadastro local de operadoras é carregado em memória.
11. O programa procura o CSV esperado dentro da pasta de cada trimestre.
12. O arquivo é lido linha por linha.
13. As contas contábeis iniciadas por `411` são selecionadas.
14. Os valores positivos são agrupados por CNPJ, trimestre e ano.
15. O arquivo `consolidado.csv` é gerado.
16. O CSV é compactado no arquivo `consolidado_despesas.zip`.

## Decisões técnicas

### Processamento incremental dos arquivos

**Escolha:** os arquivos contábeis são lidos linha por linha utilizando `BufferedReader`.

**Motivo:** os arquivos podem possuir muitas linhas, e não é necessário carregar todo o conteúdo de uma vez para realizar a consolidação.

**Ponto positivo:** reduz a quantidade de dados brutos mantidos simultaneamente na memória.

**Limitação:** o resultado agrupado ainda permanece em memória dentro de um `HashMap`.

### Armazenamento do cadastro em um `HashMap`

**Escolha:** o cadastro das operadoras é carregado em um mapa que utiliza o Registro ANS como chave.

**Motivo:** cada linha contábil precisa localizar rapidamente o CNPJ e a razão social correspondentes ao Registro ANS.

**Ponto positivo:** evita percorrer toda a lista de operadoras para cada linha processada.

**Limitação:** o cadastro completo permanece em memória durante a execução. Caso um Registro ANS apareça mais de uma vez, o último valor inserido substitui o anterior.

### Agrupamento em memória

**Escolha:** os valores são agrupados em um `HashMap`.

A chave é formada por:

```text
CNPJ + Trimestre + Ano
```

**Motivo:** diferentes linhas da mesma operadora e do mesmo período precisam ser somadas antes da geração do CSV.

**Ponto positivo:** permite acumular os valores durante a leitura, sem armazenar todas as linhas originais.

**Limitação:** o `HashMap` não garante uma ordem fixa para as linhas do CSV final.

### Identificação dos últimos trimestres

**Escolha:** os arquivos encontrados são ordenados pelo ano e pelo trimestre, do mais recente para o mais antigo.

**Motivo:** a página da ANS organiza os arquivos em diretórios separados por ano, então é necessário comparar os períodos encontrados.

**Ponto positivo:** a seleção também considera a mudança de ano, como o primeiro trimestre de um ano e os últimos trimestres do ano anterior.

**Limitação:** o programa consulta todos os anos encontrados antes de fazer a ordenação, mesmo que apenas os anos mais recentes sejam necessários.

### Inclusão de múltiplos arquivos por trimestre

**Escolha:** a contagem é feita por combinação de ano e trimestre, e não apenas pela quantidade de arquivos.

**Motivo:** um mesmo trimestre pode possuir mais de um arquivo ZIP.

**Ponto positivo:** todos os arquivos encontrados para um dos três períodos selecionados são mantidos na lista de download.

**Limitação:** a identificação do trimestre depende de o nome do arquivo começar com um número de `1` a `4`, seguido da letra `T`.

### Download direto para o disco

**Escolha:** os arquivos são baixados utilizando `BodyHandlers.ofFile`.

**Motivo:** os ZIPs não precisam ser carregados completamente na memória antes de serem salvos.

**Ponto positivo:** o conteúdo recebido é gravado diretamente no caminho de destino.

**Limitação:** caso um arquivo com o mesmo nome já exista, o comportamento depende da operação utilizada pelo `HttpClient` e do estado do arquivo no destino.

### Filtro pelo código contábil

**Escolha:** são processadas contas cujo código começa com `411`.

**Motivo:** esse prefixo é utilizado no projeto para identificar despesas relacionadas a eventos e sinistros.

**Ponto positivo:** também permite incluir subcontas que começam com o mesmo código.

**Limitação:** a descrição da conta é lida, mas não é utilizada para confirmar o tipo da despesa.

## Tratamento de inconsistências

### Valores zerados, negativos ou inválidos

O programa tenta converter o valor encontrado para `double`.

Antes da conversão:

* Aspas são removidas;
* Espaços são removidos;
* A vírgula decimal é substituída por ponto;
* Valores vazios ou representados por `-` são convertidos para zero.

Quando o texto não pode ser convertido, o método também retorna zero.

Valores menores ou iguais a zero são ignorados e não entram no consolidado.

Esse comportamento evita incluir valores não positivos na soma. Porém, valores negativos que representem ajustes ou estornos também são descartados.

### Datas incompletas

Quando a data possui menos de quatro caracteres, o ano recebe o valor:

```text
Inconsistente
```

Quando o mês não corresponde a um valor entre `01` e `12`, o trimestre também recebe:

```text
Inconsistente
```

O código espera que a data esteja em um formato que comece pelo ano, como:

```text
2026-03-31
```

Outros formatos não são convertidos automaticamente.

### Operadora não encontrada no cadastro

Quando um Registro ANS não é encontrado no cadastro local, o programa mantém uma identificação no resultado:

```text
Não encontrado - REGISTRO_ANS
```

Esse texto é utilizado tanto no campo de CNPJ quanto no campo de razão social.

Isso permite identificar qual registro não teve correspondência no cadastro.

### Colunas do cadastro

Antes de processar o cadastro, o programa verifica a existência das colunas:

```text
REGISTRO_OPERADORA
CNPJ
Razao_Social
```

Quando alguma delas não é encontrada, o cadastro não é carregado.

### Arquivo CSV não encontrado

Antes de chamar o processamento, o programa verifica se o CSV esperado existe.

Quando o arquivo não é encontrado, ele não é processado.

O código atual não apresenta uma mensagem específica para esse caso.

### Linhas repetidas do mesmo período

Quando mais de uma linha possui o mesmo CNPJ, trimestre e ano, os valores são somados no mesmo objeto de consolidação.

O código não identifica separadamente CNPJs que aparecem com razões sociais diferentes.

## Limitações atuais

* O programa espera que o CSV extraído tenha o mesmo nome-base do arquivo ZIP;
* O processamento não percorre todas as subpastas procurando arquivos automaticamente;
* Arquivos com nomes diferentes do ZIP podem não ser encontrados;
* O código atual processa somente o CSV localizado no caminho esperado;
* Arquivos TXT não são procurados automaticamente;
* O tratamento de CSV utiliza `split`, que pode falhar quando um campo contém o próprio separador entre aspas;
* As aspas são removidas antes da separação das colunas;
* A validação das colunas obrigatórias do arquivo contábil é incompleta;
* A conversão numérica não diferencia um valor realmente zerado de um valor inválido;
* Valores monetários são armazenados em `double`;
* Datas em formatos diferentes de `ano-mês-dia` não são normalizadas corretamente;
* CNPJs duplicados com razões sociais diferentes não possuem um tratamento específico;
* O cadastro das operadoras é carregado de um arquivo local e não é baixado automaticamente;
* O resultado não possui uma ordenação definida;


## Possíveis melhorias

Uma possível melhoria seria percorrer as pastas extraídas utilizando `Files.walk`, identificando todos os arquivos compatíveis em vez de depender de um nome específico.

O projeto poderia futuramente utilizar uma biblioteca própria para leitura de CSV, permitindo interpretar corretamente campos com aspas e separadores internos.

Uma possível melhoria seria adicionar suporte a arquivos XLSX utilizando uma biblioteca adequada para esse formato.

Os valores monetários poderiam ser armazenados com `BigDecimal`, evitando aproximações relacionadas ao uso de `double`.

As datas poderiam ser convertidas utilizando `LocalDate` e diferentes formatos de `DateTimeFormatter`.

O projeto também poderia validar todas as colunas necessárias antes de iniciar o processamento de cada arquivo.

Outra possível melhoria seria registrar em um arquivo separado as linhas ignoradas, os valores inválidos e os registros sem correspondência no cadastro.

## Autor

Ronaldo Dutra Filho
