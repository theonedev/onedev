package io.onedev.server.web.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface QuerySaveSupport extends Serializable {
	
	void onSaveQuery(AjaxRequestTarget target, @Nullable String query);
	
	boolean isSavedQueriesVisible();
	
}
