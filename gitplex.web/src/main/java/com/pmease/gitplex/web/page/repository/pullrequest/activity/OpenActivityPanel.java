package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.markdown.MarkdownPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class OpenActivityPanel extends Panel {

	private IModel<PullRequest> requestModel;
	
	public OpenActivityPanel(String id, IModel<PullRequest> requestModel) {
		super(id);
		this.requestModel = requestModel;
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("content", "viewFrag", this);

		String description = requestModel.getObject().getDescription();
		if (StringUtils.isNotBlank(description))
			fragment.add(new MarkdownPanel("description", Model.of(description)));
		else
			fragment.add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", new UserModel(requestModel.getObject().getSubmitter()), AvatarMode.NAME));
		add(new AgeLabel("age", Model.of(requestModel.getObject().getCreateDate())));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "editFrag", OpenActivityPanel.this);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				fragment.add(form);

				final CommentInput input = new CommentInput("input", Model.of(requestModel.getObject().getDescription()));
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						PullRequest request = requestModel.getObject();
						request.setDescription(input.getModelObject());
						GitPlex.getInstance(Dao.class).persist(request);

						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(fragment);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(fragment);
					}
					
				});
				
				OpenActivityPanel.this.replace(fragment);
				
				target.add(fragment);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canModify(requestModel.getObject()));
			}

		});
		
		add(renderForView());
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}
	
}
