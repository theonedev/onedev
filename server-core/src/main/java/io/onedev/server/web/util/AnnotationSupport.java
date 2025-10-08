package io.onedev.server.web.util;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.support.Mark;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface AnnotationSupport extends Serializable {

	@Nullable
	Mark getMark();

	@Nullable
	String getMarkUrl(Mark mark);

	void onMark(AjaxRequestTarget target, Mark mark);

	void onUnmark(AjaxRequestTarget target);

	@Nullable
	CodeComment getOpenComment();

	Map<CodeComment, PlanarRange> getOldComments(String blobPath);

	Map<CodeComment, PlanarRange> getNewComments(String blobPath);

	Collection<CodeProblem> getOldProblems(String blobPath);

	Collection<CodeProblem> getNewProblems(String blobPath);

	Map<Integer, CoverageStatus> getOldCoverages(String blobPath);

	Map<Integer, CoverageStatus> getNewCoverages(String blobPath);

	void onCommentOpened(AjaxRequestTarget target, CodeComment comment);

	void onCommentClosed(AjaxRequestTarget target);

	void onAddComment(AjaxRequestTarget target, Mark mark);

	void onSaveComment(CodeComment comment);

	void onSaveCommentReply(CodeCommentReply reply);

	void onSaveCommentStatusChange(CodeCommentStatusChange change, @Nullable String note);

}
