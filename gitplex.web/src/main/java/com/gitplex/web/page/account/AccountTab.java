package com.gitplex.web.page.account;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.gitplex.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class AccountTab extends PageTab {

	private final String iconClass;
	
	private final int count;
	
	public AccountTab(String title, String iconClass, int count, Class<? extends AccountPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconClass = iconClass;
		this.count = count;
	}

	public AccountTab(String title, String iconClass, int count, Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconClass = iconClass;
		this.count = count;
	}

	public AccountTab(String title, String iconClass, int count, Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1, 
			Class<? extends AccountPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public AccountTab(String title, String iconClass, int count, Class<? extends AccountPage> mainPageClass, 
			Class<? extends AccountPage> additionalPageClass1, 
			Class<? extends AccountPage> additionalPageClass2, 
			Class<? extends AccountPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconClass = iconClass;
		this.count = count;
	}
	
	@Override
	public Component render(String componentId) {
		return new AccountTabLink(componentId, this);
	}

	public String getIconClass() {
		return iconClass;
	}

	public int getCount() {
		return count;
	}

}
