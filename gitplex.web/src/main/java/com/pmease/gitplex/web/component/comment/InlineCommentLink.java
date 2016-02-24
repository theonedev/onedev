package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.LabelLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare.RequestComparePage;

@SuppressWarnings("serial")
public class InlineCommentLink extends LabelLink<Comment> {

	public InlineCommentLink(String id, IModel<Comment> commentModel, IModel<String> labelModel) {
		super(id, commentModel, labelModel);
	}

	@Override
	public void onClick() {
		Comment comment = getModelObject();
		GitPlex.getInstance(CommentManager.class).updateInlineInfo(comment);
		if (comment.getBlobIdent().equals(comment.getCompareWith())) {
			HistoryState state = new HistoryState();
			state.commentId = comment.getId();
			setResponsePage(DepotFilePage.class, DepotFilePage.paramsOf(comment.getDepot(), state));
		} else { 
			setResponsePage(RequestComparePage.class, RequestComparePage.paramsOf(comment));
		}
	}

}
