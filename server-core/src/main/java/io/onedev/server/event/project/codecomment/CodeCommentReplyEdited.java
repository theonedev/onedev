package io.onedev.server.event.project.codecomment;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.security.SecurityUtils;

import java.util.Date;

public class CodeCommentReplyEdited extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long replyId;
	
	public CodeCommentReplyEdited(CodeCommentReply reply) {
		super(SecurityUtils.getUser(), new Date(), reply.getComment());
		this.replyId = reply.getId();
	}
	
	public CodeCommentReply getReply() {
		return OneDev.getInstance(CodeCommentReplyManager.class).load(replyId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public String getActivity() {
		return "reply edited";
	}

}
