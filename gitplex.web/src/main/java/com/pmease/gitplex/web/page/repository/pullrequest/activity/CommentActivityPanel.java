package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestComparePage;

@SuppressWarnings("serial")
public class CommentActivityPanel extends Panel {

	private final IModel<PullRequestComment> commentModel;
	
	public CommentActivityPanel(String id, IModel<PullRequestComment> commentModel) {
		super(id);
		
		this.commentModel = commentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequestComment comment = commentModel.getObject();
		if (comment.getInlineInfo() != null) {
			Fragment fragment = new Fragment("content", "inlineFrag", this);
			fragment.add(new UserLink("name", new UserModel(comment.getUser()), AvatarMode.NAME));
			fragment.add(new AgeLabel("age", Model.of(comment.getDate())));
			
			fragment.add(new Label("file", comment.getFile()));
			
			PageParameters params = RequestComparePage.paramsOf(comment.getRequest(), comment.getOldCommitHash(), 
					comment.getNewCommitHash(), null, comment);
			fragment.add(new BookmarkablePageLink<Void>("compareView", RequestComparePage.class, params));
			
			fragment.add(new InlineCommentPanel("comment", commentModel));
			fragment.setRenderBodyOnly(true);
			add(fragment);
		} else {
			add(new CommentPanel("content", commentModel) {

				@Override
				protected Component newAdditionalCommentActions(String id, final IModel<Comment> commentModel) {
					Fragment fragment = new Fragment(id, "commentActionsFrag", CommentActivityPanel.this) {

						@Override
						protected void onDetach() {
							commentModel.detach();
							super.onDetach();
						}
						
					};
					
					fragment.add(new AjaxLink<Void>("collapse") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							send(CommentActivityPanel.this, Broadcast.BUBBLE, 
									new CommentCollapsing(target, commentModel.getObject()));
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							
							setVisible(commentModel.getObject().isResolved());
						}
						
					});
					
					fragment.add(new SinceChangesPanel("changes", new AbstractReadOnlyModel<PullRequest>() {

						@Override
						public PullRequest getObject() {
							return CommentActivityPanel.this.commentModel.getObject().getRequest();
						}
						
					}, CommentActivityPanel.this.commentModel.getObject().getDate(), "Changes since this comment"));
					
					return fragment;
				}

				@Override
				protected Component newAdditionalReplyActions(String id, final IModel<CommentReply> replyModel) {
					return new SinceChangesPanel(id, new AbstractReadOnlyModel<PullRequest>() {

						@Override
						public PullRequest getObject() {
							return commentModel.getObject().getRequest();
						}
						
					}, replyModel.getObject().getDate(), "Changes since this reply") {

						@Override
						protected void onDetach() {
							replyModel.detach();
							super.onDetach();
						}
						
					};
				}
				
			});
		}
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
