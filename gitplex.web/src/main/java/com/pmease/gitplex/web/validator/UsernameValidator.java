package com.pmease.gitplex.web.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Objects;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.validation.UserNameReservation;

@SuppressWarnings("serial")
public class UsernameValidator implements IValidator<String> {

	private final String oldName;
	private static Set<String> reservedNames;
	
	static final Pattern pName = Pattern.compile("^[a-zA-Z0-9\\.-_@]{2,}");
	
	public UsernameValidator(String oldName) {
		this.oldName = oldName;
	}
	
	@Override
	public void validate(IValidatable<String> validatable) {
		String newName = validatable.getValue();
		
		if (Objects.equal(oldName, newName)) {
			return; // not updated
		}
		
		if (!pName.matcher(newName).matches()) {
			validatable.error(new ValidationError("Username can only contains "
					+ "alphabetical characters, numbers, '.', '@', '_' and '-'"));
			return;
		}
		
		Set<String> reserved = getReservedNames();
		if (reserved.contains(newName)) {
			validatable.error(new ValidationError("'" + newName + "' is reserved by system"));
			return;
		}
		
		User user = GitPlex.getInstance(UserManager.class).findByName(newName);
		if (user != null) {
			validatable.error(new ValidationError("This username is already taken"));
		}
	}

	public static synchronized Set<String> getReservedNames() {
		if (reservedNames == null) {
			reservedNames = new HashSet<>();
	        for (UserNameReservation each : AppLoader.getExtensions(UserNameReservation.class)) {
	        	reservedNames.addAll(each.getReserved());
	        }
		}
		return reservedNames;
	}
}
