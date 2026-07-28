package io.onedev.server.ai.tools.pullrequest;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.OneDev;
import io.onedev.server.ai.PullRequestHelper;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestService;

public final class GetPullRequest implements TaskTool {

	private final long pullRequestId;

	public GetPullRequest(long pullRequestId) {
		this.pullRequestId = pullRequestId;
	}

	@Override
	public ToolSpecification getSpecification() {
		return ToolSpecification.builder()
				.name("getPullRequest")
				.description("Get info of pull request in json format")
				.build();
	}

	@Override
	public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
		var request = OneDev.getInstance(PullRequestService.class).load(pullRequestId);
		var project = request.getProject();
		if (!SecurityUtils.canReadCode(subject, project))
			throw new UnauthorizedException();
		return new ToolExecutionResult(convertToJson(PullRequestHelper.getDetail(project, request)), false);
	}
}
