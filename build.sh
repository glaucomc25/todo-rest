#!/bin/bash

set -e  #abortar se qualquer comando retornar erro (codigo != 0)

# Diretórios
TOMCAT_HOME="/opt/tomcat/tomcat11"
SRC="src/main/java"
WEB="src/main/webapp"
BUILD_DIR="build"
CLASSES_DIR="$BUILD_DIR/WEB-INF/classes"
LIB_DIR="$BUILD_DIR/WEB-INF/lib"
DB_DIR="$BUILD_DIR/WEB-INF/db" 
APP_NAME="todo-rest"

# Limpa build antigo
rm -rf "$BUILD_DIR"
mkdir -p "$CLASSES_DIR"
mkdir -p "$LIB_DIR"
mkdir -p "$DB_DIR"

# Monta o classpath separando os JARs por :
CLASSPATH=$(echo $WEB/WEB-INF/lib/* | tr ' ' ':')

# Criar/atualizar banco antes da compilação
echo "== Creating/Updating database =="
./create-db.sh

echo "== COMPILANDO =="
javac -cp "$CLASSPATH" -d "$CLASSES_DIR" $(find "$SRC" -name "*.java")


# Copiar bibliotecas para o build
cp $WEB/WEB-INF/lib/* $LIB_DIR

# Criar o diretorio para o banco
# Apaga o diretório se existir
if [ -d "$DB_DIR" ]; then
    rm -rf "$DB_DIR"
fi

# Cria o diretório novamente
mkdir -p "$DB_DIR"

# Copiar web.xml
mkdir -p "$BUILD_DIR/WEB-INF"
cp $WEB/WEB-INF/web.xml $BUILD_DIR/WEB-INF/



# ----------------------------
# DEPLOY EXPLAINED NO TOMCAT
# ----------------------------
DEPLOY_DIR="$TOMCAT_HOME/webapps/$APP_NAME"
echo "== DEPLOY NO TOMCAT ($DEPLOY_DIR) =="
rm -rf "$DEPLOY_DIR"           # remove deploy antigo
cp -r "$BUILD_DIR" "$DEPLOY_DIR"


#echo "== GERANDO WAR =="
#cd "$BUILD_DIR"
#jar cvf ../todo-rest.war *
#cd ..

#echo "WAR gerado em: todo-rest.war"

#cp todo-rest.war /opt/tomcat/tomcat11/webapps/

echo "==> Reiniciando Tomcat..."
$TOMCAT_HOME/bin/shutdown.sh 2>/dev/null
sleep 3
$TOMCAT_HOME/bin/startup.sh


echo "==> Deploy concluído."
echo "Acesse: http://localhost:8080/$APP_NAME/"