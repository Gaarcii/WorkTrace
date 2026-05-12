# Etapa de build: instala dependencias y compila la aplicacion Angular.
FROM node:20-alpine AS build
WORKDIR /app

# Copia primero los manifests para aprovechar la cache de Docker.
COPY package.json package-lock.json ./
RUN npm ci

# Copia el codigo fuente y ejecuta el build de produccion definido en package.json.
COPY . .
RUN npm run build

# Etapa de produccion: Nginx sirve los artefactos estaticos.
FROM nginx:alpine AS production

# Configuracion SPA con fallback a index.html.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular genera la salida de navegador en dist/worktrace-frontend/browser.
COPY --from=build /app/dist/worktrace-frontend/browser /usr/share/nginx/html

EXPOSE 80

# Ejecuta Nginx en primer plano dentro del contenedor.
CMD ["nginx", "-g", "daemon off;"]
