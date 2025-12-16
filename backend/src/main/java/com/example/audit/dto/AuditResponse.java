package com.example.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditResponse {
    private Boolean isSafe;
    private String category;
    private String reason;
    private String sentiment;
}
