package com.personal.store_api.controller;

import com.nimbusds.jose.JOSEException;
import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.AuthenticationRequest;
import com.personal.store_api.dto.request.LogoutRequest;
import com.personal.store_api.dto.request.RegisterRequest;
import com.personal.store_api.dto.response.AuthenticationResponse;
import com.personal.store_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

/**
 * Controller for authentication operations (login, register, logout).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request) {
        AuthenticationResponse authResponse = authService.register(request);
        return ResponseEntity.ok(buildResponse(authResponse));
    }

    /**
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @RequestBody AuthenticationRequest request) {
        AuthenticationResponse authResponse = authService.authenticate(request);
        return ResponseEntity.ok(buildResponse(authResponse));
    }

    /**
     * Test endpoint for token validation.
     */
    @GetMapping("/token")
    @PreAuthorize("hasAuthority('auth.token')")
    public String testToken() {
        return "OK";
    }

    /**
     * Test endpoint for token validation with different permission.
     */
    @GetMapping("/token2")
    @PreAuthorize("hasAuthority('auth.token2')")
    public String testToken2() {
        return "OK";
    }

    /**
     * Logout user and invalidate token.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authService.logout(request);
        return ResponseEntity.ok(buildResponse());
    }

    /**
     * Build success response with result.
     */
    private <T> ApiResponse<T> buildResponse(T result) {
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }

    /**
     * Build success response without result.
     */
    private ApiResponse<Void> buildResponse() {
        return ApiResponse.<Void>builder()
                .build();
    }
}
