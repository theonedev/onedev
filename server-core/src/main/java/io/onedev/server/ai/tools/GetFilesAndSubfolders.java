package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;
import static io.onedev.server.ai.ToolUtils.getFilesAndFolders;

import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public abstract class GetFilesAndSubfolders implements TaskTool {
    
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getFilesAndSubfolders")
            .description("List all files and subfolders under specified folder in json format")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("folderPath").description("Folder path to list all files and subfolders")
                .required("folderPath").build())
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        if (!SecurityUtils.canReadCode(subject, project))
            throw new UnauthorizedException();

        if (arguments.get("folderPath") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'folderPath' is required")), false);
        var folderPath = arguments.get("folderPath").asText();
        
        var resultData = Map.of("successful", true, "filesAndSubfolders", getFilesAndFolders(project, getCommitId(), folderPath));
        return new ToolExecutionResult(convertToJson(resultData), false);
    }

    protected abstract Long getProjectId();

    protected abstract ObjectId getCommitId();

}