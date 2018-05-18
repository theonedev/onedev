package io.onedev.server.model.support.issue.query;

import java.io.Serializable;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.google.common.base.Splitter;

import io.onedev.server.model.Issue;

public abstract class IssueCriteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract Predicate getPredicate(QueryBuildContext context);

	protected <T> Path<T> getPath(Root<Issue> root, String pathName) {
		int index = pathName.indexOf('.');
		if (index != -1) {
			Path<T> path = root.get(pathName.substring(0, index));
			for (String field: Splitter.on(".").split(pathName.substring(index+1))) 
				path = path.get(field);
			return path;
		} else {
			return root.get(pathName);
		}
	}
	
	public abstract boolean matches(Issue issue);
	
	public abstract boolean needsLogin();
	
}
