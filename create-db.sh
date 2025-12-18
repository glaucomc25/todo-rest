#!/bin/bash

DB_PATH="/home/glauco/Projects/todo-rest/src/main/webapp/WEB-INF/db/todo.db"

SQL="
CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL
);
"

# Cria diretório se não existir
mkdir -p "$(dirname "$DB_PATH")"

sqlite3 "$DB_PATH" "$SQL"

echo "Banco criado ou atualizado em: $DB_PATH"