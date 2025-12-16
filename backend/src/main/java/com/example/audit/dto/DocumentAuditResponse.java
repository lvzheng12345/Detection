package com.example.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentAuditResponse {
    private boolean success;
    private String message;
    private List<DocumentIssue> issues;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentIssue {
        private String original;
        private String reason;
        private String suggestion;
    }
}
