package com.personal.store_api.util;

import com.personal.store_api.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageUtils {
    private final MessageSource messageSource;

    public String getMessage(ErrorCode errorCode, Object... args) {
        return messageSource.getMessage(
                errorCode.getMessage(),
                args,
                LocaleContextHolder.getLocale()
        );
    }
}
