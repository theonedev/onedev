package io.onedev.server.util.criteria;

import java.io.Serializable;

import io.onedev.commons.utils.StringUtils;

public abstract class Criteria<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean withParens;
	
	public abstract boolean matches(T t);
	
	public Criteria<T> withParens(boolean withParens) {
		this.withParens = withParens;
		return this;
	}

	public void onRenameUser(String oldName, String newName) {
	}

	public void onRenameProject(String oldName, String newName) {
	}

	public void onRenameGroup(String oldName, String newName) {
	}
	
	public boolean isUsingUser(String userName) {
		return false;
	}

	public boolean isUsingProject(String projectName) {
		return false;
	}
	
	public boolean isUsingGroup(String groupName) {
		return false;
	}

	public static String quote(String value) {
		return "\"" + StringUtils.escape(value, "\"") + "\"";
	}

	@Override
	public String toString() {
		if (withParens)
			return "(" + toStringWithoutParens() + ")";
		else
			return toStringWithoutParens();
	}
	
	public abstract String toStringWithoutParens();
	
}
