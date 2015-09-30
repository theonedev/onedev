package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
class CommentPullRequest implements RenderableActivity {

	private final Long commentId;
	
	@Override
	public Date getDate() {
		return getComment().getCreateDate();
	}

	@Override
	public User getUser() {
		return getComment().getUser();
	}

	public CommentPullRequest(Comment comment) {
		this.commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		IModel<Comment> commentModel = new LoadableDetachableModel<Comment>(){
			
			@Override
			protected Comment load() {
				return getComment();
			}
			
		}; 
		if (getComment().getInlineInfo() != null)
			return new InlineCommentActivityPanel(panelId, commentModel);
		else 
			return new CommentActivityPanel(panelId, commentModel);
	}

	public Comment getComment() {
		return GitPlex.getInstance(Dao.class).load(Comment.class, commentId);
	}

}
