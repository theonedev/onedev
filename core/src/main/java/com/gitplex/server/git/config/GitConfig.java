package com.gitplex.server.git.config;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;

import com.gitplex.server.git.command.GitCommand;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.validation.Validatable;
import com.gitplex.server.util.validation.annotation.ClassValidating;

/**
 * Git relevant settings.
 * 
 * @author robin
 *
 */
@Editable
@ClassValidating
public abstract class GitConfig implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

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
