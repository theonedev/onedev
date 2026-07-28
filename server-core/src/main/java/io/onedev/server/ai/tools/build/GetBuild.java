package io.onedev.server.ai.tools.build;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.OneDev;
import io.onedev.server.ai.BuildHelper;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;

public final class GetBuild implements TaskTool {

	private final long buildId;

	public GetBuild(long buildId) {
		this.buildId = buildId;
	}

	@Override
	public ToolSpecification getSpecification() {
		return ToolSpecification.builder()
				.name("getBuild")
				.description("Get info of build in json format")
				.build();
	}

	@Override
	public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
		var build = OneDev.getInstance(BuildService.class).load(buildId);
		var project = build.getProject();
		if (!SecurityUtils.canAccessProject(subject, project))
			throw new UnauthorizedException();
		return new ToolExecutionResult(convertToJson(BuildHelper.getDetail(project, build)), false);
	}

}
