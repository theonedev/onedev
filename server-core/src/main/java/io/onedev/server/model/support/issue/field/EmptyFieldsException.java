package io.onedev.server.model.support.issue.field;

import java.util.Collection;

import io.onedev.commons.utils.ExplicitException;

public class EmptyFieldsException extends ExplicitException {

    private final Collection<String> emptyFields;

    public EmptyFieldsException(Collection<String> emptyFields) {
        this("The following fields must be set: " + String.join(", ", emptyFields), emptyFields);
    }

    public EmptyFieldsException(String message, Collection<String> emptyFields) {
        super(message);
        this.emptyFields = emptyFields;
    }

    public Collection<String> getEmptyFields() {
        return emptyFields;
    }
    
}