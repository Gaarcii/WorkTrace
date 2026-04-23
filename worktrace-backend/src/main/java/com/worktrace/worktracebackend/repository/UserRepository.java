package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.dto.user.DepartmentStatProjection;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Long countUsersByCompany_Id(UUID companyId);

    Page<User> findByCompanyIdAndRole(UUID companyId, Role role, Pageable pageable);

    @Query("SELECT u " +
            "FROM User u" +
            " WHERE u.company.id = :companyId " +
            "AND u.role = :role " +
            "AND LOWER(u.profile.fullName) " +
            "LIKE LOWER(CONCAT(:search, '%'))")
    Page<User> searchByCompanyIdAndRoleAndFullName(
            @Param("companyId") UUID companyId,
            @Param("role") Role role,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<User> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query(value = """
            SELECT j.title AS departamento,
                   COUNT(u.id) AS totalTrabajadores,
                   SUM(CASE WHEN t.id IS NOT NULL AND t.end_at IS NULL THEN 1 ELSE 0 END) AS trabajadoresActivos
            FROM users u
            JOIN profiles p ON p.user_id = u.id
            JOIN job_positions j ON p.position_id = j.id
            LEFT JOIN time_entries t ON p.user_id = t.employee_id AND t.status = 'OPEN'
            WHERE u.company_id = :companyId
            GROUP BY j.title
            """, nativeQuery = true)
    List<DepartmentStatProjection> getDepartamentStats(@Param("companyId") UUID companyId);
}
