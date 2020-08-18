package io.onedev.server.web.page.project.setting.build;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public abstract class BuildSettingPage extends ProjectSettingPage {

	public BuildSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Job Secrets"), JobSecretsPage.class) {

			@Override
			public Component render(String componentId) {
				return new TabLink(componentId, this);
			}
			
		});
		tabs.add(new PageTab(Model.of("Action Authorizations"), ActionAuthorizationsPage.class) {
			
			@Override
			public Component render(String componentId) {
				return new TabLink(componentId, this);
			}
			
		});
		tabs.add(new PageTab(Model.of("Build Preserve Rules"), BuildPreservationsPage.class) {
			
			@Override
			public Component render(String componentId) {
				return new TabLink(componentId, this);
			}
			
		});
		add(new Tabbable("buildSettingTabs", tabs));
	}

	private class TabLink extends PageTabHead {

		public TabLink(String id, PageTab tab) {
			super(id, tab);
		}

		@Override
		protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
			return new BookmarkablePageLink<Void>(linkId, pageClass, ProjectPage.paramsOf(getProject()));
		}
		
	}
}
