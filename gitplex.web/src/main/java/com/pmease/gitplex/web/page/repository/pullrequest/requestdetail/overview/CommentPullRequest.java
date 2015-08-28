package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
class CommentPullRequest implements RenderableActivity {

	private final Long commentId;
	
	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public User getUser() {
		return getComment().getUser();
	}

	public CommentPullRequest(PullRequestComment comment) {
		this.commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		IModel<PullRequestComment> commentModel = new LoadableDetachableModel<PullRequestComment>(){
			
			@Override
			protected PullRequestComment load() {
				return getComment();
			}
			
		}; 
		if (getComment().getInlineInfo() != null)
			return new InlineCommentActivityPanel(panelId, commentModel);
		else 
			return new CommentActivityPanel(panelId, commentModel);
	}

	public PullRequestComment getComment() {
		return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, commentId);
	}

}
