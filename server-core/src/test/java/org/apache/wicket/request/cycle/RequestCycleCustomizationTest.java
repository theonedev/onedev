package org.apache.wicket.request.cycle;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.junit.Test;

public class RequestCycleCustomizationTest {

	@Test
	public void pageUrlsAreReusedOnlyWithinTheSameRequestAndParameters() {
		var cycle = new CountingRequestCycle();
		var first = cycle.urlFor(WebPage.class, new PageParameters().add("page", 1));
		assertEquals(first, cycle.urlFor(WebPage.class, new PageParameters().add("page", 1)));
		assertEquals(1, cycle.mappings);
		assertNotEquals(first, cycle.urlFor(WebPage.class, new PageParameters().add("page", 2)));
		assertEquals(2, cycle.mappings);

		var nextRequest = new CountingRequestCycle();
		nextRequest.urlFor(WebPage.class, new PageParameters().add("page", 1));
		assertEquals(1, nextRequest.mappings);
	}

	@Test
	public void resourceUrlsAreReusedWithoutAThreadBoundSession() {
		var cycle = new CountingRequestCycle();
		var reference = new PackageResourceReference(getClass(), "example.js");
		var first = cycle.urlFor(reference, null);
		assertEquals(first, cycle.urlFor(reference, null));
		assertEquals(1, cycle.mappings);
		assertNotEquals(first, cycle.urlFor(reference, new PageParameters().add("version", 2)));
		assertNotEquals(first, cycle.urlFor(new PackageResourceReference(getClass(), "other.js"), null));
		assertEquals(3, cycle.mappings);
	}

	private static class CountingRequestCycle extends RequestCycle {

		private int mappings;

		private CountingRequestCycle() {
			super(new RequestCycleContext(mock(Request.class), mock(Response.class),
					mock(IRequestMapper.class), mock(IExceptionMapper.class)));
		}

		@Override
		public CharSequence urlFor(IRequestHandler handler) {
			return "url-" + ++mappings;
		}
	}
}
