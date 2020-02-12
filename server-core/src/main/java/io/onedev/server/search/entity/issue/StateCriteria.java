package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;

public class StateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public StateCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Path<?> attribute = root.get(Issue.PROP_STATE);
		return builder.equal(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getState().equals(value);
	}

	@Override
	public void fill(Issue issue) {
		issue.setState(value);
	}

	@Override
	public String asString() {
		return quote(Issue.FIELD_STATE) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
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
