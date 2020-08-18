package io.onedev.server.web.page.project;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class ProjectTab extends PageTab {

	private final String iconHref;
	
	private final int count;

	public ProjectTab(IModel<String> titleModel, String iconHref, int count, 
			Class<? extends ProjectPage> mainPageClass) {
		super(titleModel, mainPageClass);
		this.iconHref = iconHref;
		this.count = count;
	}

	public ProjectTab(IModel<String> titleModel, String iconHref, int count, 
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1) {
		super(titleModel, mainPageClass, additionalPageClass1);
		this.iconHref = iconHref;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconHref, int count, 
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2);
		this.iconHref = iconHref;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconHref, int count, 
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2,
			Class<? extends ProjectPage> additionalPageClass3) {
		super(titleModel, mainPageClass, additionalPageClass1, 
				additionalPageClass2, additionalPageClass3);
		this.iconHref = iconHref;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconHref, int count,
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2,
			Class<? extends ProjectPage> additionalPageClass3,
			Class<? extends ProjectPage> additionalPageClass4) {
		super(titleModel, mainPageClass, additionalPageClass1, 
				additionalPageClass2, additionalPageClass3, additionalPageClass4);
		this.iconHref = iconHref;
		this.count = count;
	}
	
	public ProjectTab(IModel<String> titleModel, String iconHref, int count,
			Class<? extends ProjectPage> mainPageClass, 
			Class<? extends ProjectPage> additionalPageClass1, 
			Class<? extends ProjectPage> additionalPageClass2,
			Class<? extends ProjectPage> additionalPageClass3,
			Class<? extends ProjectPage> additionalPageClass4, 
			Class<? extends ProjectPage> additionalPageClass5) {
		super(titleModel, mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
		this.iconHref = iconHref;
		this.count = count;
	}
	
	public String getIconHref() {
		return iconHref;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	public Component render(String componentId) {
		return new ProjectTabHead(componentId, this);
	}

}
