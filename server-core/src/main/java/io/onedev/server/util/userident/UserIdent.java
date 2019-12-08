package io.onedev.server.util.userident;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.onedev.server.model.User;

@JsonTypeInfo(property="@class", use = Id.CLASS)
public abstract class UserIdent implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract String getName();
	
	public static UserIdent of(User user) {
		return new OrdinaryUserIdent(user.getDisplayName(), user.getEmail());
	}
	
	public static UserIdent of(@Nullable User user, @Nullable String userName) {
		if (user != null) 
			return new OrdinaryUserIdent(user.getDisplayName(), user.getEmail());
		else if (userName != null) 
			return new ExternalUserIdent(userName);
		else
			return new ExternalUserIdent("unknown");
	}

	public static UserIdent of(PersonIdent person) {
		return new GitUserIdent(person.getName(), person.getEmailAddress());
	}
	
	public static UserIdent of(PersonIdent person, String commitRole) {
		return new GitUserIdent(person.getName(), person.getEmailAddress(), commitRole);
	}
	
}
