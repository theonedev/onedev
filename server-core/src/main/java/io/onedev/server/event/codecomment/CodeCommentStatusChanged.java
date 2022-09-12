package io.onedev.server.event.codecomment;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class CodeCommentStatusChanged extends CodeCommentEvent {

	private final CodeCommentStatusChange change;
	
	private final String note;
	
	public CodeCommentStatusChanged(CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), change.getComment());
		this.change = change;
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return change;
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null?new MarkdownText(getProject(), note):null;
	}

	@Nullable
	public String getNote() {
		return note;
	}

	@Override
	public String getActivity() {
		if (change.isResolved())
			return "resolved";
		else
			return "unresolved";
	}

	@Override
	public CodeCommentEvent cloneIn(Dao dao) {
		return new CodeCommentStatusChanged(dao.load(CodeCommentStatusChange.class, change.getId()), note);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(change);
	}
	
}
