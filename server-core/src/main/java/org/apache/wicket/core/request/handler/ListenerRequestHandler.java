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
package org.apache.wicket.core.request.handler;

import io.onedev.server.util.ComponentHierarchical;
import io.onedev.server.util.HierarchicalContext;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.core.request.handler.logger.ListenerLogData;
import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request handler that invokes an {@link IRequestListener} on component or behavior and renders page afterwards.
 *
 * @author Matej Knopp
 */
public class ListenerRequestHandler
	implements
		IPageRequestHandler,
		IComponentRequestHandler,
		ILoggableRequestHandler
{
	
	private static final Logger LOG = LoggerFactory.getLogger(ListenerRequestHandler.class);

	private final IPageAndComponentProvider pageComponentProvider;

	private final Integer behaviorId;

	private ListenerLogData logData;

	/**
	 * Construct.
	 *
	 * @param pageComponentProvider
	 * @param behaviorIndex
	 */
	public ListenerRequestHandler(IPageAndComponentProvider pageComponentProvider, Integer behaviorIndex)
	{
		Args.notNull(pageComponentProvider, "pageComponentProvider");

		this.pageComponentProvider = pageComponentProvider;
		behaviorId = behaviorIndex;
	}

	/**
	 * Construct.
	 *
	 * @param pageComponentProvider
	 */
	public ListenerRequestHandler(PageAndComponentProvider pageComponentProvider)
	{
		this(pageComponentProvider, null);
	}

	public boolean includeRenderCount()
	{
		if (behaviorId == null)
		{
			return ((IRequestListener)getComponent()).rendersPage();
		}
		else
		{
			return ((IRequestListener)getComponent().getBehaviorById(getBehaviorIndex()))
				.rendersPage();
		}
	}
	
	@Override
	public IRequestableComponent getComponent()
	{
		return pageComponentProvider.getComponent();
	}

	@Override
	public IRequestablePage getPage()
	{
		return pageComponentProvider.getPageInstance();		
	}

	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageComponentProvider.getPageClass();
	}

	@Override
	public Integer getPageId()
	{
		return pageComponentProvider.getPageId();
	}

	@Override
	public PageParameters getPageParameters()
	{
		return pageComponentProvider.getPageParameters();
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
		{
			logData = new ListenerLogData(pageComponentProvider, behaviorId);
		}
		pageComponentProvider.detach();
	}

	/**
	 * Index of target behavior or <code>null</code> if component is the target.
	 *
	 * @return behavior index or <code>null</code>
	 */
	public Integer getBehaviorIndex()
	{
		return behaviorId;
	}

	@Override
	public void respond(final IRequestCycle requestCycle)
	{
		final IRequestablePage page = getPage();
		final boolean freshPage = pageComponentProvider.doesProvideNewPage();
		final boolean isAjax = ((WebRequest)requestCycle.getRequest()).isAjax();

		IRequestableComponent component;
		try
		{
			component = getComponent();
		}
		catch (ComponentNotFoundException e)
		{
			// either the page is stateless and the component we are looking for is not added in the
			// constructor
			// or the page is stateful+stale and a new instances was created by pageprovider
			// we denote this by setting component to null
			component = null;
		}

		if ((component == null && !freshPage) || (component != null && component.getPage() != page))
		{
			throw new ComponentNotFoundException("Component '" + getComponentPath()
					+ "' has been removed from page.");
		}

		RedirectPolicy policy = page.isPageStateless()
			? RedirectPolicy.NEVER_REDIRECT
			: RedirectPolicy.AUTO_REDIRECT;

		boolean blockIfExpired = component != null && !component.canCallListenerAfterExpiry();

		boolean lateComponent = component == null && freshPage;

		if ((pageComponentProvider.wasExpired() && blockIfExpired) || lateComponent)
		{
			// A request listener is invoked on an expired page or the component couldn't be
			// determined. The best we can do is to re-paint the newly constructed page.
			// Reference: WICKET-4454, WICKET-6288

			if (LOG.isDebugEnabled())
			{
				LOG.debug(
					"An IRequestListener was called but its page/component({}) couldn't be resolved. "
						+ "Scheduling re-create of the page and ignoring the listener interface...",
					getComponentPath());
			}

			if (isAjax)
			{
				policy = RedirectPolicy.ALWAYS_REDIRECT;
			}

			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
				new PageProvider(page), policy));
			return;
		}

		invokeListener(requestCycle, policy, isAjax);
	}

	private void invokeListener(IRequestCycle requestCycle, RedirectPolicy policy, boolean ajax)
	{
		HierarchicalContext.push(new HierarchicalContext(new ComponentHierarchical((Component) getComponent())));
		try {
			if (getBehaviorIndex() == null)
			{
				invoke(requestCycle, policy, ajax, getComponent());
			}
			else
			{
				final Behavior behavior;
				try
				{
					behavior = getComponent().getBehaviorById(behaviorId);
				}
				catch (IndexOutOfBoundsException e)
				{
					throw new WicketRuntimeException("Couldn't find component behavior.", e);
				}
				invoke(requestCycle, policy, ajax, getComponent(), behavior);
			}
		} finally {
			HierarchicalContext.pop();
		}
	}
	
	/**
	 * Invokes a given interface on a component.
	 * 
	 * @param rcomponent
	 *            The component
	 * 
	 * @throws ListenerInvocationNotAllowedException
	 *             when listener invocation attempted on a component that does not allow it
	 */
	private final void invoke(final IRequestCycle requestCycle, RedirectPolicy policy, boolean ajax, final IRequestableComponent rcomponent)
	{
		// we are in Wicket core land
		final Component component = (Component)rcomponent;

		if (!component.canCallListener())
		{
			// just return so that we have a silent fail and just re-render the
			// page
			LOG.info("component not enabled or visible; ignoring call. Component: " + component);
			throw new ListenerInvocationNotAllowedException(component, null,
				"Component rejected interface invocation");
		}

		internalInvoke(requestCycle, policy, ajax, component, component);
	}

	/**
	 * Invokes a given interface on a component's behavior.
	 * 
	 * @param rcomponent
	 *            The component
	 * @param behavior
	 * @throws ListenerInvocationNotAllowedException
	 *             when listener invocation attempted on a component that does not allow it
	 */
	private final void invoke(final IRequestCycle requestCycle, RedirectPolicy policy, boolean ajax, final IRequestableComponent rcomponent, final Behavior behavior)
	{
		// we are in Wicket core land
		final Component component = (Component)rcomponent;

		if (!behavior.canCallListener(component))
		{
			LOG.warn("behavior not enabled; ignore call. Behavior {} at component {}", behavior,
				component);
			throw new ListenerInvocationNotAllowedException(component, behavior,
				"Behavior rejected interface invocation. ");
		}

		internalInvoke(requestCycle, policy, ajax, component, behavior);
	}

	private void internalInvoke(final IRequestCycle requestCycle, RedirectPolicy policy, boolean ajax, final Component component, final Object target)
	{
		// save a reference to the page because the component can be removed
		// during the invocation of the listener and thus lose its parent
		Page page = component.getPage();

		// initialization is required for stateless pages
		if (!page.isInitialized())
		{
			page.internalInitialize();
		}

		IRequestListener requestListener = (IRequestListener)target;
		
		if (requestListener.rendersPage() && !ajax)
		{
			// schedule page render after current request handler is done. this can be
			// overridden during invocation of listener
			// method (i.e. by calling RequestCycle#setResponsePage)
			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
				new PageProvider(page), policy));
		}

		requestListener.onRequest();
	}

	@Override
	public final boolean isPageInstanceCreated()
	{
		return pageComponentProvider.hasPageInstance();
	}

	@Override
	public final String getComponentPath()
	{
		return pageComponentProvider.getComponentPath();
	}

	@Override
	public final Integer getRenderCount()
	{
		return pageComponentProvider.getRenderCount();
	}

	@Override
	public ListenerLogData getLogData()
	{
		return logData;
	}
}
