package io.onedev.server.web.util.paginghistory;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public interface AjaxPagingHistorySupport extends PagingHistorySupport {
    void onPageNavigated(AjaxRequestTarget target, int currentPage);

}
