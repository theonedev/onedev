package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.query.IssueQueryConstants;

public class StateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public StateCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = root.get(IssueQueryConstants.ATTR_STATE);
		return builder.equal(attribute, value);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return issue.getState().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setState(value);
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueQueryConstants.FIELD_STATE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(value);
	}

	@Override
	public Collection<String> getUndefinedStates() {
		List<String> undefinedStates = new ArrayList<>();
		if (OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpec(value) == null)
			undefinedStates.add(value);
		return undefinedStates;
	}
	
	@Override
	public void onRenameState(String oldName, String newName) {
		if (value.equals(oldName))
			value = newName;
	}
	
	@Override
	public boolean onDeleteState(String stateName) {
		return value.equals(stateName);
	}
	
}
