package io.onedev.server.web.page.project.builds.detail;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabHead;

@ExtensionPoint
@SuppressWarnings("serial")
public class BuildTab extends PageTab {

	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2, 
			Class<? extends BuildDetailPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2, 
			Class<? extends BuildDetailPage> additionalPageClass3, Class<? extends BuildDetailPage> additionalPageClass4) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2, 
			Class<? extends BuildDetailPage> additionalPageClass3, Class<? extends BuildDetailPage> additionalPageClass4, 
			Class<? extends BuildDetailPage> additionalPageClass5) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, 
				additionalPageClass3, additionalPageClass4, additionalPageClass5);
	}
	
	@Override
	public Component render(String componentId) {
		return new PageTabHead(componentId, this) {

			@Override
			protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
				BuildDetailPage page = (BuildDetailPage) getPage();
				return new ViewStateAwarePageLink<Void>(linkId, pageClass, 
						BuildDetailPage.paramsOf(page.getBuild()));
			}
			
		};
	}
	
}