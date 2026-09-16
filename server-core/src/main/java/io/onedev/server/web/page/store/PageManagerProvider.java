package io.onedev.server.web.page.store;

import org.apache.wicket.Application;
import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.pageStore.IPageStore;

public class PageManagerProvider extends DefaultPageManagerProvider {

    public PageManagerProvider(Application application) {
        super(application);
    }

    @Override
    protected IPageStore newCachingStore(IPageStore store) {
        // Do not retain live pages in replicated HTTP sessions.
        return store;
    }

    @Override
    protected IPageStore newAsynchronousStore(IPageStore store) {
        // Write pages synchronously to disk without retaining serialized pages in memory.
        return store;
    }
}
