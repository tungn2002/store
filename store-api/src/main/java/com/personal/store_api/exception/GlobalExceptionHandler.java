package com.personal.store_api.exception;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Handles validation errors, authentication errors, and application-specific exceptions.
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtils messageUtils;

    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        String message = buildValidationMessage(exception.getBindingResult().getFieldErrors());
        return ResponseEntity.badRequest().body(createErrorResponse(ErrorCode.INVALID_KEY, message));
    }

    /**
     * Handle binding errors from @ModelAttribute annotations.
     */
    @ExceptionHandler(BindException.class)
    ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        String message = buildValidationMessage(exception.getBindingResult().getFieldErrors());
        return ResponseEntity.badRequest().body(createErrorResponse(ErrorCode.INVALID_KEY, message));
    }

    /**
     * Handle bad credentials error (invalid username/password).
     */
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException exception) {
        log.warn("Bad credentials attempt: {}", exception.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getStatusCode())
                .body(createErrorResponse(ErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * Handle application-specific exceptions.
     */
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        // Handle AppException without ErrorCode (custom message only)
        if (errorCode == null) {
            log.error("AppException without error code: {}", exception.getMessage(), exception);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                    .message(exception.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        log.error("AppException: {}", errorCode.getMessage(), exception);
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(createErrorResponse(errorCode));
    }

    /**
     * Handle access denied errors (authorization failures).
     */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode())
                .body(createErrorResponse(ErrorCode.UNAUTHORIZED));
    }

    /**
     * Handle all other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception) {
        log.error("Unexpected exception: ", exception);
        return ResponseEntity.badRequest()
                .body(createErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION));
    }

    /**
     * Build validation error message from field errors.
     */
    private String buildValidationMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> error.getField() + ": " + messageUtils.getMessage(error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
    }

    /**
     * Create error response with default message from ErrorCode.
     */
    private ApiResponse<Void> createErrorResponse(ErrorCode errorCode) {
        return ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(messageUtils.getMessage(errorCode))
                .build();
    }

    /**
     * Create error response with custom message.
     */
    private ApiResponse<Void> createErrorResponse(ErrorCode errorCode, String customMessage) {
        return ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }
}
