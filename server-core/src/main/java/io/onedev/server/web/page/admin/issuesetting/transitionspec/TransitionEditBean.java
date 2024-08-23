package io.onedev.server.web.page.admin.issuesetting.transitionspec;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable
public class TransitionEditBean implements Serializable {
	
	private TransitionSpec transitionSpec;

	@Editable
	@OmitName
	@NotNull
	public TransitionSpec getTransitionSpec() {
		return transitionSpec;
	}

	public void setTransitionSpec(TransitionSpec transitionSpec) {
		this.transitionSpec = transitionSpec;
	}
	
}
