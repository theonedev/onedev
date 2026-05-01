package io.onedev.server.ai.taskchecker;

import java.util.Set;

public class NoopTaskChecker implements TaskChecker {

    @Override
    public String preToolCall(String toolName, Set<String> calledTools) {
        return null;
    }

    @Override
    public boolean isResponseRequired(Set<String> calledTools) {
        return true;
    }

}