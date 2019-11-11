package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.issue.fieldspec.UserChoiceField;

public class MineCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private IssueCriteria getCriteria(Project project, User user) {
		IssueCriteria submitterCriteria = new SubmittedByCriteria(user, user.getName());
		List<IssueCriteria> fieldCriterias = new ArrayList<>();
		IssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs()) {
			if (field instanceof UserChoiceField) {
				IssueCriteria fieldCriteria = new FieldOperatorCriteria(field.getName(), IssueQueryLexer.IsMe);
				fieldCriterias.add(fieldCriteria);
			} 
		}
		if (!fieldCriterias.isEmpty()) {
			fieldCriterias.add(0, submitterCriteria);
			return new OrCriteria(fieldCriterias);
		} else {
			return submitterCriteria;
		}
	}
	
	@Override
	public Predicate getPredicate(Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		return getCriteria(project, user).getPredicate(project, root, builder, user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return getCriteria(issue.getProject(), user).matches(issue, user);
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.Mine);
	}

}
