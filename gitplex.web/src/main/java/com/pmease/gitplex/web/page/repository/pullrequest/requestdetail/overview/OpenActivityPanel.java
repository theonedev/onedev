package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class OpenActivityPanel extends Panel {

	private static final String BODY_ID = "body";
	
	private static final String FORM_ID = "form";
	
	private IModel<PullRequest> requestModel;
	
	public OpenActivityPanel(String id, IModel<PullRequest> requestModel) {
		super(id);
		this.requestModel = requestModel;
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment(BODY_ID, "viewFrag", this);

		String description = requestModel.getObject().getDescription();
		if (StringUtils.isNotBlank(description))
			fragment.add(new MarkdownViewer("description", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return requestModel.getObject().getDescription();
				}

				@Override
				public void setObject(String object) {
					requestModel.getObject().setDescription(object);
				}
				
			}, SecurityUtils.canModify(requestModel.getObject())));
		else
			fragment.add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		add(head);
		
		head.add(new UserLink("user", new UserModel(requestModel.getObject().getSubmitter()), AvatarMode.NAME));
		head.add(new Label("age", DateUtils.formatAge(requestModel.getObject().getCreateDate())));
		
		head.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(BODY_ID, "editFrag", OpenActivityPanel.this);
				
				Form<?> form = new Form<Void>(FORM_ID);
				form.setOutputMarkupId(true);
				fragment.add(form);

				final CommentInput input = new CommentInput("input", requestModel, 
						Model.of(requestModel.getObject().getDescription()));
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
						target.add(head);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(fragment);
						target.add(head);
					}
					
				});
				
				OpenActivityPanel.this.replace(fragment);
				
				target.add(fragment);
				target.add(head);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canModify(requestModel.getObject()) 
						&& OpenActivityPanel.this.get(BODY_ID).get(FORM_ID) == null);
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
