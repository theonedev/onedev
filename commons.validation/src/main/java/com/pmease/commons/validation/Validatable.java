package com.pmease.commons.validation;

import javax.validation.ConstraintValidatorContext;

public interface Validatable {
	
	/**
	 * Validate with supplied context. Validation violations can be added to the 
	 * context via <tt>ConstraintValidatorContext.buildConstraintViolationWithTemplate(String).addConstraintViolation()</tt>.
	 * Please note that one should NEVER call {@link ConstraintValidatorContext#disableDefaultConstraintViolation()} inside 
	 * this method.
	 *  
	 * @param constraintValidatorContext
	 */
	void validate(ConstraintValidatorContext constraintValidatorContext);
}
