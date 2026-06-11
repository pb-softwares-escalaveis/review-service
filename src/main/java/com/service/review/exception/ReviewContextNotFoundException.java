package com.service.review.exception;

import com.service.review.enums.ContextType;

public class ReviewContextNotFoundException extends RuntimeException {
    public ReviewContextNotFoundException(String contextName, ContextType type) {
        super(contextName + " not found for type: " + type);
    }
}
