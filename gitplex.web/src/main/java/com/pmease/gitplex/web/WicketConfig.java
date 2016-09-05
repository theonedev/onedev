package com.pmease.gitplex.web;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.LastModifiedResourceVersion;

import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitplex.web.mapper.UrlMapper;
import com.pmease.gitplex.web.page.error.BaseErrorPage;
import com.pmease.gitplex.web.page.error.UnexpectedExceptionPage;
import com.pmease.gitplex.web.page.error.ExpectedExceptionPage;
import com.pmease.gitplex.web.page.home.DashboardPage;

@Singleton
public class WicketConfig extends AbstractWicketConfig {
	
	private final Set<ExpectedExceptionContribution> expectedExceptionContributions;
	
	@Inject
	public WicketConfig(Set<ExpectedExceptionContribution> expectedExceptionContributions) {
		this.expectedExceptionContributions = expectedExceptionContributions;
	}
	
	public static WicketConfig get() {
		return (WicketConfig) Application.get();
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return DashboardPage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
		return new WebSession(request);
	}

	@Override
	public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath) {
		return new ServletWebRequest(servletRequest, filterPath) {

			@Override
			public boolean shouldPreserveClientUrl() {
				if (RequestCycle.get().getActiveRequestHandler() instanceof RenderPageRequestHandler) {
					RenderPageRequestHandler requestHandler = 
							(RenderPageRequestHandler) RequestCycle.get().getActiveRequestHandler();
					
					/*
					 *  Add this to make sure that the page url does not change upon errors, so that 
					 *  user can know which page is actually causing the error. This behavior is common
					 *  for main stream applications.   
					 */
					if (requestHandler.getPage() instanceof BaseErrorPage) 
						return true;
				}
				return super.shouldPreserveClientUrl();
			}
			
		};
	}

	@Override
	protected void init() {
		super.init();

		getResourceSettings().setCachingStrategy(new FilenameWithVersionResourceCachingStrategy(new LastModifiedResourceVersion()));

		mount(new UrlMapper(this));
	}

	public Iterable<IRequestMapper> getRequestMappers() {
		return getRootRequestMapperAsCompound();
	}

	@Override
	protected Page mapExceptions(Exception e) {
		for (ExpectedExceptionContribution contribution: expectedExceptionContributions) {
			for (Class<? extends Exception> expectedExceptionClass: contribution.getExpectedExceptionClasses()) {
				Exception expectedException = ExceptionUtils.find(e, expectedExceptionClass);
				if (expectedException != null)
					return new ExpectedExceptionPage(expectedException);
			}
		}
		
		return new UnexpectedExceptionPage(e);
	}
	
}
