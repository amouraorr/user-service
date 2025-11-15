# user-service

# Serviço de Usuários - Backend

## Introdução

Este microsserviço é responsável pelo cadastro, autenticação, atualização e consulta de usuários (moradores e porteiros) dentro do sistema de gerenciamento de encomendas. Ele emite tokens JWT para autenticação, persiste dados via JPA/PostgreSQL e pode publicar eventos de usuário em Kafka.

## Objetivo do Projeto

O objetivo principal deste microsserviço é oferecer uma API robusta e autônoma para gerenciar usuários, cobrindo operações de criação, atualização e consulta, além de autenticação baseada em JWT. O serviço aplica validações, garante unicidade de username e expõe rotas internas para serem consumidas por outros microsserviços.

## Requisitos do Sistema

Para executar este microsserviço, você precisará dos seguintes requisitos:

- **Sistema Operacional**: Windows, macOS ou Linux
- **Memória RAM**: Pelo menos 4 GB recomendados
- **Espaço em Disco**: Pelo menos 500 MB de espaço livre
- **Software**:
    - Docker e Docker Compose
    - Java JDK 11 ou superior
    - Maven 3.6 ou superior
    - PostgreSQL
    - Git
    - (Opcional) Kafka se desejar publicar eventos de usuário

## Estrutura do Projeto
A estrutura do projeto está organizada da seguinte forma:

```plaintext
user-service/
│
├── src/
│ └── main/
│   ├── java/
│   │ └── com.fiap.userservice
│   │   ├── adapter/ : Controladores REST (web)
│   │   ├── application/
│   │   │   ├── dto/ : DTOs de requisição/resposta
│   │   │   ├── mapper/ : MapStruct mappers
│   │   │   └── usecase/ : Casos de uso (serviços de aplicação)
│   │   ├── domain/ : Modelos de domínio e portas (interfaces)
│   │   ├── infrastructure/
│   │   │   ├── config/ : Configurações (security, swagger, kafka, persistence)
│   │   │   ├── persistence/ : Adapters e entidades JPA
│   │   │   ├── messaging/ : Produtor de eventos Kafka (opcional)
│   │   │   └── mapper/ : Helpers (ex.: UUIDMapper)
│   │   └── UserServiceApplication.java : Classe principal da aplicação.
│   └── resources/
│       └── application.properties : Configurações da aplicação e profiles.
├── pom.xml : Arquivo de configuração do Maven.
├── Dockerfile : Arquivo para construção da imagem Docker.
├── docker-compose.yml : Arquivo para orquestração de contêineres (se existir).
└── README.md : Documentação do projeto.
```

## Segurança

A segurança usa JWT para autenticação do usuário e Spring Security para proteção de endpoints. O serviço aceita tanto `security.jwt.secret` quanto `jwt.secret` como propriedade para derivar a chave HMAC usada na assinatura HS256. Em development há um profile `dev` que expõe um usuário em memória para testes; em `docker` e `prod` a validação JWT é aplicada conforme configuração.

## Visão Geral do Projeto

Desenvolvido com Spring Boot, o projeto segue uma arquitetura limpa simplificada, separando domínio, persistência, casos de uso e interface, além de utilizar MapStruct para mapeamentos e Kafka para publicação de eventos.

## Arquitetura

Camadas principais:

- Domain: entidades de negócio (User).
- UseCase: regras de negócio e casos de uso (registro, atualização, busca).
- Adapter/Gateway: implementação de persistência via JPA e repositórios.
- Controller/Adapter.web: endpoints REST para interação externa.
- Mapper: conversão entre entidades, domínios e DTOs.

## Princípios de Design e Padrões de Projeto

- Single Responsibility Principle: cada classe tem responsabilidade única.
- Gateway Pattern: abstração do acesso a dados.
- Mapper Pattern: conversão entre camadas com MapStruct.
- MVC: controladores tratam requests, domínios representam o modelo.

## Interação entre as Partes do Sistema

1. Cliente envia requisições HTTP (ou outro microsserviço pelo gateway).
2. Controller valida e delega para UseCase.
3. UseCase executa regras e usa o repository/gateway para persistência.
4. Adapter de persistência usa Spring Data JPA para armazenar no banco.
5. Quando configurado, eventos de usuário são enviados ao Kafka.

