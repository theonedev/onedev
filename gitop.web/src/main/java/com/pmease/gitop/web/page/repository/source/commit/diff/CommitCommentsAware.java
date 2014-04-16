package com.pmease.gitop.web.page.repository.source.commit.diff;

import java.util.List;

import com.pmease.gitop.model.CommitComment;

/**
 * Whether or this page is commit comments aware
 * 
 */
public interface CommitCommentsAware {
	
	List<CommitComment> getCommitComments();
	
	boolean isShowInlineComments();
	
	boolean canAddComments();
	
}
