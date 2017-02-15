package com.gitplex.server.web.page.depot.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.gitplex.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class DepotSettingTab extends PageTab {

	private final String iconClass;
	
	public DepotSettingTab(String title, String iconClass, 
			Class<? extends DepotSettingPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		this.iconClass = iconClass;
	}

	public DepotSettingTab(String title, String iconClass,
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		this.iconClass = iconClass;
	}
	
	public DepotSettingTab(String title, String iconClass, 
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconClass = iconClass;
	}
	
	public DepotSettingTab(String title, String iconClass,  
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2,
			Class<? extends DepotSettingPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		this.iconClass = iconClass;
	}
	
	public DepotSettingTab(String title, String iconClass,  
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2,
			Class<? extends DepotSettingPage> additionalPageClass3,
			Class<? extends DepotSettingPage> additionalPageClass4) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
		this.iconClass = iconClass;
	}
	
	public DepotSettingTab(String title, String iconClass,  
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2,
			Class<? extends DepotSettingPage> additionalPageClass3,
			Class<? extends DepotSettingPage> additionalPageClass4, 
			Class<? extends DepotSettingPage> additionalPageClass5) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
		this.iconClass = iconClass;
	}
	
	@Override
	public Component render(String componentId) {
		return new DepotSettingTabLink(componentId, this);
	}

	public String getIconClass() {
		return iconClass;
	}

}
