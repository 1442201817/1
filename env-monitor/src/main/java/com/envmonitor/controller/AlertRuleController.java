package com.envmonitor.controller;

import com.envmonitor.entity.AlertRule;
import com.envmonitor.entity.SysUser;
import com.envmonitor.repository.AlertRuleRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleController {

    private final AlertRuleRepository repo;

    public AlertRuleController(AlertRuleRepository repo) {
        this.repo = repo;
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("currentUser");
        return user != null && !"成员".equals(user.getRole());
    }

    @GetMapping
    public List<AlertRule> list() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AlertRule rule, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        rule.setId(null);
        rule.setCreateTime(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AlertRule rule, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        return repo.findById(id).map(existing -> {
            existing.setMetricName(rule.getMetricName());
            existing.setThreshold(rule.getThreshold());
            existing.setOperator(rule.getOperator());
            existing.setLevel(rule.getLevel());
            existing.setEnabled(rule.getEnabled());
            existing.setDescription(rule.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("msg", "删除成功"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        return repo.findById(id).map(r -> {
            r.setEnabled(!r.getEnabled());
            return ResponseEntity.ok(repo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }
}
