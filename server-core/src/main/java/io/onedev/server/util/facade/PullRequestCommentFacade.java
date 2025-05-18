package io.onedev.server.util.facade;

public class PullRequestCommentFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long requestId;

	private final String content;
	
	public PullRequestCommentFacade(Long id, Long requestId, String content) {
		super(id);
		this.requestId = requestId;
		this.content = content;
	}

	public Long getRequestId() {
		return requestId;
	}

	public String getContent() {
		return content;
	}

}