package com.hrms.backend.controller;

import com.hrms.backend.entity.Role;
import com.hrms.backend.entity.User;
import com.hrms.backend.entity.Permission;
import com.hrms.backend.repository.RoleRepository;
import com.hrms.backend.repository.UserRepository;
import com.hrms.backend.repository.PermissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public AdminController(RoleRepository roleRepository, UserRepository userRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> updateRolePermissions(@PathVariable Integer id, @RequestBody List<Integer> permissionIds) {
        return roleRepository.findById(id).map(role -> {
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            role.setPermissions(permissions.stream().collect(Collectors.toSet()));
            role.setUpdatedAt(java.time.LocalDateTime.now());
            return ResponseEntity.ok(roleRepository.save(role));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<User> updateUserStatus(@PathVariable Integer id, @RequestBody Map<String, String> statusUpdate) {
        return userRepository.findById(id).map(user -> {
            if (statusUpdate.containsKey("status")) {
                user.setStatus(statusUpdate.get("status"));
                user.setUpdatedAt(java.time.LocalDateTime.now());
                return ResponseEntity.ok(userRepository.save(user));
            }
            return ResponseEntity.badRequest().body(user);
        }).orElse(ResponseEntity.notFound().build());
    }
}
