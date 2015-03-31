package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class RepositoryPubliclyAccessibleChanged extends AjaxEvent {

	public RepositoryPubliclyAccessibleChanged(AjaxRequestTarget target) {
		super(target);
	}

}