## Tecnologias Utilizadas

- Spring Boot
- Spring Security (JWT support)
- Spring Data JPA
- PostgreSQL
- MapStruct
- Lombok (quando aplicável)
- Swagger / Springdoc OpenAPI
- Docker e Docker Compose
- Kafka (opcional) e Spring for Apache Kafka

## Pré-requisitos

Instale:

- Docker e Docker Compose
- Java JDK 11+
- Maven 3.6+
- PostgreSQL (local ou via container)
- Opcional: Kafka se quiser consumir os eventos produzidos

Variáveis e propriedades importantes:

- security.jwt.secret ou jwt.secret — chave usada para assinatura e verificação de tokens (aceita Base64 ou texto; se curto, será derivado via SHA-256).
- spring.datasource.* — configurações do banco (jdbc url, username, password).
- kafka.bootstrap-servers — endereço do Kafka (se usar eventos).
- kafka.topic.users — tópico para publicar eventos de usuário (se configurado).

### Recomendações de profile

- dev — configuração para desenvolvimento (in-memory user, permissões relaxadas para GETs e endpoints de criação de usuário).
- docker — configurações típicas quando rodando por trás de um API Gateway.
- prod — produção, com oauth2 resource server / JWT externo.

## Executando com Docker Compose

1. Garanta que Docker e Docker Compose estejam instalados.
2. No diretório do projeto, execute:
   ```bash
   docker compose up
   ```
3. A aplicação estará disponível na porta (ex.: `http://localhost:8081`).
4. Swagger UI em `http://localhost:8081/swagger-ui/index.html`.

## Conexão ao Banco via Adminer 

1. Acesse `http://localhost:8088` (se Adminer estiver configurado).
2. Em Sistema, escolha PostgreSQL.
3. Em Servidor, utilize o nome do serviço do Docker Compose (ex.: `postgres`).
4. Usuário: `postgres`.
5. Senha: `postgres`.
6. Banco de dados: `postgres`.

## Endpoints Principais

- POST /api/internal/auth/login — Autenticação, retorna JWT (LoginRequest -> LoginResponse).
- POST /api/internal/users/morador — Cria usuário com role = MORADOR.
- POST /api/internal/users/porteiro — Cria usuário com role = PORTEIRO.
- GET /api/internal/users/moradores — Lista todos os moradores.
- GET /api/internal/users/porteiros — Lista todos os porteiros.

Observações:
- Endpoints marcados como `/api/internal/**` são pensados para uso interno entre microsserviços e gateway.
- Em profile `dev` alguns endpoints/GETs podem ser permitidos sem autenticação para facilitar desenvolvimento.

## Publicação de Eventos

Se configurado com Kafka (propriedades `kafka.bootstrap-servers` e `kafka.topic.users`), o serviço publica eventos simples ao criar/atualizar usuários: eventos `USER_CREATED` e `USER_UPDATED` contendo id e username.

## Boas Práticas para Uso do JWT

- Defina `security.jwt.secret` (ou `jwt.secret`) com pelo menos 32 bytes em Base64; se usar texto curto, a chave será derivada via SHA-256 (adequado para desenvolvimento, não recomendado para produção sem revisão).
- Ajuste o TTL do token conforme sua política (padrão no projeto: 1 hora).

## Contribuição

Contribuições são bem-vindas:

1. Faça um fork do repositório.
2. Crie uma branch (`git checkout -b feature/nome-da-feature`).
3. Faça commits claros (`git commit -m "Descrição da feature"`).
4. Envie para o remoto (`git push origin feature/nome-da-feature`).
5. Abra Pull Request.

## Licença

Este projeto pode ser privado ou não possuir licença explícita. Verifique com os responsáveis antes de redistribuir.

## Referências e Recursos

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- MapStruct: https://mapstruct.org
- PostgreSQL: https://www.postgresql.org
- Springdoc OpenAPI: https://springdoc.org

## Conclusão

O user-service implementa princípios de arquitetura limpa, fornecendo APIs de autenticação e gerenciamento de usuários, com suporte a JWT, persistência via JPA. É projetado para integração com um API Gateway e outros microsserviços do ecossistema.
