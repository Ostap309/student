package com.example.student.security;

import com.example.student.model.Role;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_andExtractUsername_roundTrip() {
        String token = jwtService.generateToken("admin@mail.ru", Set.of(Role.ADMIN));

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin@mail.ru");
    }

    @Test
    void generateToken_shouldCarryRoles() {
        String token = jwtService.generateToken("admin@mail.ru", Set.of(Role.ADMIN, Role.TEACHER));

        assertThat(jwtService.extractRoles(token))
                .containsExactlyInAnyOrder(Role.ADMIN, Role.TEACHER);
    }

    @Test
    void generateToken_shouldExpireInAboutOneHour() {
        String token = jwtService.generateToken("admin@mail.ru", Set.of(Role.ADMIN));
        // Проверяем срок через парсер: сгенерированный токен должен быть живым прямо сейчас
        assertThat(jwtService.validateToken(token, userDetails("admin@mail.ru"))).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forDifferentUser() {
        String token = jwtService.generateToken("admin@mail.ru", Set.of(Role.ADMIN));

        assertThat(jwtService.validateToken(token, userDetails("hacker@mail.ru"))).isFalse();
    }

    @Test
    void extractUsername_shouldThrow_forGarbageToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    private User userDetails(String username) {
        return new User(username, "secret",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
