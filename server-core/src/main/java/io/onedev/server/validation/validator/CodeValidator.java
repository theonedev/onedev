package io.onedev.server.validation.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import groovy.text.SimpleTemplateEngine;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.annotation.Code;

public class CodeValidator implements ConstraintValidator<Code, Object> {

	private String language;
	
	private String message;
	
	@Override
	public void initialize(Code constaintAnnotation) {
		language = constaintAnnotation.language();
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext constraintContext) {
		if (value instanceof String && StringUtils.isNotBlank((String)value)
				|| value instanceof List && !((List<?>)value).isEmpty()) {
			String code;
			if (value instanceof String)
				code = (String) value;
			else 
				code = StringUtils.join((List<String>)value, "\n");
            if (language.equals(Code.GROOVY_TEMPLATE)) {
		        try {
		            new SimpleTemplateEngine().createTemplate(code);
		        } catch (Exception e) {
					constraintContext.disableDefaultConstraintViolation();
	  
					String message = this.message;
					if (message.length() == 0)
						message = e.getMessage();
					if (StringUtils.isBlank(message))
						message = "Malformed groovy template";
					constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		            return false;
		        }
            } else if (language.equals(Code.GROOVY)) {
            	try {
            		GroovyUtils.compile(code);
            	} catch (Exception e) {
					constraintContext.disableDefaultConstraintViolation();
					
					String message = this.message;
					if (message.length() == 0)
						message = e.getMessage();
					if (StringUtils.isBlank(message))
						message = "Failed to compile groovy script";
					constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		            return false;
            	}
            } 
		} 
		return true;
	}
	
}
