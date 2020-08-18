package io.onedev.server.web.page.project.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class ProjectSettingTab extends PageTab {

	private final String iconHref;
	
	public ProjectSettingTab(String title, String iconHref, 
			Class<? extends ProjectSettingPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		this.iconHref = iconHref;
	}

	public ProjectSettingTab(String title, String iconHref,
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		this.iconHref = iconHref;
	}
	
	public ProjectSettingTab(String title, String iconHref, 
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconHref = iconHref;
	}
	
	public ProjectSettingTab(String title, String iconHref,  
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2,
			Class<? extends ProjectSettingPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		this.iconHref = iconHref;
	}
	
	public ProjectSettingTab(String title, String iconHref,  
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2,
			Class<? extends ProjectSettingPage> additionalPageClass3,
			Class<? extends ProjectSettingPage> additionalPageClass4) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
		this.iconHref = iconHref;
	}
	
	public ProjectSettingTab(String title, String iconHref,  
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2,
			Class<? extends ProjectSettingPage> additionalPageClass3,
			Class<? extends ProjectSettingPage> additionalPageClass4, 
			Class<? extends ProjectSettingPage> additionalPageClass5) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
		this.iconHref = iconHref;
	}
	
	@Override
	public Component render(String componentId) {
		return new ProjectSettingTabHead(componentId, this);
	}

	public String getIconHref() {
		return iconHref;
	}

}
