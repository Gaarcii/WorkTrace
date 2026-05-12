package com.worktrace.worktracebackend.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudflareStorageServiceTest {

    @Mock
    private S3Client s3Client;

    private CloudflareStorageService cloudflareStorageService;

    private final String bucketName = "test-bucket";
    private final String publicUrl = "https://pub-test.r2.dev";
    private final String directory = "logos";

    @BeforeEach
    void setUp() {
        cloudflareStorageService = new CloudflareStorageService(s3Client, bucketName);
        try {
            java.lang.reflect.Field field = cloudflareStorageService.getClass().getDeclaredField("publicUrl");
            field.setAccessible(true);
            field.set(cloudflareStorageService, publicUrl);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testStoreSuccess() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "image content".getBytes()
        );

        String storedFilename = cloudflareStorageService.store(imageFile, directory);

        assertAll(
                () -> assertNotNull(storedFilename),
                () -> assertTrue(storedFilename.endsWith(".jpg"))
        );

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(directory + "/" + storedFilename, capturedRequest.key());
        assertEquals("image/jpeg", capturedRequest.contentType());
    }

    @Test
    void testStoreEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> cloudflareStorageService.store(emptyFile, directory));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Fichero vacío", exception.getReason());
    }

    @Test
    void testStoreUnsupportedMediaType() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "some text".getBytes()
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> cloudflareStorageService.store(textFile, directory));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
        assertEquals("Formato no válido. Solo se permiten imágenes (JPG, PNG, WEBP...).", exception.getReason());
    }

    @Test
    void testStoreIOException() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "image content".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("Simulated S3 Exception"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> cloudflareStorageService.store(imageFile, directory));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Fallo al subir el archivo a la nube", exception.getReason());
    }

    @Test
    void testGetUrl() {
        String filename = "test-file.jpg";
        String expectedUrl = publicUrl + "/" + directory + "/" + filename;
        String actualUrl = cloudflareStorageService.getUrl(filename, directory);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void testDeleteSuccess() {
        String filename = "file-to-delete.png";

        cloudflareStorageService.delete(filename, directory);

        ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(deleteObjectRequestCaptor.capture());

        DeleteObjectRequest capturedRequest = deleteObjectRequestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(directory + "/" + filename, capturedRequest.key());
    }

    @Test
    void testDeleteHandlesException() {
        String filename = "file-with-error.txt";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(new RuntimeException("Simulated S3 Deletion Error"));

        assertDoesNotThrow(() -> cloudflareStorageService.delete(filename, directory));
    }

    @Test
    void testLoadAsResourceThrowsException() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> cloudflareStorageService.loadAsResource("some-file.txt", directory));
        assertEquals("Usa getUrl() para recursos en la nube", exception.getMessage());
    }

    @Test
    void testDeleteAllThrowsException() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> cloudflareStorageService.deleteAll());
        assertEquals("Operación peligrosa deshabilitada en la nube", exception.getMessage());
    }
}
