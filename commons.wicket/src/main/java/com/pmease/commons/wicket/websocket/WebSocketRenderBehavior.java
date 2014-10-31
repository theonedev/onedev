package com.pmease.commons.wicket.websocket;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.MapMaker;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class WebSocketRenderBehavior extends WebSocketBehavior {

	private final static Map<IWebSocketConnection, ConnectionData> connections = 
			new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	
    private Component component;
    
    private final boolean renderOnConnect;
	
    public WebSocketRenderBehavior(boolean renderOnConnect) {
    	this.renderOnConnect = renderOnConnect;
	}
    
    public WebSocketRenderBehavior() {
    	this(false);
	}

    @Override
	public void bind(Component component) {
		super.bind(component);
		
		component.setOutputMarkupId(true);
		this.component = component;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		if (renderOnConnect) {
			String script = String.format("pmease.commons.websocket.renderTraits.push(%s);", asMessage(getTrait()));
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	@Override
	protected void onConnect(ConnectedMessage message) {
		super.onConnect(message);

		IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
				message.getApplication(), message.getSessionId(), message.getKey());
		
		ConnectionData data = connections.get(connection);
		if (data == null) { 
			List<Object> traits = new ArrayList<>();
			traits.add(getTrait());
			Integer pageId;
			if (message.getKey() instanceof PageIdKey) {
				try {
					Field field = PageIdKey.class.getDeclaredField("pageId");
					field.setAccessible(true);
					pageId = (Integer) field.get(message.getKey());
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else {
				pageId = null;
			}
			data = new ConnectionData(pageId!=null?new PageId(pageId):null, traits);
		} else { 
			data = data.addTrait(getTrait());
		}
		connections.put(connection, data);
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
	
	private static String asMessage(Object trait) {
		try {
			ObjectMapper mapper = AppLoader.getInstance(ObjectMapper.class);
			WebSocketMessage webSocketMessage = new WebSocketMessage(WebSocketMessage.RENDER_CALLBACK, trait);
			return mapper.writeValueAsString(webSocketMessage);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void requestToRender(Object trait, @Nullable PageId pageId) {
		String message = asMessage(trait); 

		for (Iterator<Map.Entry<IWebSocketConnection, ConnectionData>> it = connections.entrySet().iterator(); it.hasNext();) {
			Map.Entry<IWebSocketConnection, ConnectionData> entry = it.next();
			IWebSocketConnection connection = entry.getKey();
			if (connection != null && connection.isOpen()) {
				ConnectionData data = entry.getValue();
				if ((pageId == null || !pageId.equals(data.getPageId())) && data.getTraits().contains(trait)) {
					try {
						connection.sendMessage(message);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				it.remove();
			}
		}
	}
	
	private static class ConnectionData {
		
		private final PageId pageId;
		
		private final List<Object> traits;
		
		ConnectionData(@Nullable final PageId pageId, final List<Object> traits) {
			this.pageId = pageId;
			this.traits = traits;
		}

		public PageId getPageId() {
			return pageId;
		}

		public List<Object> getTraits() {
			return traits;
		}
		
		ConnectionData addTrait(final Object trait) {
			List<Object> copyOfTraits = new ArrayList<>(getTraits());
			copyOfTraits.add(trait);
			return new ConnectionData(getPageId(), copyOfTraits);
		}
	}
	
	public static class PageId implements Serializable {
		
		private final int value;
		
		public PageId(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		@Nullable
		public static PageId fromObj(Object obj) {
			if (obj instanceof PageId)
				return (PageId) obj;
			else
				return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null)  
				return false;  
		    if (getClass() != obj.getClass())  
		        return false;  

		    PageId pageId = (PageId) obj;
		    return Objects.equal(value, pageId.value);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
		
	}
}
