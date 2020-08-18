package io.onedev.server.web.page.admin.group;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class GroupTab extends PageTab {

	private final String iconHref;
	
	public GroupTab(String title, String iconHref, Class<? extends GroupPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconHref = iconHref;
	}

	public GroupTab(String title, String iconHref, Class<? extends GroupPage> mainPageClass, 
			Class<? extends GroupPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconHref = iconHref;
	}

	public GroupTab(String title, String iconHref, Class<? extends GroupPage> mainPageClass, 
			Class<? extends GroupPage> additionalPageClass1, 
			Class<? extends GroupPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconHref = iconHref;
	}
	
	public GroupTab(String title, String iconHref, Class<? extends GroupPage> mainPageClass, 
			Class<? extends GroupPage> additionalPageClass1, 
			Class<? extends GroupPage> additionalPageClass2, 
			Class<? extends GroupPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconHref = iconHref;
	}
	
	public String getIconHref() {
		return iconHref;
	}

	@Override
	public Component render(String componentId) {
		return new GroupTabHead(componentId, this);
	}

}
