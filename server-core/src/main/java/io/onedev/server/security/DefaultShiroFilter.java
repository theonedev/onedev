package io.onedev.server.security;

import java.io.IOException;

import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.web.servlet.ShiroFilter;

import jakarta.inject.Singleton;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Singleton
public class DefaultShiroFilter extends ShiroFilter {

	@Override
	protected void executeChain(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// Shiro uses scoped values on Java 25+, while our authentication filters
		// replace the subject in ThreadContext. Restore that state after every request.
		var threadState = new SubjectThreadState(org.apache.shiro.SecurityUtils.getSubject());
		try {
			threadState.bind();
			super.executeChain(request, response, chain);
		} finally {
			threadState.restore();
		}
	}

}
