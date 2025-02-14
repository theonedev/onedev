package io.onedev.server.web.util.paginghistory;

import java.io.Serializable;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public interface PagingHistorySupport extends Serializable {

	int getCurrentPage();

}
