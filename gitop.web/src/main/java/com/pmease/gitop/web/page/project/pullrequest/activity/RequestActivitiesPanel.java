package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestCommentManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestComment;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;

@SuppressWarnings("serial")
public class RequestActivitiesPanel extends Panel {

	private String commentContent;
	
	public RequestActivitiesPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestActivity>("activities", new AbstractReadOnlyModel<List<PullRequestActivity>>() {

			@Override
			public List<PullRequestActivity> getObject() {
				PullRequest request = getPullRequest();
				List<PullRequestActivity> activities = new ArrayList<>();

				activities.add(new OpenPullRequest(request));
				
				for (PullRequestComment comment: request.getComments()) {
					activities.add(new CommentPullRequest(comment));
				}
				
				for (PullRequestUpdate update: request.getUpdates()) {
					if (!update.equals(request.getInitialUpdate()))
						activities.add(new UpdatePullRequest(update));
					for (Vote vote: update.getVotes()) {
						activities.add(new VotePullRequest(vote));
					}
				}
				
				if (!request.isOpen())
					activities.add(new ClosePullRequest(request));

				Collections.sort(activities, new Comparator<PullRequestActivity>() {

					@Override
					public int compare(PullRequestActivity o1, PullRequestActivity o2) {
						return o1.getDate().compareTo(o2.getDate());
					}
					
				});
				return activities;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestActivity> item) {
				item.add(item.getModelObject().render("activity"));
			}
			
		});

		WebMarkupContainer commentContainer = new WebMarkupContainer("commentContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(Gitop.getInstance(UserManager.class).getCurrent() != null);
			}
			
		};

		commentContainer.add(new GitPersonLink("user", new LoadableDetachableModel<GitPerson>() {

			@Override
			protected GitPerson load() {
				User currentUser = Gitop.getInstance(UserManager.class).getCurrent();
				Preconditions.checkNotNull(currentUser);
				GitPerson person = new GitPerson(currentUser.getName(), currentUser.getEmail());
				return person;
			}
			
		}, GitPersonLink.Mode.AVATAR_AND_NAME));

		final TextArea<String> commentArea = new TextArea<String>("comment", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return commentContent;
			}

			@Override
			public void setObject(String object) {
				commentContent = object;
			}
			
		});
		
		commentArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				commentArea.processInput();
			}
					
		});
		commentContainer.add(commentArea);
		
		commentContainer.add(new Link<Void>("saveComment") {

			@Override
			public void onClick() {
				if (StringUtils.isNotBlank(commentContent)) {
					PullRequestComment comment = new PullRequestComment();
					comment.setContent(commentContent);
					comment.setRequest(getPullRequest());
					comment.setUser(Gitop.getInstance(UserManager.class).getCurrent());
					Gitop.getInstance(PullRequestCommentManager.class).save(comment);
					commentContent = null;
				}
			}
			
		});
		
		add(commentContainer);
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject(); 
	}
	
}
