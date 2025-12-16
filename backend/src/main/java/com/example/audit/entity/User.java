package com.example.audit.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // "USER" or "ADMIN"
    @Column(nullable = false)
    private String role;

    private String email;
    private String phone;
    private String avatarUrl;
}
