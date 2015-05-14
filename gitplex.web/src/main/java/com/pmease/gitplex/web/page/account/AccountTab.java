package com.pmease.gitplex.web.page.account;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class AccountTab extends PageTab {

	public AccountTab(IModel<String> titleModel, Class<? extends AccountPage> mainPageClass) {
		super(titleModel, mainPageClass);
	}

	public AccountTab(IModel<String> titleModel, Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1) {
		super(titleModel, mainPageClass, additionalPageClass1);
	}
	
	public AccountTab(IModel<String> titleModel, 
			Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1, 
			Class<? extends AccountPage> additionalPageClass2) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2);
	}
	
	public AccountTab(IModel<String> titleModel, 
			Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1, 
			Class<? extends AccountPage> additionalPageClass2,
			Class<? extends AccountPage> additionalPageClass3) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}
	
	public AccountTab(IModel<String> titleModel, 
			Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1, 
			Class<? extends AccountPage> additionalPageClass2,
			Class<? extends AccountPage> additionalPageClass3,
			Class<? extends AccountPage> additionalPageClass4) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
	}
	
	public AccountTab(IModel<String> titleModel, 
			Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1, 
			Class<? extends AccountPage> additionalPageClass2,
			Class<? extends AccountPage> additionalPageClass3,
			Class<? extends AccountPage> additionalPageClass4, 
			Class<? extends AccountPage> additionalPageClass5) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
	}
	
	@Override
	public Component render(String componentId) {
		return new AccountTabLink(componentId, this);
	}

}
