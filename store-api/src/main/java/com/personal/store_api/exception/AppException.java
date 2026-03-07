package com.personal.store_api.exception;

import com.personal.store_api.enums.ErrorCode;

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private String detailMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(String message) {
        super(message);
        this.detailMessage = message;
    }

    public AppException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + ": " + detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }
}
