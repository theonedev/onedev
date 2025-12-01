package io.onedev.server.service.support;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;

public interface ChatTool {

    ToolSpecification getSpecification();

    /**
     * This method should not rely on any Wicket facilities, such as page, component, model etc. 
     * If you need to access some data from these facilities, make sure to prepare them while 
     * creating the tool
     * 
     * @param arguments
     * @return
     */
    String execute(JsonNode arguments);

}
