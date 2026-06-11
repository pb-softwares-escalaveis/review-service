package com.service.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ReviewProviderException extends RuntimeException {
    public ReviewProviderException(String message) {
        super(message);
    }

    public ReviewProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
