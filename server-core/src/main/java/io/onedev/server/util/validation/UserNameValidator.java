package io.onedev.server.util.validation;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.util.validation.annotation.UserName;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	private static final Pattern PATTERN = Pattern.compile("\\w([\\w-\\.]*\\w)?");
	
	private String message;
	
	@Override
	public void initialize(UserName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				message = "Should start and end with alphanumeric or underscore. "
						+ "Only alphanumeric, underscore, dash, and dot are allowed in the middle.";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new") || value.equals(OneDev.NAME)) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "' is a reserved name";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
	public static String suggestUserName(String preferredUserName) {
		return OneDev.getInstance(SessionManager.class).call(new Callable<String>() {

			@Override
			public String call() throws Exception {
				String normalizedUserName = preferredUserName.replaceAll("[^\\w-\\.]", "-");
				int suffix = 1;
				UserManager userManager = OneDev.getInstance(UserManager.class);
				while (true) {
					String suggestedUserName = normalizedUserName;
					if (suffix > 1)
						suggestedUserName += suffix;
					if (!suggestedUserName.equals("new") 
							&& !suggestedUserName.equals(OneDev.NAME) 
							&& userManager.findByName(suggestedUserName) == null) {
						return suggestedUserName;
					}
					suffix++;
				}
			}
			
		});
	}
	
}
