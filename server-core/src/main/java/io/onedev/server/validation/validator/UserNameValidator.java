package io.onedev.server.validation.validator;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.annotation.UserName;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	private static final Pattern PATTERN = Pattern.compile("\\w[\\w-\\.]*");
	
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
				message = "Should start with alphanumeric or underscore, and contains only "
						+ "alphanumeric, underscore, dash, or dot";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new") || value.equals(User.SYSTEM_NAME.toLowerCase()) 
				|| value.equals(User.UNKNOWN_NAME.toLowerCase())) {
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
				String normalizedUserName = preferredUserName.replaceAll("[^\\w-\\.]", "-").toLowerCase();
				int suffix = 1;
				UserManager userManager = OneDev.getInstance(UserManager.class);
				while (true) {
					String suggestedUserName = normalizedUserName;
					if (suffix > 1)
						suggestedUserName += suffix;
					if (!suggestedUserName.equals("new") 
							&& !suggestedUserName.equals(User.SYSTEM_NAME)
							&& !suggestedUserName.equals(User.UNKNOWN_NAME)
							&& userManager.findByName(suggestedUserName) == null) {
						return suggestedUserName;
					}
					suffix++;
				}
			}
			
		});
	}
	
}
