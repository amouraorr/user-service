package com.fiap.userservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação de usuário.
 */
public class CreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 128)
    private String password;

    private String phone;

    // Para porteiro pode ser null/empty
    private String apartment;

    public CreateUserRequest() {}

    public CreateUserRequest(String username, String password, String phone, String apartment) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.apartment = apartment;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getApartment() { return apartment; }
    public void setApartment(String apartment) { this.apartment = apartment; }
}