package com.pmease.gitplex.web.component.user;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class AvatarChanged extends AjaxEvent {

	public AvatarChanged(AjaxRequestTarget target) {
		super(target);
	}

}
