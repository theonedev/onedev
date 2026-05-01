package io.onedev.server.ai.taskchecker;

import java.util.Set;

import org.jspecify.annotations.Nullable;

public interface TaskChecker {

    @Nullable
    String preToolCall(String toolName, Set<String> calledTools);

    boolean isResponseRequired(Set<String> calledTools);
    
}