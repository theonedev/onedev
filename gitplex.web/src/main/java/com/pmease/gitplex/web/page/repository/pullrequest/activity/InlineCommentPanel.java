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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.Partial;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;

@SuppressWarnings("serial")
public class InlineCommentPanel extends Panel {

	private final IModel<? extends InlineComment> commentModel;
	
	public InlineCommentPanel(String id, IModel<? extends InlineComment> commentModel) {
		super(id);
		
		this.commentModel = commentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("aboveOmitted") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext().isAboveOmitted());
			}
			
		});
		add(new WebMarkupContainer("belowOmitted") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentModel.getObject().getContext().isBelowOmitted());
			}
			
		});
		add(new ListView<DiffLine>("lines", new AbstractReadOnlyModel<List<DiffLine>>() {

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
				contentRow.add(new ListView<Partial>("partials", diffLine.getPartials()) {

					@Override
					protected void populateItem(ListItem<Partial> item) {
						Partial partial = item.getModelObject();
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
									send(InlineCommentPanel.this, Broadcast.BUBBLE, 
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
	}

	@Override
	protected void onDetach() {
		commentModel.detach();
		super.onDetach();
	}

}
