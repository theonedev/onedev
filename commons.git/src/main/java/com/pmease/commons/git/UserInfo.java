package com.pmease.commons.git;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class UserInfo implements Serializable {
	
	private final String name;
	
	private final String email;
	
	private final Date date;
	
	public static class Builder {
		private String name;
		private String email;
		private Date date;
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		
		public Builder date(Date date) {
			this.date = date;
			return this;
		}
		
		public boolean isValid() {
			return !Strings.isNullOrEmpty(name)
					&& !Strings.isNullOrEmpty(email)
					&& date != null;
		}
		
		public UserInfo build() {
			return new UserInfo(Preconditions.checkNotNull(name, "name"),
								Preconditions.checkNotNull(email, "email"),
								Preconditions.checkNotNull(date, "date"));
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
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
