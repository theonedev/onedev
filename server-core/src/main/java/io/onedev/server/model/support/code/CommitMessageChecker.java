package io.onedev.server.model.support.code;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;

@Editable
public interface CommitMessageChecker extends Serializable {
	
	/**
	 * Check commit message.
	 * 
	 * @param commitMessage the commit message to check
	 * @param merged whether this is a merge commit
	 * @return error message if validation fails, null otherwise
	 */
	@Nullable
	String checkCommitMessage(String commitMessage, boolean merged);
			
}
