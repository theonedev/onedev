package io.onedev.server.util.criteria;

public class NotCriteria<T> extends Criteria<T> {
	
	private static final long serialVersionUID = 1L;

	protected final Criteria<T> criteria;
	
	public NotCriteria(Criteria<T> criteria) {
		this.criteria = criteria;
	}

	@Override
	public boolean matches(T entity) {
		return !criteria.matches(entity);
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		criteria.onRenameUser(oldName, newName);
	}

	@Override
	public void onRenameProject(String oldName, String newName) {
		criteria.onRenameProject(oldName, newName);
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		criteria.onRenameGroup(oldName, newName);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return criteria.isUsingUser(userName);
	}

	@Override
	public boolean isUsingProject(String projectName) {
		return criteria.isUsingProject(projectName);
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return criteria.isUsingGroup(groupName);
	}
	
	@Override
	public String toStringWithoutParens() {
		return "not(" + criteria.toString() + ")";
	}
	
}
