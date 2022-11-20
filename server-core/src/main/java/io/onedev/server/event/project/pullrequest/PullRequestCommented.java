package io.onedev.server.event.project.pullrequest;

import java.util.Collection;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestCommented extends PullRequestEvent {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public PullRequestCommented(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getRequest());
		commentId = comment.getId();
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public PullRequestComment getComment() {
		return OneDev.getInstance(PullRequestCommentManager.class).load(commentId);
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
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
