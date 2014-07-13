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
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.component.repository.RepositoryHomeLink;
import com.pmease.gitop.web.model.RepositoryModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;
import com.pmease.gitop.web.page.repository.settings.CreateRepositoryPage;
import com.pmease.gitop.web.page.repository.settings.RepositoryOptionsPage;
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
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("newlink", CreateRepositoryPage.class, newParams(getAccount())));
		
		IModel<List<Repository>> model = new LoadableDetachableModel<List<Repository>>() {

			@Override
			protected List<Repository> load() {
				return Lists.newArrayList(getAccount().getRepositories());
			}
			
		};
		
		ListView<Repository> view = new ListView<Repository>("repositories", model) {

			@Override
			protected void populateItem(ListItem<Repository> item) {
				Repository repository = item.getModelObject();
				final IModel<Repository> repositoryModel = new RepositoryModel(repository);
				item.add(new RepositoryHomeLink("repository", repositoryModel));
				item.add(new Label("age", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return DateUtils.formatAge(repositoryModel.getObject().getCreatedAt());
					}

				}).add(AttributeModifier.replace("title",
						new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return DataTypes.DATE
										.asString(repositoryModel.getObject().getCreatedAt(),
												Constants.DATETIME_FULL_FORMAT);
							}

						})));

				if (repository.getForkedFrom() != null) {
					item.add(new RepositoryHomeLink("forkedFrom",
							new LoadableDetachableModel<Repository>() {

								@Override
								protected Repository load() {
									return repositoryModel.getObject().getForkedFrom();
								}
							}));
				} else {
					item.add(new WebMarkupContainer("forkedFrom").setVisibilityAllowed(false));
				}
				
				item.add(new BookmarkablePageLink<Void>("admin", RepositoryOptionsPage.class,
						PageSpec.forRepository(repository)));
			}
			
		};
		
		add(view);
	}
}
