package io.onedev.server.util.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class AndCriteria<T> extends Criteria<T> {
	
	private static final long serialVersionUID = 1L;

	protected final List<Criteria<T>> criterias;
	
	public AndCriteria(List<Criteria<T>> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<T, T> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		for (Criteria<T> criteria: criterias)
			predicates.add(criteria.getPredicate(query, root, builder));
		return builder.and(predicates.toArray(new Predicate[0]));
	}

	@Override
	public boolean matches(T t) {
		return criterias.stream().allMatch(it->it.matches(t));
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		criterias.stream().forEach(it->it.onRenameUser(oldName, newName));
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
		criterias.stream().forEach(it->it.onMoveProject(oldPath, newPath));
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		criterias.stream().forEach(it->it.onRenameGroup(oldName, newName));
	}

	@Override
	public void onRenameLink(String oldName, String newName) {
		criterias.stream().forEach(it->it.onRenameLink(oldName, newName));
	}
	
	@Override
	public boolean isUsingUser(String userName) {
		return criterias.stream().anyMatch(it->it.isUsingUser(userName));
	}

	@Override
	public boolean isUsingProject(String projectPath) {
		return criterias.stream().anyMatch(it->it.isUsingProject(projectPath));
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return criterias.stream().anyMatch(it->it.isUsingGroup(groupName));
	}
	
	@Override
	public boolean isUsingLink(String linkName) {
		return criterias.stream().anyMatch(it->it.isUsingLink(linkName));
	}

	@Override
	public Collection<String> getUndefinedStates() {
		List<String> undefinedStates = new ArrayList<>();
		for (Criteria<T> criteria: criterias)
			undefinedStates.addAll(criteria.getUndefinedStates());
		return undefinedStates;
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		for (Criteria<T> criteria: criterias)
			undefinedFields.addAll(criteria.getUndefinedFields());
		return undefinedFields;
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Set<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (Criteria<T> criteria: criterias)
			undefinedFieldValues.addAll(criteria.getUndefinedFieldValues());
		return undefinedFieldValues;
	}

	@Override
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Iterator<Criteria<T>> it = criterias.iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedStates(resolutions))
				it.remove();
		}
		return !criterias.isEmpty();
	}
	
	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Iterator<Criteria<T>> it = criterias.iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
		return !criterias.isEmpty();
	}
	
	@Override
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<Criteria<T>> it = criterias.iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
		return !criterias.isEmpty();
	}
	
	@Override
	public void fill(T object) {
		for (Criteria<T> criteria: criterias)
			criteria.fill(object);
	}
	
	@Override
	public String toStringWithoutParens() {
		return criterias.stream().map(it->it.toString()).collect(Collectors.joining(" and "));
	}

}
