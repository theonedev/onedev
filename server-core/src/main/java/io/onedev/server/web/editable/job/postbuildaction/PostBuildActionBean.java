package io.onedev.server.web.editable.job.postbuildaction;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class PostBuildActionBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private PostBuildAction action;

	@Editable(order=100, name="Type")
	@NotNull(message="may not be empty")
	public PostBuildAction getAction() {
		return action;
	}

	public void setAction(PostBuildAction action) {
		this.action = action;
	}

}
