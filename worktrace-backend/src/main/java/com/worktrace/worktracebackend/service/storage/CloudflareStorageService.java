package com.worktrace.worktracebackend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * Servicio de almacenamiento que interactúa con Cloudflare R2 (compatible con S3).
 * Su propósito es gestionar la subida, recuperación y eliminación de archivos
 * (como logos de empresa) en un almacenamiento en la nube escalable y de bajo costo,
 * desacoplando la lógica de almacenamiento del resto de la aplicación.
 */
@Service
public class CloudflareStorageService implements StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    @Value("${storage.r2.public-url}")
    private String publicUrl;

    public CloudflareStorageService(
            S3Client s3Client,
            @Value("${storage.r2.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void init() {
    }

    /**
     * Almacena un archivo MultipartFile en el bucket de Cloudflare R2.
     * Este método valida el tipo de archivo (solo imágenes), genera un nombre único
     * para evitar colisiones y sube el archivo al directorio especificado.
     *
     * @param file El archivo MultipartFile a almacenar.
     * @param directory El directorio dentro del bucket donde se guardará el archivo (ej. "logos").
     * @return El nombre único generado para el archivo almacenado.
     * @throws ResponseStatusException Si el archivo está vacío, no es una imagen o si ocurre un error de E/S.
     */
    @Override
    public String store(MultipartFile file, String directory) {
        try {
            if (file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichero vacío");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Formato no válido. Solo se permiten imágenes (JPG, PNG, WEBP...).");
            }

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + "." + extension;

            String objectKey = directory + "/" + storedFilename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return storedFilename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallo al subir el archivo a la nube", e);
        }
    }

    /**
     * Genera la URL pública para acceder a un archivo almacenado en Cloudflare R2.
     * Esta URL es necesaria para que los clientes (frontend) puedan mostrar las imágenes
     * directamente desde el almacenamiento en la nube.
     *
     * @param filename El nombre único del archivo.
     * @param directory El directorio donde se encuentra el archivo.
     * @return La URL pública completa del archivo.
     */
    @Override
    public String getUrl(String filename, String directory) {
        return publicUrl + "/" + directory + "/" + filename;
    }

    /**
     * Elimina un archivo específico del bucket de Cloudflare R2.
     * Se utiliza para limpiar archivos antiguos o no deseados, por ejemplo,
     * cuando se actualiza el logo de una empresa y se quiere eliminar el anterior.
     *
     * @param filename El nombre único del archivo a eliminar.
     * @param directory El directorio donde se encuentra el archivo.
     */
    @Override
    public void delete(String filename, String directory) {
        try {
            String objectKey = directory + "/" + filename;
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            System.err.println("No se pudo borrar el archivo de Cloudflare: " + filename);
        }
    }

    @Override
    public Resource loadAsResource(String filename, String directory) {
        throw new UnsupportedOperationException("Usa getUrl() para recursos en la nube");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Operación peligrosa deshabilitada en la nube");
    }
}
