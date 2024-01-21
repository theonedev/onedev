package io.onedev.server.plugin.pack.gem.marshal;

import java.util.Map;

public class RubyObject {
	
	private final String name;
	
	private final Map<String, Object> members;
	
	public RubyObject(String name, Map<String, Object> members) {
		this.name = name;
		this.members = members;
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getMembers() {
		return members;
	}
	
}