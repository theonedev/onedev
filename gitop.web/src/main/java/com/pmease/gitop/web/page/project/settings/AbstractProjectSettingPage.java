package com.pmease.gitop.web.page.project.settings;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.api.ProjectSettingTab;

@SuppressWarnings("serial")
public abstract class AbstractProjectSettingPage extends AbstractProjectPage {

	public AbstractProjectSettingPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(getProject()));
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(createSideNavs("nav"));
	}
	
	private Component createSideNavs(String id) {
		IModel<List<ProjectSettingTab>> model = new LoadableDetachableModel<List<ProjectSettingTab>>() {

			@Override
			protected List<ProjectSettingTab> load() {
				return getAllTabs();
			}
		};
		
		ListView<ProjectSettingTab> navsView = new ListView<ProjectSettingTab>(id, model) {

			@Override
			protected void populateItem(ListItem<ProjectSettingTab> item) {
				final ProjectSettingTab tab = item.getModelObject();
				Component link = tab.newTabLink("link", PageSpec.forProject(getProject()));
				item.add(link);
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return tab.isSelected(getPage()) ? "active" : "";
					}
				}));
			}
		};
		
		return navsView;
	}
	
	private List<ProjectSettingTab> getAllTabs() {
		List<ProjectSettingTab> tabs = Lists.newArrayList();
		tabs.add(new ProjectSettingTab(Model.of("Options"), ProjectOptionsPage.class));
		tabs.add(new ProjectSettingTab(Model.of("Gate Keepers"), GateKeeperSettingPage.class));
		tabs.add(new ProjectSettingTab(Model.of("Hooks"), ProjectHooksPage.class));
		tabs.add(new ProjectSettingTab(Model.of("Pull Requests"), PullRequestSettingsPage.class));
		tabs.add(new ProjectSettingTab(Model.of("Audit Log"), ProjectAuditLogPage.class));
		tabs.add(new ProjectSettingTab(Model.of("Permissions"), ProjectPermissionsPage.class));
		
		return tabs;
	}
//	
//	private Component newPageLink(String id, Category category) {
//		Class<? extends Page> pageClass;
//		switch (category) {
//		case OPTIONS:
//			pageClass = ProjectOptionsPage.class;
//			break;
//		case GATE_KEEPERS:
//			pageClass = GateKeeperSettingPage.class;
//			break;
//		case HOOKS:
//			pageClass = ProjectHooksPage.class;
//			break;
//			
//		case PULL_REQUESTS:
//			pageClass = PullRequestSettingsPage.class;
//			break;
//			
//		case AUDIT_LOG:
//			pageClass = ProjectAuditLogPage.class;
//			break;
//			
//		case PERMISSIONS:
//			pageClass = ProjectPermissionsPage.class;
//			break;
//			
//		default:
//			throw new IllegalArgumentException();
//		}
//		
//		AbstractLink link = new BookmarkablePageLink<Void>(id, pageClass, PageSpec.forProject(getProject()));
//		link.add(new Label("name", category.getName()));
//		return link;
//	}
}
