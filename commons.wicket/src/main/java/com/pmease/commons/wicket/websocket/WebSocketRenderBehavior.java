package com.pmease.commons.wicket.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.wicket.Component;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class WebSocketRenderBehavior extends WebSocketBehavior {

	@SuppressWarnings("unchecked")
	private final static Map<IWebSocketConnection, List<Object>> connections = 
			new ReferenceMap(AbstractReferenceMap.WEAK, AbstractReferenceMap.HARD);

    private Component component;
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		
		component.setOutputMarkupId(true);
		this.component = component;
	}

	@Override
	protected void onConnect(ConnectedMessage message) {
		super.onConnect(message);

		IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
				message.getApplication(), message.getSessionId(), message.getKey());
		
		synchronized (connections) {
			List<Object> traits = connections.get(connection);
			if (traits == null) {
				traits = new ArrayList<>();
				connections.put(connection, traits);
			}
			traits.add(getTrait());
		}
	}	
	
	protected abstract Object getTrait();
	
	protected void onRender(WebSocketRequestHandler handler) {
		handler.add(component);
	}

	@Override
	protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
		super.onMessage(handler, message);
		
		ObjectMapper mapper = AppLoader.getInstance(ObjectMapper.class);
		WebSocketMessage webSocketMessage;
		try {
			webSocketMessage = mapper.readValue(message.getText(), WebSocketMessage.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (webSocketMessage.getType().equals(WebSocketMessage.RENDER_CALLBACK) 
				&& webSocketMessage.getPayload().equals(getTrait())) {
			onRender(handler);
		}
	}
	
	public static void requestToRender(Object trait) {
		Collection<IWebSocketConnection> matchedConnections = new ArrayList<>(); 
		synchronized (connections) {
			for (Iterator<Map.Entry<IWebSocketConnection, List<Object>>> it = connections.entrySet().iterator(); it.hasNext();) {
				Map.Entry<IWebSocketConnection, List<Object>> entry = it.next();
				IWebSocketConnection connection = entry.getKey();
				if (connection != null && connection.isOpen()) {
					if (entry.getValue().contains(trait))
						matchedConnections.add(connection);
				} else {
					it.remove();
				}
			}
		}

		String textMessage; 
		try {
			ObjectMapper mapper = AppLoader.getInstance(ObjectMapper.class);
			WebSocketMessage webSocketMessage = new WebSocketMessage(WebSocketMessage.RENDER_CALLBACK, trait);
			textMessage = mapper.writeValueAsString(webSocketMessage);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
		for (IWebSocketConnection connection: matchedConnections) {
			try {
				connection.sendMessage(textMessage);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
