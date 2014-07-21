package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

public class RepositoryPubliclyAccessibleChanged extends AjaxEvent {

	public RepositoryPubliclyAccessibleChanged(AjaxRequestTarget target) {
		super(target);
	}

}
