package com.gitplex.server.web.page.project.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.gitplex.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class ProjectSettingTab extends PageTab {

	private final String iconClass;
	
	public ProjectSettingTab(String title, String iconClass, 
			Class<? extends ProjectSettingPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		this.iconClass = iconClass;
	}

	public ProjectSettingTab(String title, String iconClass,
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		this.iconClass = iconClass;
	}
	
	public ProjectSettingTab(String title, String iconClass, 
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconClass = iconClass;
	}
	
	public ProjectSettingTab(String title, String iconClass,  
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2,
			Class<? extends ProjectSettingPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		this.iconClass = iconClass;
	}
	
	public ProjectSettingTab(String title, String iconClass,  
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2,
			Class<? extends ProjectSettingPage> additionalPageClass3,
			Class<? extends ProjectSettingPage> additionalPageClass4) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
		this.iconClass = iconClass;
	}
	
	public ProjectSettingTab(String title, String iconClass,  
			Class<? extends ProjectSettingPage> mainPageClass, 
			Class<? extends ProjectSettingPage> additionalPageClass1, 
			Class<? extends ProjectSettingPage> additionalPageClass2,
			Class<? extends ProjectSettingPage> additionalPageClass3,
			Class<? extends ProjectSettingPage> additionalPageClass4, 
			Class<? extends ProjectSettingPage> additionalPageClass5) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
		this.iconClass = iconClass;
	}
	
	@Override
	public Component render(String componentId) {
		return new ProjectSettingTabLink(componentId, this);
	}

	public String getIconClass() {
		return iconClass;
	}

}
