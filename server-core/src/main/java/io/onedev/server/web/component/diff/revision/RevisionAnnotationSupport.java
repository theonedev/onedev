package io.onedev.server.web.component.diff.revision;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.web.util.DiffPlanarRange;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface RevisionAnnotationSupport extends Serializable {

    @Nullable
    CodeComment getOpenComment();

    Map<CodeComment, PlanarRange> getOldComments(String blobPath);

    Map<CodeComment, PlanarRange> getNewComments(String blobPath);

    Collection<CodeProblem> getOldProblems(String blobPath);

    Collection<CodeProblem> getNewProblems(String blobPath);

    Map<Integer, CoverageStatus> getOldCoverages(String blobPath);

    Map<Integer, CoverageStatus> getNewCoverages(String blobPath);

    /**
     * Caller should clear file and mark from url while add comment id to url to
     * make file and mark only determined by current comment
     *
     * @param target
     * @param comment
     */
    void onCommentOpened(AjaxRequestTarget target, CodeComment comment);

    /**
     * @param target
     * @param blobPath current file when comment is open. We should add this file
     *                 to url when remove comment id from url to make sure that
     *                 current file is still selected when refresh the page after
     *                 comment is closed
     */
    void onCommentClosed(AjaxRequestTarget target, String blobPath);

    void onAddComment(AjaxRequestTarget target);

}
