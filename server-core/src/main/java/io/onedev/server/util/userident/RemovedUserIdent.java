package io.onedev.server.util.userident;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RemovedUserIdent extends UserIdent {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	@JsonCreator
	public RemovedUserIdent(@JsonProperty("name") String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

}
