package com.worktrace.worktracebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorktraceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorktraceBackendApplication.class, args);
    }

}
