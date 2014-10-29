package com.pmease.commons.wicket.websocket;

public class WebSocketMessage {
	
	public static final String KEEP_ALIVE = "KeepAlive";
	
	public static final String RENDER_CALLBACK = "RenderCallback";
	
	public static final String ERROR_MESSAGE = "ErrorMessage";
	
	private String type;
	
	private Object payload;
	
	public WebSocketMessage(String type, Object payload) {
		this.type = type;
		this.payload = payload;
	}

	public WebSocketMessage() {
		
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public String getType() {
		return type;
	}

	public Object getPayload() {
		return payload;
	}
	
}
