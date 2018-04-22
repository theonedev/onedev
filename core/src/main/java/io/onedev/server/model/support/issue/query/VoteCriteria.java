package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class VoteCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final ArithmeticOperator operator;
	
	private final int value;
	
	public VoteCriteria(ArithmeticOperator operator, int value) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return operator.getPredicate(context.getBuilder(), context.getRoot().get("vote"), value);
	}

}
