package io.onedev.server.event.pullrequest;

import java.util.Collection;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.dao.Dao;

public class PullRequestCommented extends PullRequestEvent {

	private final PullRequestComment comment;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public PullRequestCommented(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getRequest());
		this.comment = comment;
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public PullRequestComment getComment() {
		return comment;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity() {
		return "commented";
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestCommented(
				dao.load(PullRequestComment.class, comment.getId()), 
				notifiedEmailAddresses);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(comment);
	}

}
