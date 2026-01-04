# 📘 BNDES Projetos API

![Java](https://img.shields.io/badge/Java-17-007396?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-336791?logo=postgresql)
![JWT](https://img.shields.io/badge/Auth-JWT-orange)
![Status](https://img.shields.io/badge/Status-Em%20Evolução-blue)

API REST desenvolvida em **Java com Spring Boot**, responsável pela **autenticação de usuários** e **gerenciamento de projetos**, com foco em **boas práticas**, **segurança**, **organização** e **facilidade de evolução**.

---

## 📌 Sumário
- [Visão Geral](#-visão-geral)
- [Problema Resolvido](#-problema-resolvido)
- [Motivação](#-motivação)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Segurança](#-segurança)
- [Documentação (Swagger)](#-documentação-swagger)
- [Testes de API](#-testes-de-api)
- [Execução do Projeto](#-execução-do-projeto)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Padrões e Boas Práticas](#-padrões-e-boas-práticas)
- [Roadmap](#-roadmap)
- [Licença](#-licença)

---

## 🎯 Visão Geral

O **BNDES Projetos API** é o backend da plataforma BNDES Projetos, fornecendo endpoints REST seguros para autenticação de usuários e gestão de projetos institucionais.

A API foi projetada para ser **segura**, **documentada**, **padronizada** e **pronta para integrações futuras**, servindo como base sólida para aplicações frontend.

---

## 🎯 Problema Resolvido

Em ambientes institucionais, é comum a necessidade de:

- Centralizar informações de projetos
- Controlar acesso de usuários
- Garantir segurança e padronização
- Facilitar integrações e auditoria futura

Esta API resolve esses pontos oferecendo uma base backend **robusta**, **documentada** e **extensível**.

---

## 💡 Motivação

O projeto foi desenvolvido como **desafio técnico para uma vaga de estágio**, com o objetivo de demonstrar:

- Conhecimento em desenvolvimento backend
- Organização e separação de responsabilidades
- Uso correto de autenticação e segurança
- Preocupação com documentação e testes automatizados

---

## 🚀 Funcionalidades

- Cadastro e autenticação de usuários
- Gerenciamento de projetos (CRUD completo)
- Proteção de endpoints com **JWT**
- Documentação interativa via **Swagger / OpenAPI**
- Collections de API para testes automatizados

---

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot**
- **Spring Security**
- **JWT**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **Swagger / OpenAPI**
- **Docker & Docker Compose**
- **Newman** (execução de collections de API)

---

## 🧱 Arquitetura

Arquitetura em camadas com separação clara de responsabilidades:

```txt
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

Além disso, a aplicação conta com:
- Tratamento global de exceções
- DTOs para entrada e saída de dados
- Configuração centralizada de segurança

---

## 🔐 Segurança

### Autenticação
- Autenticação **stateless** baseada em **JWT**
- Token enviado no header:

```http
Authorization: Bearer <token>
```

### Endpoints Públicos
```txt
/auth/register
/auth/login
/health
```

### Endpoints Protegidos
```txt
/projects/**
```

---

## 📄 Documentação (Swagger)

Após executar a aplicação, a documentação estará disponível em:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 🧪 Testes de API

Collections de testes disponíveis em:

```txt
src/test/resources/api-collections
```

### Execução via Newman

```bash
newman run src/test/resources/api-collections/projects-api.collection.json \
  -e src/test/resources/api-collections/local.environment.json
```

---

## ▶️ Execução do Projeto

### Pré-requisitos
- **Java 17+**
- **Docker**
- **Docker Compose**

### Subir a aplicação

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Banco PostgreSQL inicializado automaticamente
- Migrations executadas via Flyway

---

## 📁 Estrutura do Projeto

```txt
src
├── main
│   ├── java
│   │   └── br.com.edmilson.bndes.projects.api
│   │       ├── auth
│   │       ├── projects
│   │       ├── config
│   │       ├── security
│   │       └── exception
│   └── resources
│       ├── db/migration
│       └── application.yml
└── test
    └── resources
        └── api-collections
```

---

## 📐 Padrões e Boas Práticas

- Arquitetura em camadas
- DTOs para isolamento do domínio
- Segurança centralizada com Spring Security
- JWT stateless
- Tratamento global de erros
- Documentação OpenAPI
- Pronto para evolução (roles, refresh token, auditoria)

---

## 🛣️ Roadmap

- [X] Roles e permissões (ADMIN / USER)
- [X] Refresh Token
- [ ] Auditoria de ações
- [ ] Testes unitários
- [ ] CI/CD

---

## 📄 Licença

Este projeto é distribuído sob a licença **MIT**.

---

📌 **Projeto em evolução contínua**, com foco em qualidade, segurança e boas práticas.
