package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.event.AjaxEvent;

public class AvatarChanged extends AjaxEvent {

	public AvatarChanged(AjaxRequestTarget target) {
		super(target);
	}

}
