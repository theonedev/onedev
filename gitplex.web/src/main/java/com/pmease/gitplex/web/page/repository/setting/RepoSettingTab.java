package com.pmease.gitplex.web.page.repository.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class RepoSettingTab extends PageTab {

	public RepoSettingTab(String title, 
			Class<? extends RepoSettingPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
	}

	public RepoSettingTab(String title, 
			Class<? extends RepoSettingPage> mainPageClass, 
			Class<? extends RepoSettingPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
	}
	
	public RepoSettingTab(String title, 
			Class<? extends RepoSettingPage> mainPageClass, 
			Class<? extends RepoSettingPage> additionalPageClass1, 
			Class<? extends RepoSettingPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
	}
	
	public RepoSettingTab(String title,  
			Class<? extends RepoSettingPage> mainPageClass, 
			Class<? extends RepoSettingPage> additionalPageClass1, 
			Class<? extends RepoSettingPage> additionalPageClass2,
			Class<? extends RepoSettingPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}
	
	public RepoSettingTab(String title,  
			Class<? extends RepoSettingPage> mainPageClass, 
			Class<? extends RepoSettingPage> additionalPageClass1, 
			Class<? extends RepoSettingPage> additionalPageClass2,
			Class<? extends RepoSettingPage> additionalPageClass3,
			Class<? extends RepoSettingPage> additionalPageClass4) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
	}
	
	public RepoSettingTab(String title,  
			Class<? extends RepoSettingPage> mainPageClass, 
			Class<? extends RepoSettingPage> additionalPageClass1, 
			Class<? extends RepoSettingPage> additionalPageClass2,
			Class<? extends RepoSettingPage> additionalPageClass3,
			Class<? extends RepoSettingPage> additionalPageClass4, 
			Class<? extends RepoSettingPage> additionalPageClass5) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
	}
	
	@Override
	public Component render(String componentId) {
		return new RepoSettingTabLink(componentId, this);
	}

}
