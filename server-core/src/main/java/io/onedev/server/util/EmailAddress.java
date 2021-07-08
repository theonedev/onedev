package io.onedev.server.util;

import java.io.Serializable;

import io.onedev.commons.utils.StringUtils;

public class EmailAddress implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String prefix;
	
	private final String domain;
	
	public EmailAddress(String prefix, String domain) {
		this.prefix = prefix;
		this.domain = domain;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getDomain() {
		return domain;
	}

	public static EmailAddress parse(String emailAddress) {
		String prefix = StringUtils.substringBefore(emailAddress, "@");
		String domain = StringUtils.substringAfter(emailAddress, "@");
		return new EmailAddress(prefix, domain);
	}
	
	@Override
	public String toString() {
		return prefix + "@" + domain;
	}
	
}