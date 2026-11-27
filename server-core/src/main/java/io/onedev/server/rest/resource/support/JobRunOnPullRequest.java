package io.onedev.server.rest.resource.support;

import java.io.Serial;

import jakarta.validation.constraints.NotNull;

import io.onedev.server.model.Build;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;

@EntityCreate(Build.class)
public class JobRunOnPullRequest extends JobRun {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Api(order=100, description="${productName} will run the job on the selected build commit of this pull request")
	private Long pullRequestId;
	
	@NotNull
	public Long getPullRequestId() {
		return pullRequestId;
	}

	public void setPullRequestId(Long pullRequestId) {
		this.pullRequestId = pullRequestId;
	}

}