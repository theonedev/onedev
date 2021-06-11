package io.onedev.server.util.facade;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.User;
import io.onedev.server.util.NameAndEmail;

public class UserFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String fullName;
	
	private final String email;
	
	private final List<String> alternateEmails;
	
	public UserFacade(Long userId, String name, String fullName, String email, List<String> alternateEmails) {
		super(userId);
		this.name = name;
		this.fullName = fullName;
		this.email = email;
		this.alternateEmails = alternateEmails;
	}
	
	public UserFacade(User user) {
		this(user.getId(), user.getName(), user.getFullName(), user.getEmail(), user.getAlternateEmails());
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getFullName() {
		return fullName;
	}

	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}
	
	public String getEmail() {
		return email;
	}

	public List<String> getAlternateEmails() {
		return alternateEmails;
	}

	public boolean isUsingEmail(String email) {
		return this.email.equals(email) || alternateEmails.contains(email);
	}
	
	public NameAndEmail getNameAndEmail() {
		return new NameAndEmail(getDisplayName(), email);
	}
	
}
