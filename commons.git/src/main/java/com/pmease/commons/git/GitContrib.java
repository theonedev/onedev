package com.pmease.commons.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class GitContrib implements Serializable {

	private final GitPerson person;
	
	private final Date date;
	
	public GitContrib(String name, String emailAddress, Date date) {
		person = new GitPerson(name, emailAddress);
		this.date = checkNotNull(date, "date");
	}

	public GitPerson getPerson() {
		return person;
	}

	public Date getDate() {
		return date;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GitContrib)) {
			return false;
		}
		
		GitContrib rhs = (GitContrib) other;
		return Objects.equal(person, rhs.person)
				&& Objects.equal(date, rhs.date);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(person, date);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("person", person)
				.add("date", date)
				.toString();
	}
	
	public static class Builder {
		private String name;
		
		private String emailAddress;
		
		private Date date;
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
			return this;
		}
		
		public Builder date(Date date) {
			this.date = date;
			return this;
		}
		
		public GitContrib build() {
			return new GitContrib(name, emailAddress, date);
		}
	}
	
}
