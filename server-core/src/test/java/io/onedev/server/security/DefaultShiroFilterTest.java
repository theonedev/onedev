package io.onedev.server.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class DefaultShiroFilterTest {

	private final TestFilter filter = new TestFilter();

	private Map<Object, Object> originalResources;

	@BeforeEach
	public void clearThreadState() {
		originalResources = ThreadContext.getResources();
		ThreadContext.remove();
	}

	@AfterEach
	public void restoreThreadState() {
		ThreadContext.remove();
		ThreadContext.setResources(originalResources);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void tokenSubjectDoesNotLeakToNextRequest(boolean fail) throws Exception {
		var tokenSubject = filter.subject("user:1", true);
		FilterChain tokenRequest = (request, response) -> {
			assertFalse(SecurityUtils.getSubject().isAuthenticated());
			// Token authentication replaces the request subject in ThreadContext.
			ThreadContext.bind(tokenSubject);
			assertSame(tokenSubject, SecurityUtils.getSubject());
			if (fail)
				throw new IOException("Request failed");
		};
		if (fail) {
			var exception = assertThrows(IOException.class, () -> filter.request(tokenRequest));
			assertEquals("Request failed", exception.getMessage());
		} else {
			filter.request(tokenRequest);
		}
		assertNull(ThreadContext.getSubject());
		assertNull(ThreadContext.getSecurityManager());

		// Both requests execute synchronously on the same worker thread.
		filter.request((request, response) -> {
			assertFalse(SecurityUtils.getSubject().isAuthenticated());
			assertEquals(SecurityUtils.PRINCIPAL_ANONYMOUS, SecurityUtils.getSubject().getPrincipal());
		});
		assertTrue(ThreadContext.getResources().isEmpty());
	}

	@Test
	public void preservesAuthenticatedRequestSubject() throws Exception {
		filter.requestSubject = filter.subject("user:2", true);
		filter.request((request, response) -> {
			assertSame(filter.requestSubject, SecurityUtils.getSubject());
			assertTrue(SecurityUtils.getSubject().isAuthenticated());
		});
		assertTrue(ThreadContext.getResources().isEmpty());
	}

	@Test
	public void restoresEnclosingThreadState() throws Exception {
		var enclosingSubject = filter.subject("user:3", true);
		ThreadContext.bind(enclosingSubject);
		ThreadContext.bind(filter.getSecurityManager());
		ThreadContext.put("enclosing", "value");
		var enclosingResources = ThreadContext.getResources();

		filter.request((request, response) -> {
			assertSame(filter.requestSubject, SecurityUtils.getSubject());
			assertFalse(SecurityUtils.getSubject().isAuthenticated());
			ThreadContext.bind(filter.subject("user:1", true));
			ThreadContext.put("request", "value");
		});
		assertEquals(enclosingResources, ThreadContext.getResources());
	}

	private static class TestFilter extends DefaultShiroFilter {

		private WebSubject requestSubject;

		private TestFilter() {
			setSecurityManager(new DefaultWebSecurityManager());
			requestSubject = subject(SecurityUtils.PRINCIPAL_ANONYMOUS, false);
		}

		private WebSubject subject(String principal, boolean authenticated) {
			return new WebDelegatingSubject(SecurityUtils.asPrincipals(principal), authenticated,
					null, null, false, null, null, getSecurityManager());
		}

		private void request(FilterChain chain) throws ServletException, IOException {
			// Keep Shiro's real doFilterInternal/SubjectCallable request lifecycle.
			doFilterInternal(null, null, chain);
		}

		@Override
		protected ServletRequest prepareServletRequest(ServletRequest request, ServletResponse response,
				FilterChain chain) {
			return request;
		}

		@Override
		protected ServletResponse prepareServletResponse(ServletRequest request, ServletResponse response,
				FilterChain chain) {
			return response;
		}

		@Override
		protected WebSubject createSubject(ServletRequest request, ServletResponse response) {
			return requestSubject;
		}

		@Override
		protected void updateSessionLastAccessTime(ServletRequest request, ServletResponse response) {
		}
	}
}
