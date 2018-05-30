package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.unbescape.java.JavaEscape;

import com.google.common.base.Splitter;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

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
	
	public abstract String toString();

	protected String quote(String value) {
		return "\"" + JavaEscape.escapeJava(value) + "\"";
	}
	
	public Collection<String> getUndefinedStates(Project project) {
		return new HashSet<>();
	}

	public void onRenameState(String oldState, String newState) {
	}
	
	public Collection<String> getUndefinedFields(Project project) {
		return new HashSet<>();
	}
	
	public void onRenameField(String oldField, String newField) {
	}
	
	public boolean onDeleteField(String fieldName) {
		return false;
	}

	public Map<String, String> getUndefinedFieldValues(Project project) {
		return new HashMap<>();
	}

	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
	}

	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		return false;
	}
	
}
