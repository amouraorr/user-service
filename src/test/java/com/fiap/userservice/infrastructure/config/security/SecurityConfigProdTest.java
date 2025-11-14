package com.fiap.userservice.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigProdTest {

    @Test
    @DisplayName("Deve ter anotação @Profile com valor 'prod' e método bean chamado 'securityFilterChainProd'")
    void shouldHaveProfileProdAndBeanName() throws NoSuchMethodException {
        // Arrange
        Class<SecurityConfigProd> clazz = SecurityConfigProd.class;

        // Act
        Profile profile = clazz.getAnnotation(Profile.class);
        Method method = clazz.getDeclaredMethod("securityFilterChain", HttpSecurity.class);
        Bean bean = method.getAnnotation(Bean.class);

        // Assert
        assertNotNull(profile, "Classe deve ter anotação @Profile");
        assertArrayEquals(new String[]{"prod"}, profile.value(), "Profile deve ser 'prod'");

        assertNotNull(bean, "Método deve ter anotação @Bean");

        String[] names = bean.value();
        if (names == null || names.length == 0 || (names.length == 1 && names[0].isEmpty())) {

            try {
                Method nameMethod = Bean.class.getMethod("name");
                String[] nameAttr = (String[]) nameMethod.invoke(bean);
                assertTrue(nameAttr.length > 0 && "securityFilterChainProd".equals(nameAttr[0]),
                        "Nome do bean deve ser 'securityFilterChainProd'");
            } catch (NoSuchMethodException ignored) {
                fail("Não foi possível validar o nome do bean; verifique a versão da annotation @Bean");
            } catch (Exception ex) {
                fail("Erro ao acessar atributo 'name' da annotation @Bean: " + ex.getMessage());
            }
        } else {
            assertTrue(names.length > 0 && "securityFilterChainProd".equals(names[0]),
                    "Nome do bean deve ser 'securityFilterChainProd'");
        }
    }
}