package io.onedev.server.ai;

import java.util.concurrent.CompletableFuture;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;

public interface ChatTool {
			
	ToolSpecification getSpecification();
	
	CompletableFuture<ToolExecutionResult> execute(@Nullable IPartialPageRequestHandler handler, Subject subject, JsonNode arguments);
	
}
