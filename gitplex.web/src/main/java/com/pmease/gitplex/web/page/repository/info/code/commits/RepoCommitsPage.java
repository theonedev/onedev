package com.pmease.gitplex.web.page.repository.info.code.commits;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;
import com.pmease.gitplex.web.page.repository.info.code.component.RevisionSelector;

@SuppressWarnings("serial")
public class RepoCommitsPage extends RepositoryInfoPage {

	public static final int COMMITS_PER_PAGE = 30;
	
	private final int page;
	
	private final IModel<List<Commit>> commitsModel;

	public static PageParameters paramsOf(Repository repository, String revision, 
			String path, int page) {
		PageParameters params = paramsOf(repository, revision, path);
		
		if (page > 0)
			params.set("page", page);
		
		return params;
	}
	
	public RepoCommitsPage(PageParameters params) {
		super(params);
		
		page = params.get("page").toInt(1);
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Git git = getRepository().git();
				
				// load additional one commit to see whether there is still more page
				LogCommand command = new LogCommand(git.repoDir())
										.toRev(getCurrentRevision())
										.skip((page - 1) * COMMITS_PER_PAGE)
										.maxCount(COMMITS_PER_PAGE + 1);
				if (getCurrentPath() != null) 
					command.path(getCurrentPath());
				
				List<Commit> commits = command.call();
				return commits;
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revselector"));
//		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("home", 
//				SourceTreePage.class, 
//				PageSpec.forRepository(getRepository()).add(PageSpec.OBJECT_ID, getRevision()));
//		add(homeLink);
//		homeLink.add(new Label("name", new AbstractReadOnlyModel<String>() {
//
//			@Override
//			public String getObject() {
//				return getRepository().getName();
//			}
//		}));

		add(newNavLink("home", -1));
		add(new ListView<String>("pathSegments", getCurrentPathSegments()) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(newNavLink("link", item.getIndex()));
			}
		});
		
		add(new RepoCommitsPanel("commits", commitsModel, repositoryModel));
		add(new BookmarkablePageLink<Void>("newer", RepoCommitsPage.class,
				paramsOf(getRepository(), getCurrentRevision(), getCurrentPath(), page - 1)).setEnabled(page > 1));
		add(new BookmarkablePageLink<Void>("older", RepoCommitsPage.class,
				paramsOf(getRepository(), getCurrentRevision(), getCurrentPath(), page + 1)) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(commitsModel.getObject().size() > COMMITS_PER_PAGE);
			}
			
		});
	}
	
	private Component newNavLink(String id, final int pathNum) {
		List<String> all = getCurrentPathSegments();
		
		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(id,
				RepoCommitsPage.class,
				paramsOf(getRepository(), getCurrentRevision(), getCurrentPath(), 0));
		link.setEnabled(pathNum < 0 || pathNum < all.size() - 1);
		link.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (pathNum < 0) {
					return getRepository().getName();
				} else {
					List<String> all = getCurrentPathSegments();
					return all.get(pathNum);
				}
			}
			
		}));
		
		return link;
	}
	
	@Override
	protected String getPageTitle() {
		return "Commits - " + getRepository();
	}

	@Override
	public void onDetach() {
		if (commitsModel != null) {
			commitsModel.detach();
		}

		super.onDetach();
	}
}
