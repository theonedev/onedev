package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ChatToolUtils.convertToJson;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.web.websocket.ChatToolExecution;

public abstract class GetFileContent implements ChatTool {
        
    private static final int MAX_FILE_LENGTH = 500000;
    
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getFileContent")
            .description("Get content of specified text file in json format")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("filePath").description("Text file path to get content")
                .addBooleanProperty("oldRevision").description("Optionally specify whether to get file content in old revision in a diff context")
                .required("filePath").build())
            .build();
    }

    @Override
    public CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
        if (arguments.get("filePath") == null)
            return completedFuture(new ChatToolExecution.Result(convertToJson(Map.of("successful", false, "failReason", "Argument 'filePath' is required")), false));
        var filePath = arguments.get("filePath").asText();
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;

        filePath = StringUtils.strip(filePath.replace('\\', '/'), "/");
        var blobIdent = OneDev.getInstance(GitService.class).getBlobIdent(getProject(), getCommitId(oldRevision), filePath);
        if (blobIdent == null) 
            return completedFuture(new ChatToolExecution.Result(convertToJson(Map.of("successful", false, "failReason", "File not found")), false));		
        if (!blobIdent.isFile())
            return completedFuture(new ChatToolExecution.Result(convertToJson(Map.of("successful", false, "failReason", "Not a file")), false));		

        var blob = getProject().getBlob(blobIdent, true);
        if (blob.getText() != null) {
            var content = blob.getText().getContent();
            if (content.length() > MAX_FILE_LENGTH)
                return completedFuture(new ChatToolExecution.Result(convertToJson(Map.of("successful", false, "failReason", "File is too large")), false));
            else
                return completedFuture(new ChatToolExecution.Result(convertToJson(Map.of("successful", true, "fileContent", content)), false));
        } else {
            return completedFuture(new ChatToolExecution.Result(convertToJson(Map.of("successful", false, "failReason", "Not a text file")), false));
        }
    }

    protected abstract Project getProject();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}