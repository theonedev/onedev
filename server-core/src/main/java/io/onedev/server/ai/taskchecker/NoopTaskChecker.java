package io.onedev.server.ai.taskchecker;

import java.util.Map;

public class NoopTaskChecker implements TaskChecker {

    @Override
    public String preToolCall(String toolName, Map<String, Integer> toolCallCounts) {
        return null;
    }

    @Override
    public String preTaskFinish(Map<String, Integer> toolCallCounts) {
        return null;
    }

}