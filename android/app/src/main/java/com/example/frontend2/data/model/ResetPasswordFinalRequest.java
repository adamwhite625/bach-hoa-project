package com.example.frontend2.data.model;

public class ResetPasswordFinalRequest {
    private String email;
    private String token;
    private String password;

    public ResetPasswordFinalRequest(String email, String token, String password) {
        this.email = email;
        this.token = token;
        this.password = password;
    }
}
