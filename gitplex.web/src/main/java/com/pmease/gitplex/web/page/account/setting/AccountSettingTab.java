package com.pmease.gitplex.web.page.account.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class AccountSettingTab extends PageTab {

	private final String iconClass;
	
	public AccountSettingTab(String title, String iconClass, Class<? extends AccountSettingPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconClass = iconClass;
	}

	public AccountSettingTab(String title, String iconClass, Class<? extends AccountSettingPage> mainPageClass, 
			Class<? extends AccountSettingPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconClass = iconClass;
	}

	public AccountSettingTab(String title, String iconClass, Class<? extends AccountSettingPage> mainPageClass, 
			Class<? extends AccountSettingPage> additionalPageClass1, 
			Class<? extends AccountSettingPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconClass = iconClass;
	}
	
	public AccountSettingTab(String title, String iconClass, Class<? extends AccountSettingPage> mainPageClass, 
			Class<? extends AccountSettingPage> additionalPageClass1, 
			Class<? extends AccountSettingPage> additionalPageClass2, 
			Class<? extends AccountSettingPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconClass = iconClass;
	}
	
	@Override
	public Component render(String componentId) {
		return new AccountSettingTabLink(componentId, this);
	}

	public String getIconClass() {
		return iconClass;
	}

}
