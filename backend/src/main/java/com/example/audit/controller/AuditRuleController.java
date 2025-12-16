package com.example.audit.controller;

import com.example.audit.entity.AuditRule;
import com.example.audit.repository.AuditRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class AuditRuleController {

    @Autowired
    private AuditRuleRepository auditRuleRepository;

    @GetMapping("/all")
    public ResponseEntity<List<AuditRule>> getAllRules() {
        return ResponseEntity.ok(auditRuleRepository.findAll());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRule(@RequestBody AuditRule rule) {
        return ResponseEntity.ok(auditRuleRepository.save(rule));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRule(@PathVariable Long id) {
        auditRuleRepository.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }
}
