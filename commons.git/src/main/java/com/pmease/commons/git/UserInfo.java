package com.pmease.commons.git;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class UserInfo implements Serializable {
	
	private final String name;
	
	private final String email;
	
	private final Date date;
	
	public UserInfo(String name, String email, Date date) {
		this.name = name;
		this.email = email;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, email, date);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UserInfo))
			return false;
		if (this == other)
			return true;
		UserInfo otherUser = (UserInfo) other;
		return Objects.equal(name, otherUser.name) 
				&& Objects.equal(email, otherUser.email)
				&& Objects.equal(date, otherUser.date);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", name)
				.add("email", email)
				.add("date", date)
				.toString();
	}
	
}
