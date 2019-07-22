package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ValidationException;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class Secret implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	private String branches;

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@Password
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, name="Trusted Branches", description=""
			+ "Optionally specify space-separated branches to trust.\n"
			+ "Only builds from trusted branches can access this secret.\n"
			+ "Use * or ? for wildcard match. Leave empty to trust all branches")
	@BranchPatterns
	@NameOfEmptyValue("All")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}
	
	public static void validateSecrets(List<Secret> secrets) {
		Set<String> encounteredSecrets = new HashSet<>();
		for (Secret secret: secrets) {
			if (!encounteredSecrets.add(secret.getName()))
				throw new ValidationException("Duplicate secrets defined: " + secret.getName());
		}
	}
	
}
