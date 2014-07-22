package com.pmease.gitplex.web.page.account.setting;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tab;

@SuppressWarnings("serial")
public class AccountSettingTab extends PageTab {

	public AccountSettingTab(IModel<String> titleModel, Class<? extends AccountSettingPage> mainPageClass) {
		super(titleModel, mainPageClass);
	}

	public AccountSettingTab(IModel<String> titleModel, Class<? extends AccountSettingPage> mainPageClass, 
			Class<? extends AccountSettingPage> additionalPageClass) {
		super(titleModel, mainPageClass, additionalPageClass);
	}

	@Override
	public void populate(ListItem<Tab> item, String componentId) {
		item.add(new AccountSettingTabLink(componentId, this));
	}

}
