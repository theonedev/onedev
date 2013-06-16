package com.pmease.commons.wicket;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.serialize.ISerializer;

import com.pmease.commons.bootstrap.Bootstrap;

@SuppressWarnings("serial")
public abstract class AbstractWicketConfig extends WebApplication {

	private final MetaDataKey<Boolean> PAGE_RENDERING_SCHEDULED = new MetaDataKey<Boolean>() {
	};  
	
	private final MetaDataKey<Boolean> PAGE_RENDERING_RESOLVED = new MetaDataKey<Boolean>() {
	};  

	private final MetaDataKey<Boolean> PAGE_RENDERING_EXECUTED = new MetaDataKey<Boolean>() {
	};  

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		if (Bootstrap.sandboxMode && !Bootstrap.prodMode)
			return RuntimeConfigurationType.DEVELOPMENT;
		else
			return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath) {
		return new ServletWebRequest(servletRequest, filterPath) {

			@Override
			public boolean shouldPreserveClientUrl() {
				boolean preserve = super.shouldPreserveClientUrl();
				if (preserve) {
					return true;
				} else {
					/*
					 * This snippet code tells Wicket not to append page instance number after the url 
					 * for bookmarkable pages. 
					 */
					return RequestCycle.get().getMetaData(PAGE_RENDERING_RESOLVED);
				}
			}
			
		};
	}

	@Override
	protected void init() {
		super.init();

		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setStripWicketTags(true);

		getRequestCycleListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
				if (handler instanceof RenderPageRequestHandler)
					RequestCycle.get().setMetaData(PAGE_RENDERING_SCHEDULED, true);
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
				if (handler instanceof RenderPageRequestHandler) 
					RequestCycle.get().setMetaData(PAGE_RENDERING_RESOLVED, true);
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
				if (handler instanceof RenderPageRequestHandler) 
					RequestCycle.get().setMetaData(PAGE_RENDERING_EXECUTED, true);
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, 
					IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
				RequestCycle.get().setMetaData(PAGE_RENDERING_EXECUTED, false);
				RequestCycle.get().setMetaData(PAGE_RENDERING_RESOLVED, false);
				RequestCycle.get().setMetaData(PAGE_RENDERING_SCHEDULED, false);
			}
		});
		
		setPageManagerProvider(new DefaultPageManagerProvider(this) {
			
			@Override
			protected IPageStore newPageStore(IDataStore dataStore) {
				
				int inmemoryCacheSize = getStoreSettings().getInmemoryCacheSize();
				ISerializer pageSerializer = application.getFrameworkSettings().getSerializer();
				
				return new DefaultPageStore(pageSerializer, dataStore, inmemoryCacheSize) {

					/*
					 * We override storePage method here to only persist (serialize and write to disk) pages 
					 * touched by non-ajax requests, and pages touched by ajax requests but possibly be 
					 * rendered to browser. Other pages are not necessarily to be persisted as they will not 
					 * trigger browser URL change and hence will not affect the browser back button support. 
					 * The only downside is that when back to a page, state modified via ajax requests will 
					 * get lost, but this is tolerable considering CPU/IO resource being saved for ajax 
					 * requests.
					 */
					@Override
					public void storePage(String sessionId, IManageablePage page) {
						RequestCycle requestCycle = RequestCycle.get();
						Request request = requestCycle.getRequest();
						if (request instanceof WebRequest) {
							WebRequest webRequest = (WebRequest)request;
							if (!webRequest.isAjax()) {
								super.storePage(sessionId, page);
							} else {
								if (requestCycle.getMetaData(PAGE_RENDERING_EXECUTED) ||
										requestCycle.getMetaData(PAGE_RENDERING_RESOLVED) || 
										requestCycle.getMetaData(PAGE_RENDERING_SCHEDULED)) {
									super.storePage(sessionId, page);
								}
							}
						} else {
							super.storePage(sessionId, page);
						}
					}
					
				};
			}

		});
		// getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	}

}
