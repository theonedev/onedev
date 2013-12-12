package com.pmease.gitop.web.common.wicket.component.datagrid.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class PageChanged extends AjaxEvent {

	private final int page;

	public PageChanged(AjaxRequestTarget target, int page) {
		super(target);
		this.page = page;
	}

	public int getPage() {
		return page;
	}
}
