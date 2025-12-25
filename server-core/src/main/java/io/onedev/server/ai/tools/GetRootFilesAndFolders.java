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

public abstract class GetRootFilesAndFolders implements ChatTool {
        
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getRootFilesAndFolders")
            .description("List files and folders under repository root in json format")
            .parameters(JsonObjectSchema.builder()
                .addBooleanProperty("oldRevision").description("Optionally specify whether to list root files and folders in old revision in a diff context")
                .required("oldRevision").build())
            .build();
    }

    @Override
    public CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
        return completedFuture(new ChatToolExecution.Result(ChatToolUtils.getFilesAndFolders(getProject(), getCommitId(false), null), false));
    }

    protected abstract Project getProject();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}