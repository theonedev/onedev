package com.pmease.commons.wicket.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class WebSocketRenderBehavior extends WebSocketBehavior {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketRenderBehavior.class);
	
	private static final Map<IWebSocketConnection, ConnectionData> connections = new ConcurrentHashMap<>();
	
	private static final Map<PageKey, Date> recentRenderedPages = new ConcurrentHashMap<>();
	
	private static final Map<RenderData, Date> recentSentMessages = new ConcurrentHashMap<>();
	
    private Component component;
    
    @Override
	public void bind(Component component) {
		super.bind(component);
		
		component.setOutputMarkupId(true);
		this.component = component;
	}

	public static void onPageRender(String sessionId, Integer pageId) {
		recentRenderedPages.put(new PageKey(sessionId, pageId), new Date());
	}
	
	protected Component getComponent() {
		return component;
	}
	
	public static void removePagesBefore(Date date) {
		for (Iterator<Map.Entry<PageKey, Date>> it = recentRenderedPages.entrySet().iterator(); it.hasNext();) {
			Map.Entry<PageKey, Date> entry = it.next();
			if (entry.getValue().getTime() < date.getTime())
				it.remove();
		}
	}
	
	public static void removeMessagesBefore(Date date) {
		for (Iterator<Map.Entry<RenderData, Date>> it = recentSentMessages.entrySet().iterator(); it.hasNext();) {
			Map.Entry<RenderData, Date> entry = it.next();
			if (entry.getValue().getTime() < date.getTime())
				it.remove();
		}
	}

	@Override
	protected void onConnect(ConnectedMessage message) {
		super.onConnect(message);

		for (IWebSocketConnection connection: connections.keySet()) {
			if (!connection.isOpen())
				connections.remove(connection);
		}
		
		IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
				message.getApplication(), message.getSessionId(), message.getKey());
		
		ConnectionData data = connections.get(connection);
		if (data == null) { 
			List<WebSocketTrait> traits = new ArrayList<>();
			traits.add(getTrait());
			PageKey connectionKey = new PageKey(message.getSessionId(), message.getKey());
			data = new ConnectionData(connectionKey, traits);
		} else { 
			data = data.addTrait(getTrait());
		}
		connections.put(connection, data);
		
		/*
		 * When a new websocket connection arrives, we re-send all sent websocket messages since 
		 * rendering of its hosting page as otherwise the host page might be missing some 
		 * important websocket messages. For instance, when a pull request is opened, a pull 
		 * request integration preview is calculating in the background, and the request page 
		 * will be rendered, and then a websocket connection is established after browser renders
		 * the page. If integration preview calculation finishes after rendering of the request 
		 * detail page and before websocket establishing of request detail page, the integration 
		 * preview area of the page will not be refreshed without below code as it will miss the 
		 * websocket message sent upon completion of integration preview.   
		 * 
		 */
		PageKey connectionPageKey = data.pageKey;
		List<WebSocketTrait> connectionTraits = data.traits;
		AppLoader.getInstance(ExecutorService.class).execute(new Runnable() {

			@Override
			public void run() {
				Date renderDate = recentRenderedPages.get(connectionPageKey);
				if (renderDate != null) {
					for (Map.Entry<RenderData, Date> entry: recentSentMessages.entrySet()) {
						PageKey pageKey = entry.getKey().pageKey;
						WebSocketTrait trait = entry.getKey().trait;
						if ((pageKey == null || !pageKey.equals(connectionPageKey)) 
								&& entry.getValue().getTime()>renderDate.getTime()) {
							for (WebSocketTrait each: connectionTraits) {
								if (trait.is(each)) {
									try {
										connection.sendMessage(asMessage(trait));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
									break;
								}
							}
						}
					}
				}
			}
		});
	}	
	
	/**
	 * Return trait object of this behavior. When method {@link #requestToRender(Object, PageId)} gets 
	 * called, its first parameter will be compared with this trait via <tt>equals</tt> method, and if 
	 * equals, method {@link #onRender(WebSocketRequestHandler)} will be called to render this behavior.    
	 * 
	 * @return 
	 * 			trait of this behavior
	 */
	protected abstract WebSocketTrait getTrait();
	
	protected void onRender(WebSocketRequestHandler handler, WebSocketTrait trait) {
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
				&& ((WebSocketTrait)webSocketMessage.getPayload()).is(getTrait())) {
			onRender(handler, (WebSocketTrait) webSocketMessage.getPayload());
		}
	}
	
	private static String asMessage(WebSocketTrait trait) {
		try {
			ObjectMapper mapper = AppLoader.getInstance(ObjectMapper.class);
			WebSocketMessage webSocketMessage = new WebSocketMessage(WebSocketMessage.RENDER_CALLBACK, trait);
			return mapper.writeValueAsString(webSocketMessage);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void requestToRender(WebSocketTrait trait) {
		requestToRender(trait, null);
	}
	
	public static void requestToRender(WebSocketTrait trait, @Nullable PageKey pageKey) {
		try {
			String message = asMessage(trait); 
			
			for (Iterator<Map.Entry<IWebSocketConnection, ConnectionData>> it = connections.entrySet().iterator(); it.hasNext();) {
				Map.Entry<IWebSocketConnection, ConnectionData> entry = it.next();
				IWebSocketConnection connection = entry.getKey();
				if (connection != null && connection.isOpen()) {
					synchronized (connection) {
						ConnectionData data = entry.getValue();
						if ((pageKey == null || !pageKey.equals(data.pageKey))) {
							for (WebSocketTrait each: data.traits) {
								if (trait.is(each)) {
									try {
										connection.sendMessage(message);
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
									break;
								}
							}
						}
					}
				} else {
					it.remove();
				}
			}
	
			recentSentMessages.put(new RenderData(trait, pageKey), new Date());
		} catch (Exception e) {
			logger.error("Error sending websocket message", e);
		}
	}
	
	private static class RenderData {
		
		final WebSocketTrait trait;
		
		final PageKey pageKey;
		
		RenderData(WebSocketTrait trait, PageKey pageKey) {
			this.trait = trait;
			this.pageKey = pageKey;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof RenderData))
				return false;
			if (this == other)
				return true;
			RenderData otherData = (RenderData) other;
			return new EqualsBuilder()
					.append(trait, otherData.trait)
					.append(pageKey, otherData.pageKey)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(trait).append(pageKey).toHashCode();
		}
		
	}
	
	private static class ConnectionData {
		
		final PageKey pageKey;

		final List<WebSocketTrait> traits;
		
		ConnectionData(PageKey pageKey, List<WebSocketTrait> traits) {
			this.pageKey = pageKey;
			this.traits = traits;
		}

		ConnectionData addTrait(WebSocketTrait trait) {
			List<WebSocketTrait> copyOfTraits = new ArrayList<>(traits);
			copyOfTraits.add(trait);
			return new ConnectionData(pageKey, copyOfTraits);
		}
	}

}
