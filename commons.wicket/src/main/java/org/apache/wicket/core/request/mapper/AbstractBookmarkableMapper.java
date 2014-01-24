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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.info.ComponentInfo;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract encoder for Bookmarkable, Hybrid and BookmarkableListenerInterface URLs.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractBookmarkableMapper extends AbstractComponentMapper
{
	private static Logger logger = LoggerFactory.getLogger(AbstractBookmarkableMapper.class);

	/**
	 * Represents information stored in URL.
	 * 
	 * @author Matej Knopp
	 */
	protected static final class UrlInfo
	{
		private final PageComponentInfo pageComponentInfo;
		private final PageParameters pageParameters;
		private final Class<? extends IRequestablePage> pageClass;

		/**
		 * Construct.
		 * 
		 * @param pageComponentInfo
		 *            optional parameter providing the page instance and component information
		 * @param pageClass
		 *            mandatory parameter
		 * @param pageParameters
		 *            optional parameter providing pageParameters
		 */
		public UrlInfo(PageComponentInfo pageComponentInfo,
			Class<? extends IRequestablePage> pageClass, PageParameters pageParameters)
		{
			Args.notNull(pageClass, "pageClass");

			this.pageComponentInfo = pageComponentInfo;
			this.pageParameters = cleanPageParameters(pageParameters);

			this.pageClass = pageClass;
		}

		/**
		 * Cleans the original parameters from entries used by Wicket internals.
		 * 
		 * @param originalParameters
		 *            the current request's non-modified parameters
		 * @return all parameters but Wicket internal ones
		 */
		private PageParameters cleanPageParameters(final PageParameters originalParameters)
		{
			PageParameters cleanParameters = null;
			if (originalParameters != null)
			{
				cleanParameters = new PageParameters(originalParameters);

				// WICKET-4038: Ajax related parameters are set by wicket-ajax.js when needed.
				// They shouldn't be propagated to the next requests
				cleanParameters.remove(WebRequest.PARAM_AJAX);
				cleanParameters.remove(WebRequest.PARAM_AJAX_BASE_URL);
				cleanParameters.remove(WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE);

				if (cleanParameters.isEmpty())
				{
					cleanParameters = null;
				}
			}
			return cleanParameters;
		}

		/**
		 * @return PageComponentInfo instance or <code>null</code>
		 */
		public PageComponentInfo getPageComponentInfo()
		{
			return pageComponentInfo;
		}

		/**
		 * @return page class
		 */
		public Class<? extends IRequestablePage> getPageClass()
		{
			return pageClass;
		}

		/**
		 * @return PageParameters instance (never <code>null</code>)
		 */
		public PageParameters getPageParameters()
		{
			return pageParameters;
		}
	}

	/**
	 * Construct.
	 */
	public AbstractBookmarkableMapper()
	{
	}

	/**
	 * Parse the given request to an {@link UrlInfo} instance.
	 * 
	 * @param request
	 * @return UrlInfo instance or <code>null</code> if this encoder can not handle the request
	 */
	protected abstract UrlInfo parseRequest(Request request);

	/**
	 * Builds URL for the given {@link UrlInfo} instance. The URL this method produces must be
	 * parseable by the {@link #parseRequest(Request)} method.
	 * 
	 * @param info
	 * @return Url result URL
	 */
	protected abstract Url buildUrl(UrlInfo info);

	/**
	 * Indicates whether hybrid {@link RenderPageRequestHandler} URL for page will be generated only
	 * if page has been created with bookmarkable URL.
	 * <p>
	 * For generic bookmarkable encoders this method should return <code>true</code>. For explicit
	 * (mounted) encoders this method should return <code>false</code>
	 * 
	 * @return <code>true</code> if hybrid URL requires page created bookmarkable,
	 *         <code>false</code> otherwise.
	 */
	protected abstract boolean pageMustHaveBeenCreatedBookmarkable();

	/**
	 * @see IRequestMapper#getCompatibilityScore(Request)
	 */
	@Override
	public abstract int getCompatibilityScore(Request request);

	/**
	 * Creates a {@code IRequestHandler} that processes a bookmarkable request.
	 * 
	 * @param pageClass
	 * @param pageParameters
	 * @return a {@code IRequestHandler} capable of processing the bookmarkable request.
	 */
	protected IRequestHandler processBookmarkable(Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters)
	{
		PageProvider provider = new PageProvider(pageClass, pageParameters);
		provider.setPageSource(getContext());
		return new RenderPageRequestHandler(provider);
	}

	/**
	 * Creates a {@code IRequestHandler} that processes a hybrid request. When the page identified
	 * by {@code pageInfo} was not available, the request should be treated as a bookmarkable
	 * request.
	 * 
	 * @param pageInfo
	 * @param pageClass
	 * @param pageParameters
	 * @param renderCount
	 * @return a {@code IRequestHandler} capable of processing the hybrid request.
	 */
	protected IRequestHandler processHybrid(PageInfo pageInfo,
		Class<? extends IRequestablePage> pageClass, PageParameters pageParameters,
		Integer renderCount)
	{
		PageProvider provider = new PageProvider(pageInfo.getPageId(), pageClass, pageParameters,
			renderCount);
		provider.setPageSource(getContext());
		if (provider.isNewPageInstance() && !getRecreateMountedPagesAfterExpiry())
		{
			throw new PageExpiredException(String.format("Bookmarkable page id '%d' has expired.",
				pageInfo.getPageId()));
		}
		else
		{
			return new RenderPageRequestHandler(provider);
		}
	}

	boolean getRecreateMountedPagesAfterExpiry()
	{
		return WebApplication.get().getPageSettings().getRecreateMountedPagesAfterExpiry();
	}

	/**
	 * Creates a {@code IRequestHandler} that processes a listener request.
	 * 
	 * @param pageComponentInfo
	 * @param pageClass
	 * @param pageParameters
	 * @return a {@code IRequestHandler} that invokes the listener interface
	 */
	protected IRequestHandler processListener(PageComponentInfo pageComponentInfo,
		Class<? extends IRequestablePage> pageClass, PageParameters pageParameters)
	{
		PageInfo pageInfo = pageComponentInfo.getPageInfo();
		ComponentInfo componentInfo = pageComponentInfo.getComponentInfo();
		Integer renderCount = null;
		RequestListenerInterface listenerInterface = null;

		if (componentInfo != null)
		{
			renderCount = componentInfo.getRenderCount();
			listenerInterface = requestListenerInterfaceFromString(componentInfo.getListenerInterface());
		}

		if (listenerInterface != null)
		{
			/*
			if (pageInfo.getPageId() != null)
			{
				// WICKET-4594 - ignore the parsed parameters for stateful pages
				pageParameters = null;
			}
			*/

			PageAndComponentProvider provider = new PageAndComponentProvider(pageInfo.getPageId(),
				pageClass, pageParameters, renderCount, componentInfo.getComponentPath());

			provider.setPageSource(getContext());

			return new ListenerInterfaceRequestHandler(provider, listenerInterface,
				componentInfo.getBehaviorId());
		}
		else
		{
			if (logger.isWarnEnabled())
			{
				if (componentInfo != null)
				{
					logger.warn("Unknown listener interface '{}'",
						componentInfo.getListenerInterface());
				}
				else
				{
					logger.warn("Cannot extract the listener interface for PageComponentInfo: '{}'" +
						pageComponentInfo);
				}
			}
			return null;
		}
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	@Override
	public IRequestHandler mapRequest(Request request)
	{
		UrlInfo urlInfo = parseRequest(request);

		// check if the URL is long enough and starts with the proper segments
		if (urlInfo != null)
		{
			PageComponentInfo info = urlInfo.getPageComponentInfo();
			Class<? extends IRequestablePage> pageClass = urlInfo.getPageClass();
			PageParameters pageParameters = urlInfo.getPageParameters();

			if (info == null)
			{
				// if there are is no page instance information (only page map name - optionally)
				// then this is a simple bookmarkable URL
				return processBookmarkable(pageClass, pageParameters);
			}
			else if (info.getPageInfo().getPageId() != null && info.getComponentInfo() == null)
			{
				// if there is page instance information in the URL but no component and listener
				// interface then this is a hybrid URL - we need to try to reuse existing page
				// instance
				return processHybrid(info.getPageInfo(), pageClass, pageParameters, null);
			}
			else if (info.getComponentInfo() != null)
			{
				// with both page instance and component+listener this is a listener interface URL
				return processListener(info, pageClass, pageParameters);
			}
			else if (info.getPageInfo().getPageId() == null)
			{
				return processBookmarkable(pageClass, pageParameters);
			}

		}
		return null;
	}

	protected boolean checkPageInstance(IRequestablePage page)
	{
		return page != null && checkPageClass(page.getClass());
	}

	protected boolean checkPageClass(Class<? extends IRequestablePage> pageClass)
	{
		return true;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		// TODO see if we can refactor this to remove dependency on instanceof checks below and
		// eliminate the need for IRequestHandlerDelegate
		while (requestHandler instanceof IRequestHandlerDelegate)
		{
			requestHandler = ((IRequestHandlerDelegate)requestHandler).getDelegateHandler();
		}

		if (requestHandler instanceof BookmarkablePageRequestHandler)
		{
			// simple bookmarkable URL with no page instance information
			BookmarkablePageRequestHandler handler = (BookmarkablePageRequestHandler)requestHandler;

			if (!checkPageClass(handler.getPageClass()))
			{
				return null;
			}

			PageInfo info = new PageInfo();
			UrlInfo urlInfo = new UrlInfo(new PageComponentInfo(info, null),
				handler.getPageClass(), handler.getPageParameters());

			return buildUrl(urlInfo);
		}
		else if (requestHandler instanceof RenderPageRequestHandler)
		{
			// possibly hybrid URL - bookmarkable URL with page instance information
			// but only allowed if the page was created by bookmarkable URL

			RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;

			if (!checkPageClass(handler.getPageClass()))
			{
				return null;
			}

			if (handler.getPageProvider().isNewPageInstance())
			{
				// no existing page instance available, don't bother creating new page instance
				PageInfo info = new PageInfo();
				UrlInfo urlInfo = new UrlInfo(new PageComponentInfo(info, null),
					handler.getPageClass(), handler.getPageParameters());

				return buildUrl(urlInfo);
			}

			IRequestablePage page = handler.getPage();

			if (checkPageInstance(page) &&
				(!pageMustHaveBeenCreatedBookmarkable() || page.wasCreatedBookmarkable()))
			{
				PageInfo info = getPageInfo(handler);
				PageComponentInfo pageComponentInfo = info != null ? new PageComponentInfo(info,
					null) : null;

				UrlInfo urlInfo = new UrlInfo(pageComponentInfo, page.getClass(),
					handler.getPageParameters());
				return buildUrl(urlInfo);
			}
			else
			{
				return null;
			}

		}
		else if (requestHandler instanceof BookmarkableListenerInterfaceRequestHandler)
		{
			// listener interface URL with page class information
			BookmarkableListenerInterfaceRequestHandler handler = (BookmarkableListenerInterfaceRequestHandler)requestHandler;
			Class<? extends IRequestablePage> pageClass = handler.getPageClass();

			if (!checkPageClass(pageClass))
			{
				return null;
			}

			Integer renderCount = null;
			if (handler.getListenerInterface().isIncludeRenderCount())
			{
				renderCount = handler.getRenderCount();
			}

			PageInfo pageInfo = getPageInfo(handler);
			ComponentInfo componentInfo = new ComponentInfo(renderCount,
				requestListenerInterfaceToString(handler.getListenerInterface()),
				handler.getComponentPath(), handler.getBehaviorIndex());

			PageParameters parameters = getRecreateMountedPagesAfterExpiry() ? new PageParameters(
				handler.getPage().getPageParameters()).mergeWith(handler.getPageParameters())
				: handler.getPageParameters();
			UrlInfo urlInfo = new UrlInfo(new PageComponentInfo(pageInfo, componentInfo),
				pageClass, parameters);
			return buildUrl(urlInfo);
		}

		return null;
	}

	protected final PageInfo getPageInfo(IPageRequestHandler handler)
	{
		Args.notNull(handler, "handler");

		Integer pageId = null;
		if (handler.isPageInstanceCreated())
		{
			IRequestablePage page = handler.getPage();

			if (page.isPageStateless() == false)
			{
				pageId = page.getPageId();
			}
		}

		return new PageInfo(pageId);
	}
}
