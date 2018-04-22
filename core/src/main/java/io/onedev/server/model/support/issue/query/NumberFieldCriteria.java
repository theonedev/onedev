package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class NumberFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final int value;
	
	private final ArithmeticOperator operator;
	
	public NumberFieldCriteria(String name, int value, ArithmeticOperator operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	public int getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return operator.getPredicate(context.getBuilder(), context.getJoin(getFieldName()).get("value"), value);
	}

}
