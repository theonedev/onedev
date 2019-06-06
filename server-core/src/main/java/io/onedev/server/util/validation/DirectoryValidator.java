package io.onedev.server.util.validation;

import java.io.File;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.util.validation.annotation.Directory;

public class DirectoryValidator implements ConstraintValidator<Directory, String> {

	private Directory annotation;
	
	@Override
	public void initialize(Directory constaintAnnotation) {
		annotation = constaintAnnotation;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		try {
			if (value == null)
				return true;

			File dir = new File(value);
			if (annotation.absolute()) {
				if (!dir.isAbsolute()) {
					constraintContext.disableDefaultConstraintViolation();
					constraintContext.buildConstraintViolationWithTemplate("Please specify an absolute directory").addConstraintViolation();
					return false;
				}
			}
			if (annotation.writeable()) {
				if (!FileUtils.isWritable(dir)) {
					constraintContext.disableDefaultConstraintViolation();
					constraintContext.buildConstraintViolationWithTemplate("Directory is not writeable").addConstraintViolation();
					return false;
				}
			}
			if (annotation.outsideOfInstallDir()) {
				if (dir.getCanonicalFile().toPath().startsWith(Bootstrap.installDir.toPath())) {
					constraintContext.disableDefaultConstraintViolation();
					constraintContext.buildConstraintViolationWithTemplate("Please specify a directory outside of the installation directory").addConstraintViolation();
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate("Invalid directory").addConstraintViolation();
			return false;
		}
	}
}
