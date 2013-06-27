package com.pmease.commons.web;

import javax.inject.Inject;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

public class DefaultWicketServlet extends WicketServlet {

	private static final long serialVersionUID = 1L;
	
	private final WicketFilter wicketFilter;

	@Override
	public String getServletName() {
		return getClass().getSimpleName();
	}

	@Inject
	public DefaultWicketServlet(WicketFilter wicketFilter) {
		this.wicketFilter = wicketFilter;
	}
	
	@Override
	protected WicketFilter newWicketFilter() {
		return wicketFilter;
	}

}
