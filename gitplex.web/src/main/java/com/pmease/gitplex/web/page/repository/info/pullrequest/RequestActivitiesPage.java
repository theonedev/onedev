package com.pmease.gitplex.web.page.repository.info.pullrequest;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.DisableIfBlankBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserInfoSnippet;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.ActivitiesModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.PullRequestActivity;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class RequestActivitiesPage extends RequestDetailPage {

	private String newCommentContent;
	
	public RequestActivitiesPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestActivity>("activities", new ActivitiesModel() {
			
			@Override
			protected PullRequest getPullRequest() {
				return RequestActivitiesPage.this.getPullRequest();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestActivity> item) {
				PullRequestActivity activity = item.getModelObject();

				item.add(new UserInfoSnippet("activity", new UserModel(activity.getUser())) {
					
					@Override
					protected Component newInfoLine(String componentId) {
						Fragment fragment = new Fragment(componentId, "actionInfoFrag", RequestActivitiesPage.this);

						PullRequestActivity activity = item.getModelObject();
						fragment.add(new Label("action", activity.getAction()));
						fragment.add(new Label("date", DateUtils.formatAge(activity.getDate())));
						
						return fragment;
					}
				});
				
				item.add(item.getModelObject().render("detail"));
			}
			
		});

		WebMarkupContainer newCommentContainer = new WebMarkupContainer("newCommentContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(UserManager.class).getCurrent() != null);
			}
			
		};
		add(newCommentContainer);
		
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		newCommentContainer.add(new UserInfoSnippet("newComment", new UserModel(user)) {
			
			@Override
			protected Component newInfoLine(String componentId) {
				return new Label(componentId, "Add New Comment");
			}
		});
		
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
				comment.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
				comment.setContent(newCommentContent);
				GitPlex.getInstance(Dao.class).persist(comment);
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
