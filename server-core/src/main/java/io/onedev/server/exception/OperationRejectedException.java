package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class OperationRejectedException extends ExplicitException {

    public OperationRejectedException(String message) {
        super(message);
    }

}
