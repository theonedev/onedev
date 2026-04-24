package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class BadRequestException extends ExplicitException {

    public BadRequestException(String message) {
        super(message);
    }

}
