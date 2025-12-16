package com.example.audit.controller;

import com.example.audit.dto.AuditRequest;
import com.example.audit.dto.AuditResponse;
import com.example.audit.entity.AuditLog;
import com.example.audit.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.audit.dto.StatsResponse;
import com.example.audit.dto.DocumentAuditResponse;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/check")
    public ResponseEntity<AuditResponse> checkContent(@RequestBody AuditRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuditResponse(false, "错误", "内容不能为空", "neutral"));
        }
        AuditResponse response = deepSeekService.checkContent(request.getContent(), request.getUserId(), request.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AuditLog>> getHistory(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok(deepSeekService.getUserHistory(userId));
        }
        return ResponseEntity.ok(deepSeekService.getHistory());
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(deepSeekService.getStats(userId));
    }

    @GetMapping("/analysis")
    public ResponseEntity<?> getAnalysis(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(deepSeekService.getSentimentAnalysis(userId));
    }

    @PostMapping("/document")
    public ResponseEntity<DocumentAuditResponse> checkDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new DocumentAuditResponse(false, "文件不能为空", null));
        }
        return ResponseEntity.ok(deepSeekService.auditDocument(file));
    }
}
