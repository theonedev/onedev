package io.onedev.server.model.support.issue.workflow.action;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.authorized.Authorized;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;

@Editable(order=100, name="Button")
public class PressButton implements IssueAction, Button {

	private static final long serialVersionUID = 1L;

	private String name;

	private Authorized authorized;
	
	@Editable(order=100)
	@NotEmpty
	@OmitName
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Is pressed by")
	@NotNull(message="may not be empty")
	@Override
	public Authorized getAuthorized() {
		return authorized;
	}
	
	public void setAuthorized(Authorized authorized) {
		this.authorized = authorized;
	}

	@Override
	public Button getButton() {
		return this;
	}

}
