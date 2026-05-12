export const API_CONFIG = {
    // Usar ruta relativa permite que Nginx (en producción) reenvíe la llamada
    // al backend sin exponer orígenes distintos, evitando así problemas de CORS.
    baseUrl: '/api'
} as const;
