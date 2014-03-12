package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.Vote;

public class VotePullRequest implements PullRequestActivity {

	private final Vote vote;
	
	public VotePullRequest(Vote vote) {
		this.vote = vote;
	}
	
	@Override
	public Panel render(String panelId) {
		return new VoteActivityPanel(panelId, new VoteModel(vote.getId()));
	}

	@Override
	public Date getDate() {
		return vote.getDate();
	}

}
