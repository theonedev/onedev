package com.gitplex.server.web.page.depot.comments;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.support.CompareContext;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.compare.RevisionComparePage;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;

@SuppressWarnings("serial")
public class CodeCommentPage extends DepotPage {

	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_REQUEST = "request";
	
	private static final String PARAM_ANCHOR = "anchor";

	public CodeCommentPage(PageParameters params) {
		super(params);

		Long requestId = params.get(PARAM_REQUEST).toOptionalLong();
		Long commentId = params.get(PARAM_COMMENT).toLong();
		String anchor = params.get(PARAM_ANCHOR).toString();
		
		CodeComment comment = GitPlex.getInstance(CodeCommentManager.class).load(commentId);
		for (CodeCommentRelation relation: comment.getRelations()) {
			if (relation.getRequest().getId().equals(requestId)) {
				params = RequestChangesPage.paramsOf(relation.getRequest(), comment, anchor);
				throw new RestartResponseException(RequestChangesPage.class, params);
			}
		} 
		CompareContext compareContext = comment.getLastCompareContext();
		if (!compareContext.getCompareCommit().equals(comment.getCommentPos().getCommit())) {
			throw new RestartResponseException(
					RevisionComparePage.class, 
					RevisionComparePage.paramsOf(getDepot(), comment, anchor));
		} else {
			throw new RestartResponseException(
					DepotFilePage.class, 
					DepotFilePage.paramsOf(getDepot(), comment, anchor));
		}
	}

}
