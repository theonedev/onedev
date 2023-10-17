package io.onedev.server.ee.terminal;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.terminal.MessageTypes;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.web.page.base.BasePage;
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

import java.io.IOException;

@SuppressWarnings("serial")
public class BuildTerminalPage extends BasePage {

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_BUILD = "build";
	
	private final IModel<Build> buildModel;
	
	public BuildTerminalPage(PageParameters params) {
		super(params);

		Long projectId = params.get(PARAM_PROJECT).toLongObject();
		
		Long buildNumber = params.get(PARAM_BUILD).toLongObject();
		buildModel = new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
				return OneDev.getInstance(BuildManager.class).find(project, buildNumber);
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
						connection.sendMessage(MessageTypes.TERMINAL_OPEN.name());
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
					if (message.getText().equals(MessageTypes.TERMINAL_READY.name())) {
						getTerminalManager().onOpen(connection, getBuild());
					} else if (message.getText().startsWith(MessageTypes.TERMINAL_INPUT.name())) {
						String input = message.getText().substring(MessageTypes.TERMINAL_INPUT.name().length()+1);
						getTerminalManager().onInput(connection, input);
					} else if (message.getText().startsWith(MessageTypes.TERMINAL_RESIZE.name())) {
						String input = message.getText().substring(MessageTypes.TERMINAL_RESIZE.name().length()+1);
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
		return SecurityUtils.canRunJob(getBuild().getProject(), getBuild().getJobName());  
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
		response.render(JavaScriptHeaderItem.forReference(new BuildTerminalResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildTerminal.onDomReady();"));
	}

	public static PageParameters paramsOf(Build build) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PROJECT, build.getProject().getId());
		params.add(PARAM_BUILD, build.getNumber());
		return params;
	}
	
}
