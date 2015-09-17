package com.pmease.commons.wicket.behavior.markdown;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface ImageUrlProvider {
	
	void onInsert(AjaxRequestTarget target, String url);
	
	void onCancel(AjaxRequestTarget target);
	
}
