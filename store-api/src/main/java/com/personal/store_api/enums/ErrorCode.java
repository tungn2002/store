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
    USER_NOT_FOUND(1009, "error.user.not.found", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1010, "error.password.invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1011, "error.password.not.match", HttpStatus.BAD_REQUEST),
    STORE_SETTINGS_NOT_FOUND(1012, "error.store.settings.not.found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1013, "error.category.not.found", HttpStatus.NOT_FOUND),
    BRAND_NOT_FOUND(1014, "error.brand.not.found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1015, "error.product.not.found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_FOUND(1016, "error.product.variant.not.found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_DUPLICATE(1017, "error.product.variant.duplicate", HttpStatus.BAD_REQUEST),
    PRODUCT_OUT_OF_STOCK(1018, "error.product.out.of.stock", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1019, "error.cart.item.not.found", HttpStatus.NOT_FOUND),
    CART_MAX_ITEMS_REACHED(1020, "error.cart.max.items.reached", HttpStatus.BAD_REQUEST),
    CART_QUANTITY_EXCEEDS_STOCK(1021, "error.cart.quantity.exceeds.stock", HttpStatus.BAD_REQUEST),
    CART_QUANTITY_MAX_EXCEEDED(1022, "error.cart.quantity.max.exceeded", HttpStatus.BAD_REQUEST),
    CART_QUANTITY_MIN(1023, "error.cart.quantity.min", HttpStatus.BAD_REQUEST),
    CART_ITEM_ALREADY_EXISTS(1024, "error.cart.item.already.exists", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1025, "error.order.not.found", HttpStatus.NOT_FOUND),
    ORDER_OUT_OF_STOCK(1026, "error.order.out.of.stock", HttpStatus.BAD_REQUEST),
    PAYMENT_ERROR(1027, "error.payment.error", HttpStatus.BAD_REQUEST),
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