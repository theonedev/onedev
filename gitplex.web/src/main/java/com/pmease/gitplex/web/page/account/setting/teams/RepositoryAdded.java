package com.pmease.gitplex.web.page.account.setting.teams;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

public class RepositoryAdded extends AjaxEvent {

	public RepositoryAdded(AjaxRequestTarget target) {
		super(target);
	}

}
