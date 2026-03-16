package com.worktrace.worktracebackend.config;

import com.worktrace.worktracebackend.service.storage.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageService storageService;

    @Value("${upload.delete}")
    private String deleteAll;

    @PostConstruct
    public void init() {
        if ("true".equals(deleteAll)) {
            log.info("Borrando ficheros de almacenamiento...");
            storageService.deleteAll();
        }
        storageService.init();
    }
}