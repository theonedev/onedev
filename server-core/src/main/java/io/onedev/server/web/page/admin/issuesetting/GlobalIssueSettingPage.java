package io.onedev.server.web.page.admin.issuesetting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class GlobalIssueSettingPage extends AdministrationPage {

	private final GlobalIssueSetting setting;
	
	public GlobalIssueSettingPage(PageParameters params) {
		super(params);
		setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Custom Fields"), IssueFieldListPage.class));
		tabs.add(new PageTab(Model.of("States"), IssueStateListPage.class));
		tabs.add(new PageTab(Model.of("Default State Transitions"), DefaultStateTransitionsPage.class));
		tabs.add(new PageTab(Model.of("Default Boards"), DefaultBoardListPage.class));
		tabs.add(new PageTab(Model.of("Default Queries"), DefaultQueryListPage.class));
		add(new Tabbable("issueSettingTabs", tabs));
	}

	public GlobalIssueSetting getSetting() {
		return setting;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GlobalIssueSettingResourceReference()));
	}

}
