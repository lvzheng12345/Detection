package com.example.audit.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isSafe;

    private String category;

    @Column(length = 1024)
    private String reason;

    private String sentiment; // positive, neutral, negative

    private Long userId;
    private String username;

    @CreationTimestamp
    private LocalDateTime createTime;
}
