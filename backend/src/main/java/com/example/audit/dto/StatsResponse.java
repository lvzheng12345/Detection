package com.example.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class StatsResponse {
    private long total;
    private long safeCount;
    private long unsafeCount;
    private double safeRate;
    private Map<String, Long> categoryStats;
}
