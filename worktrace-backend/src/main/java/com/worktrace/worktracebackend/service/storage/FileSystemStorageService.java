package com.worktrace.worktracebackend.service.storage;

import com.worktrace.worktracebackend.controller.files.FilesController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${upload.root-location}") String path) {
        this.rootLocation = Paths.get(path);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo inicializar la carpeta de subida");
        }
    }

    @Override
    public String store(MultipartFile file, String directory) {
        try {
            if (file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichero vacío");
            }

            Path targetLocation = this.rootLocation.resolve(directory);
            Files.createDirectories(targetLocation);

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + "." + extension;

            Files.copy(file.getInputStream(), targetLocation.resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallo al almacenar el fichero", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename, String directory) {
        try {
            Path file = rootLocation.resolve(directory).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se puede leer fichero: " + filename);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error al procesar fichero: " + filename);
        }
    }

    @Override
    public String getUrl(String filename, String directory) {
        return MvcUriComponentsBuilder
                .fromMethodName(FilesController.class, "serveFile", directory, filename)
                .build().toUriString();
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void delete(String filename, String directory) {
        try {
            Path file = rootLocation.resolve(directory).resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("No se pudo borrar el archivo antiguo: " + filename);
        }
    }
}