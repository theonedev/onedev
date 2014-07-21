package com.pmease.gitplex.web.component.user;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

public class AvatarChanged extends AjaxEvent {

	public AvatarChanged(AjaxRequestTarget target) {
		super(target);
	}

}
