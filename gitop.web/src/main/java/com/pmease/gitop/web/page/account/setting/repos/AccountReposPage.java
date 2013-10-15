package com.pmease.gitop.web.page.account.setting.repos;

import java.util.List;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.model.ProjectModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;
import com.pmease.gitop.web.page.project.ProjectAdminPage;

@SuppressWarnings("serial")
public class AccountReposPage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Your Repositories";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.REPOS;
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		IModel<List<Project>> model = new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				return Lists.newArrayList(getAccount().getRepositories());
			}
			
		};
		
		ListView<Project> view = new ListView<Project>("projects", model) {

			@Override
			protected void populateItem(ListItem<Project> item) {
				Project project = item.getModelObject();
				item.add(new SimpleProjectInfo("info", new ProjectModel(project)));
				item.add(new BookmarkablePageLink<Void>("admin", ProjectAdminPage.class,
						PageSpec.forProject(project)));
			}
			
		};
		
		add(view);
	}
}
