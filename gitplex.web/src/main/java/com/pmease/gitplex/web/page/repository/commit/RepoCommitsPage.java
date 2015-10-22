package com.pmease.gitplex.web.page.repository.commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
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
	
	private RepeatingView commitsView;
	
	private WebMarkupContainer footer;
	
	private IModel<LastAndCurrentCommits> lastAndCurrentCommitsModel = new LoadableDetachableModel<LastAndCurrentCommits>() {

		@Override
		protected LastAndCurrentCommits load() {
			LastAndCurrentCommits lastAndCurrentCommits = new LastAndCurrentCommits();
			LogCommand log = new LogCommand(getRepository().git().repoDir());
			log.maxCount(step*COUNT);
			if (revisionHash != null)
				log.toRev(revisionHash);
			else
				log.allBranchesAndTags(true);
			if (path != null)
				log.path(path);
			
			List<Commit> commits = log.call();
			
			hasMore = commits.size() == step*COUNT;
			
			int lastMaxCount = (step-1)*COUNT;

			lastAndCurrentCommits.last = new ArrayList<>();
			
			for (int i=0; i<lastMaxCount; i++) 
				lastAndCurrentCommits.last.add(commits.get(i));
			
			sort(lastAndCurrentCommits.last, 0);

			lastAndCurrentCommits.current = new ArrayList<>(lastAndCurrentCommits.last);
			for (int i=lastMaxCount; i<commits.size(); i++)
				lastAndCurrentCommits.current.add(commits.get(i));
			
			sort(lastAndCurrentCommits.current, lastMaxCount);

			return lastAndCurrentCommits;
		}
		
	};
	
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

	private void sort(List<Commit> commits, int after) {
		final Map<String, Long> hash2index = new HashMap<>();
		Map<String, Commit> hash2commit = new HashMap<>();
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			hash2index.put(commit.getHash(), 1L*i*commits.size());
			hash2commit.put(commit.getHash(), commit);
		}

		Stack<Commit> stack = new Stack<>();
		
		for (int i=commits.size()-1; i>after; i--)
			stack.push(commits.get(i));

		while (!stack.isEmpty()) {
			Commit commit = stack.pop();
			long commitIndex = hash2index.get(commit.getHash());
			int count = 1;
			for (String parentHash: commit.getParentHashes()) {
				Long parentIndex = hash2index.get(parentHash);
				if (parentIndex != null && parentIndex.longValue()<commitIndex) {
					stack.push(hash2commit.get(parentHash));
					hash2index.put(parentHash, commitIndex+(count++));
				}
			}
		}
		
		Collections.sort(commits, new Comparator<Commit>() {

			@Override
			public int compare(Commit o1, Commit o2) {
				long value = hash2index.get(o1.getHash()) - hash2index.get(o2.getHash());
				if (value < 0)
					return -1;
				else if (value > 0)
					return 1;
				else
					return 0;
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(commitsView = newCommitsView());
		
		footer = new WebMarkupContainer("footer");
		footer.setOutputMarkupId(true);
		
		footer.add(new AjaxLink<Void>("more") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				step++;
				
				LastAndCurrentCommits commits = lastAndCurrentCommitsModel.getObject();
				for (int i=0; i<commits.last.size(); i++) {
					Commit lastCommit = commits.last.get(i);
					Commit currentCommit = commits.current.get(i);
					if (!lastCommit.getHash().equals(currentCommit.getHash())) {
						Component item = commitsView.get(i);
						Component newItem = newCommitItem(item.getId(), i);
						item.replaceWith(newItem);
						target.add(newItem);
					}
				}

				StringBuilder builder = new StringBuilder();
				for (int i=commits.last.size(); i<commits.current.size(); i++) {
					Component item = newCommitItem(commitsView.newChildId(), i);
					commitsView.add(item);
					target.add(item);
					builder.append(String.format("$('#repo-commits>ul>li.footer').before(\"<li id='%s'></li>\");", 
							item.getMarkupId()));
				}
				target.prependJavaScript(builder);
				
				target.add(footer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasMore);
			}
			
		});
		footer.add(new WebMarkupContainer("noMore") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!hasMore);
			}
			
		});
		add(footer);
	}
	
	private RepeatingView newCommitsView() {
		RepeatingView commitsView = new RepeatingView("commits");
		
		for (int i=0; i<lastAndCurrentCommitsModel.getObject().current.size(); i++) 
			commitsView.add(newCommitItem(commitsView.newChildId(), i));
		
		return commitsView;
	}
	
	private Component newCommitItem(String itemId, final int index) {
		WebMarkupContainer item = new WebMarkupContainer(itemId);
		Commit commit = lastAndCurrentCommitsModel.getObject().current.get(index);
		item.add(new PersonLink("avatar", Model.of(commit.getAuthor()), AvatarMode.AVATAR));

		item.add(new CommitMessagePanel("message", repoModel, new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				return lastAndCurrentCommitsModel.getObject().current.get(index);
			}
			
		}));

		item.add(new PersonLink("name", Model.of(commit.getAuthor()), AvatarMode.NAME));
		item.add(new Label("age", DateUtils.formatAge(commit.getAuthor().getWhen())));
		
		item.add(new CommitHashPanel("hash", Model.of(commit.getHash())));
		
		RepoFileState state = new RepoFileState();
		state.blobIdent.revision = commit.getHash();
		item.add(new BookmarkablePageLink<Void>("codeLink", RepoFilePage.class, 
				RepoFilePage.paramsOf(repoModel.getObject(), state)));
		
		item.setOutputMarkupId(true);
		
		return item;
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
		
		replace(commitsView = newCommitsView());
		target.add(commitsView);
		target.add(footer);
	}

	@Override
	protected void onDetach() {
		lastAndCurrentCommitsModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RepoCommitsPage.class, "repo-commits.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RepoCommitsPage.class, "repo-commits.css")));
	}
	
	private static class LastAndCurrentCommits {
		List<Commit> last;
		
		List<Commit> current;
	}
}
