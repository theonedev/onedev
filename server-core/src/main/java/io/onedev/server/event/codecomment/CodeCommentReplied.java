package io.onedev.server.event.codecomment;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class CodeCommentReplied extends CodeCommentEvent {

	private final CodeCommentReply reply;
	
	public CodeCommentReplied(CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), reply.getComment());
		this.reply = reply;
	}

	public CodeCommentReply getReply() {
		return reply;
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), reply.getContent());
	}

	@Override
	public String getActivity() {
		return "replied";
	}

	@Override
	public CodeCommentEvent cloneIn(Dao dao) {
		return new CodeCommentReplied(dao.load(CodeCommentReply.class, reply.getId()));
	}
	
	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(reply);
	}
	
}
