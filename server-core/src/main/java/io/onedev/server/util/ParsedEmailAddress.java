package io.onedev.server.util;

import java.io.Serializable;

import io.onedev.commons.utils.StringUtils;

public class ParsedEmailAddress implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String domain;
	
	public ParsedEmailAddress(String name, String domain) {
		this.name = name;
		this.domain = domain;
	}
	
	public String getName() {
		return name;
	}

	public String getDomain() {
		return domain;
	}

	public static ParsedEmailAddress parse(String emailAddress) {
		String name = StringUtils.substringBefore(emailAddress, "@");
		String domain = StringUtils.substringAfter(emailAddress, "@");
		return new ParsedEmailAddress(name, domain);
	}
	
	@Override
	public String toString() {
		return name + "@" + domain;
	}
	
	public String getSubAddressed(String subAddress) {
		return name + "+" + subAddress + "@" + domain;
	}
	
}