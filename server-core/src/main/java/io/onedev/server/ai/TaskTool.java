package io.onedev.server.ai;

import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;

public interface TaskTool {
			
	ToolSpecification getSpecification();
	
	ToolExecutionResult execute(Subject subject, JsonNode arguments);

}
