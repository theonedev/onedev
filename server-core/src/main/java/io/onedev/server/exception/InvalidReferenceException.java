package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class InvalidReferenceException extends ExplicitException {

    public InvalidReferenceException(String message) {
        super(message);
    }

}
