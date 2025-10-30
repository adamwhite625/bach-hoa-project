package com.example.frontend2.data.model;

public class User {
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String token;
    private String avatar;  // Đổi từ avatarUrl thành avatar để khớp với backend

    // Getters
    public String get_id() { return _id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }
    public String getAvatar() { return avatar; }  // Đổi getter

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }
}