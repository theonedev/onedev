package com.pmease.gitop.web.page.project.pullrequest.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface Deletable {
	void onDelete(AjaxRequestTarget target);
}
