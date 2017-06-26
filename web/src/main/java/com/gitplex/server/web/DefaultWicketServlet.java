package com.gitplex.server.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import com.gitplex.server.persistence.annotation.Sessional;

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

	@Sessional
	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		super.service(req, res);
	}

}
