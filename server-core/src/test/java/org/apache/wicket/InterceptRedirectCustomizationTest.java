package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.util.List;

import io.onedev.server.model.Chat;
import io.onedev.server.web.WebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.info.ComponentInfo;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.apache.wicket.session.HttpSessionStore;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InterceptRedirectCustomizationTest {

	private WicketTester tester;

	@BeforeEach
	public void setUp() {
		tester = new WicketTester(new MockApplication() {
			@Override
			protected void init() {
				super.init();
				// Exercise servlet session invalidation instead of MockSessionStore's
				// unbound callback, which makes WicketTester replace the current session.
				setSessionStoreProvider(HttpSessionStore::new);
			}

			@Override
			public Session newSession(Request request, Response response) {
				return new WebSession(request);
			}
		});
	}

	@AfterEach
	public void tearDown() {
		tester.destroy();
	}

	@Test
	public void expiredBehaviorCallbackReturnsToPageWithoutReplayingCallback() {
		// Generate the metadata using this Wicket version's URL encoding.
		var callback = new PageComponentInfo(new PageInfo(12), new ComponentInfo(3, "form:save", 0));
		var original = Url.parse("http://backend:6610/projects?" + callback + "&tab=files&tab=history");
		var destination = continueAfterIntercept(original);
		assertNull(destination.getQueryParameter(callback.toString()));
		assertEquals(2, destination.getQueryParameters().size());
		assertEquals("files", destination.getQueryParameters().get(0).getValue());
		assertEquals("history", destination.getQueryParameters().get(1).getValue());
	}

	@Test
	public void pageVersionAndOrdinaryParametersSurviveInterception() {
		var original = Url.parse("http://backend:6610/projects?12&tab=files&12-3.0-form-save=ordinary");
		var destination = continueAfterIntercept(original);
		assertNotNull(destination.getQueryParameter("12"));
		assertEquals("ordinary", destination.getQueryParameter("12-3.0-form-save").getValue());
		assertEquals(3, destination.getQueryParameters().size());
	}

	@Test
	public void loginReplacementPreservesOnlyTheInterceptedRequest() {
		tester.getRequest().getPostParameters().addParameterValue("selection", "first");
		tester.getRequest().getPostParameters().addParameterValue("selection", "second");
		intercept(Url.parse("http://backend:6610/projects?tab=files&tab=history"));
		var originalUrl = new Url(RestartResponseAtInterceptPageException.getOriginalUrl());
		var originalPostParameters = RestartResponseAtInterceptPageException.getOriginalPostParameters();
		assertEquals(List.of("first", "second"), originalPostParameters.get("selection").stream()
				.map(Object::toString).toList());

		var session = (WebSession) tester.getSession();
		var unrelatedKey = new MetaDataKey<String>() {};
		session.setMetaData(unrelatedKey, "discard this metadata");
		tester.getRequest().getSession().setAttribute("privateAttribute", "discard this attribute");
		session.setChatInput("discard this draft");
		session.setActiveChatId(1L);
		session.getAnonymousChats().put(1L, new Chat());
		session.setZoneId(ZoneId.of("Asia/Shanghai"));
		session.setSsoLogoutUrl("https://example.com/logout");
		session.setRedirectUrlAfterDelete(WebPage.class, "/previous-page");

		RestartResponseAtInterceptPageException.replaceSessionPreservingOriginalDestination();

		assertSame(session, Session.get());
		assertEquals(originalUrl, RestartResponseAtInterceptPageException.getOriginalUrl());
		assertEquals(originalPostParameters, RestartResponseAtInterceptPageException.getOriginalPostParameters());
		assertNull(session.getMetaData(unrelatedKey));
		assertNull(tester.getRequest().getSession().getAttribute("privateAttribute"));
		assertNull(session.getChatInput());
		assertNull(session.getActiveChatId());
		assertTrue(session.getAnonymousChats().isEmpty());
		assertNull(session.getZoneId());
		assertNull(session.getSsoLogoutUrl());
		assertNull(session.getRedirectUrlAfterDelete(WebPage.class));
	}

	@Test
	public void directLoginDoesNotInventAnInterceptedDestination() {
		var session = (WebSession) tester.getSession();
		session.bind();
		session.setChatInput("discard this draft");
		RestartResponseAtInterceptPageException.replaceSessionPreservingOriginalDestination();
		assertNull(RestartResponseAtInterceptPageException.getOriginalUrl());
		assertNull(RestartResponseAtInterceptPageException.getOriginalPostParameters());
		assertNull(session.getChatInput());
	}

	@Test
	public void ordinarySessionReplacementStillClearsTheInterceptedDestination() {
		intercept(Url.parse("projects?tab=files"));
		tester.getSession().replaceSession();
		assertNull(RestartResponseAtInterceptPageException.getOriginalUrl());
		assertNull(RestartResponseAtInterceptPageException.getOriginalPostParameters());
	}

	private Url continueAfterIntercept(Url original) {
		intercept(original);
		var redirect = assertThrows(NonResettingRestartException.class,
				RestartResponseAtInterceptPageException::continueToOriginalDestination);
		return Url.parse(((RedirectRequestHandler) redirect.getReplacementRequestHandler()).getRedirectUrl());
	}

	private void intercept(Url original) {
		var cycle = tester.getRequestCycle();
		var request = spy(cycle.getRequest().cloneWithUrl(original));
		doReturn(original).when(request).getOriginalUrl();
		cycle.setRequest(request);
		new RestartResponseAtInterceptPageException(WebPage.class);
		var saved = RestartResponseAtInterceptPageException.getOriginalUrl();
		assertNull(saved.getHost());
		assertNull(saved.getPort());
		assertNull(saved.getProtocol());
	}
}
