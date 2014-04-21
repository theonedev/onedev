package com.pmease.gitop.web.page.repository.pullrequest.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.GitPerson;
import com.pmease.commons.wicket.behavior.DisableIfBlankBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestCommentManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestComment;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.NullableUserLink;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class RequestActivitiesPanel extends Panel {

	private String newCommentContent;
	
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
				PullRequestActivity activity = item.getModelObject();

				User user = activity.getUser();
				item.add(new NullableUserLink("userAvatar", user, Mode.AVATAR));
				item.add(new NullableUserLink("userName", user, Mode.NAME));
				
				item.add(new Label("action", activity.getAction()));
				item.add(new Label("date", DateUtils.formatAge(activity.getDate())));
				item.add(item.getModelObject().render("detail"));
			}
			
		});

		WebMarkupContainer newCommentContainer = new WebMarkupContainer("newCommentContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(Gitop.getInstance(UserManager.class).getCurrent() != null);
			}
			
		};
		add(newCommentContainer);
		
		GitPerson person = Gitop.getInstance(UserManager.class).getCurrent().asPerson();
		newCommentContainer.add(new PersonLink("userAvatar", person, Mode.AVATAR));
		
		newCommentContainer.add(new PersonLink("userName", person, Mode.NAME));
		
		final TextArea<String> newCommentArea = new TextArea<String>("content", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return newCommentContent;
			}

			@Override
			public void setObject(String object) {
				newCommentContent = object;
			}

		});
		
		Link<Void> newCommentSaveLink = new Link<Void>("save") {

			@Override
			public void onClick() {
				PullRequestComment comment = new PullRequestComment();
				comment.setRequest(getPullRequest());
				comment.setUser(Gitop.getInstance(UserManager.class).getCurrent());
				comment.setContent(newCommentContent);
				Gitop.getInstance(PullRequestCommentManager.class).save(comment);
				newCommentContent = null;
			}
			
		};
		newCommentContainer.add(newCommentSaveLink);
		
		newCommentArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				newCommentArea.processInput();
			}
			
		});
		newCommentArea.add(new DisableIfBlankBehavior(newCommentSaveLink));
		
		newCommentContainer.add(newCommentArea);
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject(); 
	}
	
}
