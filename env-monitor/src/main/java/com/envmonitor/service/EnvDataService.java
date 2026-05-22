package com.envmonitor.service;

import com.envmonitor.entity.EnvData;
import com.envmonitor.entity.MonitorStation;
import com.envmonitor.repository.EnvDataRepository;
import com.envmonitor.repository.StationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnvDataService {

    private final EnvDataRepository envDataRepository;
    private final StationRepository stationRepository;
    private final QweatherService qweatherService;

    public EnvDataService(EnvDataRepository envDataRepository,
                          StationRepository stationRepository,
                          QweatherService qweatherService) {
        this.envDataRepository = envDataRepository;
        this.stationRepository = stationRepository;
        this.qweatherService = qweatherService;
    }

    @Scheduled(fixedDelay = 900000)  // 每15分钟（避免超出免费额度）
    public void simulateRealTimeData() {
        for (MonitorStation station : stationRepository.findAll()) {
            EnvData realData = qweatherService.fetchRealTimeData(station);
            if (realData != null) {
                envDataRepository.save(realData);
                System.out.println("✅ 已同步站点 [" + station.getName() + "] 的真实数据");
            } else {
                System.err.println("❌ 同步站点 [" + station.getName() + "] 数据失败");
            }
        }
    }

    public String resolveAqiLevel(int aqi) {
        if (aqi <= 50)  return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    public List<Map<String, Object>> getOverview() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MonitorStation station : stationRepository.findAll()) {
            EnvData d = envDataRepository.findTopByStationIdOrderByRecordedAtDesc(station.getId());
            if (d == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stationId",   station.getId());
            item.put("stationName", station.getName());
            item.put("location",    station.getLocation());
            item.put("status",      station.getStatus());
            item.put("aqi",         d.getAqi());
            item.put("aqiLevel",    d.getAqiLevel());
            item.put("pm25",        d.getPm25());
            item.put("pm10",        d.getPm10());
            item.put("co",          d.getCo());
            item.put("temperature", d.getTemperature());
            item.put("humidity",    d.getHumidity());
            item.put("windSpeed",   d.getWindSpeed());
            item.put("windDirection", d.getWindDirection());
            item.put("recordedAt",  d.getRecordedAt().toString());
            result.add(item);
        }
        return result;
    }

    public EnvData getLatestByStation(Long stationId) {
        return envDataRepository.findTopByStationIdOrderByRecordedAtDesc(stationId);
    }

    public List<Map<String, Object>> getTrend(Long stationId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Map<String, Object>> result = new ArrayList<>();
        for (EnvData d : envDataRepository.findByStationIdAndRecordedAtAfterOrderByRecordedAtAsc(stationId, since)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time",        d.getRecordedAt().toString().substring(11, 16));
            item.put("aqi",         d.getAqi());
            item.put("pm25",        d.getPm25());
            item.put("pm10",        d.getPm10());
            item.put("temperature", d.getTemperature());
            item.put("humidity",    d.getHumidity());
            item.put("co",          d.getCo());
            result.add(item);
        }
        return result;
    }
}
