package io.onedev.server.event.project.issue;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

import java.util.Collection;

public class IssueCommentCreated extends IssueEvent {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public IssueCommentCreated(IssueComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getIssue());
		commentId = comment.getId();
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public IssueComment getComment() {
		return OneDev.getInstance(IssueCommentManager.class).load(commentId);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
	}

	@Override
	public boolean affectsListing() {
		return false;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	public String getActivity() {
		return "commented";
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getComment());
	}

}
