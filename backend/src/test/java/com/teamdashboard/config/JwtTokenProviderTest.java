package com.teamdashboard.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "team-dashboard-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256",
                86400000L,
                604800000L
        );
    }

    @Test
    @DisplayName("should generate and validate access token")
    void generateAndValidateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com", "MEMBER");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("should include role claim in access token")
    void accessTokenContainsRole() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com", "ADMIN");

        Optional<Claims> claims = jwtTokenProvider.parseClaims(token);
        assertThat(claims).isPresent();
        assertThat(claims.get().get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("should generate and validate refresh token")
    void generateAndValidateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("should return empty for invalid token")
    void invalidTokenParseClaims() {
        assertThat(jwtTokenProvider.parseClaims("invalid-token")).isEmpty();
    }

    @Test
    @DisplayName("should fail validation for invalid token")
    void invalidTokenValidation() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    @DisplayName("should fail validation for empty token")
    void emptyTokenValidation() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }
}
