package com.pmease.gitop.web.component.user;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class AvatarChanged extends AjaxEvent {

	public AvatarChanged(AjaxRequestTarget target) {
		super(target);
	}

}
