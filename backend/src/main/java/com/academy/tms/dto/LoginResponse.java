package com.academy.tms.dto;

public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresInMs;
    private Long id;
    private String username;
    private String name;
    private String email;
    private String role;

    public LoginResponse(String token, long expiresInMs, Long id, String username,
                         String name, String email, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresInMs = expiresInMs;
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}