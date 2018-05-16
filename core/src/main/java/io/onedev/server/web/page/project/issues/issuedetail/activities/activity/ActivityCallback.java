package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface ActivityCallback extends Serializable {
	
	void onDelete(AjaxRequestTarget target);
	
}
