package io.onedev.server.ai.tools;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletableFuture;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.ai.ChatToolUtils;
import io.onedev.server.model.Project;
import io.onedev.server.web.websocket.ChatToolExecution;

public abstract class GetFilesAndSubfolders implements ChatTool {
    
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getFilesAndSubfolders")
            .description("List all files and subfolders under specified folder in json format")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("folderPath").description("Folder path to list all files and subfolders")
                .addBooleanProperty("oldRevision").description("Optionally specify whether to list files and subfolders in old revision in a diff context")
                .required("folderPath").build())
                .build();
    }

    @Override
    public CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
        var folderPath = arguments.get("folderPath").asText();
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;
        return completedFuture(new ChatToolExecution.Result(ChatToolUtils.getFilesAndFolders(getProject(), getCommitId(oldRevision), folderPath), false));
    }

    protected abstract Project getProject();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}