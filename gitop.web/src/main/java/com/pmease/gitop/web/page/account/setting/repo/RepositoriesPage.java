package com.pmease.gitop.web.page.account.setting.repo;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.component.link.ProjectHomeLink;
import com.pmease.gitop.web.model.RepositoryModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;
import com.pmease.gitop.web.page.project.settings.CreateRepositoryPage;
import com.pmease.gitop.web.page.project.settings.RepositoryOptionsPage;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class RepositoriesPage extends AccountSettingPage {

	public RepositoriesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Your Repositories";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new BookmarkablePageLink<Void>("newlink", CreateRepositoryPage.class, newParams(getAccount())));
		
		IModel<List<Project>> model = new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				return Lists.newArrayList(getAccount().getProjects());
			}
			
		};
		
		ListView<Project> view = new ListView<Project>("projects", model) {

			@Override
			protected void populateItem(ListItem<Project> item) {
				Project project = item.getModelObject();
				final IModel<Project> projectModel = new RepositoryModel(project);
				item.add(new ProjectHomeLink("project", projectModel));
				item.add(new Label("age", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return DateUtils.formatAge(projectModel.getObject().getCreatedAt());
					}

				}).add(AttributeModifier.replace("title",
						new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return DataTypes.DATE
										.asString(projectModel.getObject().getCreatedAt(),
												Constants.DATETIME_FULL_FORMAT);
							}

						})));

				if (project.getForkedFrom() != null) {
					item.add(new ProjectHomeLink("forkedFrom",
							new LoadableDetachableModel<Project>() {

								@Override
								protected Project load() {
									return projectModel.getObject().getForkedFrom();
								}
							}));
				} else {
					item.add(new WebMarkupContainer("forkedFrom").setVisibilityAllowed(false));
				}
				
				item.add(new BookmarkablePageLink<Void>("admin", RepositoryOptionsPage.class,
						PageSpec.forProject(project)));
			}
			
		};
		
		add(view);
	}
}
