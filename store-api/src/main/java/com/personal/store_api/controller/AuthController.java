package com.personal.store_api.controller;

import com.nimbusds.jose.JOSEException;
import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.AuthenticationRequest;
import com.personal.store_api.dto.request.LogoutRequest;
import com.personal.store_api.dto.response.AuthenticationResponse;
import com.personal.store_api.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authResponse = authService.authenticate(request);
        ApiResponse<AuthenticationResponse> response =  ApiResponse.<AuthenticationResponse>builder().result(authResponse).build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/token")
    public String testToken() {
        return "abc";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/token2")
    public String testToken2() {
        return "abc";
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authService.logout(request);
        return ApiResponse.<Void>builder().build();
    }
}
