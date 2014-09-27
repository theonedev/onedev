package com.pmease.gitplex.web.page.account.repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.repository.RepositoryHomeLink;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.repository.RepositoryHomePage;

@SuppressWarnings("serial")
public class RepositoriesPage extends AccountPage {

	public RepositoriesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Repositories";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		add(new BookmarkablePageLink<Void>("newAccountRepo", NewRepositoryPage.class, 
				NewRepositoryPage.paramsOf(currentUser)));
		
		ListView<Repository> repositoriesView = new ListView<Repository>("repositories", new AbstractReadOnlyModel<List<Repository>>() {

			@Override
			public List<Repository> getObject() {
				List<Repository> repositories = Lists.newArrayList(getAccount().getRepositories());
				Collections.sort(repositories, new Comparator<Repository>() {

					@Override
					public int compare(Repository repo1, Repository repo2) {
						return repo1.getName().compareTo(repo2.getName());
					}
					
				});
				return repositories;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Repository> item) {
				Repository repo = item.getModelObject();
				item.add(new BookmarkablePageLink<Void>("repositorylink", RepositoryHomePage.class, RepositoryHomePage.paramsOf(repo))
						.add(new Label("name", repo.getName())));
				
				if (repo.getForkedFrom() != null) {
					item.add(new RepositoryHomeLink("forklink", new RepositoryModel(repo.getForkedFrom())));
				} else {
					item.add(new WebMarkupContainer("forklink").setVisibilityAllowed(false));
				}
				
				item.add(new Label("description", repo.getDescription()));
				
				final Long repositoryId = repo.getId();
				item.add(new AgeLabel("lastUpdated", new AbstractReadOnlyModel<Date>() {

					@Override
					public Date getObject() {
						Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, repositoryId);
						if (repository.git().hasCommits()) {
							LogCommand command = new LogCommand(repository.git().repoDir());
							List<Commit> commits = command.maxCount(1).call();
							Commit first = Iterables.getFirst(commits, null);
							return first.getCommitter().getWhen();
						} else {
							return repository.getCreatedAt();
						}
					}
				}));
			}
			
		};
		
		add(repositoriesView);
	}
}
