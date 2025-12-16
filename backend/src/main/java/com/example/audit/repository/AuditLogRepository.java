package com.example.audit.repository;

import com.example.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByCreateTimeDesc();
    List<AuditLog> findByUserIdOrderByCreateTimeDesc(Long userId);

    long countByUserId(Long userId);
    long countByUserIdAndIsSafe(Long userId, Boolean isSafe);

    long countByIsSafe(Boolean isSafe);

    @Query("SELECT a.category, COUNT(a) FROM AuditLog a WHERE a.userId = :userId GROUP BY a.category")
    List<Object[]> countCategoryByUserId(Long userId);

    @Query("SELECT a.category, COUNT(a) FROM AuditLog a GROUP BY a.category")
    List<Object[]> countCategoryAll();
}
