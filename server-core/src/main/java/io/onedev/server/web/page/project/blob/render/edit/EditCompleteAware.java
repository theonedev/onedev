package io.onedev.server.web.page.project.blob.render.edit;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface EditCompleteAware {
	
	boolean onEditComplete(AjaxRequestTarget target);
	
	void onEditCancel(AjaxRequestTarget target);
	
}
