package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class StateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final EnumOperator operator;
	
	public StateCriteria(String value, EnumOperator operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return operator.getPredicate(context.getBuilder(), context.getRoot().get("state"), value);
	}

}
