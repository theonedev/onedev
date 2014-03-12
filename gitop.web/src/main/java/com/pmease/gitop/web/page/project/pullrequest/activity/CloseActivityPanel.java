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
import org.apache.wicket.model.Model;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.CloseInfo;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class CloseActivityPanel extends Panel {

	private WebMarkupContainer container;
	
	private String comment;
	
	public CloseActivityPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("body", "viewFrag", this);

		comment = getPullRequest().getCloseInfo().getComment();
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
				if (getPullRequest().getCloseInfo().getCloseStatus() == CloseInfo.Status.INTEGRATED)
					return "panel-success";
				else
					return "panel-danger";
			}
			
		}));
		
		PullRequest request = getPullRequest();
		User closedBy = request.getCloseInfo().getClosedBy();
		if (closedBy != null) {
			GitPerson person = new GitPerson(closedBy.getName(), closedBy.getEmail());
			container.add(new GitPersonLink("user", Model.of(person), GitPersonLink.Mode.NAME_AND_AVATAR));
		} else {
			container.add(new Label("<i>Unknown</i>").setEscapeModelStrings(false));
		}
		
		if (request.getCloseInfo().getCloseStatus() == CloseInfo.Status.INTEGRATED)
			container.add(new Label("action", "integrated"));
		else
			container.add(new Label("action", "discarded"));
		
		container.add(new Label("date", DateUtils.formatAge(request.getCloseInfo().getCloseDate())));
		
		container.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				comment = getPullRequest().getCloseInfo().getComment();
				
				Fragment fragment = new Fragment("body", "editFrag", CloseActivityPanel.this);
				
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
						PullRequest request = getPullRequest();
						request.getCloseInfo().setComment(comment);
						Gitop.getInstance(PullRequestManager.class).save(request);

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
						.canModify(getPullRequest()));
			}

		});
		
		container.add(renderForView());

		container.setOutputMarkupId(true);
		
		add(container);
	}

	private PullRequest getPullRequest() {
		PullRequest request = (PullRequest) getDefaultModelObject();
		Preconditions.checkNotNull(request.getCloseInfo());
		return request;
	}
	
}
