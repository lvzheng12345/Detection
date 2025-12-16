package com.example.audit.controller;

import com.example.audit.entity.AppConfig;
import com.example.audit.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private AppConfigRepository appConfigRepository;

    @GetMapping("/all")
    public ResponseEntity<List<AppConfig>> getAllConfigs() {
        return ResponseEntity.ok(appConfigRepository.findAll());
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, String> payload) {
        String key = payload.get("key");
        String value = payload.get("value");
        
        AppConfig config = appConfigRepository.findByConfigKey(key).orElse(new AppConfig());
        config.setConfigKey(key);
        config.setConfigValue(value);
        appConfigRepository.save(config);
        
        return ResponseEntity.ok("配置已更新");
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<?> getConfig(@PathVariable String key) {
        return ResponseEntity.ok(appConfigRepository.findByConfigKey(key)
                .map(AppConfig::getConfigValue)
                .orElse(null));
    }
}
