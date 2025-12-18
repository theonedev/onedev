package io.onedev.server.web.websocket;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

import com.fasterxml.jackson.databind.JsonNode;

public class ChatToolExecution implements IWebSocketPushMessage {
	
	private final String toolName;

    private final JsonNode toolArguments;

    private volatile CompletableFuture<Result> executionFuture;
    	
    public ChatToolExecution(String toolName, JsonNode toolArguments) {
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
    public CompletableFuture<Result> getExecutionFuture() {
        return executionFuture;
    }

    public void setExecutionFuture(CompletableFuture<Result> executionFuture) {
        this.executionFuture = executionFuture;
    }
    
    public static class Result {
        
        private final String content;

        private final boolean asSystemMessage;

        public Result(String content, boolean asSystemMessage) {
            this.content = content;
            this.asSystemMessage = asSystemMessage;
        }

        public String getContent() {
            return content;
        }

        public boolean isAsSystemMessage() {
            return asSystemMessage;
        }

    }
}