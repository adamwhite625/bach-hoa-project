package com.example.frontend2.data.model;

public class ResetPasswordRequest {
    private String email;
    private String newPassword;

    public ResetPasswordRequest(String email, String newPassword) {
        this.email = email;
        this.newPassword = newPassword;
    }
}
