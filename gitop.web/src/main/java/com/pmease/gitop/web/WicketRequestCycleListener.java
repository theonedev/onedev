package com.pmease.gitop.web;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.error.AccessDeniedPage;
import com.pmease.gitop.web.page.error.PageNotFoundPage;
import com.pmease.gitop.web.shiro.LoginPage;

public class WicketRequestCycleListener extends AbstractRequestCycleListener {
	
	/**
     * Exception types we consider "recoverable", meaning we don't have to
     * log a detailed stack trace for these.
     */
    List<Class<? extends WicketRuntimeException>> RECOVERABLE_EXCEPTIONS = 
    		ImmutableList.of(
    				StalePageException.class,
    				PageExpiredException.class,
    				AuthorizationException.class);

    @Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
    	if (ex instanceof EntityNotFoundException) {
    		return createErrorPageHandler(cycle, PageNotFoundPage.class);
    	}
    	
    	if (ex instanceof AccessDeniedException) {
    		if (SecurityUtils.getSubject() != null) {
    			return createErrorPageHandler(cycle, AccessDeniedPage.class);
    		} else {
    			return createErrorPageHandler(cycle, LoginPage.class);
    		}
    	}
    	
    	// null means we want Wicket's default onException behavior to be used
    	return null;
    }
    
    private IRequestHandler createErrorPageHandler(RequestCycle cycle, Class<? extends Page> pageClass) {
    	RedirectPolicy redirectPolicy = RedirectPolicy.NEVER_REDIRECT;
    	if (cycle.getRequest() instanceof WebRequest &&
    			((WebRequest) cycle.getRequest()).isAjax()) {
    		redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;
    	}
    	
    	return new RenderPageRequestHandler(new PageProvider(pageClass), redirectPolicy);
    }
}
