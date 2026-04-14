# 🎫 GestEvent — Sistema de Gerenciamento de Eventos3
 
# 📚 Engenharia da Computação

## 👨‍💻 Desenvolvido por

* **Filipe Quaresma Pereira**
* **Vitor Luis Amaral**
  
---

## 💡 Sobre o Projeto

O **GestEvent** é um sistema de gerenciamento de eventos inspirado em plataformas como o **Sympla**, com o objetivo de facilitar o controle de eventos acadêmicos e tecnológicos.

A proposta é oferecer uma solução completa para:

* 📅 Cadastro de eventos
* 🎤 Gerenciamento de palestrantes
* 👥 Cadastro de participantes
* 📝 Controle de inscrições

Tudo isso **sem uso de banco de dados tradicional**, utilizando **arquivos binários** para persistência de dados, conforme os requisitos da disciplina de AED III 

---

## 🎯 Objetivo

Desenvolver um sistema completo que permita:

* CRUD de todas as entidades (Eventos, Participantes, Palestrantes e Inscrições)
* Persistência de dados em arquivos binários
* Controle de exclusão lógica (lápide)
* Estrutura preparada para futuras otimizações (Hash Extensível e Árvore B+)

---

## 🏗️ Arquitetura

O sistema segue uma arquitetura baseada em:

* **MVC (Model-View-Controller)**
* **DAO (Data Access Object)**

### 🔹 Camadas:

* **Model** → Classes como `Evento`, `Participante`, etc.
* **DAO** → Responsável pela persistência em arquivos
* **Controller** → Regras de negócio e controle da aplicação
* **View** → Interface (React)

Essa separação melhora a organização, manutenção e evolução do sistema 

---

## 🔗 Relacionamentos do Sistema

* **1:N** → Evento → Palestrantes
* **N:N** → Evento ↔ Participantes (via Inscrição)

---

## 🌐 Tecnologias Utilizadas

### Backend

* Java
* HttpServer (API REST)
* Arquivos binários para persistência

### Frontend

* React (Vite)
* JavaScript
* CSS

---

## 🔥 Funcionalidades

* ✅ Cadastrar eventos
* ✅ Cadastrar participantes
* ✅ Cadastrar palestrantes
* ✅ Realizar inscrições
* ✅ Listar dados
* ✅ Atualizar registros
* ✅ Exclusão lógica

---

## 🚀 Como Executar

### 🔹 Backend

```bash
cd backend/src
javac *.java
java Servidor
```

Servidor rodando em:

```
http://localhost:8080
```

---

### 🔹 Frontend

```bash
cd frontend
npm install
npm run dev
```

Acesse:

```
http://localhost:5173
```

---

## 🔄 Comunicação (API REST)

O frontend se comunica com o backend através de requisições HTTP:

* `GET` → Listar dados
* `POST` → Criar registros
* `PUT` → Atualizar
* `DELETE` → Remover

Exemplo:

```
http://localhost:8080/eventos
```

---

## 📦 Persistência de Dados

* Armazenamento em arquivos binários
* Controle de:

  * IDs
  * Número de registros
  * Exclusão lógica (lápide)

---

## 🧠 Motivação

O sistema foi desenvolvido para resolver a dificuldade de gerenciar eventos sem depender de plataformas pagas, oferecendo uma alternativa acadêmica funcional e escalável 

---

💥 Projeto desenvolvido para a disciplina de **Algoritmos e Estruturas de Dados III**
