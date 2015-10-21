package com.pmease.gitplex.web.page.repository.commit;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
public class RepoCommitsPage extends RepositoryPage {

	private static final int COUNT = 100;
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	protected String revision;
	
	protected String revisionHash;
	
	protected String path;
	
	private int step;
	
	private boolean hasMore;
	
	private WebMarkupContainer commitsContainer;
	
	public RepoCommitsPage(PageParameters params) {
		super(params);
		
		revision = GitUtils.normalizePath(params.get(PARAM_REVISION).toString());
		path = GitUtils.normalizePath(params.get(PARENT_PATH).toString());
		initState();
	}
	
	private void initState() {
		if (revision != null)
			revisionHash = getRepository().getObjectId(revision).name();
		step = 1;
	}

	private List<Commit> loadCommits() {
		LogCommand log = new LogCommand(getRepository().git().repoDir());
		log.maxCount(step*COUNT+1);
		if (revisionHash != null)
			log.toRev(revisionHash);
		else
			log.allBranchesAndTags(true);
		if (path != null)
			log.path(path);
		
		List<Commit> commits = log.call();
		hasMore = commits.size() > step*COUNT;
		
		return commits;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		commitsContainer = new WebMarkupContainer("commitsContainer");
		commitsContainer.setOutputMarkupId(true);
		add(commitsContainer);
		
		commitsContainer.add(new ListView<Commit>("commits", new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				return loadCommits();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				
				item.add(new PersonLink("avatar", Model.of(commit.getAuthor()), AvatarMode.AVATAR));

				item.add(new CommitMessagePanel("message", repoModel, new AbstractReadOnlyModel<Commit>() {

					@Override
					public Commit getObject() {
						return item.getModelObject();
					}
					
				}));

				item.add(new PersonLink("name", Model.of(commit.getAuthor()), AvatarMode.NAME));
				item.add(new Label("age", DateUtils.formatAge(commit.getAuthor().getWhen())));
				
				item.add(new CommitHashPanel("hash", Model.of(commit.getHash())));
				
				RepoFileState state = new RepoFileState();
				state.blobIdent.revision = commit.getHash();
				item.add(new BookmarkablePageLink<Void>("codeLink", RepoFilePage.class, 
						RepoFilePage.paramsOf(repoModel.getObject(), state)));
			}
			
		});
		
		commitsContainer.add(new AjaxLink<Void>("more") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				step++;
				target.add(commitsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasMore);
			}
			
		});
		commitsContainer.add(new WebMarkupContainer("noMore") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!hasMore);
			}
			
		});
	}
	
	public static PageParameters paramsOf(Repository repository, String revision, String path) {
		PageParameters params = paramsOf(repository);
		params.set(PARAM_REVISION, revision);
		params.set(PARAM_PATH, path);
		return params;
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoCommitsPage.class, paramsOf(repository));
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		RepoCommitsState state = (RepoCommitsState) data;
		revision = state.revision;
		path = state.path;
		
		initState();
		
		target.add(commitsContainer);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RepoCommitsPage.class, "repo-commits.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RepoCommitsPage.class, "repo-commits.css")));
	}
	
}
