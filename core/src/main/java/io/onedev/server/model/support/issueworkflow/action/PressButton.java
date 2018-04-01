package io.onedev.server.model.support.issueworkflow.action;

import io.onedev.server.model.support.submitter.Submitter;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;

@Editable(order=100, name="Button")
public class PressButton implements IssueAction, Button {

	private static final long serialVersionUID = 1L;

	private String name;

	private Submitter submitter;
	
	@Editable(order=100)
	@OmitName
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Is pressed by")
	@Override
	public Submitter getSubmitter() {
		return submitter;
	}
	
	public void setSubmitter(Submitter submitter) {
		this.submitter = submitter;
	}

	@Override
	public Button getButton() {
		return this;
	}

}
