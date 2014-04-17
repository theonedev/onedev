package com.pmease.commons.git;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class GitContribInfo implements Serializable {
	
	private final GitUser user;
	
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
		
		public GitContribInfo build() {
			return new GitContribInfo(Preconditions.checkNotNull(name, "name"),
								Preconditions.checkNotNull(email, "email"),
								Preconditions.checkNotNull(date, "date"));
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public GitContribInfo(String name, String email, Date date) {
		this(new GitUser(name, email), date);
	}
	
	public GitContribInfo(GitUser user, Date date) {
		this.user = user;
		this.date = date;
	}
	
	public String getName() {
		return user.getName();
	}

	public String getEmail() {
		return user.getEmail();
	}

	public Date getDate() {
		return date;
	}
	
	public GitUser getUser() {
		return user;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(user, date);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GitContribInfo))
			return false;
		if (this == other)
			return true;
		GitContribInfo otherInfo = (GitContribInfo) other;
		return Objects.equal(user, otherInfo.user) 
				&& Objects.equal(date, otherInfo.date);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("user", user)
				.add("date", date)
				.toString();
	}
	
}
