package com.pmease.gitop.web.page.repository.pullrequest;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;
import com.pmease.gitop.web.page.repository.source.commit.SourceCommitPage;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class UpdateCommitsPanel extends Panel {

	public UpdateCommitsPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Commit>("commits", new LoadableDetachableModel<List<Commit>>() {

			@Override
			public List<Commit> load() {
				PullRequestUpdate update = getUpdate();
				PullRequest request = update.getRequest();
				
				List<Commit> commits;
				Git git = request.getTarget().getRepository().git();
				int index = request.getSortedUpdates().indexOf(update);
				if (index == request.getSortedUpdates().size() - 1) {
					commits = git.log(request.getBaseCommit(), 
							request.getInitialUpdate().getHeadCommit(), 
							null, 0, 0); 
				} else {
					commits = git.log(request.getSortedUpdates().get(index+1).getHeadCommit(), 
							update.getHeadCommit(), null, 0, 0); 
				}
				
				Collections.reverse(commits);
				return commits;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				item.add(new PersonLink("author", commit.getAuthor(), PersonLink.Mode.NAME_AND_AVATAR));

				item.add(new Label("message", commit.getSubject()));
				
				item.add(new Label("date", DateUtils.formatAge(commit.getAuthor().getWhen())));
				
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				AbstractLink link = new BookmarkablePageLink<Void>("shaLink",
						SourceCommitPage.class,
						SourceCommitPage.newParams(page.getRepository(), commit.getHash()));
				link.add(new Label("sha", GitUtils.abbreviateSHA(commit.getHash())));
				
				item.add(link);
				
				if (getUpdate().getRequest().getMergedCommits().contains(commit.getHash())) {
					item.add(new Label("label", "merged").add(AttributeAppender.append("class", "label label-success")));
				} else if (getUpdate().getRequest().getPendingCommits().contains(commit.getHash())) {
					item.add(new WebMarkupContainer("label"));
				} else {
					item.add(new Label("label", "rebased").add(AttributeAppender.append("class", "label label-danger")));
				}
			}
			
		});
	}

	private PullRequestUpdate getUpdate() {
		return (PullRequestUpdate) getDefaultModelObject();
	}
	
}
