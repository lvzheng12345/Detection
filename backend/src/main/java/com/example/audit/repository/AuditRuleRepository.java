package com.example.audit.repository;

import com.example.audit.entity.AuditRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRuleRepository extends JpaRepository<AuditRule, Long> {
}
