package com.pmease.commons.hibernate;

public class EntityEvent {
	
	public enum Operation {CREATE, UPDATE, DELETE};
	
	private final Object entity;
	
	private final Operation operation;
	
	public EntityEvent(Object entity, Operation operation) {
		this.entity = entity;
		this.operation = operation;
	}

	public Object getEntity() {
		return entity;
	}

	public Operation getOperation() {
		return operation;
	}
	
}
