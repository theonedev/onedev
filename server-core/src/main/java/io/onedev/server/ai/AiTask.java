package io.onedev.server.ai;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import io.onedev.server.ai.taskchecker.TaskChecker;

public class AiTask {

    private final String systemPrompt;

    private final String userPrompt;

    private final Collection<TaskTool> tools;

    private final TaskChecker taskChecker;

    private final ResponseHandler responseHandler;    

    public AiTask(@Nullable String systemPrompt, String userPrompt, Collection<TaskTool> tools, 
            TaskChecker taskChecker, ResponseHandler responseHandler) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.tools = tools;
        this.taskChecker = taskChecker;
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

    public TaskChecker getTaskChecker() {
        return taskChecker;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

}