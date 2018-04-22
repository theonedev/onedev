package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;

public class SubmitterCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final UserOperator operator;
	
	public SubmitterCriteria(String value, UserOperator operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		User user = OneDev.getInstance(UserManager.class).findByName(value);
		return operator.getPredicate(context.getBuilder(), context.getRoot().get("submitter"), user);
	}

}
