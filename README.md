📘 BNDES Projetos API

API REST desenvolvida em Java com Spring Boot para autenticação de usuários e gerenciamento de projetos, com foco em boas práticas, segurança e organização.

🚀 O que este sistema faz

Permite o cadastro e autenticação de usuários

Gerencia projetos institucionais (CRUD completo)

Protege endpoints com JWT

Fornece documentação interativa via Swagger

Disponibiliza collections de API para testes

🎯 Problema que resolve

Em ambientes institucionais é comum a necessidade de:

Centralizar informações de projetos

Controlar acesso de usuários

Garantir segurança, padronização e rastreabilidade

Facilitar integrações futuras

Esta API resolve esses pontos oferecendo uma base backend segura, documentada e extensível.

💡 Motivação

O projeto foi desenvolvido como desafio técnico para uma vaga de estágio, com o objetivo de demonstrar:

Conhecimento em desenvolvimento backend

Organização de código

Uso correto de autenticação e segurança

Preocupação com documentação e testes

🛠️ Tecnologias

Java 17

Spring Boot

Spring Security

JWT

Spring Data JPA

PostgreSQL

Flyway

Swagger / OpenAPI

Docker

Newman (execução de collections de API)

🧱 Arquitetura

Arquitetura em camadas:

Controller → Service → Repository → Database


Com separação clara de responsabilidades e tratamento global de erros.

🔐 Segurança

Autenticação stateless via JWT

Endpoints públicos:

/auth/register

/auth/login

/health

Endpoints protegidos:

/projects/**

📄 Documentação (Swagger)

Após executar a aplicação:

http://localhost:8080/swagger-ui/index.html

🧪 Testes de API

Collections de testes disponíveis em:

src/test/resources/api-collections


Execução via Newman:

newman run src/test/resources/api-collections/projects-api.collection.json \
  -e src/test/resources/api-collections/local.environment.json

▶️ Como executar
Pré-requisitos

Java 17+

Docker e Docker Compose

Subir banco e a API
docker-compose up --build

✅ Considerações finais

Este projeto vai além de um CRUD simples, priorizando:

Segurança

Organização

Boas práticas

Clareza para manutenção e evolução futura