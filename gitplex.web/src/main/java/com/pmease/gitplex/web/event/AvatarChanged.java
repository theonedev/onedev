package com.pmease.gitplex.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class AvatarChanged extends AjaxEvent {

	public AvatarChanged(AjaxRequestTarget target) {
		super(target);
	}

}
