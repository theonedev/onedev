package io.onedev.server.event.codecomment;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeCommentStatusChange;

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
	public String getMarkdown() {
		return getNote();
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

}
