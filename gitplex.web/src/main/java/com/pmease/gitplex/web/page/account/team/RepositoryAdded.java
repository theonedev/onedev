package com.pmease.gitplex.web.page.account.team;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class RepositoryAdded extends AjaxEvent {

	public RepositoryAdded(AjaxRequestTarget target) {
		super(target);
	}

}
