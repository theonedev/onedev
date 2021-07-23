package io.onedev.server.util.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import groovy.text.SimpleTemplateEngine;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.validation.annotation.Code;

public class CodeValidator implements ConstraintValidator<Code, List<String>> {

	private String language;
	
	private String message;
	
	@Override
	public void initialize(Code constaintAnnotation) {
		language = constaintAnnotation.language();
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(List<String> value, ConstraintValidatorContext constraintContext) {
		if (value != null && !value.isEmpty()) {
			String joinedValue = StringUtils.join(value, "\n");
            if (language.equals(Code.HTML_TEMPLATE)) {
		        try {
		            new SimpleTemplateEngine().createTemplate(joinedValue);
		        } catch (Exception e) {
					constraintContext.disableDefaultConstraintViolation();
	  
					String message = this.message;
					if (message.length() == 0)
						message = e.getMessage();
					if (StringUtils.isBlank(message))
						message = "Malformed html template";
					constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		            return false;
		        }
            } else if (language.equals(Code.GROOVY)) {
            	try {
            		GroovyUtils.compile(joinedValue);
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
