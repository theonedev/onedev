package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

public class StringFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final StringOperator operator;
	
	public StringFieldCriteria(String name, String value, StringOperator operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		return operator.getPredicate(context.getBuilder(), context.getJoin(getFieldName()).get("value"), value);
	}

}
