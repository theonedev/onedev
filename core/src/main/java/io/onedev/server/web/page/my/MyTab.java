package io.onedev.server.web.page.my;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class MyTab extends PageTab {

	private final String iconClass;
	
	public MyTab(String title, String iconClass, Class<? extends MyPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
		
		this.iconClass = iconClass;
	}

	public MyTab(String title, String iconClass, Class<? extends MyPage> mainPageClass, 
			Class<? extends MyPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
		
		this.iconClass = iconClass;
	}

	public MyTab(String title, String iconClass, Class<? extends MyPage> mainPageClass, 
			Class<? extends MyPage> additionalPageClass1, 
			Class<? extends MyPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
		
		this.iconClass = iconClass;
	}
	
	public MyTab(String title, String iconClass, Class<? extends MyPage> mainPageClass, 
			Class<? extends MyPage> additionalPageClass1, 
			Class<? extends MyPage> additionalPageClass2, 
			Class<? extends MyPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		
		this.iconClass = iconClass;
	}
	
	public String getIconClass() {
		return iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new MyTabLink(componentId, this);
	}

}
