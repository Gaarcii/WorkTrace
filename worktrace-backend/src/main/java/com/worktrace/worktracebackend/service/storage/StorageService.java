package com.worktrace.worktracebackend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();

    String store(MultipartFile file);

    Resource loadAsResource(String filename);

    String getUrl(String filename);

    void deleteAll();

    void delete(String filename);
}