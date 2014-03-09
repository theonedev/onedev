package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.commons.lang3.StringUtils;
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
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.PullRequest;

@SuppressWarnings("serial")
public class DescriptionPanel extends Panel {

	private String description;
	
	public DescriptionPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("content", "viewFrag", this);
		
		description = getPullRequest().getDescription();
		if (StringUtils.isNotBlank(description))
			fragment.add(new MultiLineLabel("description", description));
		else
			fragment.add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
		
		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				description = getPullRequest().getDescription();
				
				Fragment fragment = new Fragment("content", "editFrag", DescriptionPanel.this);
				
				final TextArea<String> descriptionArea = new TextArea<String>("description", new IModel<String>() {

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
				
				descriptionArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						descriptionArea.processInput();
					}
					
				});
				
				fragment.add(descriptionArea);
				
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
				
				setVisible(Gitop.getInstance(AuthorizationManager.class)
						.canModify(getPullRequest()));
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
