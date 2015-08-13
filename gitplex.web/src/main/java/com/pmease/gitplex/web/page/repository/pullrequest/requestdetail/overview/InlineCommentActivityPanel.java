package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.lang.diff.DiffLine;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.comment.CommentCollapsing;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.pullrequest.PullRequestChanged;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare.RequestComparePage;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class InlineCommentActivityPanel extends Panel {

	private final IModel<PullRequestComment> commentModel;
	
	public InlineCommentActivityPanel(String id, IModel<PullRequestComment> commentModel) {
		super(id);
		
		this.commentModel = commentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequestComment comment = commentModel.getObject();
		GitPlex.getInstance(PullRequestCommentManager.class).updateInline(comment);
		add(new UserLink("name", new UserModel(comment.getUser()), AvatarMode.NAME));
		add(new Label("age", DateUtils.formatAge(comment.getDate())));
		
		add(new Label("file", comment.getBlobIdent().path) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					AjaxRequestTarget target = pullRequestChanged.getTarget();
					target.add(this);
				}
			}
			
		}.setOutputMarkupId(true));
		
		add(new Link<Void>("compareView") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext() != null);
			}

			@Override
			public void onClick() {
				PullRequestComment comment = commentModel.getObject();
				setResponsePage(RequestComparePage.class, 
						RequestComparePage.paramsOf(comment.getRequest(), comment, null, null, null));
			}
			
		});
		
		WebMarkupContainer contextContainer = new WebMarkupContainer("context") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext() != null);
			}
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					AjaxRequestTarget target = pullRequestChanged.getTarget();
					target.add(this);
				}
			}

		};
		contextContainer.setOutputMarkupPlaceholderTag(true);
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
				return commentModel.getObject().getContext().getDiffLines();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<DiffLine> item) {
				DiffLine diffLine = item.getModelObject();

				if (diffLine.getOperation() == Operation.INSERT) {
					item.add(AttributeAppender.append("class", " new"));
					item.add(new Label("oldLineNo"));
					item.add(new Label("newLineNo", diffLine.getNewLineNo() + 1));
					item.add(new Label("diffMark", "+"));
				} else if (diffLine.getOperation() == Operation.DELETE) {
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
				item.add(new ListView<CmToken>("tokens", diffLine.getTokens()) {

					@Override
					protected void populateItem(ListItem<CmToken> item) {
						CmToken token = item.getModelObject();
						Label label;
						if (token.getText().equals("\r"))
							label = new Label("token", " ");
						else
							label = new Label("token", token.getText());
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
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					AjaxRequestTarget target = pullRequestChanged.getTarget();
					target.add(this);
				}
			}

		}.setOutputMarkupPlaceholderTag(true));
		
		/*
		 * Make comment panel independent of context in order not to reset comment content while editing
		 * when we update context of the inline comment
		 */
		add(new CommentPanel("comment", commentModel) {

			@Override
			protected Component newAdditionalCommentOperations(String id, IModel<Comment> comment) {
				Fragment fragment = new Fragment(id, "operationsFrag", InlineCommentActivityPanel.this);
				fragment.add(new AjaxLink<Void>("collapse") {

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

				});
				fragment.setRenderBodyOnly(true);
				return fragment;
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					AjaxRequestTarget target = pullRequestChanged.getTarget();
					target.prependJavaScript(String.format("$('body').append($('#%s'));", getMarkupId(true)));
					target.appendJavaScript(getAppendScript());
				}
			}
			
			private String getAppendScript() {
				return String.format(""
						+ "var $beforeComment = $('#%s .before-comment');"
						+ "if ($beforeComment.hasClass('line')) {"
						+ "  var $tr = $('<tr class=\"line comments\"><td colspan=\"3\"></td></tr>').insertAfter($beforeComment);"
						+ "  $tr.children().append($('#%s'));"
						+ "} else {"
						+ "  $('#%s').insertAfter($beforeComment);"
						+ "}", 
						InlineCommentActivityPanel.this.getMarkupId(true), getMarkupId(true), getMarkupId(true));
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(OnDomReadyHeaderItem.forScript(getAppendScript()));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
