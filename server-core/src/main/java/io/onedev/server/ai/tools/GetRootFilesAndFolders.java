package io.onedev.server.ai.tools;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.ai.ToolUtils;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public abstract class GetRootFilesAndFolders implements TaskTool {
        
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getRootFilesAndFolders")
            .description("List files and folders under repository root in json format")
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        if (!SecurityUtils.canReadCode(subject, project))
            throw new UnauthorizedException();
        return new ToolExecutionResult(ToolUtils.getFilesAndFolders(project, getCommitId(), null), false);
    }

    protected abstract Long getProjectId();

    protected abstract ObjectId getCommitId();

}