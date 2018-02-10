package com.turbodev.server.web.page.user;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.turbodev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class UserTab extends PageTab {

	private final String iconClass;
	
	public UserTab(String title, String iconClass, Class<? extends UserPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconClass = iconClass;
	}

	public UserTab(String title, String iconClass, Class<? extends UserPage> mainPageClass, 
			Class<? extends UserPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconClass = iconClass;
	}

	public UserTab(String title, String iconClass, Class<? extends UserPage> mainPageClass, 
			Class<? extends UserPage> additionalPageClass1, 
			Class<? extends UserPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconClass = iconClass;
	}
	
	public UserTab(String title, String iconClass, Class<? extends UserPage> mainPageClass, 
			Class<? extends UserPage> additionalPageClass1, 
			Class<? extends UserPage> additionalPageClass2, 
			Class<? extends UserPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconClass = iconClass;
	}
	
	public String getIconClass() {
		return iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new UserTabLink(componentId, this);
	}

}
