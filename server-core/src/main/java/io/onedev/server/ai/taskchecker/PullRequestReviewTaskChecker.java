package io.onedev.server.ai.taskchecker;

import static io.onedev.server.model.PullRequest.APPROVE_TOOL_NAME;
import static io.onedev.server.model.PullRequest.REQUEST_FOR_CHANGES_TOOL_NAME;

import java.util.Set;

public class PullRequestReviewTaskChecker implements TaskChecker {

    @Override
    public String preToolCall(String toolName, Set<String> calledTools) {
        if (toolName.equals(APPROVE_TOOL_NAME) || toolName.equals(REQUEST_FOR_CHANGES_TOOL_NAME)) {
            if (calledTools.contains(APPROVE_TOOL_NAME)) 
                return "You've already approved the pull request";
            else if (calledTools.contains(REQUEST_FOR_CHANGES_TOOL_NAME))
                return "You've already requested changes for the pull request";
            else
                return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean isResponseRequired(Set<String> calledTools) {
        return !calledTools.contains(APPROVE_TOOL_NAME) && !calledTools.contains(REQUEST_FOR_CHANGES_TOOL_NAME);
    }

}