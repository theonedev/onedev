package com.pmease.commons.wicket.websocket;

import javax.annotation.Nullable;

import com.pmease.commons.wicket.CommonPage;

public interface WebSocketManager {
	
	static final String ERROR_MESSAGE = "ErrorMessage";
	
	static final String RENDER_CALLBACK = "RenderCallback";
	
	static final String KEEP_ALIVE = "KeepAlive";
	
	void onRegionChange(CommonPage page);
	
	void onRenderPage(CommonPage page);
	
	void onDestroySession(String sessionId);
	
	void onConnect(WebSocketConnection connection);
	
	void requestToRender(WebSocketRegion region, 
			@Nullable PageKey sourcePageKey, @Nullable PageKey targetPageKey);
	
	void start();
	
	void stop();
	
}
