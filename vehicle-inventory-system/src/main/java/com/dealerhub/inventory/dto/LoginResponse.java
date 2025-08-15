package com.dealerhub.inventory.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String token;
    private final String tokenType = "Bearer";
    private final long expiresInSeconds;
    private final String username;

    public LoginResponse(String token, long expiresInSeconds, String username) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
        this.username = username;
    }
}
