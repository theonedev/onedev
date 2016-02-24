package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.component.comment.CommentPanel;

@SuppressWarnings("serial")
class CommentActivityPanel extends AbstractActivityPanel {

	private final IModel<Comment> commentModel = new LoadableDetachableModel<Comment>(){

		@Override
		protected Comment load() {
			return ((CommentPullRequest)activity).getComment();
		}
		
	};
	
	public CommentActivityPanel(String id, CommentPullRequest activity) {
		super(id, activity);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CommentPanel("content", commentModel) {

			@Override
			protected Component newAdditionalCommentOperations(String id, final IModel<Comment> commentModel) {
				return new SinceChangesLink(id, new AbstractReadOnlyModel<PullRequest>() {

					@Override
					public PullRequest getObject() {
						return CommentActivityPanel.this.commentModel.getObject().getRequest();
					}
					
				}, CommentActivityPanel.this.commentModel.getObject().getDate(), "Changes since this comment");
			}

			@Override
			protected Component newAdditionalReplyOperations(String id, CommentReply reply) {
				return new SinceChangesLink(id, new AbstractReadOnlyModel<PullRequest>() {

					@Override
					public PullRequest getObject() {
						return commentModel.getObject().getRequest();
					}
					
				}, reply.getDate(), "Changes since this reply");
			}
			
		});
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
