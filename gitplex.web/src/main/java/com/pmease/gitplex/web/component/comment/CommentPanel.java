package com.pmease.gitplex.web.component.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.comment.event.CommentReplied;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.markdown.MarkdownPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class CommentPanel extends Panel {

	private RepeatingView repliesView;
	
	public CommentPanel(String id, IModel<? extends Comment> commentModel) {
		super(id, commentModel);
	}

	private Comment getComment() {
		return (Comment) getDefaultModelObject();
	}
	
	private Fragment renderForView(String content) {
		Fragment fragment = new Fragment("content", "viewFrag", this);
		fragment.add(new MarkdownPanel("comment", Model.of(content)));
		fragment.setOutputMarkupId(true);
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserLink("user", new UserModel(getComment().getUser()), AvatarMode.NAME));
		add(new AgeLabel("age", Model.of(getComment().getDate())));

		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "editFrag", CommentPanel.this);

				Form<?> form = new Form<Void>("form");
				fragment.add(form);
				final CommentInput input = new CommentInput("input", Model.of(getComment().getContent()));
				input.setRequired(true);
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						Comment comment = getComment();
						comment.saveContent(input.getModelObject());

						Fragment fragment = renderForView(comment.getContent());
						CommentPanel.this.replace(fragment);
						target.add(fragment);
					}
					
					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView(getComment().getContent());
						CommentPanel.this.replace(fragment);
						target.add(fragment);
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				CommentPanel.this.replace(fragment);
				target.add(fragment);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(getComment()));
			}

		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getComment().delete();
				send(CommentPanel.this, Broadcast.BUBBLE, new CommentRemoved(target, getComment()));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(getComment()));
			}

		}.add(new ConfirmBehavior("Deleting this comment will also delete all its replies. Do you really want to continue?")));
		
		final AjaxLink<Void> resolveLink;
		add(resolveLink = new AjaxLink<Void>("resolve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getComment().resolve(!getComment().isResolved());
				if (getComment().isResolved())
					send(CommentPanel.this, Broadcast.BUBBLE, new CommentCollapsing(target, getComment()));
				if (findPage() != null) // only render this checkbox if the whole comment panel is not replaced
					target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(getComment()));
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (getComment().isResolved()) {
					tag.put("class", "fa fa-checkbox-checked resolve");
					tag.put("title", "Mark this comment as unresolved");
				} else {
					tag.put("class", "fa fa-checkbox-unchecked resolve");
					tag.put("title", "Mark this comment as resolved");
				}
			}
			
		});
		resolveLink.setOutputMarkupId(true);
		
		add(new WebMarkupContainer("resolved") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				resolveLink.configure();
				setVisible(!resolveLink.isVisible());
			}
			
		});
		
		add(renderForView(getComment().getContent()));

		add(repliesView = newRepliesView());
		
		add(newAddReplyRow());
	}
	
	private Component newAddReplyRow() {
		WebMarkupContainer row = new WebMarkupContainer("addReply");
		row.setOutputMarkupId(true);
		
		Fragment fragment = new Fragment("content", "addFrag", this);
		fragment.add(new AjaxLink<Void>("addReply") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				final WebMarkupContainer row = new WebMarkupContainer("addReply");
				
				row.setOutputMarkupId(true);
					
				Fragment fragment = new Fragment("content", "editFrag", CommentPanel.this);
				row.add(fragment);
				
				Form<?> form = new Form<Void>("form");
				fragment.add(form);
				final CommentInput input = new CommentInput("input", Model.of(""));
				input.setRequired(true);
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						Component row = newAddReplyRow();
						CommentPanel.this.replace(row);
						target.add(row);
						
						CommentReply newReply = getComment().addReply(input.getModelObject());
						send(CommentPanel.this, Broadcast.BUBBLE, new CommentReplied(target, newReply));
						
						Component newReplyRow = newReplyRow(repliesView.newChildId(), newReply); 
						repliesView.add(newReplyRow);
						String script = String.format("$(\"<tr id='%s' class='reply'></tr>\").insertBefore('#%s');", 
								newReplyRow.getMarkupId(), row.getMarkupId());
						target.prependJavaScript(script);
						target.add(newReplyRow);
					}
					
					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Component row = newAddReplyRow();
						CommentPanel.this.replace(row);
						target.add(row);
					}
					
				});

				CommentPanel.this.replace(row);
				target.add(row);
			}
			
		});
		row.add(fragment);
		
		return row;
	}
	
	private RepeatingView newRepliesView() {
		RepeatingView repliesView = new RepeatingView("replies");

		List<CommentReply> replies = new ArrayList<>(getComment().getReplies());
		Collections.sort(replies, new Comparator<CommentReply>() {

			@Override
			public int compare(CommentReply reply1, CommentReply reply2) {
				return reply1.getDate().compareTo(reply2.getDate());
			}
			
		});
		for (CommentReply reply: replies)
			repliesView.add(newReplyRow(repliesView.newChildId(), reply));
		
		return repliesView;
	}

	@Override
	protected void onBeforeRender() {
		replace(repliesView = newRepliesView());
		
		super.onBeforeRender();
	}

	private Component newReplyRow(String id, final CommentReply reply) {
		final WebMarkupContainer row = new WebMarkupContainer(id);
		row.setOutputMarkupId(true);
		
		row.add(new UserLink("avatar", new UserModel(reply.getUser()), AvatarMode.AVATAR));
		row.add(new UserLink("user", new UserModel(reply.getUser()), AvatarMode.NAME));
		row.add(new AgeLabel("age", Model.of(reply.getDate())));

		row.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "editFrag", CommentPanel.this);

				Form<?> form = new Form<Void>("form");
				fragment.add(form);
				final CommentInput input = new CommentInput("input", Model.of(reply.getContent()));
				input.setRequired(true);
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						reply.saveContent(input.getModelObject());

						Fragment fragment = renderForView(reply.getContent());
						row.replace(fragment);
						target.add(fragment);
					}
					
					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView(reply.getContent());
						row.replace(fragment);
						target.add(fragment);
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				row.replace(fragment);
				target.add(fragment);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(getComment()));
			}

		});
		
		row.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				reply.delete();
				row.remove();
				target.appendJavaScript(String.format("$('#%s').remove();", row.getMarkupId()));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(getComment()));
			}

		}.add(new ConfirmBehavior("Do you really want to delete this reply?")));
		
		row.add(renderForView(reply.getContent()));
		
		return row;
	}

}
