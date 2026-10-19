package io.onedev.server.validation.validator;

import java.io.File;

import org.apache.wicket.Application;

import static io.onedev.server.oem.OemUtils.interpolate;
import static io.onedev.server.web.translation.Translation._T;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.annotation.DataDirectory;

public class DataDirectoryValidator implements ConstraintValidator<DataDirectory, String> {

	private DataDirectory annotation;
	
	@Override
	public void initialize(DataDirectory constaintAnnotation) {
		annotation = constaintAnnotation;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;

		try {
			var siteDir = Bootstrap.getSiteDir();
			File dir = new File(value);
			if (!dir.isAbsolute())
				dir = new File(siteDir, value);
			if (annotation.exists() && !dir.exists()) {
				constraintContext.disableDefaultConstraintViolation();
				String message = annotation.message();
				if (message.length() == 0)
					message = "Directory not exist";
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
			var dirPath = dir.getCanonicalFile().toPath();
			if (!dirPath.startsWith(siteDir.toPath()) 
					&& dir.getCanonicalFile().toPath().startsWith(Bootstrap.installDir.toPath())) {
				constraintContext.disableDefaultConstraintViolation();
				String message = annotation.message();
				if (message.length() == 0) {
					message = _T("Specified directory should be either under site directory or outside of ${productName} " +
							"installation directory");
					if (!Application.exists())
						message = interpolate(message);
				}
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
			return true;
		} catch (Exception e) {
			constraintContext.disableDefaultConstraintViolation();
			String message = annotation.message();
			if (message.length() == 0)
				message = "Invalid directory";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
}
