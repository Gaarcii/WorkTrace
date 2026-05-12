#!/bin/sh
set -e

: "Entrypoint que espera a que la base de datos esté lista antes de iniciar la app" 

DB_HOST=${DB_HOST:-db}
DB_PORT=${DB_PORT:-5432}
DB_USER=${SPRING_DATASOURCE_USERNAME:-${DB_USER}}
DB_NAME=${DB_NAME}

MAX_RETRIES=${DB_WAIT_RETRIES:-30}
SLEEP_SECONDS=${DB_WAIT_SLEEP:-2}

echo "[entrypoint] Esperando a que la BD ${DB_HOST}:${DB_PORT} esté lista..."

retries=0
until pg_isready -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" >/dev/null 2>&1; do
  retries=$((retries+1))
  if [ "$retries" -ge "$MAX_RETRIES" ]; then
    echo "[entrypoint] Timeout esperando la base de datos después de $retries intentos. Abortando."
    exit 1
  fi
  echo "[entrypoint] intent $retries/${MAX_RETRIES} - esperando ${SLEEP_SECONDS}s..."
  sleep $SLEEP_SECONDS
done

echo "[entrypoint] Base de datos disponible. Iniciando la aplicación..."

# Si SPRING_DATASOURCE_PASSWORD no está seteada pero DB_PASSWORD sí, exportar
if [ -z "$SPRING_DATASOURCE_PASSWORD" ] && [ -n "$DB_PASSWORD" ]; then
  export SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD"
fi

exec java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar /app/app.jar
