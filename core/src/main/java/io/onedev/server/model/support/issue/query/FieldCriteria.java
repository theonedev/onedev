package io.onedev.server.model.support.issue.query;

public abstract class FieldCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

}
