package com.personal.store_api.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "error.uncategorized", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "error.invalid.key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "error.user.existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "error.user.not.existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "error.unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "error.unauthorized", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1008, "error.auth.invalid.credentials", HttpStatus.UNAUTHORIZED),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

/*
  USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST);

 */