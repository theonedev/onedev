package io.onedev.server.web.component.commandpalette;

import java.util.Map;

public abstract class ParamSegment implements UrlSegment {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final boolean optional;
	
	public ParamSegment(String name, boolean optional) {
		this.name = name;
		this.optional = optional;
	}
	
	public String getName() {
		return name;
	}

	public boolean isOptional() {
		return optional;
	}
	
	@Override
	public String toString() {
		if (optional)
			return "#{" + name + "}";
		else
			return "${" + name + "}";
	}
	
	public abstract boolean isExactMatch(String matchWith, Map<String, String> paramValues);
	
	public abstract Map<String, String> suggest(String matchWith, Map<String, String> paramValues, int count);
	
}
