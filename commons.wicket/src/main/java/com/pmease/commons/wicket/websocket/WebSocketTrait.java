package com.pmease.commons.wicket.websocket;

import java.io.Serializable;

public interface WebSocketTrait extends Serializable {
	boolean is(WebSocketTrait trait);
}
