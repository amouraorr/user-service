package com.fiap.userservice.application.dto.request;

import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para atualização de usuário.
 */
public class UpdateUserRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 6, max = 128)
    private String password;

    @Size(max = 50)
    private String phone;

    @Size(max = 50)
    private String apartment;

    private String role;

    public UpdateUserRequest() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }
}