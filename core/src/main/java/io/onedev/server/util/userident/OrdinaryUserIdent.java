package io.onedev.server.util.userident;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrdinaryUserIdent extends EmailAwareIdent {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String email;
	
	@JsonCreator
	public OrdinaryUserIdent(@JsonProperty("name") String name, @JsonProperty("email") String email) {
		this.name = name;
		this.email = email;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getEmail() {
		return email;
	}

}
