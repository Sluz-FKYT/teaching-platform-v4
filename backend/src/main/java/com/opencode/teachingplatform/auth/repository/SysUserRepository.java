package com.opencode.teachingplatform.auth.repository;

import com.opencode.teachingplatform.auth.entity.SysUser;
import com.opencode.teachingplatform.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsernameAndRole(String username, UserRole role);
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
