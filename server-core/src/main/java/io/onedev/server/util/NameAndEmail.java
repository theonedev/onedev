package io.onedev.server.util;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.PersonIdent;

public class NameAndEmail implements Comparable<NameAndEmail>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String emailAddress;
	
	public NameAndEmail(@Nullable String name, @Nullable String emailAddress) {
		this.name = name!=null?name:"";
		this.emailAddress = emailAddress!=null?emailAddress:"";
	}

	public NameAndEmail(PersonIdent person) {
		this(person.getName(), person.getEmailAddress());
	}
	
	public PersonIdent asPersonIdent() {
		return new PersonIdent(name, emailAddress);
	}
	
	public String getName() {
		return name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof NameAndEmail))
			return false;
		if (this == other)
			return true;
		NameAndEmail otherNameAndEmail = (NameAndEmail) other;
		return new EqualsBuilder()
				.append(name, otherNameAndEmail.name)
				.append(emailAddress, otherNameAndEmail.emailAddress)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(name).append(emailAddress).toHashCode();
	}

	@Override
	public int compareTo(NameAndEmail nameAndEmail) {
		int result = name.compareTo(nameAndEmail.name);
		if (result != 0)
			return result;
		else
			return emailAddress.compareTo(nameAndEmail.emailAddress);
	}
	
}