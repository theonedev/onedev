package io.onedev.server.ai;

import java.util.List;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

public class ToolExecutionResult {
    
    private final String content;

    private final boolean asSystemMessage;

    public ToolExecutionResult(String content, boolean asSystemMessage) {
        this.content = content;
        this.asSystemMessage = asSystemMessage;
    }

    public void addToMessages(List<ChatMessage> messages, ToolExecutionRequest toolRequest) {
        if (asSystemMessage) {
            messages.add(new SystemMessage(content));
            var toolResultMessage = ToolExecutionResultMessage.from(
                toolRequest.id(), toolRequest.name(), "Result returned as system message");
            messages.add(toolResultMessage);
        } else {
            var toolResultMessage = ToolExecutionResultMessage.from(toolRequest.id(), toolRequest.name(), content);
            messages.add(toolResultMessage);
        }
    }
}