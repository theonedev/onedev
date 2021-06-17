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
	
	private final String gitEmail;
	
	private final List<String> alternateEmails;
	
	public UserFacade(Long userId, String name, @Nullable String fullName, String email, 
			@Nullable String gitEmail, List<String> alternateEmails) {
		super(userId);
		this.name = name;
		this.fullName = fullName;
		this.email = email;
		this.gitEmail = gitEmail;
		this.alternateEmails = alternateEmails;
	}
	
	public UserFacade(User user) {
		this(user.getId(), user.getName(), user.getFullName(), user.getEmail(), 
				user.getGitEmail(), user.getAlternateEmails());
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

	public String getGitEmail() {
		return gitEmail;
	}

	public List<String> getAlternateEmails() {
		return alternateEmails;
	}

	public boolean isUsingEmail(String email) {
		return email.equals(this.email) || email.equals(gitEmail) || alternateEmails.contains(email);
	}
	
	public NameAndEmail getNameAndEmail() {
		return new NameAndEmail(getDisplayName(), email);
	}
	
}
