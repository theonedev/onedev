package io.onedev.server.util.criteria;

import java.util.List;
import java.util.stream.Collectors;

public class AndCriteria<T> extends Criteria<T> {
	
	private static final long serialVersionUID = 1L;

	protected final List<? extends Criteria<T>> criterias;
	
	public AndCriteria(List<? extends Criteria<T>> criterias) {
		this.criterias = criterias;
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
	public void onRenameProject(String oldName, String newName) {
		criterias.stream().forEach(it->it.onRenameProject(oldName, newName));
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		criterias.stream().forEach(it->it.onRenameGroup(oldName, newName));
	}

	@Override
	public boolean isUsingUser(String userName) {
		return criterias.stream().anyMatch(it->it.isUsingUser(userName));
	}

	@Override
	public boolean isUsingProject(String projectName) {
		return criterias.stream().anyMatch(it->it.isUsingProject(projectName));
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return criterias.stream().anyMatch(it->it.isUsingGroup(groupName));
	}

	@Override
	public String toStringWithoutParens() {
		return criterias.stream().map(it->it.toString()).collect(Collectors.joining(" and "));
	}

}
