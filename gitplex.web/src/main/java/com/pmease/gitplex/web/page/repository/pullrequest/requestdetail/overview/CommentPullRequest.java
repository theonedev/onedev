package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
class CommentPullRequest extends AbstractRenderableActivity {

	private final Long commentId;
	
	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public User getUser() {
		return getComment().getUser();
	}

	public CommentPullRequest(Comment comment) {
		super(comment.getRequest(), comment.getUser(), comment.getDate());
		this.commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		if (getComment().getInlineInfo() != null)
			return new InlineCommentActivityPanel(panelId, this);
		else 
			return new CommentActivityPanel(panelId, this);
	}

	public Comment getComment() {
		return GitPlex.getInstance(Dao.class).load(Comment.class, commentId);
	}

}
