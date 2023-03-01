package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.annotation.Editable;

@Editable
public interface NamedQuery extends Serializable {

	public static final String COMMON_NAME_PREFIX = "g:";
	
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
	public static String getCommonName(String name) {
		if (name.startsWith(COMMON_NAME_PREFIX))
			return name.substring(COMMON_NAME_PREFIX.length());
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