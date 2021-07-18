package io.onedev.server.web.component.taskbutton;

import java.io.Serializable;

public class TaskResult implements Serializable {
	
	private static final long serialVersionUID = 1L;

	final boolean successful;
	
	private final String feedback;
	
	public TaskResult(boolean successful, String feedback) {
		this.successful = successful;
		this.feedback = feedback;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public String getFeedback() {
		return feedback;
	}
	
}