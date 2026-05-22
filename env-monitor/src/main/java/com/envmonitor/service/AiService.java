package com.envmonitor.service;

import com.envmonitor.entity.EnvData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiService {

    @Value("${ai.api.url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.api.key:your-deepseek-api-key-here}")
    private String apiKey;

    @Value("${ai.api.model:deepseek-chat}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // 对话历史记录：sessionId -> List<Message>
    private final Map<String, LinkedList<Map<String, String>>> conversationHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 10; // 最多保留10次对话

    /**
     * 调用 API（支持多轮对话上下文）
     */
    private String callApiWithHistory(String systemPrompt, List<Map<String, String>> history) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 1024);
            
            ArrayNode messages = mapper.createArrayNode();
            
            // 添加系统提示词
            ObjectNode sys = mapper.createObjectNode();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
            
            // 添加历史对话（最近10轮）
            for (Map<String, String> msg : history) {
                ObjectNode node = mapper.createObjectNode();
                node.put("role", msg.get("role"));
                node.put("content", msg.get("content"));
                messages.add(node);
            }
            
            body.set("messages", messages);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode root = mapper.readTree(resp.body());
                return root.path("choices").path(0).path("message").path("content").asText("（AI 无返回内容）");
            }
            System.err.println("AI API error: " + resp.statusCode() + " " + resp.body());
            return "AI 接口返回错误（" + resp.statusCode() + "），请检查 API Key 或网络。";
        } catch (Exception e) {
            System.err.println("AI service exception: " + e.getMessage());
            return "AI 服务暂时不可用：" + e.getMessage();
        }
    }

    /**
     * 兼容旧版本的单轮对话方法
     */
    private String callApi(String systemPrompt, String userMessage) {
        List<Map<String, String>> history = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        history.add(userMsg);
        return callApiWithHistory(systemPrompt, history);
    }

    public String generateOutingAdvice(EnvData d) {
        String system = "你是一名专业的环境健康顾问，名叫\"绿境AI\"。" +
                "根据用户提供的环境监测数据，给出简洁实用的出行健康建议。" +
                "格式：第一行总体评价（加emoji），然后3-4条具体建议（以\"• \"开头），最后一行温馨提示。" +
                "全程中文，语气亲切，200字以内。";
        String user = String.format(
                "当前环境监测数据：\nAQI：%d（%s）\nPM2.5：%.1f μg/m³ | PM10：%.1f μg/m³\n" +
                "温度：%.1f℃ | 湿度：%.1f%%\nCO：%.2f mg/m³ | 风速：%.1f m/s %s风\n请给出今天的出行健康建议。",
                d.getAqi(), d.getAqiLevel(), d.getPm25(), d.getPm10(),
                d.getTemperature(), d.getHumidity(), d.getCo(), d.getWindSpeed(), d.getWindDirection());
        return callApi(system, user);
    }

    /**
     * 自由聊天（带记忆功能）
     * @param sessionId 会话ID，用于区分不同用户的对话
     * @param message 用户消息
     * @param latest 最新环境数据
     * @return AI 回复
     */
    public String freeChatWithMemory(String sessionId, String message, EnvData latest) {
        // 获取或创建该会话的历史记录
        LinkedList<Map<String, String>> history = conversationHistory.computeIfAbsent(sessionId, k -> new LinkedList<>());
        
        // 添加用户消息到历史
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        history.addLast(userMsg);
        
        // 构建系统提示词（包含当前环境数据）
        String currentData = latest == null ? "暂无监测数据" :
                String.format("AQI=%d(%s), PM2.5=%.1f, 温度=%.1f°C, 湿度=%.1f%%, 天气=%s",
                        latest.getAqi(), latest.getAqiLevel(),
                        latest.getPm25(), latest.getTemperature(), latest.getHumidity(),
                        latest.getWeatherDescription() != null ? latest.getWeatherDescription() : "未知");
        String system = "你是一名环境监测智能助手，名叫\"绿境AI\"，擅长解读AQI、PM2.5、CO等环境指标，" +
                "提供健康防护建议，解释污染物危害，回答环保气象问题。" +
                "当前站点实时数据（供参考）：" + currentData + "。" +
                "请用专业友好的中文回答，适当使用emoji使回复生动。";
        
        // 限制历史记录大小（保留最近10轮对话，即20条消息）
        while (history.size() > MAX_HISTORY_SIZE * 2) {
            history.removeFirst();
        }
        
        // 调用 API（传入完整历史）
        String response = callApiWithHistory(system, new ArrayList<>(history));
        
        // 添加 AI 回复到历史
        Map<String, String> aiMsg = new HashMap<>();
        aiMsg.put("role", "assistant");
        aiMsg.put("content", response);
        history.addLast(aiMsg);
        
        return response;
    }

    /**
     * 清除指定会话的历史记录
     */
    public void clearHistory(String sessionId) {
        conversationHistory.remove(sessionId);
    }

    /**
     * 获取指定会话的历史记录数量
     */
    public int getHistorySize(String sessionId) {
        LinkedList<Map<String, String>> history = conversationHistory.get(sessionId);
        return history == null ? 0 : history.size() / 2; // 除以2因为每轮对话有2条消息
    }
}
