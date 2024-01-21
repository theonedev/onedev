package io.onedev.server.security;

import io.onedev.server.exception.ExceptionUtils;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public abstract class ExceptionHandleFilter extends PathMatchingFilter {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandleFilter.class);
	
	@Override
	protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) 
			throws ServletException, IOException {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
		if (existing != null && !httpResponse.isCommitted()) {
			ExceptionUtils.handle(httpResponse, existing);
			if (httpResponse.getStatus() >= SC_INTERNAL_SERVER_ERROR) 
				logger.error("Error processing servlet request", existing);
			existing = null;
		}
		
		super.cleanup(request, response, existing);
	}

}
