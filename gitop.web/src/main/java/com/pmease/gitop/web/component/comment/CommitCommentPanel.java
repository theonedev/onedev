package com.pmease.gitop.web.component.comment;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.UserAvatarLink;
import com.pmease.gitop.web.component.wiki.WikiTextPanel;
import com.pmease.gitop.web.model.UserModel;

@SuppressWarnings("serial")
public class CommitCommentPanel extends Panel {

	public CommitCommentPanel(String id, IModel<CommitComment> model) {
		super(id, model);
		
		this.setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserAvatarLink("author", new UserModel(getCommitComment().getAuthor())));
		add(new WebMarkupContainer("authorType") {
			
		});
		
		add(new AgeLabel("age", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommitComment().getCreatedDate();
			}
			
		}).setOutputMarkupId(true));
		
		add(new AjaxLink<Void>("editlink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				User current = Gitop.getInstance(UserManager.class).getCurrent();
				
				if (current == null) {
					setVisibilityAllowed(false);
				} else {
					setVisibilityAllowed(Objects.equal(current, getCommitComment().getAuthor()));
				}
			}
			
		});
		
		add(new AjaxConfirmLink<Void>("removelink", 
				Model.of("Are you sure you want to delete this comment?")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				User current = Gitop.getInstance(UserManager.class).getCurrent();
				if (current == null) {
					setVisibilityAllowed(false);
				} else {
					setVisibilityAllowed(Objects.equal(current, getCommitComment().getAuthor())
							|| current.isAdmin());
				}
			}
		});
		
		add(newCommentLabel());
	}
	
	private Component newCommentLabel() {
		return new WikiTextPanel("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String str = getCommitComment().getContent();
				if (Strings.isNullOrEmpty(str)) {
					return "Nothing to be shown";
				} else {
					return str;
				}
			}
			
		}).setOutputMarkupId(true);
	}
	
	protected void onEdit(AjaxRequestTarget target) {
		Component c = new CommitCommentEditor("content", Model.of(getCommitComment().getContent())) {

			@Override
			protected Component createSubmitButtons(String id, Form<?> form) {
				Fragment frag = new Fragment(id, "submitfrag", CommitCommentPanel.this);
				frag.add(new AjaxLink<Void>("btnCancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						updateCommentLabel(target);
					}
				});
				
				frag.add(new AjaxButton("btnUpdate", form) {
					
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						String comment = getCommentText();
						if (Strings.isNullOrEmpty(comment)) {
							form.error("Comment can not be empty");
							target.add(form);
							return;
						}
						
						CommitComment cc = getCommitComment();
						cc.setUpdatedDate(new Date());
						cc.setContent(comment);
						
						Gitop.getInstance(CommitCommentManager.class).save(cc);
						updateCommentLabel(target);
//						updateAgeLabel(target);
					}
				});
				
				return frag;
			}
			
			private void updateCommentLabel(AjaxRequestTarget target) {
				Component label = newCommentLabel();
				CommitCommentPanel.this.addOrReplace(label);
				target.add(label);
			}
		};
		
		c.setOutputMarkupId(true);
		addOrReplace(c);
		target.add(c);
	}
	
	protected void onDelete(AjaxRequestTarget target) {
		CommitComment comment = getCommitComment();
		Gitop.getInstance(CommitCommentManager.class).delete(comment);
		send(getPage(), Broadcast.BREADTH, new CommitCommentRemoved(target, comment));
	}
	
	private CommitComment getCommitComment() {
		return (CommitComment) getDefaultModelObject();
	}
}
