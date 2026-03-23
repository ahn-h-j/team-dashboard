package com.teamdashboard.domain.user;

import com.teamdashboard.common.AppException;
import com.teamdashboard.config.JwtTokenProvider;
import com.teamdashboard.domain.user.dto.LoginRequest;
import com.teamdashboard.domain.user.dto.SignupRequest;
import com.teamdashboard.domain.user.dto.TokenRequest;
import com.teamdashboard.domain.user.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .name("Test User")
                .role("MEMBER")
                .build();
    }

    @Nested
    @DisplayName("signup")
    class Signup {

        @Test
        @DisplayName("should return tokens on successful signup")
        void signupSuccess() {
            SignupRequest request = new SignupRequest("test@example.com", "Password1!", "Test User");

            given(userRepository.existsByEmail("test@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password1!")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(testUser);
            given(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(604800000L);
            given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);

            TokenResponse response = authService.signup(request);

            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("should throw exception on duplicate email")
        void signupDuplicateEmail() {
            SignupRequest request = new SignupRequest("test@example.com", "Password1!", "Test User");

            given(userRepository.existsByEmail("test@example.com")).willReturn(true);

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return tokens on successful login")
        void loginSuccess() {
            LoginRequest request = new LoginRequest("test@example.com", "Password1!");

            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("Password1!", "encodedPassword")).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(604800000L);
            given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);

            TokenResponse response = authService.login(request);

            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("should throw exception when email not found")
        void loginUserNotFound() {
            LoginRequest request = new LoginRequest("unknown@example.com", "Password1!");

            given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AppException.class);
        }

        @Test
        @DisplayName("should throw exception on wrong password")
        void loginWrongPassword() {
            LoginRequest request = new LoginRequest("test@example.com", "WrongPass1!");

            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("WrongPass1!", "encodedPassword")).willReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("should return new tokens with valid refresh token")
        void refreshSuccess() {
            TokenRequest request = new TokenRequest("valid-refresh-token");
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .token("valid-refresh-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusMillis(604800000))
                    .build();

            given(jwtTokenProvider.validateToken("valid-refresh-token")).willReturn(true);
            given(refreshTokenRepository.findByToken("valid-refresh-token")).willReturn(Optional.of(refreshToken));
            given(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).willReturn("new-access-token");
            given(jwtTokenProvider.generateRefreshToken(any())).willReturn("new-refresh-token");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(604800000L);
            given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);

            TokenResponse response = authService.refresh(request);

            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
            verify(refreshTokenRepository).delete(refreshToken);
        }

        @Test
        @DisplayName("should throw exception when JWT signature is invalid")
        void refreshInvalidJwtSignature() {
            TokenRequest request = new TokenRequest("tampered-token");

            given(jwtTokenProvider.validateToken("tampered-token")).willReturn(false);

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(AppException.class);
        }

        @Test
        @DisplayName("should throw exception on expired refresh token")
        void refreshExpiredToken() {
            TokenRequest request = new TokenRequest("expired-refresh-token");
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .token("expired-refresh-token")
                    .user(testUser)
                    .expiryDate(Instant.now().minusMillis(1000))
                    .build();

            given(jwtTokenProvider.validateToken("expired-refresh-token")).willReturn(true);
            given(refreshTokenRepository.findByToken("expired-refresh-token")).willReturn(Optional.of(refreshToken));

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should delete refresh token on logout")
        void logoutSuccess() {
            TokenRequest request = new TokenRequest("valid-refresh-token");
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .token("valid-refresh-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusMillis(604800000))
                    .build();

            given(refreshTokenRepository.findByToken("valid-refresh-token")).willReturn(Optional.of(refreshToken));

            authService.logout(request);

            verify(refreshTokenRepository).delete(refreshToken);
        }

        @Test
        @DisplayName("should throw exception on invalid refresh token")
        void logoutInvalidToken() {
            TokenRequest request = new TokenRequest("invalid-refresh-token");

            given(refreshTokenRepository.findByToken("invalid-refresh-token")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(AppException.class);
        }
    }
}
