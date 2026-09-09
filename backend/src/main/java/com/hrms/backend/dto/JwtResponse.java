package com.hrms.backend.dto;
import java.util.List;
public class JwtResponse {
    private String token;
    private String email;
    private List<String> roles;
    private List<String> permissions;
    public JwtResponse(String token, String email, List<String> roles, List<String> permissions) {
        this.token = token; this.email = email; this.roles = roles; this.permissions = permissions;
    }
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }
}
