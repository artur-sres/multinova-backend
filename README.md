# MultiNova API

Backend REST do **MultiNova**, um sistema de gestão para pequenos negócios. A API concentra catálogo, estoque, clientes, vendas, contas, entradas e saídas de caixa, painel financeiro e histórico de operações. Os dados de cada usuário são isolados por autenticação JWT.

## Sumário

- [Recursos](#recursos)
- [Tecnologias e requisitos](#tecnologias-e-requisitos)
- [Configuração local](#configuração-local)
- [Executar a aplicação](#executar-a-aplicação)
- [Autenticação](#autenticação)
- [Visão da API](#visão-da-api)
- [Regras de negócio importantes](#regras-de-negócio-importantes)
- [Imagens locais](#imagens-locais)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Respostas de erro](#respostas-de-erro)

## Recursos

- Cadastro, verificação de e-mail, login e recuperação de senha.
- Produtos, marcas, categorias e histórico de preços.
- Controle de estoque por compras/entradas e vendas; estoque não pode ser alterado diretamente pelo endpoint de produto.
- Clientes, pagamentos, débitos e extrato com reversão de lançamentos.
- Vendas com itens, desconto por item e cancelamento.
- Contas a pagar, entradas e saídas de caixa.
- Resumo financeiro por período e histórico de operações.
- Comprovantes de venda/pagamento e exportação do extrato do cliente.
- Upload privado de imagens de produtos e clientes.

## Tecnologias e requisitos

| Componente | Versão/uso |
| --- | --- |
| Java | JDK 21 |
| Framework | Spring Boot 4.1 |
| Banco | PostgreSQL |
| Persistência | Spring Data JPA / Hibernate |
| Evolução de schema | Flyway |
| Segurança | Spring Security, JWT e BCrypt |
| Mapeamento | MapStruct |
| Build | Maven Wrapper |

Para executar localmente, instale o JDK 21 e tenha um PostgreSQL acessível. Não é necessário instalar Maven: o projeto inclui `mvnw` e `mvnw.cmd`.

O envio de e-mails usa SMTP; para desenvolvimento, a configuração atual é compatível com uma caixa de entrada do Mailtrap.

## Configuração local

1. Crie um banco PostgreSQL vazio chamado `multinova` (ou ajuste a URL em `src/main/resources/application.properties`).
2. Copie `.env.example` para `.env`.
3. Preencha as variáveis abaixo. O arquivo `.env` é ignorado pelo Git e não deve ser versionado.

```dotenv
DB_PASSWORD=sua-senha-do-postgres
JWT_SECRET=uma-chave-longa-e-aleatoria
MAILTRAP_USERNAME=seu-usuario-smtp
MAILTRAP_PASSWORD=sua-senha-smtp
```

Variáveis opcionais:

| Variável | Padrão | Finalidade |
| --- | --- | --- |
| `IMAGE_STORAGE_DIRECTORY` | `uploads` | Diretório onde as imagens locais são gravadas. Use um caminho absoluto quando a aplicação for iniciada de outro diretório. |

As demais propriedades ficam em [`application.properties`](src/main/resources/application.properties). A conexão padrão é `jdbc:postgresql://localhost:5432/multinova`, com usuário `postgres`.

## Executar a aplicação

No PowerShell, a partir da raiz deste repositório:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Na primeira execução, o Flyway aplica automaticamente todas as migrations em `src/main/resources/db/migration`. Para gerar o artefato e executar os testes:

```powershell
.\mvnw.cmd clean verify
```

Por padrão, a aplicação Spring Boot atende em `http://localhost:8080`.

## Autenticação

Somente estes endpoints são públicos:

- `POST /users` — cadastro;
- `POST /auth/login` — login;
- `POST /auth/verify` — confirmação de e-mail;
- `POST /auth/forgot-password` — solicitação de código de redefinição;
- `POST /auth/reset-password` — redefinição de senha.

Os demais exigem o cabeçalho:

```http
Authorization: Bearer <token JWT>
```

Exemplo de login:

```http
POST /auth/login
Content-Type: application/json

{
  "email": "usuario@exemplo.com",
  "password": "senha-com-ao-menos-8-caracteres"
}
```

O retorno contém o token a ser usado nas chamadas autenticadas. A configuração CORS permite, atualmente, `http://localhost:5173` e `http://127.0.0.1:5500`.

## Visão da API

Todos os endpoints desta tabela, exceto os de autenticação/cadastro acima, requerem JWT. IDs na rota são numéricos.

| Área | Endpoints principais |
| --- | --- |
| Usuários | `GET/PATCH /users/{id}` |
| Marcas e categorias | `POST/GET /brands`, `GET/PUT/DELETE /brands/{id}`; mesma estrutura para `/categories` |
| Produtos | `POST/GET /products`, `GET/PATCH/DELETE /products/{id}` |
| Preços | `POST/GET /products/{productId}/prices`, `GET /products/{productId}/prices/current` |
| Imagens | `PUT/GET/DELETE /products/{id}/image`; `PUT/GET/DELETE /customers/{id}/photo` |
| Clientes | `POST/GET /customers`, `GET/PATCH/DELETE /customers/{id}` |
| Débitos e pagamentos | `POST /customers/{customerId}/debts`; `POST /customer-payments`; `GET /customer-payments?customerId={id}` |
| Extrato | `GET /customers/{customerId}/statement?page=0`; `POST /customers/{customerId}/statement/{entryId}/reverse`; `GET /customers/{customerId}/statement/export?limit=50`; `GET /customers/{customerId}/statement/{entryId}/receipt` |
| Vendas | `POST/GET /sales`, `DELETE /sales/{id}`, `GET /sales/{id}/receipt` |
| Estoque | `POST/GET /stock-purchases` |
| Contas | `POST/GET /bills`, `GET /bills/{id}`, `PATCH /bills/{id}/pay`, `DELETE /bills/{id}`. A lista aceita `?status=`. |
| Caixa | `POST/GET /cash-entries`, `POST/GET /cash-exits` |
| Indicadores | `GET /dashboard/summary?from=AAAA-MM-DD&to=AAAA-MM-DD` |
| Histórico | `GET /history?category=all&search=&from=AAAA-MM-DD&to=AAAA-MM-DD&page=0` |

### Formatos mais usados

Criar produto requer o preço inicial:

```json
{
  "name": "Camiseta básica",
  "brandId": 1,
  "categoryId": 2,
  "brandCode": "CAM-001",
  "stock": 10,
  "price": { "purchasePrice": 25.00, "salePrice": 49.90 }
}
```

Criar uma venda:

```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 12,
      "quantity": 2,
      "unitPrice": 49.90,
      "discountType": "PERCENTAGE",
      "discountValue": 10
    }
  ]
}
```

`discountType` aceita `NONE`, `PERCENTAGE` e `AMOUNT`. O desconto é calculado no servidor sobre a linha inteira (`quantidade × preço unitário`), preservando o preço unitário original.

Uma entrada de estoque aceita `entryType` `ADJUSTMENT`, `PAID` ou `PENDING`. Para `PENDING`, informe `dueDate`; o sistema cria a conta pendente correspondente.

## Regras de negócio importantes

- O acesso a registros é limitado ao usuário autenticado.
- Produtos são excluídos logicamente; o histórico de preços e imagens é preservado conforme as regras do serviço.
- Alterar `stock` com `PATCH /products/{id}` é rejeitado. Use compras de estoque e vendas para que os movimentos tenham rastreabilidade.
- A criação de produto com estoque positivo registra uma entrada inicial de ajuste, sem efeito de caixa.
- Entradas `PAID` e contas pagas entram no cálculo financeiro pela data de pagamento; entradas `ADJUSTMENT` e `PENDING` não reduzem o caixa imediatamente.
- Uma venda reduz o estoque e atualiza o saldo do cliente; cancelar a venda reverte esses efeitos.
- Pagamentos de clientes são aplicados ao saldo geral, não a uma venda específica.
- O extrato mantém snapshots de nome e itens. Alterações posteriores no cadastro não reescrevem o histórico.
- O histórico de operações é gravado por triggers PostgreSQL na mesma transação da operação de origem; uma transação revertida não deixa evento registrado.

## Imagens locais

As imagens de produto e cliente são privadas: não há rota pública para a pasta de arquivos. Os endpoints autenticados retornam o conteúdo da imagem após validar a propriedade do registro.

- Formato aceito: JPEG ou PNG.
- Limite do upload: 5 MB.
- Limite da imagem decodificada: 20 megapixels.
- A imagem é redimensionada para até 1200 px em cada lado e armazenada como PNG sem metadados originais.
- Por padrão, os arquivos ficam em `uploads/` na raiz de execução; a pasta é ignorada pelo Git.

Ao fazer backup ou migrar uma instalação, mantenha o banco de dados e o diretório de imagens juntos.

## Banco de dados e migrations

O Flyway executa arquivos versionados em `src/main/resources/db/migration`. Em ambientes compartilhados, migrations já aplicadas não devem ser renomeadas, removidas ou alteradas: isso muda o checksum e impede a validação do histórico.

O projeto usa `spring.jpa.hibernate.ddl-auto=validate`, portanto o Hibernate apenas valida se o schema produzido pelas migrations corresponde às entidades; ele não cria ou altera tabelas automaticamente.

## Testes

Executar toda a suíte Maven:

```powershell
.\mvnw.cmd test
```

Executar os testes de serviços:

```powershell
.\mvnw.cmd "-Dtest=*ServiceTests" test
```

Há também uma verificação de migrations e histórico que inicia um PostgreSQL temporário e isolado na porta local `55439`. Ela exige PostgreSQL 18 instalado em `C:\Program Files\PostgreSQL\18\bin` (ajuste o script se necessário):

```powershell
powershell -ExecutionPolicy Bypass -File src/test/test-history.ps1 -StatementTests
```

O script aplica todas as migrations, executa fixtures SQL e valida recuperação de histórico, reversões, escopo por usuário, cancelamentos, saldos e comprovantes.

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/com/artur_sres/multinova/
│   │   ├── config/       Configurações de segurança e aplicação
│   │   ├── controller/   Endpoints HTTP
│   │   ├── domain/       Entidades JPA e enums
│   │   ├── dto/          Contratos de entrada e saída da API
│   │   ├── exception/    Exceções de domínio e tratamento global
│   │   ├── mapper/       Mapeadores MapStruct
│   │   ├── repository/   Acesso a dados
│   │   ├── security/     Filtro JWT e contexto do usuário
│   │   ├── service/      Casos de uso e regras de negócio
│   │   └── util/         Utilitários
│   └── resources/
│       ├── application.properties
│       └── db/migration/ Migrations Flyway
└── test/
    ├── java/             Testes de unidade e integração
    └── *.sql, *.ps1      Fixtures e validação de migrations
```

## Respostas de erro

Erros da API seguem, em geral, este formato:

```json
{
  "message": "Descrição do erro",
  "status": 400
}
```

Os códigos mais comuns são `400` para dados inválidos, `401` para token ausente/inválido, `403` para recurso sem permissão, `404` para recurso não encontrado, `409` para conflitos de negócio e `413` para upload acima do limite.
