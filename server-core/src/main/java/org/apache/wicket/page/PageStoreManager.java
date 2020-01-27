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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.pageStore.IPageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class PageStoreManager extends AbstractPageManager
{
	private static final Logger logger = LoggerFactory.getLogger(PageStoreManager.class);
	
	/**
	 * A cache that holds all registered page managers. <br/>
	 * applicationName -> page manager
	 */
	private static final ConcurrentMap<String, PageStoreManager> MANAGERS = new ConcurrentHashMap<>();

	private static final String ATTRIBUTE_NAME = "wicket:persistentPageManagerData";

	/**
	 * A flag indicating whether this session entry is being re-set in the Session.
	 * <p>
	 * Web containers intercept
	 * {@link javax.servlet.http.HttpSession#setAttribute(String, Object)} to detect changes and
	 * replicate the session. If the attribute has been already bound in the session then
	 * {@link SessionEntry#valueUnbound(HttpSessionBindingEvent)} might get called - this flag
	 * helps us to ignore the invocation in that case.
	 * 
	 * @see SessionEntry#valueUnbound(HttpSessionBindingEvent)
	 */
	private static final ThreadLocal<Boolean> STORING_TOUCHED_PAGES = new ThreadLocal<Boolean>()
	{
		protected Boolean initialValue()
		{
			return Boolean.FALSE;
		};
	};

	private final IPageStore pageStore;

	private final String applicationName;

	/**
	 * Construct.
	 * 
	 * @param applicationName
	 * @param pageStore
	 * @param context
	 */
	public PageStoreManager(final String applicationName, final IPageStore pageStore,
		final IPageManagerContext context)
	{
		super(context);

		this.applicationName = applicationName;
		this.pageStore = pageStore;

		if (MANAGERS.containsKey(applicationName))
		{
			throw new IllegalStateException(
					"Manager for application with key '" + applicationName + "' already exists.");
		}
		MANAGERS.put(applicationName, this);
	}

	/**
	 * Represents entry for single session. This is stored as session attribute and caches pages
	 * between requests.
	 * 
	 * @author Matej Knopp
	 */
	private static class SessionEntry implements Serializable, HttpSessionBindingListener
	{
		private static final long serialVersionUID = 1L;

		private final String applicationName;

		/**
		 * The id handed to the {@link IPageStore} to identify the session.
		 * <p>
		 * Note: If the container changes a session's id, this field remains unchanged on its
		 * initial value.
		 */
		private final String sessionId;

		/**
		 * Construct.
		 * 
		 * @param applicationName
		 * @param sessionId
		 */
		public SessionEntry(String applicationName, String sessionId)
		{
			this.applicationName = applicationName;
			this.sessionId = sessionId;
		}

		/**
		 * 
		 * @return page store
		 */
		private IPageStore getPageStore()
		{
			PageStoreManager manager = MANAGERS.get(applicationName);

			if (manager == null)
			{
				return null;
			}

			return manager.pageStore;
		}

		/**
		 * 
		 * @param id
		 * @return manageable page
		 */
		public synchronized IManageablePage getPage(int id)
		{
			IManageablePage page = null;
			final IPageStore pageStore = getPageStore();
			if (pageStore != null)
			{
				page = pageStore.getPage(sessionId, id);
			}
			return page;
		}

		/**
		 * set the list of pages to remember after the request
		 * 
		 * @param pages
		 */
		public synchronized void setSessionCache(final List<IManageablePage> pages)
		{
		}

		@Override
		public void valueBound(HttpSessionBindingEvent event)
		{
		}

		@Override
		public void valueUnbound(HttpSessionBindingEvent event)
		{
			if (STORING_TOUCHED_PAGES.get())
			{
				// triggered by #storeTouchedPages(), so do not remove the data
				return;
			}
			clear();
		}

		@Override
		public boolean equals(Object o)
		{
			// see https://issues.apache.org/jira/browse/WICKET-5390
			return false;
		}
		
		public void clear()
		{
			// WICKET-5164 use the original sessionId
			IPageStore store = getPageStore();
			// store might be null if destroyed already
			if (store != null)
			{
				store.unbind(sessionId);
			}
		}
	}

	private String getAttributeName()
	{
		return ATTRIBUTE_NAME + " - " + applicationName;
	}

	/**
	 * {@link RequestAdapter} for {@link PageStoreManager}
	 * 
	 * @author Matej Knopp
	 */
	protected class PersistentRequestAdapter extends RequestAdapter
	{
		/**
		 * Construct.
		 * 
		 * @param context
		 */
		public PersistentRequestAdapter(IPageManagerContext context)
		{
			super(context);
		}

		@Override
		protected IManageablePage getPage(int id)
		{
			IManageablePage touchedPage = findPage(id);
			if (touchedPage != null)
			{
				return touchedPage;
			}

			// try to get session entry for this session
			SessionEntry entry = getSessionEntry(false);

			if (entry != null)
			{
				return entry.getPage(id);
			}
			else
			{
				return null;
			}
		}

		/**
		 * 
		 * @param create
		 * @return Session Entry
		 */
		private SessionEntry getSessionEntry(boolean create)
		{
			SessionEntry entry = (SessionEntry)getSessionAttribute(getAttributeName());
			if (entry == null && create)
			{
				bind();
				entry = new SessionEntry(applicationName, getSessionId());
			}
			return entry;
		}

		@Override
		protected void newSessionCreated()
		{
			// if the session is not temporary bind a session entry to it
			if (getSessionId() != null)
			{
				getSessionEntry(true);
			}
		}

		@Override
		protected void storeTouchedPages(final List<IManageablePage> touchedPages)
		{
			if (!touchedPages.isEmpty())
			{
				try {
					SessionEntry entry = getSessionEntry(true);
					entry.setSessionCache(touchedPages);
					for (IManageablePage page : touchedPages)
					{
						// WICKET-5103 use the same sessionId as used in
						// SessionEntry#getPage()
						pageStore.storePage(entry.sessionId, page);
					}

					STORING_TOUCHED_PAGES.set(true);
					try
					{
						setSessionAttribute(getAttributeName(), entry);
					}
					finally
					{
						STORING_TOUCHED_PAGES.remove();
					}
				} catch (IllegalStateException e) {
					if (e.getMessage().contains("Response is committed")) 
						logger.debug("Error storing touched pages", e);
					else 
						throw e;
				}
			}
		}
	}

	@Override
	protected RequestAdapter newRequestAdapter(IPageManagerContext context)
	{
		return new PersistentRequestAdapter(context);
	}

	@Override
	public boolean supportsVersioning()
	{
		return true;
	}

	@Override
	public void clear()
	{
		RequestAdapter requestAdapter = getRequestAdapter();
		requestAdapter.clear();
		String sessionEntryAttributeName = getAttributeName();
		Serializable sessionEntry = requestAdapter.getSessionAttribute(sessionEntryAttributeName);
		if (sessionEntry instanceof SessionEntry)
		{
			((SessionEntry)sessionEntry).clear();
		}
	}

	@Override
	public void destroy()
	{
		MANAGERS.remove(applicationName);
		pageStore.destroy();
	}
}
