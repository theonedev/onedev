package io.onedev.server.validation.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.io.FilenameUtils;

import io.onedev.server.annotation.Path;

public class PathValidator implements ConstraintValidator<Path, Object> {

	private Path.Type type;

	private String message;
	
	@Override
	public void initialize(Path constaintAnnotation) {
		type = constaintAnnotation.value();
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		if (value instanceof List) {
			for (Object element : (List<?>) value) {
				if (element instanceof String && !isValidPath((String) element, constraintContext))
					return false;
			}
			return true;
		} else if (value instanceof String) {
			return isValidPath((String) value, constraintContext);
		}
		return true;
	}

	private boolean isValidPath(String value, ConstraintValidatorContext constraintContext) {
		String checkMessage = checkPath(type, value);
		if (checkMessage != null) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = checkMessage;
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
		return true;
	}
	
	public static String checkPath(Path.Type type, String value) {
		if (value.contains(".."))
			return "'..' is not allowed";
		else if (FilenameUtils.getPrefixLength(value) == 0 && type == Path.Type.ABSOLUTE)
			return "Absolute path is required";
		else if (FilenameUtils.getPrefixLength(value) != 0 && type == Path.Type.RELATIVE)
			return "Relative path is required";
		return null;
	}

}
