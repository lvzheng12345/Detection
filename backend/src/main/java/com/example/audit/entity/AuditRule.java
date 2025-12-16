package com.example.audit.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "audit_rules")
public class AuditRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String category; // Violence, Politics, etc.

    @Column(nullable = false)
    private String level; // High, Medium, Low
}
