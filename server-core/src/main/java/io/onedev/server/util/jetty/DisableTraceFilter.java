package io.onedev.server.util.jetty;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DisableTraceFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (httpRequest.getMethod().equals("TRACE")) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}
	
}