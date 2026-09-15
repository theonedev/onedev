package org.apache.wicket.page;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RequestAdapterCustomizationTest {

	@Test
	public void commitDoesNotInspectPartiallyInitializedPagesDuringLoginRedirect() {
		var stored = new ArrayList<IManageablePage>();
		var adapter = new RequestAdapter(mock(IPageManagerContext.class)) {
			@Override
			protected IManageablePage getPage(int id) {
				return null;
			}

			@Override
			protected void storeTouchedPages(List<IManageablePage> pages) {
				stored.addAll(pages);
			}

			@Override
			protected void newSessionCreated() {
			}
		};
		var page = mock(IManageablePage.class);
		when(page.getPageId()).thenReturn(1);
		when(page.isPageStateless()).thenThrow(new IllegalStateException("Uninitialized enclosure"));
		adapter.touch(page);
		adapter.commitRequest();

		assertEquals(List.of(page), stored);
		verify(page, never()).isPageStateless();
	}
}
