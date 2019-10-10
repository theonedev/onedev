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
package org.apache.wicket.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Request scoped helper class for {@link IPageManager}.
 * 
 * @author Matej Knopp
 */
public abstract class RequestAdapter
{
	private final IPageManagerContext context;

	private final List<IManageablePage> touchedPages = new ArrayList<IManageablePage>();

	/**
	 * Construct.
	 * 
	 * @param context
	 *            The page manager context
	 */
	public RequestAdapter(final IPageManagerContext context)
	{
		this.context = context;
	}

	/**
	 * Returns the page with specified id. The page is then cached by {@link RequestAdapter} during
	 * the rest of request processing.
	 * 
	 * @param id
	 * @return page instance or <code>null</code> if the page does not exist.
	 */
	protected abstract IManageablePage getPage(int id);

	/**
	 * Store the list of stateful pages.
	 * 
	 * @param touchedPages
	 */
	protected abstract void storeTouchedPages(List<IManageablePage> touchedPages);

	/**
	 * Notification on new session being created.
	 */
	protected abstract void newSessionCreated();

	/**
	 * Bind the session
	 * 
	 * @see IPageManagerContext#bind()
	 */
	protected void bind()
	{
		context.bind();
	}

	/**
	 * @see IPageManagerContext#setSessionAttribute(String, Serializable)
	 * 
	 * @param key
	 * @param value
	 */
	public void setSessionAttribute(String key, Serializable value)
	{
		context.setSessionAttribute(key, value);
	}

	/**
	 * @see IPageManagerContext#getSessionAttribute(String)
	 * 
	 * @param key
	 * @return the session attribute
	 */
	public Serializable getSessionAttribute(final String key)
	{
		return context.getSessionAttribute(key);
	}

	/**
	 * @see IPageManagerContext#getSessionId()
	 * 
	 * @return session id
	 */
	public String getSessionId()
	{
		return context.getSessionId();
	}

	/**
	 * 
	 * @param id
	 * @return null, if not found
	 */
	protected IManageablePage findPage(final int id)
	{
		for (IManageablePage page : touchedPages)
		{
			if (page.getPageId() == id)
			{
				return page;
			}
		}
		return null;
	}

	/**
	 * Touches a page, so it will be stored in the page stores
	 * at the end of the request cycle
	 *
	 * @param page The page to mark as dirty
	 */
	protected void touch(final IManageablePage page)
	{
		if (findPage(page.getPageId()) == null)
		{
			touchedPages.add(page);
		}
	}

	/**
	 * @param page The page to unmark as dirty, so it won't be stored
	 *                at the end of the request cycle
	 */
	protected void untouch(final IManageablePage page)
	{
		Iterator<IManageablePage> iterator = touchedPages.iterator();
		while (iterator.hasNext())
		{
			IManageablePage touchedPage = iterator.next();
			if (touchedPage.getPageId() == page.getPageId())
			{
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * Modified by Robin to remove the logic to check stateless page as otherwise Wicket reports error 
	 * when a page containing "wicket:enclosure" is accessed anonymously and is redirected to signin page     
	 */
	protected void commitRequest()
	{
		// store pages that are not stateless
		if (touchedPages.isEmpty() == false)
		{
			List<IManageablePage> statefulPages = new ArrayList<IManageablePage>(
				touchedPages.size());
			for (IManageablePage page : touchedPages)
			{
				statefulPages.add(page);
			}

			if (statefulPages.isEmpty() == false)
			{
				storeTouchedPages(statefulPages);
			}
		}
	}

	public void clear() {
		touchedPages.clear();
	}
}