package com.pmease.commons.wicket.websocket;

import javax.annotation.Nullable;

import org.apache.wicket.MetaDataKey;

import com.pmease.commons.wicket.CommonPage;

public interface WebSocketManager {
	
	static final String ERROR_MESSAGE = "ErrorMessage";
	
	static final String INITIAL_RENDER_CALLBACK = "InitialRenderCallback";
	
	static final String RENDER_CALLBACK = "RenderCallback";
	
	static final String KEEP_ALIVE = "KeepAlive";
	
	static final MetaDataKey<Boolean> INITIAL_RENDER = new MetaDataKey<Boolean>() {

		private static final long serialVersionUID = 1L;
		
	}; 
	
	void onRegionChange(CommonPage page);
	
	void onDestroySession(String sessionId);
	
	void onConnect(WebSocketConnection connection);
	
	void render(WebSocketRegion region, @Nullable PageKey sourcePageKey);
	
	void renderAsync(WebSocketRegion region, @Nullable PageKey sourcePageKey);
	
	void start();
	
	void stop();
	
}
