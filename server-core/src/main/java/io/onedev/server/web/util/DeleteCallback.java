package io.onedev.server.web.util;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface DeleteCallback extends Serializable {
	
	void onDelete(AjaxRequestTarget target);
	
}
