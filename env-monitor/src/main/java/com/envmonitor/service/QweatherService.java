package com.envmonitor.service;

import com.envmonitor.entity.EnvData;
import com.envmonitor.entity.MonitorStation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.zip.GZIPInputStream;

@Service
public class QweatherService {

    @Value("${qweather.api.key}")
    private String apiKey;

    @Value("${qweather.api.host:https://devapi.qweather.com}")
    private String apiHost;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 获取指定站点的实时环境数据
     */
    public EnvData fetchRealTimeData(MonitorStation station) {
        try {
            double lat = station.getLat();
            double lon = station.getLng();

            System.out.println("🌤 开始获取站点 [" + station.getName() + "] 的数据...");
            System.out.println("   API Host: " + apiHost);
            System.out.println("   坐标: " + lat + ", " + lon);

            JsonNode weatherData = fetchWeatherData(lat, lon);
            JsonNode airData = fetchAirQualityData(lat, lon);

            if (weatherData == null || airData == null) {
                System.err.println("❌ 站点 [" + station.getName() + "] 数据获取失败");
                return null;
            }

            EnvData envData = new EnvData();
            envData.setStation(station);
            envData.setRecordedAt(LocalDateTime.now());

            parseWeatherData(envData, weatherData);
            parseAirQualityData(envData, airData);

            System.out.println("✅ 站点 [" + station.getName() + "] 数据获取成功: AQI=" + envData.getAqi());
            return envData;

        } catch (Exception e) {
            System.err.println("❌ 站点 [" + station.getName() + "] 异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取实时天气数据
     */
    private JsonNode fetchWeatherData(double lat, double lon) throws Exception {
        String url = String.format("%s/v7/weather/now?location=%.2f,%.2f&unit=m",
                apiHost, lon, lat);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-QW-Api-Key", apiKey)
                .header("Accept-Encoding", "gzip")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        System.out.println("   📡 Weather API 响应码: " + response.statusCode());

        if (response.statusCode() == 200) {
            String body = decompressGzip(response.body());
            JsonNode root = mapper.readTree(body);
            String code = root.path("code").asText();

            if ("200".equals(code)) {
                System.out.println("   ✅ Weather API 调用成功");
                return root;
            } else {
                System.err.println("   ⚠️ Weather API 业务错误: code=" + code);
                System.err.println("   响应内容: " + body);
                return null;
            }
        } else {
            System.err.println("   ❌ Weather API HTTP error: " + response.statusCode());
            System.err.println("   响应内容: " + new String(response.body()));
            return null;
        }
    }

    /**
     * 获取实时空气质量数据
     */
    private JsonNode fetchAirQualityData(double lat, double lon) throws Exception {
        String url = String.format("%s/airquality/v1/current/%.2f/%.2f",
                apiHost, lat, lon);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-QW-Api-Key", apiKey)
                .header("Accept-Encoding", "gzip")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        System.out.println("   📡 Air Quality API 响应码: " + response.statusCode());

        if (response.statusCode() == 200) {
            String body = decompressGzip(response.body());
            JsonNode root = mapper.readTree(body);
            
            // 空气质量 API v1 版本不返回 code 字段，直接检查是否有 indexes 数据
            if (root.has("indexes") && root.has("pollutants")) {
                System.out.println("   ✅ Air Quality API 调用成功");
                return root;
            } else {
                System.err.println("   ⚠️ Air Quality API 响应格式异常");
                System.err.println("   响应内容: " + body);
                return null;
            }
        } else {
            System.err.println("   ❌ Air Quality API HTTP error: " + response.statusCode());
            System.err.println("   响应内容: " + new String(response.body()));
            return null;
        }
    }

    /**
     * 解析天气数据
     */
    private void parseWeatherData(EnvData envData, JsonNode weatherData) {
        JsonNode now = weatherData.path("now");

        envData.setTemperature(now.path("temp").asDouble());
        envData.setHumidity(now.path("humidity").asDouble());

        double windSpeedKmh = now.path("windSpeed").asDouble();
        envData.setWindSpeed(round(windSpeedKmh / 3.6));

        envData.setWindDirection(now.path("windDir").asText());
        
        // 保存天气状况描述（如：多云、晴、雨等）
        envData.setWeatherDescription(now.path("text").asText());
    }

    /**
     * 解析空气质量数据
     */
    private void parseAirQualityData(EnvData envData, JsonNode airData) {
        // 从 indexes 数组中获取 AQI 信息
        JsonNode indexes = airData.path("indexes");
        if (indexes.isArray() && indexes.size() > 0) {
            JsonNode firstIndex = indexes.get(0);
            int aqi = firstIndex.path("aqi").asInt();
            String category = firstIndex.path("category").asText();
            
            envData.setAqi(aqi);
            envData.setAqiLevel(category);
        }
        
        // 从 pollutants 数组中获取污染物浓度
        JsonNode pollutants = airData.path("pollutants");
        if (pollutants.isArray()) {
            for (JsonNode pollutant : pollutants) {
                String code = pollutant.path("code").asText();
                double concentration = pollutant.path("concentration").path("value").asDouble();

                switch (code) {
                    case "pm2p5":
                        envData.setPm25(concentration);
                        break;
                    case "pm10":
                        envData.setPm10(concentration);
                        break;
                    case "co":
                        envData.setCo(concentration);
                        break;
                }
            }
        }
    }

    /**
     * 根据 AQI 值解析空气质量等级
     */
    private String resolveAqiLevel(int aqi) {
        if (aqi <= 50)  return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    /**
     * 四舍五入保留两位小数
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * 解压 Gzip 数据
     */
    private String decompressGzip(byte[] compressed) throws IOException {
        // 检查是否为 Gzip 格式（前两个字节应该是 0x1F 0x8B）
        if (compressed.length < 2 || compressed[0] != 0x1F || compressed[1] != (byte) 0x8B) {
            // 不是 Gzip 数据，直接返回字符串
            return new String(compressed);
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
             GZIPInputStream gzis = new GZIPInputStream(bais)) {
            
            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, len));
            }
            return sb.toString();
        }
    }
}
