package com.example.frontend2.data.model;

public class User {
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String token;

    public String get_id() {
        return _id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}