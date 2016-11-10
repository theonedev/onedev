package com.gitplex.commons.git;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;

import com.gitplex.commons.git.command.GitCommand;
import com.gitplex.commons.validation.ClassValidating;
import com.gitplex.commons.validation.Validatable;
import com.gitplex.commons.wicket.editable.annotation.Editable;

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
	public boolean isValid(ConstraintValidatorContext context) {
		if (getExecutable() != null) {
			String error = GitCommand.checkError(getExecutable());
			if (error != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(error).addConstraintViolation();
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
}
