package io.onedev.server.ai;

import java.util.List;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
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
            var systemMessage = new SystemMessage(content);
            int lastAiMessageIndex = -1;
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i) instanceof AiMessage) {
                    lastAiMessageIndex = i;
                    break;
                }
            }
            if (lastAiMessageIndex >= 0) {
                messages.add(lastAiMessageIndex, systemMessage);
                var toolResultMessage = ToolExecutionResultMessage.from(
                    toolRequest.id(), toolRequest.name(), "Result added as system message");
                messages.add(toolResultMessage);
                return;
            }
        }
        var toolResultMessage = ToolExecutionResultMessage.from(toolRequest.id(), toolRequest.name(), content);
        messages.add(toolResultMessage);
    }
}