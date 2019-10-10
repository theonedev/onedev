package io.onedev.server.web.page.project.setting.issue;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.IssueSetting;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issueworkflowreconcile.WorkflowChangeAlertPanel;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public abstract class IssueSettingPage extends ProjectSettingPage {

	public IssueSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WorkflowChangeAlertPanel("workflowChangeAlert") {
			
			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}
			
		});
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("State Transitions"), StateTransitionsPage.class) {

			@Override
			public Component render(String componentId) {
				return new TabLink(componentId, this);
			}
			
		});
		
		tabs.add(new PageTab(Model.of("Prompt Fields Upon Issue Open"), PromptFieldsUponIssueOpenSettingPage.class) {
			
			@Override
			public Component render(String componentId) {
				return new TabLink(componentId, this);
			}
			
		});
		add(new Tabbable("issueSettingTabs", tabs));
	}

	protected IssueSetting getSetting() {
		return getProject().getIssueSetting();
	}
	
	protected GlobalIssueSetting getGlobalSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private class TabLink extends PageTabLink {

		public TabLink(String id, PageTab tab) {
			super(id, tab);
		}

		@Override
		protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
			return new BookmarkablePageLink<Void>(linkId, pageClass, ProjectPage.paramsOf(getProject()));
		}
		
	}
}
