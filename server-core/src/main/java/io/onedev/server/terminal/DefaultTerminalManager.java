package io.onedev.server.terminal;

import javax.inject.Singleton;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

import io.onedev.server.model.Build;

@Singleton
public class DefaultTerminalManager implements TerminalManager {

	@Override
	public boolean isTerminalSupported() {
		return false;
	}

	@Override
	public void onOpen(IWebSocketConnection connection, Build build) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onClose(IWebSocketConnection connection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onResize(IWebSocketConnection connection, int rows, int cols) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onInput(IWebSocketConnection connection, String input) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTerminalUrl(Build build) {
		throw new UnsupportedOperationException();
	}

}
