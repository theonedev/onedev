package com.pmease.gitplex.web.page.repository.info.pullrequest;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.ActivitiesModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.PullRequestActivity;

@SuppressWarnings("serial")
public class RequestActivitiesPage extends RequestDetailPage {
	
	private WebMarkupContainer activitiesContainer;
	
	public RequestActivitiesPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		activitiesContainer = new WebMarkupContainer("activities");
		activitiesContainer.setOutputMarkupId(true);
		add(activitiesContainer);
		
		activitiesContainer.add(new ListView<PullRequestActivity>("activityRows", new ActivitiesModel() {
			
			@Override
			protected PullRequest getPullRequest() {
				return RequestActivitiesPage.this.getPullRequest();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestActivity> item) {
				PullRequestActivity activity = item.getModelObject();
				if (activity.isDiscussion()) {
					item.add(AttributeAppender.append("class", " discussion"));
					item.add(new UserLink("avatar", new UserModel(activity.getUser()), AvatarMode.AVATAR));
				} else {
					if (getModelObject().get(item.getIndex()-1).isDiscussion())
						item.add(AttributeAppender.append("class", " first"));
					item.add(AttributeAppender.append("class", " action"));
					item.add(new WebMarkupContainer("avatar"));
				}
				
				item.add(item.getModelObject().render("activity"));
			}
			
		});

		WebMarkupContainer newCommentRow = new WebMarkupContainer("newCommentRow") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(UserManager.class).getCurrent() != null);
			}
			
		};
		activitiesContainer.add(newCommentRow);
		
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		newCommentRow.add(new UserLink("avatar", new UserModel(user), AvatarMode.AVATAR));

		Form<?> form = new Form<Void>("form");
		newCommentRow.add(form);
		
		final CommentInput input = new CommentInput("input", Model.of(""));
		input.setRequired(true);
		form.add(input);
		
		form.add(new AjaxSubmitLink("comment") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				PullRequestComment comment = new PullRequestComment();
				comment.setRequest(getPullRequest());
				comment.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
				comment.setContent(input.getModelObject());
				GitPlex.getInstance(Dao.class).persist(comment);
				input.setModelObject("");
				
				target.add(activitiesContainer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof RefreshActivities) {
			RefreshActivities refresh = (RefreshActivities) event.getPayload();
			refresh.getTarget().add(activitiesContainer);
		}
	}

	public static class RefreshActivities extends AjaxEvent {

		public RefreshActivities(AjaxRequestTarget target) {
			super(target);
		}
		
	}
}
