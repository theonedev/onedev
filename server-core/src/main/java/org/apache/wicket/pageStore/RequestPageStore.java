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
package org.apache.wicket.pageStore;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buffers storage of added pages until the end of the request, when they are delegated to the next store in
 * the identical order they where added.
 */
public class RequestPageStore extends DelegatingPageStore
{

	private static final Logger log = LoggerFactory.getLogger(RequestPageStore.class);

	private static final MetaDataKey<RequestData> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	public RequestPageStore(IPageStore delegate)
	{
		super(delegate);
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		IManageablePage page = getRequestData(context).get(id);
		if (page != null)
		{
			return page;
		}

		return getDelegate().getPage(context, id);
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		getRequestData(context).add(page);
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		getRequestData(context).remove(page);

		getDelegate().removePage(context, page);
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		getRequestData(context).removeAll();

		getDelegate().removeAllPages(context);
	}

	@Override
	public void revertPage(IPageContext context, IManageablePage page)
	{
		getRequestData(context).remove(page);
		
		getDelegate().revertPage(context, page);
	}
	
	@Override
	public void end(IPageContext context)
	{
		getDelegate().end(context);
		
		RequestData requestData = getRequestData(context);
		for (IManageablePage page : requestData.pages())
		{
			if (isPageStateless(page) == false)
			{
				// last opportunity to create a session
				try
				{
					context.getSessionId(true);
				}
				catch (IllegalStateException e)
				{
					handlePageStoreException(e);
					// Detach must not retry creating a session after response commitment.
					requestData.removeAll();
				}
				break;
			}
		}
	}
	
	@Override
	public void detach(IPageContext context)
	{
		RequestData requestData = getRequestData(context);
		try
		{
			for (IManageablePage page : requestData.pages())
			{
				if (isPageStateless(page) == false)
				{
					getDelegate().addPage(context, page);
				}
			}
		}
		catch (IllegalStateException e)
		{
			handlePageStoreException(e);
		}
		requestData.removeAll();

		getDelegate().detach(context);
	}

	private void handlePageStoreException(IllegalStateException exception)
	{
		// Preserve the former PageStoreManager's handling of late session creation.
		if (exception.getMessage() != null && exception.getMessage().contains("Response is committed"))
			log.debug("Error storing touched pages", exception);
		else
			throw exception;
	}

	private boolean isPageStateless(final IManageablePage page) {
        // Checking statelessness during an intercepted login request can evaluate
        // an uninitialized wicket:enclosure. Preserve OneDev's stateful page storage.
        return false;
    }

	private RequestData getRequestData(IPageContext context)
	{
		return context.getRequestData(KEY, RequestData::new);
	}
	
	/**
	 * Data kept in the {@link RequestCycle}.
	 */
	static class RequestData
	{
		private final LinkedList<IManageablePage> pages = new LinkedList<>();
		
		public void add(IManageablePage page)
		{
			// add as last
			pages.remove(page);
			pages.addLast(page);
		}

		public Iterable<IManageablePage> pages()
		{
			// must work on copy to prevent concurrent modification when page is re-added during detaching 
			return new ArrayList<>(pages);
		}

		public IManageablePage get(int id)
		{
			for (IManageablePage page : pages)
			{
				if (page.getPageId() == id)
				{
					return page;
				}
			}
			return null;
		}

		public void remove(IManageablePage page)
		{
			pages.remove(page);
		}

		public void removeAll()
		{
			pages.clear();
		}		
	}
}
