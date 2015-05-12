package com.pmease.gitplex.web.page.layout;

import java.util.List;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;

@SuppressWarnings("serial")
public abstract class SidebarPage extends MaintabPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Tabbable("sideTabs", newSideTabs()));
	}

	protected abstract List<PageTab> newSideTabs();
}
