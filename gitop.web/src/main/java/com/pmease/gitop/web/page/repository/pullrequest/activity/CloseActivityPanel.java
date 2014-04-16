package com.pmease.gitop.web.page.repository.pullrequest.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.CloseInfo;
import com.pmease.gitop.model.PullRequest;

@SuppressWarnings("serial")
public class CloseActivityPanel extends Panel {

	private String comment;
	
	public CloseActivityPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("comment", "viewFrag", this);

		comment = getPullRequest().getCloseInfo().getComment();
		if (StringUtils.isNotBlank(comment))
			fragment.add(new MultiLineLabel("content", comment));
		else
			fragment.add(new Label("content", "<i>No comment</i>").setEscapeModelStrings(false));
		
		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				comment = getPullRequest().getCloseInfo().getComment();
				
				Fragment fragment = new Fragment("comment", "editFrag", CloseActivityPanel.this);
				
				final TextArea<String> commentArea = new TextArea<String>("content", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return comment;
					}

					@Override
					public void setObject(String object) {
						comment = object;
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
						request.getCloseInfo().setComment(comment);
						Gitop.getInstance(PullRequestManager.class).save(request);

						Fragment fragment = renderForView();
						CloseActivityPanel.this.replace(fragment);
						target.add(CloseActivityPanel.this);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						CloseActivityPanel.this.replace(fragment);
						target.add(CloseActivityPanel.this);
					}
					
				});
				
				CloseActivityPanel.this.replace(fragment);
				
				target.add(CloseActivityPanel.this);
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
		
		add(new WebMarkupContainer("icon") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (getPullRequest().getCloseInfo().getCloseStatus() == CloseInfo.Status.INTEGRATED) {
					tag.put("class",  "icon icon-2x icon-check-circle");
					tag.put("title", "This request has been integrated");
				} else {
					tag.put("class",  "icon icon-2x icon-delete-circle");
					tag.put("title", "This request has been discarded");
				}
			}
			
		});

		setOutputMarkupId(true);
	}

	private PullRequest getPullRequest() {
		PullRequest request = (PullRequest) getDefaultModelObject();
		Preconditions.checkNotNull(request.getCloseInfo());
		return request;
	}
	
}
