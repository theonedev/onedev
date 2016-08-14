package com.pmease.gitplex.core.event.mention;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;

public class AccountMentionedInCodeComment {

	private final Account user;
	
	private final String markdown;
	
	private final CodeComment comment;
	
	public AccountMentionedInCodeComment(CodeComment comment, Account user, String markdown) {
		this.comment = comment;
		this.user = user;
		this.markdown = markdown;
	}

	public Account getUser() {
		return user;
	}

	public String getMarkdown() {
		return markdown;
	}

	public CodeComment getComment() {
		return comment;
	}

}
