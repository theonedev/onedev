package com.pmease.commons.validation;

import java.io.File;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.pmease.commons.util.FileUtils;

public class DirectoryValidator implements ConstraintValidator<Directory, String> {
	
	public void initialize(Directory constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;

		File dir = new File(value);
		if (!dir.isAbsolute()) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate("Please specify an absolute directory.").addConstraintViolation();
			return false;
		}
		
		boolean dirExists = dir.exists();
		File testFile = new File(dir, DirectoryValidator.class.getName() + ".test");
		try {
			FileUtils.createDir(dir);
			FileUtils.touchFile(testFile);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (testFile.exists())
				FileUtils.deleteFile(testFile);
			if (!dirExists)
				FileUtils.deleteDir(dir);
		}

	}
}
