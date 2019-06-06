package io.onedev.server.search.commit;

import java.io.Serializable;

import javax.annotation.Nullable;

public class Revision implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum Scope {SINCE, UNTIL};
	
	private final String value;
	
	private Scope scope;
	
	public Revision(String value, @Nullable Scope scope) {
		this.value = value;
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public String getValue() {
		return value;
	}
	
}