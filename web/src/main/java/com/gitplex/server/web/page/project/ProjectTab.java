package com.gitplex.server.web.page.project;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class ProjectTab extends PageTab {

	private final String iconClass;
	
	private final int count;

	public ProjectTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends ProjectPage> mainPageClass) {
		super(titleModel, mainPageClass);
		this.iconClass = iconClass;
		this.count = count;
	}

	public ProjectTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1) {
		super(titleModel, mainPageClass, additionalPageClass1);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2,
			Class<? extends ProjectPage> additionalPageClass3) {
		super(titleModel, mainPageClass, additionalPageClass1, 
				additionalPageClass2, additionalPageClass3);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconClass, int count,
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2,
			Class<? extends ProjectPage> additionalPageClass3,
			Class<? extends ProjectPage> additionalPageClass4) {
		super(titleModel, mainPageClass, additionalPageClass1, 
				additionalPageClass2, additionalPageClass3, additionalPageClass4);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconClass, int count,
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2,
			Class<? extends ProjectPage> additionalPageClass3,
			Class<? extends ProjectPage> additionalPageClass4, 
			Class<? extends ProjectPage> additionalPageClass5) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public String getIconClass() {
		return iconClass;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	@Override
	public Component render(String componentId) {
		return new ProjectTabLink(componentId, this);
	}

}
