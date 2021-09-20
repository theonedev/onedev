package io.onedev.server.util.validation;

import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.http.client.utils.URIBuilder;

import io.onedev.server.util.validation.annotation.UrlPath;

public class UrlPathValidator implements ConstraintValidator<UrlPath, String> {

	public static final Pattern PATTERN = Pattern.compile("\\w([\\w-\\.]*\\w)?");
	
	private String message;
	
	@Override
	public void initialize(UrlPath constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		try {
			new URIBuilder(value);
			return true;
		} catch (URISyntaxException e) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) 
				message = e.getMessage();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
	
}
