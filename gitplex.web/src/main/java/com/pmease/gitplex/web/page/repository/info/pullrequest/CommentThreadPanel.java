package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ThreadVisitManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.markdown.MarkdownPanel;
import com.pmease.gitplex.web.component.user.AvatarByUser;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class CommentThreadPanel extends Panel {

	private final IModel<List<CommitComment>> commentsModel;
	
	private final IModel<List<CommentThread>> threadsModel;
	
	private final IModel<Map<CommentPosition, Date>> visitsModel;
	
	public CommentThreadPanel(String id, final IModel<List<CommitComment>> commentsModel) {
		super(id);
		
		this.commentsModel = commentsModel;
		
		threadsModel = new LoadableDetachableModel<List<CommentThread>>() {

			@Override
			protected List<CommentThread> load() {
				Map<CommentPosition, List<CommitComment>> threadMap = new HashMap<>(); 
				for (CommitComment comment: commentsModel.getObject()) {
					List<CommitComment> threadComments = threadMap.get(comment.getPosition());
					if (threadComments == null) {
						threadComments = new ArrayList<>();
						threadMap.put(comment.getPosition(), threadComments);
					}
					threadComments.add(comment);
				}
				
				List<CommentThread> threads = new ArrayList<>();
				for (Map.Entry<CommentPosition, List<CommitComment>> entry: threadMap.entrySet()) 
					threads.add(new CommentThread(entry.getKey(), entry.getValue()));
				
				Collections.sort(threads);
				return threads;
			}
			
		};
		
		visitsModel = new LoadableDetachableModel<Map<CommentPosition, Date>>() {

			@Override
			protected Map<CommentPosition, Date> load() {
				CommitComment aComment = commentsModel.getObject().get(0);
				return GitPlex.getInstance(ThreadVisitManager.class).calcVisitMap(
						aComment.getRepository(), aComment.getCommit());
			}
			
		};
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
				
				link.add(new AgeLabel("age", Model.of(thread.getLastComment().getDate())));

				int unread = 0;
				for (CommitComment comment: thread.getComments()) {
					Date lastVisit = visitsModel.getObject().get(thread.getPosition());
					if (lastVisit == null || comment.getDate().after(lastVisit)) 
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
		commentsModel.detach();
		threadsModel.detach();
		visitsModel.detach();
		
		super.onDetach();
	}

}
