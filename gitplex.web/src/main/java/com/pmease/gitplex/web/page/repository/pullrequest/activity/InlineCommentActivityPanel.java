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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.Token;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
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
		
		add(new Link<Void>("compareView") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext() != null);
			}

			@Override
			public void onClick() {
				setResponsePage(RequestComparePage.class, RequestComparePage.paramsOf(commentModel.getObject()));
			}
			
		});
		
		WebMarkupContainer contextContainer = new WebMarkupContainer("context") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext() != null);
			}
			
		};
		add(contextContainer);
		
		contextContainer.add(new WebMarkupContainer("aboveOmitted") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext().isAboveOmitted());
			}
			
		});
		contextContainer.add(new WebMarkupContainer("belowOmitted") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext().isBelowOmitted());
			}
			
		});
		contextContainer.add(new ListView<DiffLine>("lines", new AbstractReadOnlyModel<List<DiffLine>>() {

			@Override
			public List<DiffLine> getObject() {
				return commentModel.getObject().getContext().getDiffs();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<DiffLine> item) {
				DiffLine diffLine = item.getModelObject();

				if (diffLine.getAction() == ADD) {
					item.add(AttributeAppender.append("class", " new"));
					item.add(new Label("oldLineNo"));
					item.add(new Label("newLineNo", diffLine.getNewLineNo() + 1));
					item.add(new Label("diffMark", "+"));
				} else if (diffLine.getAction() == DELETE) {
					item.add(AttributeAppender.append("class", " old"));
					item.add(new Label("oldLineNo", diffLine.getOldLineNo() + 1));
					item.add(new Label("newLineNo"));
					item.add(new Label("diffMark", "-"));
				} else {
					item.add(AttributeAppender.append("class", " equal"));
					item.add(new Label("oldLineNo", diffLine.getOldLineNo()+1));
					item.add(new Label("newLineNo", diffLine.getNewLineNo()+1));
					item.add(new Label("diffMark", " "));
				}
				item.add(new ListView<Token>("partials", diffLine.getTokens()) {

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

				if (item.getIndex() == commentModel.getObject().getContext().getLine())
					item.add(AttributeAppender.append("class", " before-comment"));
			}
			
		});
		add(new WebMarkupContainer("noContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext() == null);
			}
			
		});
		
		add(new CommentPanel("comment", commentModel) {

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

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				String script = String.format(""
						+ "var $beforeComment = $('#%s .before-comment');"
						+ "if ($beforeComment.hasClass('line')) {"
						+ "  var $tr = $('<tr class=\"line comments\"><td colspan=\"3\"></td></tr>').insertAfter($beforeComment);"
						+ "  $tr.children().append($('#%s'));"
						+ "} else {"
						+ "  $('#%s').insertAfter($beforeComment);"
						+ "}", 
						InlineCommentActivityPanel.this.getMarkupId(true), getMarkupId(true), getMarkupId(true));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
