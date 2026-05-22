package com.envmonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "env_data")
public class EnvData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MonitorStation station;

    private Integer aqi;
    private Double pm25;
    private Double pm10;

    // CO (一氧化碳) 浓度，单位：mg/m³ 或 ppm
    private Double co;

    private Double temperature;
    private Double humidity;
    private Double windSpeed;
    private String windDirection;
    private String aqiLevel;
    private String weatherDescription; // 天气状况描述（如：多云、晴、雨等）
    private LocalDateTime recordedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MonitorStation getStation() { return station; }
    public void setStation(MonitorStation station) { this.station = station; }
    public Integer getAqi() { return aqi; }
    public void setAqi(Integer aqi) { this.aqi = aqi; }
    public Double getPm25() { return pm25; }
    public void setPm25(Double pm25) { this.pm25 = pm25; }
    public Double getPm10() { return pm10; }
    public void setPm10(Double pm10) { this.pm10 = pm10; }
    public Double getCo() { return co; }
    public void setCo(Double co) { this.co = co; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }
    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }
    public String getAqiLevel() { return aqiLevel; }
    public void setAqiLevel(String aqiLevel) { this.aqiLevel = aqiLevel; }
    public String getWeatherDescription() { return weatherDescription; }
    public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
