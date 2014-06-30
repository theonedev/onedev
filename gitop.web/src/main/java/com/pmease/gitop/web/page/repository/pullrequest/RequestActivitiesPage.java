package com.pmease.gitop.web.page.repository.pullrequest;

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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.DisableIfBlankBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestComment;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.NullableUserLink;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.page.repository.pullrequest.activity.ClosePullRequest;
import com.pmease.gitop.web.page.repository.pullrequest.activity.CommentPullRequest;
import com.pmease.gitop.web.page.repository.pullrequest.activity.OpenPullRequest;
import com.pmease.gitop.web.page.repository.pullrequest.activity.PullRequestActivity;
import com.pmease.gitop.web.page.repository.pullrequest.activity.UpdatePullRequest;
import com.pmease.gitop.web.page.repository.pullrequest.activity.VotePullRequest;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class RequestActivitiesPage extends RequestDetailPage {

	private String newCommentContent;
	
	public RequestActivitiesPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestActivity>("activities", new LoadableDetachableModel<List<PullRequestActivity>>() {

			@Override
			public List<PullRequestActivity> load() {
				PullRequest request = getPullRequest();
				List<PullRequestActivity> activities = new ArrayList<>();

				activities.add(new OpenPullRequest(request));
				
				for (PullRequestComment comment: request.getComments()) {
					activities.add(new CommentPullRequest(comment));
				}
				
				for (PullRequestUpdate update: request.getUpdates()) {
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
		
		PersonIdent person = Gitop.getInstance(UserManager.class).getCurrent().asPerson();
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
				Gitop.getInstance(Dao.class).persist(comment);
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

}
