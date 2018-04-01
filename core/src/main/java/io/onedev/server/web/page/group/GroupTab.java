package io.onedev.server.web.page.group;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class GroupTab extends PageTab {

	private final String iconClass;
	
	public GroupTab(String title, String iconClass, Class<? extends GroupPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconClass = iconClass;
	}

	public GroupTab(String title, String iconClass, Class<? extends GroupPage> mainPageClass, 
			Class<? extends GroupPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconClass = iconClass;
	}

	public GroupTab(String title, String iconClass, Class<? extends GroupPage> mainPageClass, 
			Class<? extends GroupPage> additionalPageClass1, 
			Class<? extends GroupPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconClass = iconClass;
	}
	
	public GroupTab(String title, String iconClass, Class<? extends GroupPage> mainPageClass, 
			Class<? extends GroupPage> additionalPageClass1, 
			Class<? extends GroupPage> additionalPageClass2, 
			Class<? extends GroupPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconClass = iconClass;
	}
	
	public String getIconClass() {
		return iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new GroupTabLink(componentId, this);
	}

}
