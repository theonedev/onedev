package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class NotAcceptableException extends ExplicitException {

    public NotAcceptableException(String message) {
        super(message);
    }

}
