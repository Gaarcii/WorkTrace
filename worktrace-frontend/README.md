# WorkTrace Frontend

WorkTrace Frontend es el cliente web SPA de WorkTrace, desarrollado con Angular para la gestión de fichajes, incidencias, perfiles y paneles operativos por rol. La aplicación actúa como capa de presentación y consume la API REST del backend de WorkTrace para autenticar usuarios, cargar datos de negocio y ejecutar operaciones sobre trabajadores, administradores e inspectores.

## Requisitos previos

- Node.js compatible con Angular CLI 21: `^20.19.0 || ^22.12.0 || >=24.0.0`.
- npm 11.x. El proyecto declara `npm@11.10.0` como gestor de paquetes en `package.json`.

Se recomienda trabajar con Node.js 22 LTS y npm 11 para mantener coherencia con el entorno usado en el proyecto.

## Instalación

Instalar las dependencias del frontend desde la raíz del módulo:

```bash
npm install
```

## Servidor de desarrollo

Levantar la aplicación en local:

```bash
npm run start
```

El script ejecuta `ng serve`. Por defecto, Angular expone la aplicación en:

```text
http://localhost:4200
```

## Documentación interna con Compodoc

El proyecto incluye Compodoc para generar y consultar la documentación técnica de componentes, servicios, modelos y rutas Angular.

Generar y servir la documentación:

```bash
npm run compodoc
```

El comando configurado utiliza `tsconfig.app.json`, levanta el servidor de documentación y lo publica en el puerto `8085`:

```text
http://localhost:8085
```

## Estructura y arquitectura

La aplicación sigue una organización modular por capas funcionales:

```text
src/
`-- app/
    |-- core/
    |-- features/
    `-- shared/
```

- `core/`: contiene configuración transversal de la aplicación, como `API_CONFIG`, autenticación, guards de ruta e interceptores HTTP. El interceptor de autenticación añade el token JWT a las peticiones salientes mediante la cabecera `Authorization`.
- `features/`: agrupa las vistas de negocio por dominio y rol. Incluye módulos funcionales para autenticación, trabajador, administrador, inspector y perfil. Las rutas usan componentes standalone cargados de forma diferida mediante `loadComponent`.
- `shared/`: centraliza elementos reutilizables, como modelos TypeScript, servicios de acceso a datos y componentes compartidos. Los servicios encapsulan la comunicación con la API REST y exponen datos a las vistas mediante `Observable` y señales de Angular cuando aplica.

El frontend aplica una separación de responsabilidades basada en componentes Smart/Dumb:

- Los componentes Smart se encargan de orquestar la vista, inyectar servicios, gestionar estado local, transformar datos y reaccionar a eventos de usuario.
- Los componentes Dumb o presentacionales reciben datos por entrada, emiten eventos y mantienen una responsabilidad visual acotada, lo que facilita su reutilización y testeo.

La comunicación con backend se realiza mediante `HttpClient` contra la API REST configurada en:

```text
http://localhost:8080/api/
```

Esta separación permite mantener las pantallas desacopladas de los detalles de transporte, concentrando las llamadas HTTP en servicios reutilizables y tipados.
