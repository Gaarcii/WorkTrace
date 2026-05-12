# WorkTrace Backend

Backend de **WorkTrace**, implementado con **Spring Boot**, que expone la API REST principal del sistema. Este módulo centraliza la autenticación, la gestión de usuarios, empresas, fichajes, incidencias, horarios, centros de trabajo, auditorías y exportación de información operativa.

## Requisitos Previos

- **JDK 21**.
- **Gradle 9.3.1**, incluido mediante Gradle Wrapper.
- **PostgreSQL** en local o en un entorno accesible desde la aplicación.

No es necesario instalar Gradle globalmente si se utiliza el wrapper del repositorio.

## Configuración y Variables de Entorno

La configuración principal se encuentra en `src/main/resources/application.yaml`.

La aplicación utiliza **PostgreSQL** como base de datos:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

Por defecto, Spring Boot levanta la API en el puerto **8080**. El datasource espera PostgreSQL en `localhost:5432`.

El proyecto importa variables desde un archivo externo opcional:

```yaml
spring:
  config:
    import: optional:file:../.env
```

Por tanto, se puede definir un archivo `.env` en el directorio superior al backend o configurar las variables directamente en el entorno del sistema.

Variables requeridas:

```env
DB_NAME=worktrace
DB_USER=postgres
DB_PASSWORD=postgres

JWT_SECRET=change-me
JWT_EXPIRATION=86400

MAIL_USERNAME=example@gmail.com
MAIL_PASSWORD=app-password

ABSTRACT_API_KEY=abstract-api-key

R2_ENDPOINT=https://example.r2.cloudflarestorage.com
R2_ACCESS_KEY=r2-access-key
R2_SECRET_KEY=r2-secret-key
R2_BUCKET=worktrace
R2_PUBLIC_URL=https://cdn.example.com
```

Propiedades relevantes:

- **Base de datos**: PostgreSQL mediante `spring.datasource.*`.
- **Puerto HTTP**: `8080` por defecto. Puede sobrescribirse con `SERVER_PORT` o `--server.port`.
- **JWT**: `JWT_SECRET` y `JWT_EXPIRATION` controlan la firma y expiración de tokens.
- **Correo**: SMTP de Gmail en el puerto `587`.
- **Almacenamiento**: Cloudflare R2 compatible con S3 para recursos como logos o archivos.
- **JPA**: `spring.jpa.hibernate.ddl-auto=validate`, por lo que el esquema debe existir y ser compatible con las entidades.

## Despliegue Local

Compilar el proyecto:

```bash
./gradlew clean build
```

En Windows:

```bat
gradlew.bat clean build
```

Levantar la aplicación en modo desarrollo:

```bash
./gradlew bootRun
```

En Windows:

```bat
gradlew.bat bootRun
```

Ejecutar tests:

```bash
./gradlew test
```

Una vez arrancado correctamente, la API queda disponible en:

```text
http://localhost:8080
```

## Documentación de la API

El backend incluye documentación interactiva mediante **Swagger/OpenAPI** con `springdoc-openapi`.

Con la aplicación arrancada, acceder a:

```text
http://localhost:8080/swagger-ui/index.html
```

El contrato OpenAPI también queda disponible en:

```text
http://localhost:8080/v3/api-docs
```

Las rutas de Swagger están permitidas explícitamente en la configuración de seguridad.

## Arquitectura

El proyecto sigue una arquitectura por capas propia de una API REST con Spring Boot:

- **Controllers** (`controller`): exponen los endpoints REST bajo `/api/**`. Incluyen módulos como autenticación, usuarios, empresas, fichajes, incidencias, horarios, puestos, centros de trabajo e inspector.
- **Services** (`service`): concentran la lógica de negocio. Incluyen servicios de autenticación, gestión de fichajes, incidencias, usuarios, exportación PDF/Excel, correo, almacenamiento y auditoría.
- **DTOs** (`dto`): definen los contratos de entrada y salida de la API, separando la representación externa de las entidades JPA.
- **Models** (`model`): contienen las entidades de dominio persistidas en PostgreSQL.
- **Repositories** (`repository`): encapsulan el acceso a datos mediante Spring Data JPA.
- **Security** (`security`): implementa seguridad stateless con JWT, filtros personalizados, `SecurityFilterChain`, `BCryptPasswordEncoder` y control de acceso por roles mediante `@PreAuthorize`.
- **Config** (`config`): agrupa configuración transversal, como Jackson y cliente S3/R2.

La autenticación se realiza mediante `/api/auth/**`. El resto de endpoints requieren token JWT salvo las rutas públicas configuradas para autenticación y Swagger.
