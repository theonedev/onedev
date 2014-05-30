package com.pmease.commons.git;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;

import com.pmease.commons.editable.Editable;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;

/**
 * Git relevant settings.
 * 
 * @author robin
 *
 */
@Editable
@ClassValidating
@SuppressWarnings("serial")
public abstract class GitConfig implements Serializable, Validatable {
	/**
	 * Get git executable, for instance <tt>/usr/bin/git</tt>.
	 * 
	 * @return
	 * 			git executable
	 */
	public abstract String getExecutable();

	@Override
	public void validate(ConstraintValidatorContext constraintValidatorContext) {
		if (getExecutable() != null) {
			String error = GitCommand.checkError(getExecutable());
			if (error != null)
				constraintValidatorContext.buildConstraintViolationWithTemplate(error).addConstraintViolation();
		}
	}
	
}
