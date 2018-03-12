package io.onedev.server.model.support.issueworkflow.statetransition;

import io.onedev.server.model.support.issueworkflow.IssueAction;
import io.onedev.server.model.support.issueworkflow.StateTransition;
import io.onedev.server.model.support.submitter.Submitter;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=100)
public class PressButton extends StateTransition implements IssueAction {

	private static final long serialVersionUID = 1L;

	private String name;

	private Submitter submitter;
	
	@Override
	public IssueAction getAction() {
		return this;
	}

	@Editable(order=100)
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@Override
	public Submitter getSubmitter() {
		return submitter;
	}
	
	public void setSubmitter(Submitter submitter) {
		this.submitter = submitter;
	}

}
