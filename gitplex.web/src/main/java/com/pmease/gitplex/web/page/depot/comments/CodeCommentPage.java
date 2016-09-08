package com.pmease.gitplex.web.page.depot.comments;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;

@SuppressWarnings("serial")
public class CodeCommentPage extends DepotPage {

	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_REQUEST = "request";

	public CodeCommentPage(PageParameters params) {
		super(params);

		Long requestId = params.get(PARAM_REQUEST).toOptionalLong();
		Long commentId = params.get(PARAM_COMMENT).toLong();
		CodeComment comment = GitPlex.getInstance(CodeCommentManager.class).load(commentId);
		for (CodeCommentRelation relation: comment.getRelations()) {
			if (relation.getRequest().getId().equals(requestId)) {
				params = RequestChangesPage.paramsOf(relation.getRequest(), comment);
				throw new RestartResponseException(RequestChangesPage.class, params);
			}
		} 
		CompareContext compareContext = comment.getLastCompareContext();
		if (!compareContext.getCompareCommit().equals(comment.getCommentPos().getCommit())) {
			throw new RestartResponseException(
					RevisionComparePage.class, 
					RevisionComparePage.paramsOf(getDepot(), comment));
		} else {
			throw new RestartResponseException(
					DepotFilePage.class, 
					DepotFilePage.paramsOf(getDepot(), comment));
		}
	}

}
