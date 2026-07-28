package io.onedev.server.ai.tools.issue;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.ai.IssueHelper;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.security.SecurityUtils;

public final class GetValidFields implements TaskTool {

	@Override
	public ToolSpecification getSpecification() {
		return ToolSpecification.builder()
				.name("getValidFields")
				.description("Get valid issue fields and their allowed values in json format. "
						+ "Call this to discover field names and values accepted by the 'fields' "
						+ "argument of createIssue tool")
				.build();
	}

	@Override
	public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
		if (SecurityUtils.getUser(subject) == null)
			throw new UnauthenticatedException();
		return new ToolExecutionResult(convertToJson(IssueHelper.getValidFields()), false);
	}

}
