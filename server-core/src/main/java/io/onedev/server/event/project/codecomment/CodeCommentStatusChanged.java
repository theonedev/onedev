package io.onedev.server.event.project.codecomment;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.web.UrlService;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class CodeCommentStatusChanged extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String note;
	
	public CodeCommentStatusChanged(CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), change.getComment());
		changeId = change.getId();
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return OneDev.getInstance(CodeCommentStatusChangeService.class).load(changeId);
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
		if (getChange().isResolved())
			return "resolved";
		else
			return "unresolved";
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(getChange(), true);
	}
	
}
