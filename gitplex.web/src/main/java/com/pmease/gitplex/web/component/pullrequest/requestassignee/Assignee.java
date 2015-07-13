package com.pmease.gitplex.web.component.pullrequest.requestassignee;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class Assignee implements Serializable {
	
	private final User user;
	
	private final String alias;
	
	public Assignee(User user, @Nullable String alias) {
		this.user = user;
		this.alias = alias;
	}

	public User getUser() {
		return user;
	}

	@Nullable
	public String getAlias() {
		return alias;
	}

	public boolean equals(Object other) {
		if (!(other instanceof Assignee))
			return false;
		if (this == other)
			return true;
		Assignee otherAssignee = (Assignee) other;
		return new EqualsBuilder().append(getUser(), otherAssignee.getUser()).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getUser()).toHashCode();
	}
	
}
