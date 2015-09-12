package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.LabelLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.web.page.repository.file.HistoryState;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare.RequestComparePage;

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
			setResponsePage(RepoFilePage.class, RepoFilePage.paramsOf(comment.getRepository(), state));
		} else { 
			setResponsePage(RequestComparePage.class, RequestComparePage.paramsOf(comment));
		}
	}

}
