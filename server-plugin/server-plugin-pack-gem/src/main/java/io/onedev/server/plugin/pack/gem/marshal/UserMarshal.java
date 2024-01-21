package io.onedev.server.plugin.pack.gem.marshal;

public class UserMarshal {
	
	private final String name;
	
	private final Object value;
	
	public UserMarshal(String name, Object value) {
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