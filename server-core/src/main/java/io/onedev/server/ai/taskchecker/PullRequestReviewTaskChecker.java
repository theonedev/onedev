package io.onedev.server.ai.taskchecker;

import java.util.Map;

import io.onedev.server.model.PullRequest;

public class PullRequestReviewTaskChecker implements TaskChecker {

    @Override
    public String preToolCall(String toolName, Map<String, Integer> toolCallCounts) {
        if ((toolName.equals(PullRequest.APPROVE_TOOL_NAME) || toolName.equals(PullRequest.REQUEST_FOR_CHANGES_TOOL_NAME)) 
                && toolCallCounts.getOrDefault(toolName, 0) >= 1) {
            return "Tool '%s' should only be called once".formatted(toolName);
        } else {
            return null;
        }
    }

    @Override
    public String preTaskFinish(Map<String, Integer> toolCallCounts) {
        if (toolCallCounts.getOrDefault(PullRequest.APPROVE_TOOL_NAME, 0) == 0 
                && toolCallCounts.getOrDefault(PullRequest.REQUEST_FOR_CHANGES_TOOL_NAME, 0) == 0) {
            return "You must call exactly one of the following tools: '%s' or '%s'".formatted(PullRequest.APPROVE_TOOL_NAME, PullRequest.REQUEST_FOR_CHANGES_TOOL_NAME);
        } else {
            return null;
        }
    }

}