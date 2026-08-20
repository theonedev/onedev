package io.onedev.server.ai.tools.issue;

import static io.onedev.server.ai.ToolUtils.convertToJson;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import io.onedev.server.OneDev;
import io.onedev.server.ai.IssueHelper;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public final class CreateIssue implements TaskTool {

	private final long projectId;

	public CreateIssue(long projectId) {
		this.projectId = projectId;
	}

	@Override
	public ToolSpecification getSpecification() {
		return ToolSpecification.builder()
				.name("createIssue")
				.description("Create an issue in current project and return created issue in json format")
				.parameters(JsonObjectSchema.builder()
						.addStringProperty("title").description("Title of the issue")
						.addStringProperty("description").description("Description of the issue")
						.addBooleanProperty("confidential").description("Whether the issue is confidential")
						.addProperty("iterations", JsonArraySchema.builder()
								.description("Names of iterations to schedule the issue into")
								.items(new JsonStringSchema()).build())
						.addIntegerProperty("ownEstimatedTime").description("Estimated time in hours for this issue only (requires active subscription and time tracking enabled)")
						.addProperty("fields", IssueHelper.getFieldsSchema())
						.required("title").build())
				.build();
	}

	@Override
	public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
		var project = OneDev.getInstance(ProjectService.class).load(projectId);
		if (!SecurityUtils.canAccessProject(subject, project))
			throw new UnauthorizedException();

		if (!project.isIssueManagement())
			return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Issue management not enabled in this project")), false);

		var data = new LinkedHashMap<String, Serializable>();
		if (arguments.get("title") != null) {
			var title = trimToNull(arguments.get("title").asText());
			if (title != null)
				data.put("title", title);
		}
		if (arguments.get("description") != null)
			data.put("description", arguments.get("description").asText());
		if (arguments.get("confidential") != null)
			data.put("confidential", arguments.get("confidential").asBoolean());
		if (arguments.get("ownEstimatedTime") != null)
			data.put("ownEstimatedTime", arguments.get("ownEstimatedTime").asInt());

		if (arguments.get("iterations") != null) {
			var iterations = new ArrayList<String>();
			for (var iterationNode : arguments.get("iterations"))
				iterations.add(iterationNode.asText());
			data.put("iterations", iterations);
		}

		IssueHelper.setFieldValues(data, arguments.get("fields"));

		try {
			var issue = IssueHelper.createIssue(subject, project, data);
			return new ToolExecutionResult(convertToJson(IssueHelper.getDetail(project, issue)), false);
		} catch (UnauthorizedException e) {
			throw e;
		} catch (RuntimeException e) {
			return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", e.getMessage())), false);
		}
	}

}
