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
import com.pmease.gitop.web.page.project.RepositoryBasePage;
import com.pmease.gitop.web.page.project.api.RepositorySettingTab;

@SuppressWarnings("serial")
public abstract class AbstractRepositorySettingPage extends RepositoryBasePage {

	public AbstractRepositorySettingPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(getProject()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createSideNavs("nav"));
	}
	
	private Component createSideNavs(String id) {
		IModel<List<RepositorySettingTab>> model = new LoadableDetachableModel<List<RepositorySettingTab>>() {

			@Override
			protected List<RepositorySettingTab> load() {
				return getAllTabs();
			}
		};
		
		ListView<RepositorySettingTab> navsView = new ListView<RepositorySettingTab>(id, model) {

			@Override
			protected void populateItem(ListItem<RepositorySettingTab> item) {
				final RepositorySettingTab tab = item.getModelObject();
				Component link = tab.newTabLink("link", PageSpec.forRepository(getProject()));
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
	
	private List<RepositorySettingTab> getAllTabs() {
		List<RepositorySettingTab> tabs = Lists.newArrayList();
		tabs.add(new RepositorySettingTab(Model.of("Options"), RepositoryOptionsPage.class));
		tabs.add(new RepositorySettingTab(Model.of("Gate Keepers"), GateKeeperSettingPage.class));
		tabs.add(new RepositorySettingTab(Model.of("Hooks"), RepositoryHooksPage.class));
		tabs.add(new RepositorySettingTab(Model.of("Pull Requests"), PullRequestSettingsPage.class));
		tabs.add(new RepositorySettingTab(Model.of("Audit Log"), RepositoryAuditLogPage.class));
		tabs.add(new RepositorySettingTab(Model.of("Permissions"), RepositoryPermissionsPage.class));
		
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
