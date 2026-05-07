package com.worktrace.worktracebackend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz que define el contrato para los servicios de almacenamiento de archivos.
 * Su propósito es abstraer la lógica de almacenamiento, permitiendo que la aplicación
 * pueda cambiar fácilmente entre diferentes proveedores (p. ej., almacenamiento local,
 * Cloudflare R2, AWS S3) sin modificar el código de negocio que depende de ella.
 * Esto sigue el Principio de Inversión de Dependencias.
 */
public interface StorageService {
    /**
     * Inicializa el servicio de almacenamiento.
     * Puede ser utilizado para crear directorios base o realizar otras configuraciones iniciales.
     */
    void init();

    /**
     * Almacena un archivo en un directorio específico.
     *
     * @param file El archivo MultipartFile a almacenar.
     * @param directory El directorio de destino.
     * @return El nombre único generado para el archivo almacenado.
     */
    String store(MultipartFile file, String directory);

    /**
     * Carga un archivo como un recurso de Spring.
     * Útil para servir archivos directamente desde el sistema de archivos local.
     *
     * @param filename El nombre del archivo a cargar.
     * @param directory El directorio donde se encuentra el archivo.
     * @return Un objeto Resource que representa el archivo.
     */
    Resource loadAsResource(String filename, String directory);

    /**
     * Obtiene la URL pública de un archivo almacenado.
     * Esencial para servicios de almacenamiento en la nube donde los archivos se acceden a través de una URL.
     *
     * @param filename El nombre del archivo.
     * @param directory El directorio donde se encuentra el archivo.
     * @return La URL pública completa del archivo.
     */
    String getUrl(String filename, String directory);

    /**
     * Elimina todos los archivos gestionados por el servicio.
     * Es una operación peligrosa que debe usarse con precaución.
     */
    void deleteAll();

    /**
     * Elimina un archivo específico de un directorio.
     *
     * @param filename El nombre del archivo a eliminar.
     * @param directory El directorio donde se encuentra el archivo.
     */
    void delete(String filename, String directory);
}
