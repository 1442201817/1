package com.envmonitor.repository;

import com.envmonitor.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    List<SysUser> findByNameContainingOrRoleContaining(String name, String role);
}
