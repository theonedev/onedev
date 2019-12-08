package io.onedev.server.util.userident;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitUserIdent extends EmailAwareIdent {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String email;
	
	private final String commitRole;

	@JsonCreator
	public GitUserIdent(@JsonProperty("name") String name, @JsonProperty("email") String email, @JsonProperty("gitRole") String gitRole) {
		this.name = name;
		this.email = email;
		this.commitRole = gitRole;
	}
	
	public GitUserIdent(String name, String email) {
		this(name, email, "Name in Git");
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public String getCommitRole() {
		return commitRole;
	}

}
