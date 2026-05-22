package com.envmonitor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monitor_station")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MonitorStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private Double lat;
    private Double lng;

    @Enumerated(EnumType.STRING)
    private StationStatus status = StationStatus.ONLINE;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum StationStatus { ONLINE, OFFLINE, MAINTENANCE }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public StationStatus getStatus() { return status; }
    public void setStatus(StationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
