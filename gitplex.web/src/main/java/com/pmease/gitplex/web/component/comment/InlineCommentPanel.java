package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.comment.event.CommentResized;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.websocket.PullRequestChanged;

@SuppressWarnings("serial")
public class InlineCommentPanel extends GenericPanel<Comment> {

	public InlineCommentPanel(String id, IModel<Comment> commentModel) {
		super(id, commentModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer alert = new WebMarkupContainer("alert") {
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					if (pullRequestChanged.getEvent() == PullRequest.Event.UPDATED) {
						AjaxRequestTarget target = pullRequestChanged.getTarget();
						setVisible(true);
						target.add(this);
						send(this, Broadcast.BUBBLE, new CommentResized(target, InlineCommentPanel.this.getModelObject()));
					}
 				}				
			}

		};
		alert.add(new InlineCommentLink("refresh", getModel(), 
				Model.of("Context of comment updated, click to display in new context")));
		alert.setOutputMarkupPlaceholderTag(true);
		alert.setVisible(false);
		add(alert);
		
		add(new UserLink("avatar", new UserModel(getModelObject().getUser()), AvatarMode.AVATAR));
		add(new CommentPanel("detail", getModel()));
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(InlineCommentPanel.class, "inline-comment.css")));
	}
	
}
