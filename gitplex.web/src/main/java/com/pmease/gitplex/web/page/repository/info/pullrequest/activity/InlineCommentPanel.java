package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import static com.pmease.commons.util.diff.DiffLine.Action.ADD;
import static com.pmease.commons.util.diff.DiffLine.Action.DELETE;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.Partial;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.web.component.comment.CommentPanel;

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
					contentRow.add(new Label("newLineNo", "+ " + (diffLine.getNewLineNo() + 1)));
				} else if (diffLine.getAction() == DELETE) {
					contentRow.add(AttributeAppender.append("class", " old"));
					contentRow.add(new Label("oldLineNo", "- " + (diffLine.getOldLineNo() + 1)));
					contentRow.add(new Label("newLineNo"));
				} else {
					contentRow.add(AttributeAppender.append("class", " equal"));
					contentRow.add(new Label("oldLineNo", "  " + (diffLine.getOldLineNo()+1)));
					contentRow.add(new Label("newLineNo", "  " + (diffLine.getNewLineNo()+1)));
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
				if (comment.getCommit().equals(comment.getOldCommit()) && comment.getLine() == diffLine.getOldLineNo()
						|| comment.getCommit().equals(comment.getNewCommit()) && comment.getLine() == diffLine.getNewLineNo()) {
					item.add(new CommentPanel("comment", commentModel));
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
