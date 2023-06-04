package io.onedev.server.validation.validator;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.ReservedOptions;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class ReservedOptionsValidator implements ConstraintValidator<ReservedOptions, String> {

	private String message;
	
	private Collection<Pattern> reservedPatterns = new ArrayList<>();
	
	@Override
	public void initialize(ReservedOptions constaintAnnotation) {
		message = constaintAnnotation.message();
		for (var each: constaintAnnotation.value()) 
			reservedPatterns.add(Pattern.compile(each));
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		for (var option: StringUtils.parseQuoteTokens(value)) {
			for (var reservedPattern: reservedPatterns) {
				var matcher = reservedPattern.matcher(option);
				if (matcher.matches()) {
					constraintContext.disableDefaultConstraintViolation();
					String message = this.message;
					if (message.length() == 0) {
						if (matcher.groupCount() >= 1 && matcher.group(1) != null)
							message = "Option '" + matcher.group(1) + "' is reserved";
						else
							message = "Option '" + option + "' is reserved";
					}
					constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
					return false;
				}
			}
		}
		return true;
	}
	
}
