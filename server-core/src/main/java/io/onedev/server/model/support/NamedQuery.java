package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface NamedQuery extends Serializable {

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
	
}