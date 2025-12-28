package io.onedev.server.web.websocket;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.server.ai.ToolExecutionResult;

public class AiToolExecution implements IWebSocketPushMessage {
	    
	private final String toolName;

    private final JsonNode toolArguments;

    private volatile CompletableFuture<ToolExecutionResult> future;
    	
    public AiToolExecution(String toolName, JsonNode toolArguments) {
        this.toolName = toolName;
        this.toolArguments = toolArguments;
    }
    
    public String getToolName() {
        return toolName;
    }

    public JsonNode getToolArguments() {
        return toolArguments;
    }

    @Nullable
    public CompletableFuture<ToolExecutionResult> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<ToolExecutionResult> future) {
        this.future = future;
    }
}