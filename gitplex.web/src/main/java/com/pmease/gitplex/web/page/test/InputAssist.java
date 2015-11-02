package com.pmease.gitplex.web.page.test;

import java.io.Serializable;
import java.util.List;

public abstract class InputAssist implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<InputError> inputErrors;
	
	private final List<AssistItem> assistItems;
	
	public InputAssist(List<InputError> inputErrors, List<AssistItem> assistItems) {
		this.inputErrors = inputErrors;
		this.assistItems = assistItems;
	}
	
	public List<InputError> getInputErrors() {
		return inputErrors;
	}

	public List<AssistItem> getAssistItems() {
		return assistItems;
	}
	
}
