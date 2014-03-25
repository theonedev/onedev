package com.pmease.gitop.web.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Objects;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.validation.UserNameReservation;

@SuppressWarnings("serial")
public class UsernameValidator implements IValidator<String> {

	private final String oldName;
	private static Set<String> reservedNames;
	
	public UsernameValidator(String oldName) {
		this.oldName = oldName;
	}
	
	@Override
	public void validate(IValidatable<String> validatable) {
		String newName = validatable.getValue();
		
		if (Objects.equal(oldName, newName)) {
			return; // not updated
		}
		
		Set<String> reserved = getReservedNames();
		if (reserved.contains(newName)) {
			validatable.error(new ValidationError("'" + newName + "' is reserved by system"));
			return;
		}
		
		User user = Gitop.getInstance(UserManager.class).findByName(newName);
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
