package io.onedev.server.web.util.paginghistory;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public interface ParamPagingHistorySupport extends PagingHistorySupport {
    PageParameters newPageParameters(int currentPage);

}
