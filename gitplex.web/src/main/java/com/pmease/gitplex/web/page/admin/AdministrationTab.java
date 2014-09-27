package com.pmease.gitplex.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class AdministrationTab extends PageTab {

	private final String iconClass;
	
	public AdministrationTab(IModel<String> titleModel, String iconClass, Class<? extends AdministrationPage> mainPageClass) {
		super(titleModel, mainPageClass);
		
		this.iconClass = iconClass;
	}

	public AdministrationTab(IModel<String> titleModel, String iconClass, Class<? extends AdministrationPage> mainPageClass, 
			Class<? extends AdministrationPage> additionalPageClass) {
		super(titleModel, mainPageClass, additionalPageClass);
		
		this.iconClass = iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new AdministrationTabLink(componentId, this);
	}

	public String getIconClass() {
		return iconClass;
	}

}
