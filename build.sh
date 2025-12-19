#!/bin/bash

set -e  # aborta se algum comando falhar

# ----------------------------
# CONFIGURAÇÃO
# ----------------------------
TOMCAT_HOME="/opt/tomcat/tomcat11"
APP_NAME="todo-rest"

SRC="src/main/java"
WEB="src/main/webapp"
BUILD_DIR="build"
CLASSES_DIR="$BUILD_DIR/WEB-INF/classes"
LIB_DIR="$BUILD_DIR/WEB-INF/lib"

# ----------------------------
# BANCO DE DADOS EXTERNO
# ----------------------------
EXTERNAL_DB_DIR="$HOME/databases"
DB_FILE="$EXTERNAL_DB_DIR/todo.db"
SCHEMA_FILE="src/main/resources/db/schema.sql"

mkdir -p "$EXTERNAL_DB_DIR"

echo "== Creating database =="
if [ ! -f "$DB_FILE" ]; then
    sqlite3 "$DB_FILE" < "$SCHEMA_FILE"
    echo "Database created at $DB_FILE"
else
    echo "Database already exists at $DB_FILE"
fi

# ----------------------------
# LIMPA BUILD ANTIGO
# ----------------------------
rm -rf "$BUILD_DIR"
mkdir -p "$CLASSES_DIR"
mkdir -p "$LIB_DIR"

# ----------------------------
# COMPILANDO
# ----------------------------
CLASSPATH=$(echo $WEB/WEB-INF/lib/* | tr ' ' ':')

echo "== COMPILANDO =="
javac -cp "$CLASSPATH" -d "$CLASSES_DIR" $(find "$SRC" -name "*.java")

# ----------------------------
# COPIA LIBS
# ----------------------------
cp $WEB/WEB-INF/lib/* $LIB_DIR

# ----------------------------
# COPIA WEB.XML
# ----------------------------
mkdir -p "$BUILD_DIR/WEB-INF"
cp $WEB/WEB-INF/web.xml $BUILD_DIR/WEB-INF/

# ----------------------------
# DEPLOY NO TOMCAT
# ----------------------------
DEPLOY_DIR="$TOMCAT_HOME/webapps/$APP_NAME"
echo "== DEPLOY NO TOMCAT ($DEPLOY_DIR) =="
rm -rf "$DEPLOY_DIR"
cp -r "$BUILD_DIR" "$DEPLOY_DIR"

echo "==> Reiniciando Tomcat..."
$TOMCAT_HOME/bin/shutdown.sh 2>/dev/null || true
sleep 3
$TOMCAT_HOME/bin/startup.sh

echo "==> Deploy concluído."
echo "Acesse: http://localhost:8080/$APP_NAME/"
echo "Banco usado: $DB_FILE"
