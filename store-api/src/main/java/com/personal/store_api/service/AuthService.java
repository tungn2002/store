package com.personal.store_api.service;

import com.nimbusds.jose.JOSEException;
import com.personal.store_api.dto.request.AuthenticationRequest;
import com.personal.store_api.dto.request.LogoutRequest;
import com.personal.store_api.dto.response.AuthenticationResponse;
import com.personal.store_api.entity.InvalidatedToken;
import com.personal.store_api.repository.InvalidatedTokenRepository;
import com.personal.store_api.repository.UserRepository;
import com.personal.store_api.security.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return AuthenticationResponse.builder().token(token).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = tokenProvider.verifyToken(request.getToken());

        String jti=signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

}
