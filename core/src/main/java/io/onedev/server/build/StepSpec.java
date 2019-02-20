package io.onedev.server.build;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class StepSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String command;

	@Editable
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
}
