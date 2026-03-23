package com.teamdashboard.domain.user;

import com.teamdashboard.common.AppException;
import com.teamdashboard.config.JwtTokenProvider;
import com.teamdashboard.domain.user.dto.LoginRequest;
import com.teamdashboard.domain.user.dto.SignupRequest;
import com.teamdashboard.domain.user.dto.TokenRequest;
import com.teamdashboard.domain.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.badRequest("이미 사용 중인 이메일입니다");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role("MEMBER")
                .build();

        userRepository.save(user);

        return createTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AppException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw AppException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        refreshTokenRepository.deleteByUser(user);

        return createTokens(user);
    }

    @Transactional
    public TokenResponse refresh(TokenRequest request) {
        String tokenStr = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(tokenStr)) {
            throw AppException.unauthorized("유효하지 않은 리프레시 토큰입니다");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> AppException.unauthorized("유효하지 않은 리프레시 토큰입니다"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw AppException.unauthorized("리프레시 토큰이 만료되었습니다");
        }

        User user = refreshToken.getUser();

        refreshTokenRepository.delete(refreshToken);

        return createTokens(user);
    }

    @Transactional
    public void logout(TokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> AppException.unauthorized("유효하지 않은 리프레시 토큰입니다"));

        refreshTokenRepository.delete(refreshToken);
    }

    private TokenResponse createTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenStr);
    }
}
