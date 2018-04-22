package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class DescriptionCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final TextOperator operator;
	
	public DescriptionCriteria(String value, TextOperator operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return operator.getPredicate(context.getBuilder(), context.getRoot().get("description"), value);
	}

}
