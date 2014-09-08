package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.markdown.MarkdownPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestActivitiesPage;

@SuppressWarnings("serial")
public class CommentActivityPanel extends Panel {

	public CommentActivityPanel(String id, IModel<PullRequestComment> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("content", "viewFrag", this);

		fragment.add(new MarkdownPanel("comment", Model.of(getComment().getContent())));
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserLink("user", new UserModel(getComment().getUser()), AvatarMode.NAME));
		add(new AgeLabel("age", Model.of(getComment().getDate())));

		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "editFrag", CommentActivityPanel.this);

				Form<?> form = new Form<Void>("form");
				fragment.add(form);
				final CommentInput input = new CommentInput("input", Model.of(getComment().getContent()));
				input.setRequired(true);
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						PullRequestComment comment = getComment();
						comment.setContent(input.getModelObject());
						GitPlex.getInstance(Dao.class).persist(comment);

						Fragment fragment = renderForView();
						CommentActivityPanel.this.replace(fragment);
						target.add(CommentActivityPanel.this);
					}
					
					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						CommentActivityPanel.this.replace(fragment);
						target.add(CommentActivityPanel.this);
					}
					
				});
				
				CommentActivityPanel.this.replace(fragment);
				
				target.add(CommentActivityPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class)
						.canModify(getComment().getRequest()));
			}

		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitPlex.getInstance(Dao.class).remove(getComment());
				send(getPage(), Broadcast.BUBBLE, new RequestActivitiesPage.RefreshActivities(target));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class)
						.canModify(getComment().getRequest()));
			}

		}.add(new ConfirmBehavior("Do you really want to delete this comment?")));
		
		add(renderForView());

		setOutputMarkupId(true);
	}

	private PullRequestComment getComment() {
		return (PullRequestComment) getDefaultModelObject();
	}
	
}
