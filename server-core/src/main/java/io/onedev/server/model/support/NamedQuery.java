package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface NamedQuery extends Serializable {

	public static final String GLOBAL_NAME_PREFIX = "g:";
	
	public static final String PERSONAL_NAME_PREFIX = "p:";
	
	String getName();
	
	String getQuery();
	
	@Nullable
	public static <T extends NamedQuery> T find(Collection<T> namedQueries, String name) {
		for (T namedQuery: namedQueries) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	@Nullable
	public static String getGlobalName(String name) {
		if (name.startsWith(GLOBAL_NAME_PREFIX))
			return name.substring(GLOBAL_NAME_PREFIX.length());
		else
			return null;
	}
	
	@Nullable
	public static String getPersonalName(String name) {
		if (name.startsWith(PERSONAL_NAME_PREFIX))
			return name.substring(PERSONAL_NAME_PREFIX.length());
		else
			return null;
	}
	
}