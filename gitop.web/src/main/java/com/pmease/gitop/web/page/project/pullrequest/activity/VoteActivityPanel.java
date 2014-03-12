package com.pmease.gitop.web.page.project.pullrequest.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class VoteActivityPanel extends Panel {

	private WebMarkupContainer container;
	
	private String comment;
	
	public VoteActivityPanel(String id, IModel<Vote> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("body", "viewFrag", this);

		comment = getVote().getComment();
		if (StringUtils.isNotBlank(comment))
			fragment.add(new MultiLineLabel("comment", comment));
		else
			fragment.add(new Label("comment", "<i>No comment</i>").setEscapeModelStrings(false));
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("container");
		container.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getVote().getResult() == Vote.Result.APPROVE)
					return "panel-info";
				else
					return "panel-warning";
			}
			
		}));
		
		container.add(new GitPersonLink("user", new LoadableDetachableModel<GitPerson>() {

			@Override
			protected GitPerson load() {
				GitPerson person = new GitPerson(getVote().getVoter().getName(), 
						getVote().getVoter().getEmail());
				return person;
			}
			
		}, GitPersonLink.Mode.AVATAR_AND_NAME));
		
		if (getVote().getResult() == Vote.Result.APPROVE) 
			container.add(new Label("vote", "approved"));
		else
			container.add(new Label("vote", "disapproved"));
		
		container.add(new Label("date", DateUtils.formatAge(getVote().getDate())));
		
		container.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				comment = getVote().getComment();
				
				Fragment fragment = new Fragment("body", "editFrag", VoteActivityPanel.this);
				
				final TextArea<String> commentArea = new TextArea<String>("comment", new IModel<String>() {

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
						Gitop.getInstance(VoteManager.class).save(vote);

						Fragment fragment = renderForView();
						container.replace(fragment);
						target.add(container);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						container.replace(fragment);
						target.add(container);
					}
					
				});
				
				container.replace(fragment);
				
				target.add(container);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(Gitop.getInstance(AuthorizationManager.class)
						.canModify(getVote()));
			}

		});
		
		container.add(renderForView());

		container.setOutputMarkupId(true);
		
		add(container);
	}

	private Vote getVote() {
		return (Vote) getDefaultModelObject();
	}
	
}
