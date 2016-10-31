package com.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.PullRequestComment;
import com.gitplex.core.manager.PullRequestCommentManager;
import com.gitplex.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class CommentedActivity implements PullRequestActivity {

	private final Long commentId;
	
	public CommentedActivity(PullRequestComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new CommentedPanel(panelId, new LoadableDetachableModel<PullRequestComment>() {

			@Override
			protected PullRequestComment load() {
				return getComment();
			}
			
		});
	}
	
	public PullRequestComment getComment() {
		return GitPlex.getInstance(PullRequestCommentManager.class).load(commentId);
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public String getAnchor() {
		return getComment().getAnchor();
	}

}
