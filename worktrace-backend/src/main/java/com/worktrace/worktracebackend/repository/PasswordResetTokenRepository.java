package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.PasswordResetToken;
import com.worktrace.worktracebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    void deleteByUser(User user);

    Optional<PasswordResetToken> findByToken(String token);
}