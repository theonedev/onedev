package io.onedev.server.event.project.codecomment;

import java.util.Date;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

public class CodeCommentTouched extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public CodeCommentTouched(Project project, Long commentId) {
		super(SecurityUtils.getUser(), new Date(), project);
		this.commentId = commentId;
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public String getActivity() {
		return "touched";
	}
	
}
