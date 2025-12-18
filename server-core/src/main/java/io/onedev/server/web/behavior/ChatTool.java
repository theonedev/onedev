package io.onedev.server.web.behavior;

import java.util.concurrent.CompletableFuture;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.web.websocket.ChatToolExecution;

public abstract class ChatTool extends Behavior {
			
	public abstract ToolSpecification getSpecification();
	
	public abstract CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments);

}
