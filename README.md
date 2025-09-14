# Medical Inventory

Простое Java-приложение для управления медицинскими товарами.  
Проект упакован в Docker и использует PostgreSQL в качестве базы данных.  
Миграции и сиддинг выполняются автоматически с помощью Liquibase.

---

## 📋 Требования
- [Docker Desktop](https://www.docker.com/products/docker-desktop) (или установленный Docker + docker-compose)
- Git

---

## 🚀 Запуск проекта

### 1. Клонируйте репозиторий
```bash
git clone https://github.com/JackieChan08/Medical-inventory-jar.git
cd Medical-inventory-jar

### 2. Запуск через Docker
Находясь в корневой директории проекта:
```bash
docker build -t medical-inventory .
docker run -p 8080:8080 medical-inventory


### 3. Запуск через Docker Compose
docker compose up --build


🗄️ База данных

СУБД: PostgreSQL

При первом запуске миграции и сидинг накатываются автоматически через Liquibase.

Вам не нужно вручную создавать таблицы или наполнять их — всё произойдет автоматически.

⚙️ Настройки

По умолчанию:

Приложение доступно по адресу: http://localhost:8080

PostgreSQL доступен на порту 5432

📦 Используемые технологии

Java (Spring Boot)

PostgreSQL

Liquibase

Docker / Docker Compose

