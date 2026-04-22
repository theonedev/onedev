package io.onedev.server.ai;

import java.util.Map;

import org.jspecify.annotations.Nullable;

public interface TaskChecker {

    @Nullable
    String preToolCallCheck(String toolName, Map<String, Integer> toolCallCounts);

    @Nullable
    String preTaskFinishCheck(Map<String, Integer> toolCallCounts);

    public class NoopTaskChecker implements TaskChecker {

        @Override
        public String preToolCallCheck(String toolName, Map<String, Integer> toolCallCounts) {
            return null;
        }

        @Override
        public String preTaskFinishCheck(Map<String, Integer> toolCallCounts) {
            return null;
        }
    }
    
}