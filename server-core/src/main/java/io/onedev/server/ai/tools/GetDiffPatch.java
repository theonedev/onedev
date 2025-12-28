package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.git.service.GitService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public abstract class GetDiffPatch implements TaskTool {

    private static final int MAX_PATCH_SIZE = 1024 * 1024;

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getDiffPatch")
            .description("Get diff patch between old and new revisions in json format")
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        if (!SecurityUtils.canReadCode(subject, project))
            throw new UnauthorizedException();
        var patch = OneDev.getInstance(GitService.class).getPatch(project, getOldCommitId(), getNewCommitId());
        if (patch.length() > MAX_PATCH_SIZE)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Patch is too large")), false);
        else
            return new ToolExecutionResult(convertToJson(Map.of("successful", true, "patch", patch)), false);
    }

    protected abstract Long getProjectId();

    protected abstract ObjectId getOldCommitId();

    protected abstract ObjectId getNewCommitId();

}