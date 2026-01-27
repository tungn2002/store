package com.personal.store_api.handler;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtils messageUtils;

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception) {
        log.error("Exception: ", exception);
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(messageUtils.getMessage(ErrorCode.UNCATEGORIZED_EXCEPTION));

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException exception) {
        log.warn("Bad credentials attempt: {}", exception.getMessage());

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.INVALID_CREDENTIALS.getCode());
        apiResponse.setMessage(messageUtils.getMessage(ErrorCode.INVALID_CREDENTIALS));

        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(messageUtils.getMessage(errorCode));

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(messageUtils.getMessage(errorCode))
                        .build());
    }
}
