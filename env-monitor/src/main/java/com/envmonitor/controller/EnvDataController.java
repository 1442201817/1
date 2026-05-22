package com.envmonitor.controller;

import com.envmonitor.entity.MonitorStation;
import com.envmonitor.entity.SysUser;
import com.envmonitor.repository.EnvDataRepository;
import com.envmonitor.repository.StationRepository;
import com.envmonitor.service.EnvDataService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EnvDataController {

    private final EnvDataService envDataService;
    private final StationRepository stationRepository;
    private final EnvDataRepository envDataRepository;

    public EnvDataController(EnvDataService envDataService,
                             StationRepository stationRepository,
                             EnvDataRepository envDataRepository) {
        this.envDataService = envDataService;
        this.stationRepository = stationRepository;
        this.envDataRepository = envDataRepository;
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("currentUser");
        return user != null && !"成员".equals(user.getRole());
    }

    @GetMapping("/stations")
    public List<MonitorStation> getStations() {
        return stationRepository.findAll();
    }

    @PostMapping("/stations")
    public ResponseEntity<?> createStation(@RequestBody MonitorStation station, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        station.setId(null);
        station.setCreatedAt(LocalDateTime.now());
        if (station.getStatus() == null) station.setStatus(MonitorStation.StationStatus.ONLINE);
        return ResponseEntity.ok(stationRepository.save(station));
    }

    @PutMapping("/stations/{id}")
    public ResponseEntity<?> updateStation(@PathVariable Long id,
                                                         @RequestBody MonitorStation station,
                                                         HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        return stationRepository.findById(id).map(existing -> {
            existing.setName(station.getName());
            existing.setLocation(station.getLocation());
            existing.setLat(station.getLat());
            existing.setLng(station.getLng());
            existing.setStatus(station.getStatus());
            return ResponseEntity.ok(stationRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @DeleteMapping("/stations/{id}")
    public ResponseEntity<?> deleteStation(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        if (!stationRepository.existsById(id)) return ResponseEntity.notFound().build();
        envDataRepository.deleteByStationId(id);
        stationRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("msg", "删除成功"));
    }

    @GetMapping("/overview")
    public List<Map<String, Object>> getOverview() {
        return envDataService.getOverview();
    }

    @GetMapping("/stations/{id}/latest")
    public Object getLatest(@PathVariable Long id) {
        var data = envDataService.getLatestByStation(id);
        if (data == null) return Map.of("error", "暂无数据");
        return data;
    }

    @GetMapping("/stations/{id}/trend")
    public List<Map<String, Object>> getTrend(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") int hours) {
        return envDataService.getTrend(id, hours);
    }
}
