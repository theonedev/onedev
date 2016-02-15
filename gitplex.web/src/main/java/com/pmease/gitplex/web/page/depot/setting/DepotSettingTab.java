package com.pmease.gitplex.web.page.depot.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class DepotSettingTab extends PageTab {

	public DepotSettingTab(String title, 
			Class<? extends DepotSettingPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
	}

	public DepotSettingTab(String title, 
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
	}
	
	public DepotSettingTab(String title, 
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
	}
	
	public DepotSettingTab(String title,  
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2,
			Class<? extends DepotSettingPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}
	
	public DepotSettingTab(String title,  
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2,
			Class<? extends DepotSettingPage> additionalPageClass3,
			Class<? extends DepotSettingPage> additionalPageClass4) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
	}
	
	public DepotSettingTab(String title,  
			Class<? extends DepotSettingPage> mainPageClass, 
			Class<? extends DepotSettingPage> additionalPageClass1, 
			Class<? extends DepotSettingPage> additionalPageClass2,
			Class<? extends DepotSettingPage> additionalPageClass3,
			Class<? extends DepotSettingPage> additionalPageClass4, 
			Class<? extends DepotSettingPage> additionalPageClass5) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
	}
	
	@Override
	public Component render(String componentId) {
		return new DepotSettingTabLink(componentId, this);
	}

}
