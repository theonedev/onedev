package io.onedev.server.jetty;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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