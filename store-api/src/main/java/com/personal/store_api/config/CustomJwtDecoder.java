package com.personal.store_api.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.personal.store_api.repository.InvalidatedTokenRepository;
import com.personal.store_api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

/**
 * Custom JWT Decoder that adds revoked token checking on top of standard JWT validation.
 * Note: Revoked token check is already performed in JwtTokenProvider.verifyToken()
 */
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.signerKey}")
    private String jwtSecret;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Verify token signature and check revoked status (already done in JwtTokenProvider)
            jwtTokenProvider.verifyToken(token);

            // Decode token with lazy-initialized decoder
            return getNimbusJwtDecoder().decode(token);

        } catch (JOSEException | ParseException e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Lazy initialization of NimbusJwtDecoder using singleton pattern
     */
    private NimbusJwtDecoder getNimbusJwtDecoder() {
        if (nimbusJwtDecoder == null) {
            nimbusJwtDecoder = createNimbusJwtDecoder();
        }
        return nimbusJwtDecoder;
    }

    private NimbusJwtDecoder createNimbusJwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(), "HS256");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
