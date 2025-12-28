package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.ai.ToolUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public abstract class GetFileContent implements TaskTool {
        
    private static final int MAX_FILE_LENGTH = 500000;
    
    private final boolean inDiffContext;

    public GetFileContent(boolean inDiffContext) {
        this.inDiffContext = inDiffContext;
    }

    @Override
    public ToolSpecification getSpecification() {
        var paramsBuilder = JsonObjectSchema.builder().addStringProperty("filePath").description("Text file path to get content");
        if (inDiffContext) 
            paramsBuilder.addBooleanProperty("oldRevision").description("Specify whether to get file content from old revision");
        paramsBuilder.required(ToolUtils.required(inDiffContext, "filePath"));

        return ToolSpecification.builder()
            .name("getFileContent")
            .description("Get content of specified text file in json format")
            .parameters(paramsBuilder.build())
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        if (!SecurityUtils.canReadCode(subject, project))
            throw new UnauthorizedException();

        if (arguments.get("filePath") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'filePath' is required")), false);
        var filePath = arguments.get("filePath").asText();
        if (inDiffContext && arguments.get("oldRevision") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'oldRevision' is required")), false);
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;

        filePath = StringUtils.strip(filePath.replace('\\', '/'), "/");
        var blobIdent = OneDev.getInstance(GitService.class).getBlobIdent(project, getCommitId(oldRevision), filePath);
        if (blobIdent == null) 
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "File not found")), false);		
        if (!blobIdent.isFile())
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Not a file")), false);		

        var blob = project.getBlob(blobIdent, true);
        if (blob.getText() != null) {
            var content = blob.getText().getContent();
            if (content.length() > MAX_FILE_LENGTH)
                return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "File is too large")), false);
            else
                return new ToolExecutionResult(convertToJson(Map.of("successful", true, "fileContent", content)), false);
        } else {
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Not a text file")), false);
        }
    }

    protected abstract Long getProjectId();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}