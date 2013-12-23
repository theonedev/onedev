package com.pmease.gitop.web.page.account.setting.api;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.wicket.component.tab.AbstractPageTab;

public class AccountSettingTab extends AbstractPageTab {
	private static final long serialVersionUID = 1L;

	public AccountSettingTab(IModel<String> title,
			Class<? extends Page> pageClass) {
		super(title, pageClass);
	}

	public AccountSettingTab(IModel<String> title, final Class<? extends Page>[] pageClasses) {
		super(title, pageClasses);
	}
	
	@Override
	public final String getGroupName() {
		throw new UnsupportedOperationException();
	}

}
