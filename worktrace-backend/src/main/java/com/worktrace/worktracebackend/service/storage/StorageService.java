package com.worktrace.worktracebackend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();

    String store(MultipartFile file, String directory);

    Resource loadAsResource(String filename, String directory);

    String getUrl(String filename, String directory);

    void deleteAll();

    void delete(String filename, String directory);
}