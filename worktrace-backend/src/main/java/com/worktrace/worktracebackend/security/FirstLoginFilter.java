package com.worktrace.worktracebackend.security;

import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class FirstLoginFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public FirstLoginFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") || path.equals("/api/worker/password")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            String email = authentication.getName();

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && user.getProfile().getIsFirstLogin()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Acceso denegado. Debes cambiar la contraseña temporal generada por el administrador.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}