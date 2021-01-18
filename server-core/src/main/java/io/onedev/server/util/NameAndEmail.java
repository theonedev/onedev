package io.onedev.server.util;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.PersonIdent;

public class NameAndEmail implements Comparable<NameAndEmail>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String emailAddress;
	
	public NameAndEmail(String name, String emailAddress) {
		this.name = name;
		this.emailAddress = emailAddress;
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
		return name.compareTo(nameAndEmail.name);
	}
	
}