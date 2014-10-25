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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.component.markdown.MarkdownInput;
import com.pmease.commons.wicket.component.markdown.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class CommentPanel extends Panel {

	private RepeatingView repliesView;
	
	private IModel<List<CommentReply>> repliesModel = new LoadableDetachableModel<List<CommentReply>>() {

		@Override
		protected List<CommentReply> load() {
			List<CommentReply> replies = new ArrayList<>(getComment().getReplies());
			Collections.sort(replies, new Comparator<CommentReply>() {

				@Override
				public int compare(CommentReply reply1, CommentReply reply2) {
					return reply1.getDate().compareTo(reply2.getDate());
				}
				
			});
			return replies;
		}
		
	};
	
	public CommentPanel(String id, IModel<? extends Comment> commentModel) {
		super(id, commentModel);
	}

	private Comment getComment() {
		return (Comment) getDefaultModelObject();
	}
	
	private Fragment renderForView(String content) {
		Fragment fragment = new Fragment("content", "viewFrag", this);
		fragment.add(new MarkdownViewer("comment", Model.of(content)));
		fragment.setOutputMarkupId(true);
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WebMarkupContainer summary = new WebMarkupContainer("summary"); 
		summary.setOutputMarkupId(true);
		add(summary);
		
		summary.add(new UserLink("user", new UserModel(getComment().getUser()), AvatarMode.NAME));
		summary.add(new AgeLabel("age", Model.of(getComment().getDate())));

		summary.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "editFrag", CommentPanel.this);

				Form<?> form = new Form<Void>("form");
				fragment.add(form);
				final MarkdownInput input = new MarkdownInput("input", Model.of(getComment().getContent()));
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
		
		summary.add(new AjaxLink<Void>("delete") {

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
		summary.add(resolveLink = new AjaxLink<Void>("resolve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getComment().resolve(!getComment().isResolved());
				if (getComment().isResolved())
					send(CommentPanel.this, Broadcast.BUBBLE, new CommentCollapsing(target, getComment()));
				if (findPage() != null) // only render this checkbox if the whole comment panel is not replaced
					target.add(summary);
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
		
		summary.add(new WebMarkupContainer("resolved") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				resolveLink.configure();
				setVisible(!resolveLink.isVisible());
			}
			
		});
		
		summary.add(newAdditionalCommentActions("additionalActions", new AbstractReadOnlyModel<Comment>() {

			@Override
			public Comment getObject() {
				return getComment();
			}
			
		}));
		
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
				final MarkdownInput input = new MarkdownInput("input", Model.of(""));
				input.setRequired(true);
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						Component row = newAddReplyRow();
						CommentPanel.this.replace(row);
						target.add(row);
						
						getComment().addReply(input.getModelObject());
						Component newReplyRow = newReplyRow(repliesView.newChildId(), repliesModel.getObject().size()-1); 
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

		for (int i=0; i<repliesModel.getObject().size(); i++)
			repliesView.add(newReplyRow(repliesView.newChildId(), i));
		
		return repliesView;
	}

	@Override
	protected void onBeforeRender() {
		replace(repliesView = newRepliesView());
		
		super.onBeforeRender();
	}

	private Component newReplyRow(String id, int index) {
		final WebMarkupContainer row = new WebMarkupContainer(id, Model.of(index));
		row.setOutputMarkupId(true);
		
		CommentReply reply = repliesModel.getObject().get(index);
		row.add(new UserLink("avatar", new UserModel(reply.getUser()), AvatarMode.AVATAR));
		row.add(new UserLink("user", new UserModel(reply.getUser()), AvatarMode.NAME));
		row.add(new AgeLabel("age", Model.of(reply.getDate())));

		row.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("content", "editFrag", CommentPanel.this);

				Form<?> form = new Form<Void>("form");
				fragment.add(form);
				CommentReply reply = repliesModel.getObject().get((int)row.getDefaultModelObject());
				final MarkdownInput input = new MarkdownInput("input", Model.of(reply.getContent()));
				input.setRequired(true);
				form.add(input);
				
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						CommentReply reply = repliesModel.getObject().get((int)row.getDefaultModelObject());
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
						CommentReply reply = repliesModel.getObject().get((int)row.getDefaultModelObject());
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
				int index = (int)row.getDefaultModelObject();
				CommentReply reply = repliesModel.getObject().get(index);
				repliesModel.getObject().remove(index);
				reply.delete();
				row.remove();
				for (int i=index; i<repliesView.size(); i++) {
					Component child = repliesView.get(i); 
					child.setDefaultModelObject((int)child.getDefaultModelObject()-1);
				}
				
				target.appendJavaScript(String.format("$('#%s').remove();", row.getMarkupId()));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(getComment()));
			}

		}.add(new ConfirmBehavior("Do you really want to delete this reply?")));
		
		row.add(newAdditionalReplyActions("additionalActions", new AbstractReadOnlyModel<CommentReply>() {

			@Override
			public CommentReply getObject() {
				return repliesModel.getObject().get((int)row.getDefaultModelObject());
			}
			
		}));
		
		row.add(renderForView(reply.getContent()));
		
		return row;
	}

	protected Component newAdditionalCommentActions(String id, IModel<Comment> comment) {
		return new WebMarkupContainer(id);
	}
	
	protected Component newAdditionalReplyActions(String id, IModel<CommentReply> reply) {
		return new WebMarkupContainer(id);
	}

	@Override
	protected void onDetach() {
		repliesModel.detach();
		
		super.onDetach();
	}
	
}
