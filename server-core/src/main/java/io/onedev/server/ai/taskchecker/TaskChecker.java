package io.onedev.server.ai.taskchecker;

import java.util.Map;

import org.jspecify.annotations.Nullable;

public interface TaskChecker {

    @Nullable
    String preToolCall(String toolName, Map<String, Integer> toolCallCounts);

    @Nullable
    String preTaskFinish(Map<String, Integer> toolCallCounts);
    
}