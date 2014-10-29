package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.manager.PullRequestCommentReplyManager;
import com.pmease.gitplex.core.manager.UserManager;

@SuppressWarnings("serial")
@Entity
public class PullRequestComment extends AbstractPullRequestComment {
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<PullRequestCommentReply> replies = new ArrayList<>();
	
	@Override
	public Collection<PullRequestCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<PullRequestCommentReply> replies) {
		this.replies = replies;
	}

	@Override
	public CommentReply addReply(String content) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		Preconditions.checkNotNull(user);
		PullRequestCommentReply reply = new PullRequestCommentReply();
		reply.setUser(user);
		reply.setDate(new Date());
		reply.setContent(content);
		reply.setComment(this);
		GitPlex.getInstance(PullRequestCommentReplyManager.class).save(reply);
		return reply;
	}

}
