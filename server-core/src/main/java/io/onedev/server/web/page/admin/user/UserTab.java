package io.onedev.server.web.page.admin.user;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class UserTab extends PageTab {

	private final String iconHref;
	
	public UserTab(String title, @Nullable String iconHref, Class<? extends UserPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconHref = iconHref;
	}

	public UserTab(String title, @Nullable String iconHref, Class<? extends UserPage> mainPageClass, 
			Class<? extends UserPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconHref = iconHref;
	}

	public UserTab(String title, @Nullable String iconHref, Class<? extends UserPage> mainPageClass, 
			Class<? extends UserPage> additionalPageClass1, 
			Class<? extends UserPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconHref = iconHref;
	}
	
	public UserTab(String title, @Nullable String iconHref, Class<? extends UserPage> mainPageClass, 
			Class<? extends UserPage> additionalPageClass1, 
			Class<? extends UserPage> additionalPageClass2, 
			Class<? extends UserPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconHref = iconHref;
	}
	
	public String getIconHref() {
		return iconHref;
	}

	@Override
	public Component render(String componentId) {
		return new UserTabHead(componentId, this);
	}

}
