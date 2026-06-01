package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

public interface HashPort {
    String sha256Hex(String input);
}
