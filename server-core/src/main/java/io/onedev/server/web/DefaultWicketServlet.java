package io.onedev.server.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import io.onedev.server.persistence.annotation.Sessional;

@Singleton
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
		((HttpServletResponse)res).setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
		super.service(req, res);
	}

}
