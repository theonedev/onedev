package com.gitplex.server.web.page.depot;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class DepotTab extends PageTab {

	private final String iconClass;
	
	private final int count;

	public DepotTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends DepotPage> mainPageClass) {
		super(titleModel, mainPageClass);
		this.iconClass = iconClass;
		this.count = count;
	}

	public DepotTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1) {
		super(titleModel, mainPageClass, additionalPageClass1);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, int count, 
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2,
			Class<? extends DepotPage> additionalPageClass3) {
		super(titleModel, mainPageClass, additionalPageClass1, 
				additionalPageClass2, additionalPageClass3);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, int count,
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2,
			Class<? extends DepotPage> additionalPageClass3,
			Class<? extends DepotPage> additionalPageClass4) {
		super(titleModel, mainPageClass, additionalPageClass1, 
				additionalPageClass2, additionalPageClass3, additionalPageClass4);
		this.iconClass = iconClass;
		this.count = count;
	}
	
	public DepotTab(IModel<String> titleModel, String iconClass, int count,
			Class<? extends DepotPage> mainPageClass, 
			Class<? extends DepotPage> additionalPageClass1, 
			Class<? extends DepotPage> additionalPageClass2,
			Class<? extends DepotPage> additionalPageClass3,
			Class<? extends DepotPage> additionalPageClass4, 
			Class<? extends DepotPage> additionalPageClass5) {
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
		return new DepotTabLink(componentId, this);
	}

}
