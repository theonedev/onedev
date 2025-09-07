package io.onedev.server.exception;

import java.util.ArrayList;
import java.util.Map;

import io.onedev.commons.utils.ExplicitException;

public class InvalidIssueFieldsException extends ExplicitException {

    private final Map<String, String> invalidFields;

    public InvalidIssueFieldsException(Map<String, String> invalidFields) {
        this(buildMessage(invalidFields), invalidFields);
    }

    public InvalidIssueFieldsException(String message, Map<String, String> invalidFields) {
        super(message);
        this.invalidFields = invalidFields;
    }

    public static String buildMessage(Map<String, String> invalidFields) {
        var fieldErrors = new ArrayList<String>();
        for (var entry: invalidFields.entrySet()) {
            fieldErrors.add(entry.getKey() + " (" + entry.getValue() + ")");
        }
        return "Invalid fields: " + String.join(", ", fieldErrors);
    }

    public Map<String, String> getInvalidFields() {
        return invalidFields;
    }
    
}