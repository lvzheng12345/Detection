package com.example.audit.dto;

import lombok.Data;

@Data
public class AuditRequest {
    private String content;
    private Long userId;
    private String username;
}
