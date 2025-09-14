
---

# 🏥 Medical Inventory

Java-приложение для управления медицинскими товарами.
Проект контейнеризован в **Docker** и использует **PostgreSQL** в качестве базы данных.
Миграции и начальное заполнение выполняются автоматически с помощью **Liquibase**.

---

## 📋 Требования

* [Docker Desktop](https://www.docker.com/products/docker-desktop) или установленный **Docker + docker-compose**
* **Git**

---

## 🚀 Запуск проекта

### 1. Клонировать репозиторий

```bash
git clone https://github.com/JackieChan08/Medical-inventory-jar.git
cd Medical-inventory-jar
```

### 2. Запуск через Docker

Находясь в корневой директории проекта:

```bash
docker build -t medical-inventory .
docker run -p 8080:8080 medical-inventory
```

### 3. Запуск через Docker Compose

```bash
docker compose up --build
```

---

## 🗄️ База данных

* **СУБД:** PostgreSQL
* При первом запуске все миграции и сиддинг выполняются автоматически через Liquibase
* Не требуется вручную создавать таблицы или наполнять их данными

---

## ⚙️ Настройки по умолчанию

* Приложение доступно по адресу: **[http://localhost:8080](http://localhost:8080)**
* PostgreSQL работает на порту: **5432** (если не меняли `docker-compose.yml`)

---

## 📦 Технологии

* Java (Spring Boot)
* PostgreSQL
* Liquibase
* Docker / Docker Compose
* Maven
  
---
Есть так же колекции, они находяться в корневой папке /сollections просто импортируйте эту коллекцию в свой POSTMAN
