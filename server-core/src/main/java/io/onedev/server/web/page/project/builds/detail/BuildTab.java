package io.onedev.server.web.page.project.builds.detail;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabHead;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

@ExtensionPoint
@SuppressWarnings("serial")
public class BuildTab extends PageTab {

	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass) {
		super(Model.of(title), mainPageClass);
	}

	public BuildTab(String title, String icon, Class<? extends BuildDetailPage> mainPageClass) {
		super(Model.of(title), Model.of(icon), mainPageClass);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1) {
		super(Model.of(title), mainPageClass, additionalPageClass1);
	}

	public BuildTab(String title, String icon, Class<? extends BuildDetailPage> mainPageClass,
					Class<? extends BuildDetailPage> additionalPageClass1) {
		super(Model.of(title), Model.of(icon), mainPageClass, additionalPageClass1);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2);
	}

	public BuildTab(String title, String icon, Class<? extends BuildDetailPage> mainPageClass,
					Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2) {
		super(Model.of(title), Model.of(icon), mainPageClass, additionalPageClass1, additionalPageClass2);
	}
	
	public BuildTab(String title, Class<? extends BuildDetailPage> mainPageClass, 
			Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2, 
			Class<? extends BuildDetailPage> additionalPageClass3) {
		super(Model.of(title), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}

	public BuildTab(String title, String icon, Class<? extends BuildDetailPage> mainPageClass,
					Class<? extends BuildDetailPage> additionalPageClass1, Class<? extends BuildDetailPage> additionalPageClass2,
					Class<? extends BuildDetailPage> additionalPageClass3) {
		super(Model.of(title), Model.of(icon), mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
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