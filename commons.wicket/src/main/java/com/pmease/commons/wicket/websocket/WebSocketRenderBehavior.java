package com.pmease.commons.wicket.websocket;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.apache.wicket.request.component.IRequestablePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapMaker;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.Pair;

@SuppressWarnings("serial")
public abstract class WebSocketRenderBehavior extends WebSocketBehavior {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketRenderBehavior.class);
	
	private static final Map<IWebSocketConnection, ConnectionData> connections = 
			new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	
	private static final Map<Integer, Date> recentRenderedPages = new ConcurrentHashMap<>();
	
	private static final Map<RenderData, Date> recentSentMessages = new ConcurrentHashMap<>();
	
    private Component component;
    
    @Override
	public void bind(Component component) {
		super.bind(component);
		
		component.setOutputMarkupId(true);
		this.component = component;
	}

	public static void onPageRender(Integer pageId) {
		recentRenderedPages.put(pageId, new Date());
	}
	
	protected Component getComponent() {
		return component;
	}
	
	public static void removePagesBefore(Date date) {
		for (Iterator<Map.Entry<Integer, Date>> it = recentRenderedPages.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Integer, Date> entry = it.next();
			if (entry.getValue().before(date))
				it.remove();
		}
	}
	
	public static void removeMessagesBefore(Date date) {
		for (Iterator<Map.Entry<RenderData, Date>> it = recentSentMessages.entrySet().iterator(); it.hasNext();) {
			Map.Entry<RenderData, Date> entry = it.next();
			if (entry.getValue().before(date))
				it.remove();
		}
	}

	@Override
	protected void onConnect(ConnectedMessage message) {
		super.onConnect(message);

		IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
				message.getApplication(), message.getSessionId(), message.getKey());
		
		ConnectionData data = connections.get(connection);
		if (data == null) { 
			List<WebSocketTrait> traits = new ArrayList<>();
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
			data = new ConnectionData(pageId, traits);
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
		Integer connectionPageId = data.getPageId();
		List<WebSocketTrait> connectionTraits = data.getTraits();
		AppLoader.getInstance(ExecutorService.class).execute(new Runnable() {

			@Override
			public void run() {
				
				if (connectionPageId != null) {
					Date renderDate = recentRenderedPages.get(connectionPageId);
					if (renderDate != null) {
						for (Iterator<Map.Entry<RenderData, Date>> it = recentSentMessages.entrySet().iterator(); it.hasNext();) {
							Map.Entry<RenderData, Date> entry = it.next();
							Integer pageId = entry.getKey().getPageId();
							WebSocketTrait trait = entry.getKey().getTrait();
							if ((pageId == null || !pageId.equals(connectionPageId)) && entry.getValue().after(renderDate)) {
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
	
	public static void requestToRender(WebSocketTrait trait, @Nullable IRequestablePage page) {
		try {
			String message = asMessage(trait); 
			
			for (Iterator<Map.Entry<IWebSocketConnection, ConnectionData>> it = connections.entrySet().iterator(); it.hasNext();) {
				Map.Entry<IWebSocketConnection, ConnectionData> entry = it.next();
				IWebSocketConnection connection = entry.getKey();
				if (connection != null && connection.isOpen()) {
					synchronized (connection) {
						ConnectionData data = entry.getValue();
						if ((page == null || !Integer.valueOf(page.getPageId()).equals(data.getPageId()))) {
							for (WebSocketTrait each: data.getTraits()) {
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
	
			recentSentMessages.put(new RenderData(trait, page!=null?page.getPageId():null), new Date());
		} catch (Exception e) {
			logger.error("Error sending websocket message", e);
		}
	}
	
	private static class RenderData extends Pair<WebSocketTrait, Integer> {
		
		RenderData(WebSocketTrait trait, Integer pageId) {
			super(trait, pageId);
		}

		public WebSocketTrait getTrait() {
			return getFirst();
		}

		public Integer getPageId() {
			return getSecond();
		}
		
	}
	
	private static class ConnectionData {
		
		private final Integer pageId;
		
		private final List<WebSocketTrait> traits;
		
		ConnectionData(@Nullable Integer pageId, List<WebSocketTrait> traits) {
			this.pageId = pageId;
			this.traits = traits;
		}

		@Nullable
		public Integer getPageId() {
			return pageId;
		}

		public List<WebSocketTrait> getTraits() {
			return traits;
		}
		
		ConnectionData addTrait(final WebSocketTrait trait) {
			List<WebSocketTrait> copyOfTraits = new ArrayList<>(getTraits());
			copyOfTraits.add(trait);
			return new ConnectionData(getPageId(), copyOfTraits);
		}
	}
	
}
