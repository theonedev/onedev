package io.onedev.server.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class AdministrationTab extends PageTab {

	private final String iconClass;
	
	public AdministrationTab(String title, String iconClass, Class<? extends AdministrationPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconClass = iconClass;
	}

	public AdministrationTab(String title, String iconClass, Class<? extends AdministrationPage> mainPageClass, 
			Class<? extends AdministrationPage> additionalPageClass) {
		super(Model.of(title), mainPageClass, additionalPageClass);
		
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
