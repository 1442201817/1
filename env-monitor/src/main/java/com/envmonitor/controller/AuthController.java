package com.envmonitor.controller;

import com.envmonitor.entity.SysUser;
import com.envmonitor.repository.SysUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserRepository userRepository;

    public AuthController(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        // 查找用户
        List<SysUser> users = userRepository.findAll();
        SysUser user = users.stream()
                .filter(u -> username.equals(u.getUsername()) && password.equals(u.getPassword()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(result);
        }

        if ("禁用".equals(user.getStatus())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "该账户已被禁用");
            return ResponseEntity.status(403).body(result);
        }

        // 登录成功，保存用户信息到Session
        session.setAttribute("currentUser", user);
        
        // 判断是否为管理员（角色不是"成员"的都是管理员）
        boolean isAdmin = !"成员".equals(user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "role", user.getRole(),
                "isAdmin", isAdmin
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("currentUser");
        
        if (user == null) {
            // 未登录，返回游客身份
            Map<String, Object> result = new HashMap<>();
            result.put("loggedIn", false);
            result.put("user", Map.of(
                    "name", "游客",
                    "role", "游客",
                    "isAdmin", false
            ));
            return ResponseEntity.ok(result);
        }

        boolean isAdmin = !"成员".equals(user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("loggedIn", true);
        result.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "role", user.getRole(),
                "isAdmin", isAdmin
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.removeAttribute("currentUser");
        session.invalidate();
        
        Map<String, String> result = new HashMap<>();
        result.put("message", "已退出登录");
        return ResponseEntity.ok(result);
    }

    /**
     * 检查是否有管理员权限
     */
    @GetMapping("/check-admin")
    public ResponseEntity<Map<String, Object>> checkAdmin(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("currentUser");
        
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("isAdmin", false);
            result.put("message", "未登录");
            return ResponseEntity.ok(result);
        }

        boolean isAdmin = !"成员".equals(user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", isAdmin);
        return ResponseEntity.ok(result);
    }
}
