package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import org.apache.wicket.Component;
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
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestComparePage;

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
			
			PageParameters params = RequestComparePage.paramsOf(comment.getRequest(), comment.getCommit(), null, null, comment);
			fragment.add(new BookmarkablePageLink<Void>("compareWithLatest", RequestComparePage.class, params));
			
			fragment.add(new InlineCommentPanel("comment", commentModel));
			fragment.setRenderBodyOnly(true);
			add(fragment);
		} else {
			add(new CommentPanel("content", commentModel) {

				@Override
				protected Component newAdditionalCommentActions(String id, Comment comment) {
					return new SinceChangesPanel(id, new AbstractReadOnlyModel<PullRequest>() {

						@Override
						public PullRequest getObject() {
							return commentModel.getObject().getRequest();
						}
						
					}, comment.getDate(), "Changes since this comment");
				}

				@Override
				protected Component newAdditionalReplyActions(String id, CommentReply reply) {
					return new SinceChangesPanel(id, new AbstractReadOnlyModel<PullRequest>() {

						@Override
						public PullRequest getObject() {
							return commentModel.getObject().getRequest();
						}
						
					}, reply.getDate(), "Changes since this reply");
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
