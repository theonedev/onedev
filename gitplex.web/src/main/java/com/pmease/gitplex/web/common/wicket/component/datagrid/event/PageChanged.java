package com.pmease.gitplex.web.common.wicket.component.datagrid.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

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
