package io.onedev.server.web.editable.job.postbuildaction;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.ci.job.action.PostBuildAction;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ActionBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private PostBuildAction action;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public PostBuildAction getAction() {
		return action;
	}

	public void setAction(PostBuildAction action) {
		this.action = action;
	}

}
