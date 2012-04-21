package com.pmease.commons.wicket;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class WebServlet extends WicketServlet {

	private final WicketFilter wicketFilter;
	
	@Inject
	public WebServlet(WicketFilter wicketFilter) {
		this.wicketFilter = wicketFilter;
	}
	
	@Override
	public String getServletName() {
		return "wicket";
	}

	@Override
	protected WicketFilter newWicketFilter() {
		return wicketFilter;
	}

}
