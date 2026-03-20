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

    @Override
    public String getUrl(String filename, String directory) {
        return publicUrl + "/" + directory + "/" + filename;
    }

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