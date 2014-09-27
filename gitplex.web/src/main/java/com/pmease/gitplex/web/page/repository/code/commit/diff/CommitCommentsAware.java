package com.pmease.gitplex.web.page.repository.code.commit.diff;

import java.util.List;

import com.pmease.gitplex.core.model.OldCommitComment;

/**
 * Whether or this page is commit comments aware
 * 
 */
public interface CommitCommentsAware {
	
	List<OldCommitComment> getCommitComments();
	
	boolean isShowInlineComments();
	
	boolean canAddComments();
	
}
