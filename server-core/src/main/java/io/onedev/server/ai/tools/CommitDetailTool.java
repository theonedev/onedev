package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UrlService;

public abstract class CommitDetailTool implements TaskTool {

	@Override
	public ToolSpecification getSpecification() {
		return ToolSpecification.builder()
			.name("getCurrentCommit")
			.description("Get info of current commit in json format")
			.build();
	}

	@Override
	public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
		var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
		if (!SecurityUtils.canReadCode(subject, project))
			throw new UnauthorizedException();

		var commit = project.getRevCommit(getCommitId(), true);
		return new ToolExecutionResult(convertToJson(getDetail(project, commit)), false);
	}

	private Map<String, Object> getDetail(Project project, RevCommit commit) {
		var authorIdent = commit.getAuthorIdent();
		var committerIdent = commit.getCommitterIdent();
		return Map.of(
			"project", project.getPath(),
			"hash", commit.name(),
			"shortMessage", commit.getShortMessage(),
			"fullMessage", commit.getFullMessage(),
			"author", Map.of(
				"name", authorIdent.getName(),
				"emailAddress", authorIdent.getEmailAddress(),
				"date", authorIdent.getWhen()),
			"committer", Map.of(
				"name", committerIdent.getName(),
				"emailAddress", committerIdent.getEmailAddress(),
				"date", committerIdent.getWhen()),
			"parents", List.of(commit.getParents()).stream().map(RevCommit::name).toList(),
			"link", OneDev.getInstance(UrlService.class).urlFor(project, commit.copy(), true));
	}

	protected abstract Long getProjectId();

	protected abstract ObjectId getCommitId();

}
