package com.pmease.gitop.web.page.project;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class RepositoryPubliclyAccessibleChanged extends AjaxEvent {

	public RepositoryPubliclyAccessibleChanged(AjaxRequestTarget target) {
		super(target);
	}

}
