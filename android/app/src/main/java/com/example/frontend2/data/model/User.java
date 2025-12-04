package com.example.frontend2.data.model;

import java.io.Serializable;

public class User implements Serializable { // Implement Serializable
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String token;
    private String avatar;
    private String phone;
    private String gender;

    // Getters
    public String get_id() { return _id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }
    public String getAvatar() { return avatar; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }

    public String getFullName() {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        } else if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        } else if (lastName != null && !lastName.isEmpty()) {
            return lastName;
        }
        return email;
    }
}
