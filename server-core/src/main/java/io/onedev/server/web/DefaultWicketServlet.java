package io.onedev.server.web;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import io.onedev.server.persistence.SessionService;

@Singleton
public class DefaultWicketServlet extends WicketServlet {

	private static final long serialVersionUID = 1L;
	
	private final SessionService sessionService;
	
	private final WicketFilter wicketFilter;
	
	@Override
	public String getServletName() {
		return getClass().getSimpleName();
	}

	@Inject
	public DefaultWicketServlet(SessionService sessionService, WicketFilter wicketFilter) {
		this.sessionService = sessionService;
		this.wicketFilter = wicketFilter;
	}
	
	@Override
	protected WicketFilter newWicketFilter() {
		return wicketFilter;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		sessionService.run(() -> {
			var httpRes = (HttpServletResponse) res;
			httpRes.setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
			// Disable cloudflare suggested prefetch to fix OD-2120
			httpRes.setHeader("Speculation-Rules", "\"/prefetch.json\"");
			try {
				DefaultWicketServlet.super.service(req, res);
			} catch (ServletException | IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
