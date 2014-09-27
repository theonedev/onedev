package com.pmease.gitplex.web.page.account;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class AccountTab extends PageTab {

	private final String iconClass;
	
	public AccountTab(IModel<String> titleModel, String iconClass, Class<? extends AccountPage> mainPageClass) {
		super(titleModel, mainPageClass);
		
		this.iconClass = iconClass;
	}

	public AccountTab(IModel<String> titleModel, String iconClass, Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass) {
		super(titleModel, mainPageClass, additionalPageClass);
		
		this.iconClass = iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new AccountTabLink(componentId, this);
	}

	public String getIconClass() {
		return iconClass;
	}

}
