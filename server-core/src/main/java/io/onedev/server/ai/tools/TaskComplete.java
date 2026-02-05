package io.onedev.server.ai.tools;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ai.ToolUtils;

/**
 * Special tool that signals the AI has completed its task and is ready to provide
 * the final response. This tool is handled specially by DefaultChatService - when
 * called, it completes the conversation with the provided response instead of
 * continuing the tool execution loop.
 */
public class TaskComplete {

    public static final String TOOL_NAME = "taskComplete";

    public static final int MAX_NO_TOOL_RETRIES = 3;

    public static final String SYSTEM_MESSAGE = 
        "IMPORTANT: When you have completed your task, you MUST call the 'taskComplete' tool with your final response. " +
        "Do not provide a response without calling a tool. Always either call a tool to perform an action, or call 'taskComplete' to finish.";

    public static final String CONTINUATION_PROMPT = 
        "Please continue with your task, or call 'taskComplete' tool with your final response if you are finished.";

    public static ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name(TOOL_NAME)
            .description("Call this tool when you have completed the task and are ready to provide your final response to the user. " +
                "You MUST call this tool to finish the conversation - do not respond without calling a tool.")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("response", "Your final response to the user's request. This will be shown directly to the user.")
                .required("response")
                .build())
            .build();
    }

    /**
     * Checks if the tool request is a taskComplete call and extracts the response.
     * 
     * @param toolRequest the tool execution request
     * @return the response string if this is a taskComplete call, null otherwise
     */
    @Nullable
    public static String getResponse(ToolExecutionRequest toolRequest) {
        if (TOOL_NAME.equals(toolRequest.name())) {
            var arguments = ToolUtils.getToolArguments(toolRequest);
            var responseNode = arguments.get("response");
            return responseNode != null ? StringUtils.trimToEmpty(responseNode.asText()) : "";
        }
        return null;
    }

}
