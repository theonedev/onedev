package com.pmease.gitop.web.component.comment;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
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
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.component.comment.event.CommitCommentRemoved;
import com.pmease.gitop.web.component.comment.event.CommitCommentUpdated;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.UserAvatarLink;
import com.pmease.gitop.web.component.wiki.WikiTextPanel;
import com.pmease.gitop.web.model.UserModel;

@SuppressWarnings("serial")
public class CommitCommentPanel extends Panel {

	private final IModel<Repository> repositoryModel;
	
	public CommitCommentPanel(String id,
			IModel<Repository> repositoryModel,
			IModel<CommitComment> model) {
		super(id, model);
	
		this.repositoryModel = repositoryModel;
		
		this.setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newCommentContent());
		add(createCommentHead("head"));
	}
	
	protected Component createCommentHead(String id) {
		Fragment frag = new Fragment(id, "headfrag", this);
		
		frag.add(new UserAvatarLink("author", new UserModel(getCommitComment().getAuthor())));
//		frag.add(new WebMarkupContainer("authorType") {
//			@Override
//			protected void onConfigure() {
//				super.onConfigure();
//				
//				User author = getCommitComment().getAuthor();
//				setVisibilityAllowed(Objects.equal(author, projectModel.getObject().getOwner()));
//			}
//		});
		
		frag.add(new AgeLabel("age", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommitComment().getCreatedDate();
			}
			
		}).setOutputMarkupId(true));
		
		frag.add(newEditLink("editlink"));
		frag.add(newRemoveLink("removelink"));
		return frag;
	}
	
	protected Component newEditLink(String id) {
		return new AjaxLink<Void>(id) {

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
			
		};
	}
	
	protected Component newRemoveLink(String id) {
		return new AjaxConfirmLink<Void>(id, 
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
		};
	}
	
	private Component newCommentContent() {
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
			
			private void updateCommentLabel(AjaxRequestTarget target) {
				Component label = newCommentContent();
				CommitCommentPanel.this.addOrReplace(label);
				target.add(label);
			}

			@Override
			protected void onCancel(AjaxRequestTarget target, Form<?> form) {
				updateCommentLabel(target);
			}

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
				
				send(getPage(), Broadcast.DEPTH, new CommitCommentUpdated(target, cc));
				updateCommentLabel(target);
			}
			
			@Override
			protected IModel<String> getCancelButtonLabel() {
				return Model.of("Cancel");
			}
			
			@Override
			protected IModel<String> getSubmitButtonLabel() {
				return Model.of("Update comment");
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
	
	protected CommitComment getCommitComment() {
		return (CommitComment) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (repositoryModel != null) {
			repositoryModel.detach();
		}
	}
}
