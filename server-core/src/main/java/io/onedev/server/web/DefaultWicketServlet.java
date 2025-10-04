package io.onedev.server.web;

import io.onedev.server.persistence.SessionService;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
			httpRes.setHeader("Speculation-Rules", "/prefetch.json");
			try {
				super.service(req, res);
			} catch (ServletException | IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
