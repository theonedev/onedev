package io.onedev.server.ai;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

public class AiTask {

    private final String systemPrompt;

    private final String userPrompt;

    private final Collection<TaskTool> tools;

    private final ResponseHandler responseHandler;

    public AiTask(@Nullable String systemPrompt, String userPrompt, Collection<TaskTool> tools, 
            ResponseHandler responseHandler) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.tools = tools;
        this.responseHandler = responseHandler;
    }

    @Nullable
    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public Collection<TaskTool> getTools() {
        return tools;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

}