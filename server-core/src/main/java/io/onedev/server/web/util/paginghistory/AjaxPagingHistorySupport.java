package io.onedev.server.web.util.paginghistory;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface AjaxPagingHistorySupport extends PagingHistorySupport {
    void onPageNavigated(AjaxRequestTarget target, int currentPage);

}
