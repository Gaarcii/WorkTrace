package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.out;

import com.worktrace.worktracebackend.dailyclosure.domain.port.out.HashPort;
import com.worktrace.worktracebackend.service.hash.HashService;
import org.springframework.stereotype.Component;

@Component
public class HashAdapter implements HashPort {

    private final HashService hashService;

    public HashAdapter(HashService hashService) {
        this.hashService = hashService;
    }

    @Override
    public String sha256Hex(String input) {
        return hashService.sha256Hex(input);
    }
}