package io.onedev.server.plugin.pack.gem.marshal;

public class UserDefined {
	
	private final String name;
	
	private final Object value;
	
	public UserDefined(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	
}