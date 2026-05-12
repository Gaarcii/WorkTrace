package com.worktrace.worktracebackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:worktrace_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password",
        "jwt.secret=test-secret-key-for-context-loads",
        "jwt.expiration=3600000",
        "abstract.api-key=test-abstract-api-key",
        "storage.r2.endpoint=http://localhost",
        "storage.r2.access-key=test-access-key",
        "storage.r2.secret-key=test-secret-key",
        "storage.r2.bucket=test-bucket",
        "storage.r2.public-url=http://localhost/public"
})
public class WorktraceBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
