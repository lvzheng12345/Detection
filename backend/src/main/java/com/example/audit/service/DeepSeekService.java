package com.example.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.audit.dto.AuditResponse;
import com.example.audit.entity.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.example.audit.dto.StatsResponse;
import com.example.audit.dto.DocumentAuditResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;


    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public AuditResponse checkContent(String content, Long userId, String username) {
        // 1. Construct API Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个内容审核助手。请检测用户输入的文本是否存在违规行为，并分析其情感倾向。请严格仅返回一个JSON对象，不要包含Markdown格式或其他文字。JSON格式如下：{\"isSafe\": true/false, \"category\": \"正常/色情/暴力/辱骂/政治敏感/广告/其他\", \"reason\": \"如果不安全，请简述原因；如果安全，返回'通过'\", \"sentiment\": \"positive/neutral/negative\"}");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", content);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 2. Call API
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String contentResponse = jsonResponse.get("choices")
                        .get(0)
                        .get("message")
                        .get("content")
                        .asText();

                // Clean up markdown code blocks if present
                contentResponse = contentResponse.replace("```json", "").replace("```", "").trim();

                JsonNode result = objectMapper.readTree(contentResponse);
                Boolean isSafe = result.get("isSafe").asBoolean();
                String category = result.has("category") ? result.get("category").asText() : "未知";
                String reason = result.get("reason").asText();
                String sentiment = result.has("sentiment") ? result.get("sentiment").asText() : "neutral";

                // 3. Save to Database
                AuditLog log = new AuditLog();
                log.setContent(content);
                log.setIsSafe(isSafe);
                log.setCategory(category);
                log.setReason(reason);
                log.setSentiment(sentiment);
                log.setUserId(userId);
                log.setUsername(username);
                auditLogRepository.save(log);

                return new AuditResponse(isSafe, category, reason, sentiment);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuditResponse(false, "错误", "API调用失败: " + e.getMessage(), "neutral");
        }

        return new AuditResponse(false, "错误", "未知错误", "neutral");
    }

    public List<AuditLog> getHistory() {
        return auditLogRepository.findAllByOrderByCreateTimeDesc();
    }

    public List<AuditLog> getUserHistory(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public StatsResponse getStats(Long userId) {
        long total;
        long safe;
        List<Object[]> categoryList;
        
        if (userId != null) {
            total = auditLogRepository.countByUserId(userId);
            safe = auditLogRepository.countByUserIdAndIsSafe(userId, true);
            categoryList = auditLogRepository.countCategoryByUserId(userId);
        } else {
            total = auditLogRepository.count();
            safe = auditLogRepository.countByIsSafe(true);
            categoryList = auditLogRepository.countCategoryAll();
        }
        
        long unsafe = total - safe;
        double rate = total == 0 ? 0 : (double) safe / total * 100;

        Map<String, Long> categoryStats = new HashMap<>();
        for (Object[] row : categoryList) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            if (category != null) {
                categoryStats.put(category, count);
            }
        }
        
        return new StatsResponse(total, safe, unsafe, rate, categoryStats);
    }

    public List<Map<String, Object>> getSentimentAnalysis(Long userId) {
        // 1. Get logs
        List<AuditLog> logs;
        if (userId != null) {
            logs = auditLogRepository.findByUserIdOrderByCreateTimeDesc(userId);
        } else {
            logs = auditLogRepository.findAllByOrderByCreateTimeDesc();
        }

        // 2. Group by Week
        Map<String, List<AuditLog>> weeklyLogs = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (AuditLog log : logs) {
            LocalDateTime date = log.getCreateTime();
            if (date == null) continue;
            // Get start of week (Monday)
            LocalDateTime startOfWeek = date.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
            String key = startOfWeek.format(formatter);
            
            weeklyLogs.computeIfAbsent(key, k -> new ArrayList<>()).add(log);
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        int weeksProcessed = 0;

        // Process each week (limit to last 5 weeks to avoid timeout)
        for (Map.Entry<String, List<AuditLog>> entry : weeklyLogs.entrySet()) {
            if (weeksProcessed >= 5) break;
            weeksProcessed++;

            String weekStart = entry.getKey();
            List<AuditLog> weekLogs = entry.getValue();
            
            // Calc stats
            Map<String, Integer> counts = new HashMap<>();
            counts.put("positive", 0);
            counts.put("neutral", 0);
            counts.put("negative", 0);
            
            for (AuditLog log : weekLogs) {
                String s = log.getSentiment();
                if (s == null) s = "neutral";
                s = s.toLowerCase();
                counts.put(s, counts.getOrDefault(s, 0) + 1);
            }
            
            // Generate Advice
            String advice = generateAdvice(weekLogs.size(), counts);
            
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("weekStart", weekStart);
            weekData.put("sentimentCounts", counts);
            weekData.put("advice", advice);
            resultList.add(weekData);
        }
        
        return resultList;
    }

    private String generateAdvice(int total, Map<String, Integer> counts) {
        if (total == 0) return "暂无数据";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个心理分析师。根据用户本周的发言情感统计数据，给出一段简短的心理分析和建议（80字以内）。");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            String statsStr = String.format("本周共发言%d条，其中正面%d条，中性%d条，负面%d条。", 
                total, counts.get("positive"), counts.get("neutral"), counts.get("negative"));
            userMessage.put("role", "user");
            userMessage.put("content", statsStr);
            messages.add(userMessage);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.get("choices").get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "生成建议时发生错误。";
        }
        return "无法生成建议";
    }

    public DocumentAuditResponse auditDocument(MultipartFile file) {
        try {
            String text = "";
            String filename = file.getOriginalFilename().toLowerCase();
            
            if (filename.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(file.getInputStream())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(document);
                }
            } else if (filename.endsWith(".docx")) {
                try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                    text = extractor.getText();
                }
            } else if (filename.endsWith(".doc")) {
                try (HWPFDocument document = new HWPFDocument(file.getInputStream())) {
                    WordExtractor extractor = new WordExtractor(document);
                    text = extractor.getText();
                }
            } else {
                // Assume text file, force UTF-8
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            System.out.println("Extracted text length: " + text.length());
            System.out.println("Extracted text preview: " + (text.length() > 100 ? text.substring(0, 100) : text));

            if (text.trim().isEmpty()) {
                return new DocumentAuditResponse(false, "文档内容为空或无法读取", null);
            }

            // Limit text length for API (increased to 15000)
            if (text.length() > 15000) {
                text = text.substring(0, 15000);
            }

            // Call DeepSeek
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1); 

            List<Map<String, String>> messages = new ArrayList<>();
            
            // Strategy Change: Put instructions in User message for better attention
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个严格的内容审核API。你只输出JSON格式的数据。");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", 
                "请审核以下文本内容，找出其中包含的违规信息（包括色情、暴力、恐怖、政治敏感、辱骂、歧视、非法广告、低俗内容等）\n" +
                "即使是轻微的违规,疑似违规或不含违规词的嘲讽，也请务必指出来。\n\n" +
                "文本内容：\n" +
                "```\n" +
                text + "\n" +
                "```\n\n" +
                "请直接返回一个 JSON 数组，不要包含任何 Markdown 标记或解释性文字。\n" +
                "数组格式：[{\"original\": \"违规原文\", \"reason\": \"违规类型及原因\", \"suggestion\": \"修改建议\"}]\n" +
                "如果未发现违规，返回 []。"
            );
            messages.add(userMessage);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String contentResponse = jsonResponse.get("choices").get(0).get("message").get("content").asText();
                
                // Robust JSON extraction
                int startIndex = contentResponse.indexOf("[");
                int endIndex = contentResponse.lastIndexOf("]");
                if (startIndex != -1 && endIndex != -1) {
                    contentResponse = contentResponse.substring(startIndex, endIndex + 1);
                } else {
                    // Fallback cleanup if brackets not found (unlikely for valid array)
                    contentResponse = contentResponse.replace("```json", "").replace("```", "").trim();
                }
                
                List<DocumentAuditResponse.DocumentIssue> issues = new ArrayList<>();
                JsonNode issuesNode = objectMapper.readTree(contentResponse);
                if (issuesNode.isArray()) {
                    for (JsonNode node : issuesNode) {
                        DocumentAuditResponse.DocumentIssue issue = new DocumentAuditResponse.DocumentIssue();
                        issue.setOriginal(node.has("original") ? node.get("original").asText() : "");
                        issue.setReason(node.has("reason") ? node.get("reason").asText() : "");
                        issue.setSuggestion(node.has("suggestion") ? node.get("suggestion").asText() : "");
                        issues.add(issue);
                    }
                }
                return new DocumentAuditResponse(true, "检测完成", issues);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return new DocumentAuditResponse(false, "文件读取失败: " + e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new DocumentAuditResponse(false, "分析过程出错: " + e.getMessage(), null);
        }
        return new DocumentAuditResponse(false, "未知错误", null);
    }
}
