package com.pmease.gitop.web.page.account.setting.teams;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.event.AjaxEvent;

public class ProjectAdded extends AjaxEvent {

	public ProjectAdded(AjaxRequestTarget target) {
		super(target);
	}

}
