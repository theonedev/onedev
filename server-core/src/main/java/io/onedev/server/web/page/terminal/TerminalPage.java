package io.onedev.server.web.page.terminal;

import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.terminal.TerminalMessages;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TerminalPage extends BasePage {

	private static final String PARAM_BUILD = "build";
	
	private final IModel<Build> buildModel;
	
	public TerminalPage(PageParameters params) {
		super(params);
		
		Long buildId = params.get(PARAM_BUILD).toLongObject();
		buildModel = new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				return OneDev.getInstance(BuildManager.class).load(buildId);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebSocketBehavior() {

			private TerminalManager getTerminalManager() {
				return OneDev.getInstance(TerminalManager.class);
			}
			
			@Override
			protected void onConnect(ConnectedMessage message) {
				IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
						message.getApplication(), message.getSessionId(), message.getKey());				
				if (connection != null) {
					try {
						connection.sendMessage(TerminalMessages.OPEN);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			protected void onClose(ClosedMessage message) {
				IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
						message.getApplication(), message.getSessionId(), message.getKey());				
				if (connection != null)
					getTerminalManager().onClose(connection);
			}

			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
				IWebSocketConnection connection = new SimpleWebSocketConnectionRegistry().getConnection(
						Application.get(), Session.get().getId(), new PageIdKey(getPageId()));				
				if (connection != null) {
					if (message.getText().equals(TerminalMessages.READY)) {
						getTerminalManager().onOpen(connection, getBuild());
					} else if (message.getText().startsWith(TerminalMessages.INPUT)) {
						String input = message.getText().substring(TerminalMessages.INPUT.length()+1);
						getTerminalManager().onInput(connection, input);
					} else if (message.getText().startsWith(TerminalMessages.RESIZE)) {
						String input = message.getText().substring(TerminalMessages.RESIZE.length()+1);
						int rows = Integer.parseInt(StringUtils.substringBefore(input, ":"));
						int cols = Integer.parseInt(StringUtils.substringAfter(input, ":"));
						getTerminalManager().onResize(connection, rows, cols);
					}
				}
			}
			
		});
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getBuild().getProject());
	}
	
	private Build getBuild() {
		return buildModel.getObject();
	}

	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	@Override
	protected String getPageTitle() {
		return "Web Terminal";
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TerminalResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.terminal.onDomReady();"));
	}

	public static PageParameters paramsOf(Build build) {
		PageParameters params = new PageParameters();
		params.add(PARAM_BUILD, build.getId());
		return params;
	}
	
}
