package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.page.project.AbstractProjectPage;

@SuppressWarnings("serial")
public class DescriptionPanel extends Panel {

	private String description;
	
	public DescriptionPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("content", "viewFrag", this);
		String comment = getPullRequest().getDescription();
		if (StringUtils.isNotBlank(comment))
			fragment.add(new MultiLineLabel("description", getPullRequest().getDescription()));
		else
			fragment.add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
		
		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				description = getPullRequest().getDescription();
				
				Fragment fragment = new Fragment("content", "editFrag", DescriptionPanel.this);
				
				final TextArea<String> commentArea = new TextArea<String>("description", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return description;
					}

					@Override
					public void setObject(String object) {
						description = object;
					}

				});
				
				commentArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						commentArea.processInput();
					}
					
				});
				
				fragment.add(commentArea);
				
				fragment.add(new AjaxLink<Void>("save") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						request.setDescription(description);
						Gitop.getInstance(PullRequestManager.class).save(request);

						Fragment fragment = renderForView();
						DescriptionPanel.this.replace(fragment);
						target.add(DescriptionPanel.this);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						DescriptionPanel.this.replace(fragment);
						target.add(DescriptionPanel.this);
					}
					
				});
				
				DescriptionPanel.this.replace(fragment);
				
				target.add(DescriptionPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(page.getProject()))) {
					setVisible(true);
				} else {
					User currentUser = Gitop.getInstance(UserManager.class).getCurrent();
					User submittedBy = getPullRequest().getSubmittedBy();
					setVisible(submittedBy != null && submittedBy.equals(currentUser));
				}
			}

		});
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(renderForView());

		setOutputMarkupId(true);
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
	
}
