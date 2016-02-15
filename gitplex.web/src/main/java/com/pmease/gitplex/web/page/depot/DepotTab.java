package com.pmease.gitplex.web.page.depot;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class DepotTab extends PageTab {

	private final String iconClass;

	public DepotTab(IModel<String> titleModel, String iconClass, Class<? extends DepotPage> mainPageClass) {
		super(titleModel, mainPageClass);
		this.iconClass = iconClass;
	}

	public DepotTab(IModel<String> titleModel, String iconClass, Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1) {
		super(titleModel, mainPageClass, additionalPageClass1);
		this.iconClass = iconClass;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconClass = iconClass;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2,
			Class<? extends DepotPage> additionalPageClass3) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		this.iconClass = iconClass;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2,
			Class<? extends DepotPage> additionalPageClass3,
			Class<? extends DepotPage> additionalPageClass4) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
		this.iconClass = iconClass;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2,
			Class<? extends DepotPage> additionalPageClass3,
			Class<? extends DepotPage> additionalPageClass4, 
			Class<? extends DepotPage> additionalPageClass5) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
		this.iconClass = iconClass;
	}
	
	public String getIconClass() {
		return iconClass;
	}
	
	@Override
	public Component render(String componentId) {
		return new DepotTabLink(componentId, this);
	}

}
