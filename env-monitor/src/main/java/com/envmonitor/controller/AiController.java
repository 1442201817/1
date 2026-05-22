package com.envmonitor.controller;

import com.envmonitor.entity.EnvData;
import com.envmonitor.service.AiService;
import com.envmonitor.service.EnvDataService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final EnvDataService envDataService;

    public AiController(AiService aiService, EnvDataService envDataService) {
        this.aiService = aiService;
        this.envDataService = envDataService;
    }

    @PostMapping("/outing-advice")
    public Map<String, String> outingAdvice(@RequestBody Map<String, Object> req) {
        Long stationId = Long.valueOf(req.getOrDefault("stationId", 1).toString());
        EnvData latest = envDataService.getLatestByStation(stationId);
        if (latest == null) return Map.of("advice", "暂无监测数据，无法生成建议。");
        return Map.of("advice", aiService.generateOutingAdvice(latest));
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> req) {
        String message = req.getOrDefault("message", "").toString();
        Long stationId = Long.valueOf(req.getOrDefault("stationId", 1).toString());
        // 获取或生成会话ID（前端可以传递，否则使用默认值）
        String sessionId = req.getOrDefault("sessionId", "default-session").toString();
        
        EnvData latest = envDataService.getLatestByStation(stationId);
        String response = aiService.freeChatWithMemory(sessionId, message, latest);
        
        // 返回回复和当前历史记录数量
        int historySize = aiService.getHistorySize(sessionId);
        return Map.of(
            "response", response,
            "historySize", historySize
        );
    }

    /**
     * 清除指定会话的历史记录
     */
    @PostMapping("/clear-history")
    public Map<String, String> clearHistory(@RequestBody Map<String, Object> req) {
        String sessionId = req.getOrDefault("sessionId", "default-session").toString();
        aiService.clearHistory(sessionId);
        return Map.of("message", "会话历史已清除");
    }
}
