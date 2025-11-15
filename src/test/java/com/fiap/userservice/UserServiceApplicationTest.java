package com.fiap.userservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTest {

    @Test
    @DisplayName("main chama SpringApplication.run com os argumentos fornecidos")
    void testMainCallsSpringApplicationRunWithArgs() {
        // Arrange
        String[] args = new String[] { "arg1", "arg2" };

        try (MockedStatic<org.springframework.boot.SpringApplication> mocked = Mockito.mockStatic(org.springframework.boot.SpringApplication.class)) {
            // Act
            UserServiceApplication.main(args);

            // Assert & Verify
            mocked.verify(() -> org.springframework.boot.SpringApplication.run(UserServiceApplication.class, args), times(1));
        }
    }

    @Test
    @DisplayName("main chama SpringApplication.run quando args é nulo")
    void testMainCallsSpringApplicationRunWithNullArgs() {
        // Arrange
        String[] args = null;

        try (MockedStatic<org.springframework.boot.SpringApplication> mocked = Mockito.mockStatic(org.springframework.boot.SpringApplication.class)) {
            // Act
            UserServiceApplication.main(args);

            // Assert & Verify
            mocked.verify(() -> org.springframework.boot.SpringApplication.run(UserServiceApplication.class, (String[]) null), times(1));
        }
    }
}