package io.onedev.server.git.config;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

/**
 * Git relevant settings.
 * 
 * @author robin
 *
 */
@Editable
@ClassValidating
public abstract class CurlConfig implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(CurlConfig.class);
	
	public abstract String getExecutable();

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		String curlExe = getExecutable();
		if (curlExe != null) {
			String errorMessage = null;
			try {
				new Commandline(curlExe).addArgs("--version").execute(new LineConsumer() {
	
					@Override
					public void consume(String line) {
						logger.trace(line);
					}
					
				}, new LineConsumer() {
	
					@Override
					public void consume(String line) {
						logger.error(line);
					}
					
				}).checkReturnCode();
			} catch (Exception e) {
				errorMessage = ExceptionUtils.getMessage(e);
				if (errorMessage.contains("CreateProcess error=2"))
					errorMessage = "Unable to find curl command: " + curlExe;
				else if (errorMessage.contains("error launching curl"))
					errorMessage = "Unable to launch git command: " + curlExe;
			}
			
			if (errorMessage != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
}
