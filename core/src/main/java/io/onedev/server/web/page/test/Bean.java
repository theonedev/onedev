package io.onedev.server.web.page.test;

import java.io.Serializable;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {HUMAN, AMIMAL, PLANT};

	private Type type;

	@Editable
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
}
