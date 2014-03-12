package com.pmease.gitop.web.page.project.pullrequest.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.PullRequestCommentManager;
import com.pmease.gitop.model.PullRequestComment;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class CommentActivityPanel extends Panel {

	private String content;
	
	public CommentActivityPanel(String id, IModel<PullRequestComment> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("body", "viewFrag", this);

		content = getComment().getContent();
		if (StringUtils.isNotBlank(content))
			fragment.add(new MultiLineLabel("comment", content));
		else
			fragment.add(new Label("comment", "<i>No description</i>").setEscapeModelStrings(false));
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequestComment comment = getComment();
		User user = comment.getUser();
		if (user != null) {
			GitPerson person = new GitPerson(user.getName(), user.getEmail());
			add(new GitPersonLink("user", Model.of(person), GitPersonLink.Mode.AVATAR_AND_NAME));
		} else {
			add(new Label("<i>Unknown</i>").setEscapeModelStrings(false));
		}
		
		add(new Label("date", DateUtils.formatAge(comment.getDate())));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				content = getComment().getContent();
				
				Fragment fragment = new Fragment("body", "editFrag", CommentActivityPanel.this);
				
				final TextArea<String> commentArea = new TextArea<String>("comment", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return content;
					}

					@Override
					public void setObject(String object) {
						content = object;
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
						PullRequestComment comment = getComment();
						comment.setContent(content);
						Gitop.getInstance(PullRequestCommentManager.class).save(comment);

						Fragment fragment = renderForView();
						CommentActivityPanel.this.replace(fragment);
						target.add(CommentActivityPanel.this);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

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
				
				setVisible(Gitop.getInstance(AuthorizationManager.class)
						.canModify(getComment().getRequest()));
			}

		});
		
		add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				Gitop.getInstance(PullRequestCommentManager.class).delete(getComment());
			}
			
		}.add(new ConfirmBehavior("Do you really want to delete this comment?")));
		
		add(renderForView());

		setOutputMarkupId(true);
	}

	private PullRequestComment getComment() {
		return (PullRequestComment) getDefaultModelObject();
	}
	
}
