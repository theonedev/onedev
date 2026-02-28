package io.onedev.server.web.component.terminal;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;

import io.onedev.commons.utils.StringUtils;

public abstract class TerminalPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	public TerminalPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebSocketBehavior() {

			@Override
			protected void onConnect(ConnectedMessage message) {
				IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
						message.getApplication(), message.getSessionId(), message.getKey());
				if (connection != null) 
					onConnectionOpen(connection);
			}

			@Override
			protected void onClose(ClosedMessage message) {
				IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
						message.getApplication(), message.getSessionId(), message.getKey());
				if (connection != null)
					onConnectionClose(connection);
			}

			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
				IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
						Application.get(), Session.get().getId(), new PageIdKey(getPage().getPageId()));
				if (connection != null) {
					if (message.getText().startsWith("SHELL_INPUT:")) {
						String data = message.getText().substring("SHELL_INPUT:".length());
						writeToStdin(connection, data);
					} else if (message.getText().startsWith("TERMINAL_RESIZE:")) {
						String size = message.getText().substring("TERMINAL_RESIZE:".length());
						int rows = Integer.parseInt(StringUtils.substringBefore(size, ","));
						int cols = Integer.parseInt(StringUtils.substringAfter(size, ","));
						onResized(connection, rows, cols);
					}
				}
			}

		});

		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {		
		super.onEvent(event);
		if (event.getPayload() instanceof WebSocketPushPayload payload) {
			if (payload.getMessage() instanceof ShellExit) 
				onShellExit(payload.getHandler());
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TerminalResourceReference()));
		var script = String.format("onedev.server.terminal.onDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract void onConnectionOpen(IWebSocketConnection connection);

	protected abstract void onConnectionClose(IWebSocketConnection connection);

	protected abstract void writeToStdin(IWebSocketConnection connection, String data);

	protected abstract void onResized(IWebSocketConnection connection, int rows, int cols);

	protected abstract void onShellExit(IPartialPageRequestHandler handler);

}
