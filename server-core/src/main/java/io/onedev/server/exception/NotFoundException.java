package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class NotFoundException extends ExplicitException {

    public NotFoundException(String message) {
        super(message);
    }

}
