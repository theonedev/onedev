package io.onedev.server.event.project.pullrequest;

import java.util.Collection;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.UrlService;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestCommentCreated extends PullRequestEvent {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	private final Collection<String> listeningEmailAddresses;
	
	public PullRequestCommentCreated(PullRequestComment comment, Collection<String> listeningEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getRequest());
		commentId = comment.getId();
		this.listeningEmailAddresses = listeningEmailAddresses;
	}

	public PullRequestComment getComment() {
		return OneDev.getInstance(PullRequestCommentService.class).load(commentId);
	}

	public Collection<String> getListeningEmailAddresses() {
		return listeningEmailAddresses;
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
		return OneDev.getInstance(UrlService.class).urlFor(getComment(), true);
	}

}
