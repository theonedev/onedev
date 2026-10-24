package io.onedev.server.util.facade;

import java.io.Serial;

public class IssueCommentFacade extends EntityFacade {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private final Long issueId;

	private final String content;
	
	public IssueCommentFacade(Long id, Long issueId, String content) {
		super(id);
		this.issueId = issueId;
		this.content = content;
	}

	public Long getIssueId() {
		return issueId;
	}

	public String getContent() {
		return content;
	}

}