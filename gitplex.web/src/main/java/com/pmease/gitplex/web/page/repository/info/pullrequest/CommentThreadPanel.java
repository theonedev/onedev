package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentThread;
import com.pmease.gitplex.core.manager.CommentVisitManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.markdown.MarkdownPanel;
import com.pmease.gitplex.web.component.user.AvatarByUser;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class CommentThreadPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<List<CommentThread>> threadsModel;
	
	private final IModel<Map<CommentPosition, Date>> visitsModel;
	
	public CommentThreadPanel(String id, final IModel<PullRequest> requestModel, 
			final IModel<List<CommentThread>> threadsModel, 
			final IModel<Map<CommentPosition, Date>> visitsModel) {
		super(id);
		
		this.requestModel = requestModel;
		this.visitsModel = visitsModel;
		this.threadsModel = threadsModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final boolean isGuest = GitPlex.getInstance(UserManager.class).getCurrent() == null;
		
		add(new ListView<CommentThread>("threads", threadsModel) {

			@Override
			protected void populateItem(final ListItem<CommentThread> item) {
				CommentThread thread = item.getModelObject();

				Link<Void> link = new Link<Void>("link") {

					@Override
					public void onClick() {
						CommentThread thread = item.getModelObject();
						User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
						if (currentUser != null) {
							Repository repo = requestModel.getObject().getTarget().getRepository();
							CommentVisitManager manager = GitPlex.getInstance(CommentVisitManager.class);
							manager.visitComment(repo, currentUser, thread.getLastComment());
						}
						if (thread.getPosition() == null) {
							// TODO: navigate to commit comments page
						} else if (thread.getPosition().getLineNo() == null) {
							// TODO: navigate to commit file comments page
						} else {
							PageParameters params = RequestComparePage.paramsOf(
									requestModel.getObject(), 
									null, null, null, thread.getLastComment());
							setResponsePage(RequestComparePage.class, params);
						}
					}
					
				};
				
				link.add(new AvatarByUser("userAvatar", new UserModel(thread.getLastComment().getUser())));
				link.add(new Label("userName", thread.getLastComment().getUser().getDisplayName()));
				
				if (thread.getPosition() == null) { 
					link.add(new Label("position", "whole commit"));
				} else if (thread.getPosition().getLineNo() == null) { 
					link.add(new Label("position", "file " + thread.getPosition().getFilePath()));
				} else {
					link.add(new Label("position", "line " + thread.getPosition().getLineNo() + " of file " + thread.getPosition().getFilePath()));
				}
				
				link.add(new AgeLabel("age", Model.of(thread.getLastComment().getCommentDate())));

				if (!isGuest) {
					Fragment fragment = new Fragment("stats", "userStatsFrag", CommentThreadPanel.this);
					int unread = 0;
					for (CommitComment comment: thread.getComments()) {
						Date lastVisit = visitsModel.getObject().get(thread.getPosition());
						if (lastVisit == null || comment.getCommentDate().after(lastVisit)) 
							unread++;
					}
					fragment.add(new Label("unread", unread));
					fragment.add(new Label("total", thread.getComments().size()));
					link.add(fragment);

					if (unread != 0)
						item.add(AttributeAppender.append("class", " unread"));
				} else {
					Fragment fragment = new Fragment("stats", "guestStatsFrag", CommentThreadPanel.this);
					fragment.add(new Label("total", thread.getComments().size()));
					link.add(fragment);
				}
				
				item.add(link);
			
				item.add(new MarkdownPanel("lastComment", Model.of(thread.getLastComment().getContent())));
			}
			
		});
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!threadsModel.getObject().isEmpty());
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		threadsModel.detach();
		visitsModel.detach();
		
		super.onDetach();
	}

}
