/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.ws.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.logger.PageLogData;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.page.PartialPageUpdate;
import org.apache.wicket.page.XmlPartialPageUpdate;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.websocket.WebSocketManager;

/**
 * A handler of WebSocket requests.
 *
 * @since 6.0
 */
public class WebSocketRequestHandler implements IWebSocketRequestHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketRequestHandler.class);

	private final Page page;

	private final IWebSocketConnection connection;

	private PartialPageUpdate update;

	private PageLogData logData;

	public WebSocketRequestHandler(final Component component, final IWebSocketConnection connection)
	{
		this.page = Args.notNull(component, "component").getPage();
		this.connection = Args.notNull(connection, "connection");
	}

	@Override
	public void push(CharSequence message)
	{
		if (connection.isOpen())
		{
			Args.notNull(message, "message");
			try
			{
				connection.sendMessage(message.toString());
			} catch (IOException iox)
			{
				LOG.error("An error occurred while pushing text message.", iox);
			}
		}
		else
		{
			LOG.warn("The websocket connection is already closed. Cannot push the text message '{}'", message);
		}
	}

	@Override
	public void push(byte[] message, int offset, int length)
	{
		if (connection.isOpen())
		{
			Args.notNull(message, "message");
			try
			{
				connection.sendMessage(message, offset, length);
			} catch (IOException iox)
			{
				LOG.error("An error occurred while pushing binary message.", iox);
			}
		}
		else
		{
			LOG.warn("The websocket connection is already closed. Cannot push the binary message '{}'", message);
		}
	}

	@Override
	public void add(Component component, String markupId)
	{
		getUpdate().add(component, markupId);
	}

	private PartialPageUpdate getUpdate() {
		if (update == null) {
			update = new XmlPartialPageUpdate(page) {

				@Override
				protected void onBeforeRespond(Response response) {
					super.onBeforeRespond(response);
					BasePage page = (BasePage) getPage();
					if (page.getSessionFeedback().anyMessage())
						WebSocketRequestHandler.this.add(page.getSessionFeedback());
					
					for (Component component: markupIdToComponent.values()) {
						prependJavaScript((String.format("$(document).trigger('beforeElementReplace', '%s');", component.getMarkupId())));
						appendJavaScript((String.format("$(document).trigger('afterElementReplace', '%s');", component.getMarkupId())));
					}
				}

				@Override
				protected void onAfterRespond(Response response) {
					if (!markupIdToComponent.isEmpty())
						OneDev.getInstance(WebSocketManager.class).observe((BasePage) getPage());
					super.onAfterRespond(response);
				}
				
			};
		}
		return update;
	}

	@Override
	public void add(Component... components)
	{
		for (final Component component : components)
		{
			Args.notNull(component, "component");

			if (component.getOutputMarkupId() == false)
			{
				throw new IllegalArgumentException(
						"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
								component.toString());
			}
			add(component, component.getMarkupId());
		}
	}

	@Override
	public final void addChildren(MarkupContainer parent, Class<?> childCriteria)
	{
		Args.notNull(parent, "parent");
		Args.notNull(childCriteria, "childCriteria");

		parent.visitChildren(childCriteria, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				add(component);
				visit.dontGoDeeper();
			}
		});
	}

	@Override
	public void appendJavaScript(CharSequence javascript)
	{
		getUpdate().appendJavaScript(javascript);
	}

	@Override
	public void prependJavaScript(CharSequence javascript)
	{
		getUpdate().prependJavaScript(javascript);
	}

	@Override
	public Collection<? extends Component> getComponents()
	{
		if (update == null) {
			return Collections.emptyList();
		} else {
			return update.getComponents();
		}
	}

	@Override
	public final void focusComponent(Component component)
	{
		if (component != null && component.getOutputMarkupId() == false)
		{
			throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
							component.toString());
		}
		final String id = component != null ? ("'" + component.getMarkupId() + "'") : "null";
		appendJavaScript("Wicket.Focus.setFocusOnId(" + id + ");");
	}

	@Override
	public IHeaderResponse getHeaderResponse()
	{
		return getUpdate().getHeaderResponse();
	}

	@Override
	public Page getPage()
	{
		return page;
	}

	@Override
	public Integer getPageId()
	{
		return page.getPageId();
	}

	@Override
	public boolean isPageInstanceCreated()
	{
		return true;
	}

	@Override
	public Integer getRenderCount()
	{
		return page.getRenderCount();
	}

	@Override
	public ILogData getLogData()
	{
		return logData;
	}

	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return page.getPageClass();
	}

	@Override
	public PageParameters getPageParameters()
	{
		return page.getPageParameters();
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		if (update != null)
		{
			update.writeTo(requestCycle.getResponse(), "UTF-8");
		}
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
		{
			logData = new PageLogData(page);
		}

		if (update != null) {
			update.detach(requestCycle);
			update = null;
		}
	}
}
