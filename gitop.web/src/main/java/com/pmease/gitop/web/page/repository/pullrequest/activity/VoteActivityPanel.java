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

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.model.Vote;

@SuppressWarnings("serial")
public class VoteActivityPanel extends Panel {

	private String comment;
	
	public VoteActivityPanel(String id, IModel<Vote> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("comment", "viewFrag", this);

		comment = getVote().getComment();
		if (StringUtils.isNotBlank(comment))
			fragment.add(new MultiLineLabel("content", comment));
		else
			fragment.add(new Label("content", "<i>No comment</i>").setEscapeModelStrings(false));
		
		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				comment = getVote().getComment();
				
				Fragment fragment = new Fragment("comment", "editFrag", VoteActivityPanel.this);
				
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
						Vote vote = getVote();
						vote.setComment(comment);
						Gitop.getInstance(Dao.class).persist(vote);

						Fragment fragment = renderForView();
						VoteActivityPanel.this.replace(fragment);
						target.add(VoteActivityPanel.this);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						VoteActivityPanel.this.replace(fragment);
						target.add(VoteActivityPanel.this);
					}
					
				});
				
				VoteActivityPanel.this.replace(fragment);
				
				target.add(VoteActivityPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(Gitop.getInstance(AuthorizationManager.class)
						.canModify(getVote()));
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
				
				if (getVote().getResult() == Vote.Result.APPROVE) {
					tag.put("class",  "fa fa-2x fa-smile-o");
					tag.put("title", "Approved");
				} else {
					tag.put("class",  "fa fa-2x fa-frown-o");
					tag.put("title", "Disapproved");
				}
			}
			
		});

		setOutputMarkupId(true);
	}

	private Vote getVote() {
		return (Vote) getDefaultModelObject();
	}
	
}
