package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.comment.CommentThread;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.PullRequest;
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
		
		add(new ListView<CommentThread>("threads", threadsModel) {

			@Override
			protected void populateItem(final ListItem<CommentThread> item) {
				CommentThread thread = item.getModelObject();

				Link<Void> link = new Link<Void>("link") {

					@Override
					public void onClick() {
						CommentThread thread = item.getModelObject();
						if (thread.getPosition() == null) {
							// TODO: navigate to commit comments page
						} else if (thread.getPosition().getLineNo() == null) {
							// TODO: navigate to commit file comments page
						} else {
							PageParameters params = RequestComparePage.params4(
									requestModel.getObject(), 
									null, null, null, thread.getLastComment().getId());
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

				int unread = 0;
				for (CommitComment comment: thread.getComments()) {
					Date lastVisit = visitsModel.getObject().get(thread.getPosition());
					if (lastVisit == null || comment.getCommentDate().after(lastVisit)) 
						unread++;
				}
				link.add(new Label("unread", unread));
				link.add(new Label("total", thread.getComments().size()));
				
				item.add(link);
			
				if (unread != 0)
					item.add(AttributeAppender.append("class", " unread"));
				
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
