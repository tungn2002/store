package com.personal.store_api.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.repository.InvalidatedTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public JwtTokenProvider(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Value("${jwt.signerKey}")
    private String jwtSecret;

    @Value("${jwt.valid-duration}")
    private long validDuration;

    public String generateToken(Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("pos-api")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                jwtClaimsSet
        );

        try {
            signedJWT.sign(new MACSigner(jwtSecret.getBytes()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(UserPrincipal user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .collect(Collectors.joining(" "));
    }

    public SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        SignedJWT jwt = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());

        Date expiryTime = jwt.getJWTClaimsSet().getExpirationTime();

        var verified = jwt.verify(verifier);

        if(!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(jwt.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return jwt;
    }
}
