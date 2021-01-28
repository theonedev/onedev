package io.onedev.server.web.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.git.BlobChange;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.support.Mark;

public class RevisionDiff {
	
	private final List<BlobChange> displayChanges;
	
	private final int totalChangeCount;
	
	public RevisionDiff(List<BlobChange> displayChanges, int totalChangeCount) {
		this.displayChanges = displayChanges;
		this.totalChangeCount = totalChangeCount;
	}
	
	/**
	 * Get list of changes we are capable to handle, note that size of this list 
	 * might be less than total number of changes in order not to put heavy 
	 * burden on the system and browser
	 * 
	 * @return
	 * 			list of changes we are capable to handle
	 */
	public List<BlobChange> getDisplayChanges() {
		return displayChanges;
	}

	/**
	 * Get total number of changes detected
	 * 
	 * @return
	 * 			total number of changes detected
	 */
	public int getTotalChangeCount() {
		return totalChangeCount;
	}
	
	public static interface AnnotationSupport extends Serializable {
		
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
		
		Map<Integer, Integer> getOldCoverages(String blobPath);
		
		Map<Integer, Integer> getNewCoverages(String blobPath);
		
		void onCommentOpened(AjaxRequestTarget target, CodeComment comment);
		
		void onCommentClosed(AjaxRequestTarget target);
		
		void onAddComment(AjaxRequestTarget target, Mark mark);

		void onSaveComment(CodeComment comment);
		
		void onSaveCommentReply(CodeCommentReply reply);
		
	}	
}