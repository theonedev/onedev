package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import static com.pmease.commons.util.diff.DiffLine.Action.ADD;
import static com.pmease.commons.util.diff.DiffLine.Action.DELETE;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.Token;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.manager.PullRequestInlineCommentManager;
import com.pmease.gitplex.core.model.PullRequestInlineComment;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestComparePage;

@SuppressWarnings("serial")
public class InlineCommentActivityPanel extends Panel {

	private final IModel<PullRequestInlineComment> commentModel;
	
	public InlineCommentActivityPanel(String id, IModel<PullRequestInlineComment> commentModel) {
		super(id);
		
		this.commentModel = commentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequestInlineComment comment = commentModel.getObject();
		PullRequestInlineComment inlineComment = (PullRequestInlineComment) comment;
		GitPlex.getInstance(PullRequestInlineCommentManager.class).update(inlineComment);
		add(new UserLink("name", new UserModel(comment.getUser()), AvatarMode.NAME));
		add(new AgeLabel("age", Model.of(comment.getDate())));
		
		add(new Label("file", inlineComment.getBlobInfo().getPath()));
		
		PageParameters params = RequestComparePage.paramsOf(comment);
		add(new BookmarkablePageLink<Void>("compareView", RequestComparePage.class, params));
		
		if (commentModel.getObject().getContext() != null) {
			Fragment fragment = new Fragment("context", "withContextFrag", this);
			fragment.add(new WebMarkupContainer("aboveOmitted") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(commentModel.getObject().getContext().isAboveOmitted());
				}
				
			});
			fragment.add(new WebMarkupContainer("belowOmitted") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(commentModel.getObject().getContext().isBelowOmitted());
				}
				
			});
			fragment.add(new ListView<DiffLine>("lines", new AbstractReadOnlyModel<List<DiffLine>>() {

				@Override
				public List<DiffLine> getObject() {
					return commentModel.getObject().getContext().getDiffs();
				}
				
			}) {

				@Override
				protected void populateItem(final ListItem<DiffLine> item) {
					DiffLine diffLine = item.getModelObject();

					WebMarkupContainer contentRow = new WebMarkupContainer("contentRow");
					item.add(contentRow);
					if (diffLine.getAction() == ADD) {
						contentRow.add(AttributeAppender.append("class", " new"));
						contentRow.add(new Label("oldLineNo"));
						contentRow.add(new Label("newLineNo", diffLine.getNewLineNo() + 1));
						contentRow.add(new Label("diffMark", "+"));
					} else if (diffLine.getAction() == DELETE) {
						contentRow.add(AttributeAppender.append("class", " old"));
						contentRow.add(new Label("oldLineNo", diffLine.getOldLineNo() + 1));
						contentRow.add(new Label("newLineNo"));
						contentRow.add(new Label("diffMark", "-"));
					} else {
						contentRow.add(AttributeAppender.append("class", " equal"));
						contentRow.add(new Label("oldLineNo", diffLine.getOldLineNo()+1));
						contentRow.add(new Label("newLineNo", diffLine.getNewLineNo()+1));
						contentRow.add(new Label("diffMark", " "));
					}
					contentRow.add(new ListView<Token>("partials", diffLine.getTokens()) {

						@Override
						protected void populateItem(ListItem<Token> item) {
							Token partial = item.getModelObject();
							Label label;
							if (partial.getContent().equals("\r"))
								label = new Label("partial", " ");
							else
								label = new Label("partial", partial.getContent());
							if (partial.isEmphasized())
								label.add(AttributeAppender.append("class", "emphasize"));
							item.add(label);
						}
						
					});

					InlineComment comment = commentModel.getObject();
					if (item.getIndex() == comment.getContext().getCommentLine()) {
						item.add(new CommentPanel("comment", commentModel) {

							@Override
							protected Component newAdditionalCommentActions(String id, IModel<Comment> comment) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										send(InlineCommentActivityPanel.this, Broadcast.BUBBLE, 
												new CommentCollapsing(target, commentModel.getObject()));
									}

									@Override
									protected void onConfigure() {
										super.onConfigure();
										
										setVisible(commentModel.getObject().isResolved());
									}

									@Override
									protected void onComponentTag(ComponentTag tag) {
										super.onComponentTag(tag);
										tag.put("class", "fa fa-collapse");
										tag.put("title", "Collapse this comment");
										tag.put("style", "cursor:pointer;");
									}
									
								};
								
							}
							
						});
					} else { 
						item.add(new WebMarkupContainer("comment").setVisible(false));
					}
				}
				
			});
			add(fragment);
		} else {
			Fragment fragment = new Fragment("context", "withoutContextFrag", this);
			fragment.add(AttributeAppender.append("class", " panel-body"));
			fragment.add(new CommentPanel("comment", commentModel) {

				@Override
				protected Component newAdditionalCommentActions(String id, IModel<Comment> comment) {
					return new AjaxLink<Void>(id) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							send(InlineCommentActivityPanel.this, Broadcast.BUBBLE, 
									new CommentCollapsing(target, commentModel.getObject()));
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							
							setVisible(commentModel.getObject().isResolved());
						}

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.put("class", "fa fa-collapse");
							tag.put("title", "Collapse this comment");
							tag.put("style", "cursor:pointer;");
						}
						
					};
					
				}
				
			});
			add(fragment);
		}
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
