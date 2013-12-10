package com.pmease.gitop.web.page.project;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class ProjectPubliclyAccessibleChanged extends AjaxEvent {

	public ProjectPubliclyAccessibleChanged(AjaxRequestTarget target) {
		super(target);
	}

}
