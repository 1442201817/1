package com.envmonitor.controller;

import com.envmonitor.entity.SysUser;
import com.envmonitor.repository.SysUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    private final SysUserRepository repo;

    public SysUserController(SysUserRepository repo) {
        this.repo = repo;
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("currentUser");
        return user != null && !"成员".equals(user.getRole());
    }

    /**
     * 获取用户列表 - 需要管理员权限
     */
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权访问，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(repo.findByNameContainingOrRoleContaining(keyword, keyword));
        }
        return ResponseEntity.ok(repo.findAll());
    }

    /**
     * 创建用户 - 需要管理员权限
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SysUser user, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        user.setId(null);
        user.setCreateTime(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(user));
    }

    /**
     * 更新用户 - 需要管理员权限
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysUser user, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "无权操作，需要管理员权限");
            return ResponseEntity.status(403).body(error);
        }
        
        return repo.findById(id).map(existing -> {
            existing.setName(user.getName());
            existing.setRole(user.getRole());
            existing.setPhone(user.getPhone());
            existing.setEmail(user.getEmail());
            existing.setStatus(user.getStatus());
            existing.setRemark(user.getRemark());
            existing.setUsername(user.getUsername());
            existing.setPassword(user.getPassword());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除用户 - 需要管理员权限
     */
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
}
