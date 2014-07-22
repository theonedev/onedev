package com.pmease.gitplex.web.page.repository.info.code.commit.diff;

import java.util.List;

import com.pmease.gitplex.core.model.CommitComment;

/**
 * Whether or this page is commit comments aware
 * 
 */
public interface CommitCommentsAware {
	
	List<CommitComment> getCommitComments();
	
	boolean isShowInlineComments();
	
	boolean canAddComments();
	
}
