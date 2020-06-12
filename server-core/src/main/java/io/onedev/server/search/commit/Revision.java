package io.onedev.server.search.commit;

import java.io.Serializable;

import javax.annotation.Nullable;

public class Revision implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum Scope {SINCE, UNTIL};
	
	private final String value;
	
	private final Scope scope;

	private final String toString;
	
	public Revision(String value, @Nullable Scope scope, @Nullable String toString) {
		this.value = value;
		this.scope = scope;
		this.toString = toString;
	}

	public Revision(String value, @Nullable Scope scope) {
		this(value, scope, null);
	}
	
	public Scope getScope() {
		return scope;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		String toString = this.toString;
		if (toString == null) {
			toString = CommitCriteria.getRuleName(CommitQueryLexer.COMMIT) + "(" + value + ")";
			if (scope != null)
				toString = scope.name().toLowerCase() + " " + toString;
		}
		return toString;
	}
	
}