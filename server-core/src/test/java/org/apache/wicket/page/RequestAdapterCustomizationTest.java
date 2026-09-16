package org.apache.wicket.page;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.RequestPageStore;
import org.junit.jupiter.api.Test;

public class RequestAdapterCustomizationTest {
    @Test
    public void committedResponseDoesNotRetrySessionCreationDuringDetach() {
        var context = newContext();
        var delegate = mock(IPageStore.class);
        var store = new RequestPageStore(delegate);
        var page = mock(IManageablePage.class);
        store.addPage(context, page);
        when(context.getSessionId(true)).thenThrow(new IllegalStateException("Response is committed"));

        store.end(context);
        store.detach(context);

        verify(delegate, never()).addPage(any(), any());
        verify(delegate).detach(context);
        verify(context).getSessionId(true);
    }

    @Test
    public void unexpectedSessionAndStorageFailuresStillPropagate() {
        for (String message : new String[] { "Session store unavailable", null }) {
            var context = newContext();
            var delegate = mock(IPageStore.class);
            var store = new RequestPageStore(delegate);
            var page = mock(IManageablePage.class);
            store.addPage(context, page);
            var failure = new IllegalStateException(message);
            when(context.getSessionId(true)).thenThrow(failure);
            assertSame(failure, assertThrows(IllegalStateException.class, () -> store.end(context)));
            doThrow(failure).when(delegate).addPage(context, page);
            assertSame(failure, assertThrows(IllegalStateException.class, () -> store.detach(context)));
        }
    }

    @Test
    public void committedResponseDuringStorageStillDetachesDelegate() {
        var context = newContext();
        var delegate = mock(IPageStore.class);
        var store = new RequestPageStore(delegate);
        var page = mock(IManageablePage.class);
        store.addPage(context, page);
        doThrow(new IllegalStateException("Response is committed")).when(delegate).addPage(context, page);

        store.detach(context);
        store.detach(context);

        verify(delegate).addPage(context, page);
        verify(delegate, times(2)).detach(context);
    }

    private IPageContext newContext() {
        var data = new HashMap<Object, Object>();
        var context = mock(IPageContext.class);
        when(context.getRequestData(any(), any())).thenAnswer(invocation ->
                data.computeIfAbsent(invocation.getArgument(0), key ->
                        ((Supplier<?>) invocation.getArgument(1)).get()));
        return context;
    }

    @Test
    public void commitDoesNotInspectPartiallyInitializedPagesDuringLoginRedirect() {
        var context = newContext();
        var delegate = mock(IPageStore.class);
        var store = new RequestPageStore(delegate);
        var page = mock(IManageablePage.class);
        when(page.getPageId()).thenReturn(1);
        when(page.isPageStateless()).thenThrow(new IllegalStateException("Uninitialized enclosure"));
        store.addPage(context, page);
        store.end(context);
        store.detach(context);

        verify(context).getSessionId(true);
        verify(delegate).addPage(context, page);
        verify(page, never()).isPageStateless();
    }
}
