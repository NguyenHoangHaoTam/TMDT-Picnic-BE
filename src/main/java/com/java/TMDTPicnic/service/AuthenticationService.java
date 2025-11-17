package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.AuthenticationRequest;
import com.java.TMDTPicnic.dto.request.ForgotPasswordRequest;
import com.java.TMDTPicnic.dto.request.IntrospectRequest;
import com.java.TMDTPicnic.dto.request.RegisterRequest;
import com.java.TMDTPicnic.dto.response.AuthenticationResponse;
import com.java.TMDTPicnic.entity.InvalidatedToken;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.enums.Role;
import com.java.TMDTPicnic.exception.AppException;
import com.java.TMDTPicnic.exception.ErrorCode;
import com.java.TMDTPicnic.repository.InvalidatedTokenRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final EmailService emailService;
    private final ConcurrentHashMap<String, AbstractMap.SimpleEntry<String, LocalDateTime>> verificationCodes = new ConcurrentHashMap<>();

    public final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid_duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh_token}")
    protected long REFRESH_INTERVAL;

    public String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        String userId = String.valueOf(user.getId());
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                //.subject(user.getUserName());
                .subject(userId)
                .issuer("TMDTPicnic.com")
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .claim("username", user.getUsername())
                .claim("type", "access_token")
                .build();
        return getSignerToken(jwsHeader, jwtClaimsSet);
    }

    public String generateRefreshToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("TMDTPicnic.com")
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(REFRESH_INTERVAL, ChronoUnit.DAYS).toEpochMilli()))
                .claim("type", "refresh_token")
                .build();
        return getSignerToken(jwsHeader, jwtClaimsSet);
    }

    private String getSignerToken(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet) {
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        var token = generateToken(user);
        var refreshToken = generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    public String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        String role = String.valueOf(user.getRole());
        if (role != null && !role.trim().isEmpty()) {
            joiner.add("ROLE_" + role.trim());
        }
        return joiner.toString();
    }

    public void logout(IntrospectRequest request, String refreshToken) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken(), false);
        String accessTokenJti = signToken.getJWTClaimsSet().getJWTID();
        Date accessTokenExpiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(accessTokenJti)
                .expiryTime(accessTokenExpiryTime)
                .build());

        if (refreshToken != null) {
            var signRefreshToken = verifyToken(refreshToken, true);
            String refreshTokenJti = signRefreshToken.getJWTClaimsSet().getJWTID();
            Date refreshTokenExpiryTime = signRefreshToken.getJWTClaimsSet().getExpirationTime();

            invalidatedTokenRepository.save(InvalidatedToken.builder()
                    .id(refreshTokenJti)
                    .expiryTime(refreshTokenExpiryTime)
                    .build());
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(verifier)) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (isRefresh) {
            String type = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (!"refresh_token".equals(type)) throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public AuthenticationResponse refreshAccessToken(String refreshToken) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(refreshToken, true);
        String username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(generateToken(user))
                .refreshToken(refreshToken)
                .build();
    }

    private String generateVerificationCode() {
        int length = 6;
        String characters = "0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }
    public void generateVerificationCode(ForgotPasswordRequest request) throws IOException, MessagingException {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        String code = generateVerificationCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        emailService.sendResetCode(request.getEmail(), code);
        verificationCodes.put(request.getEmail(), new AbstractMap.SimpleEntry<>(code, expiry));
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.USER)   // mặc định USER
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

}
