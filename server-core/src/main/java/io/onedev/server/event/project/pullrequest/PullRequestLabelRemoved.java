package io.onedev.server.event.project.pullrequest;

import java.text.MessageFormat;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestLabelRemoved extends PullRequestEvent {

	private static final long serialVersionUID = 1L;

	private final String label;

	public PullRequestLabelRemoved(@Nullable User user, Date date, PullRequest request, String label) {
		super(user, date, request);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String getActivity() {
		return MessageFormat.format("removed label \"{0}\"", label);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
