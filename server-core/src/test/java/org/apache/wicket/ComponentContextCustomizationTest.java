package org.apache.wicket;

import static org.junit.Assert.*;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.onedev.server.util.ComponentHierarchical;
import io.onedev.server.util.HierarchicalContext;

public class ComponentContextCustomizationTest {

	private WicketTester tester;
	private HierarchicalContext outer;

	@Before
	public void setUp() {
		tester = new WicketTester();
		outer = new HierarchicalContext(new ComponentHierarchical(new Label("outer")));
		HierarchicalContext.push(outer);
	}

	@After
	public void tearDown() {
		try {
			assertSame("Callback must restore the enclosing context", outer, HierarchicalContext.get());
		} finally {
			HierarchicalContext.pop();
			tester.destroy();
		}
	}

	@Test
	public void initializationResolvesTheCurrentComponentAndItsParents() {
		var page = new WebPage() {
			private static final long serialVersionUID = 1L;
		};
		var initialized = new boolean[1];
		var child = new Label("child") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				assertSame(this, HierarchicalContext.get().findData(Label.class));
				assertSame(page, HierarchicalContext.get().findData(WebPage.class));
				initialized[0] = true;
			}
		};
		page.add(child);
		page.internalInitialize();
		assertTrue(initialized[0]);
	}

	@Test
	public void listenerFailureStillRestoresTheEnclosingContext() {
		var failure = new WicketRuntimeException("Listener failed");
		var link = new Link<Void>("link") {
			@Override
			public void onClick() {
				assertSame(this, HierarchicalContext.get().findData(Link.class));
				throw failure;
			}
		};
		new WebPage() {
			private static final long serialVersionUID = 1L;
		}.add(link);
		assertSame(failure, assertThrows(WicketRuntimeException.class,
				() -> ILinkListener.INTERFACE.invoke(link)));
	}

	@Test
	public void behaviorAuthorizationRunsInTheOwningComponentContext() {
		var owner = new Label("owner");
		var behavior = new Behavior() {
			@Override
			public boolean canCallListenerInterface(Component component, java.lang.reflect.Method method) {
				assertSame(owner, HierarchicalContext.get().findData(Label.class));
				return false;
			}
		};
		owner.add(behavior);
		assertThrows(org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException.class,
				() -> ILinkListener.INTERFACE.invoke(owner, behavior));
	}
}
