package com.pmease.gitplex.web.page.account.home;

import java.util.Date;
import java.util.List;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Iterables;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.repository.RepositoryHomeLink;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.repository.source.RepositoryHomePage;

@SuppressWarnings("serial")
public class RepositoryListPanel extends Panel {

	public RepositoryListPanel(String id, IModel<List<Repository>> repos) {
		super(id, repos);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		@SuppressWarnings("unchecked")
		ListView<Repository> repositoriesView = new ListView<Repository>("repository", (IModel<List<Repository>>) getDefaultModel()) {

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
