package com.gitplex.server.web.util.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReviewInvitation;

@SuppressWarnings("serial")
public class ReviewersModel extends LoadableDetachableModel<List<PullRequestReviewInvitation>>{

	private final IModel<PullRequest> requestModel;
	
	public ReviewersModel(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	protected List<PullRequestReviewInvitation> load() {
		List<PullRequestReviewInvitation> invitations = new ArrayList<>();
		for (PullRequestReviewInvitation invitation: requestModel.getObject().getReviewInvitations()) {
			if (invitation.getStatus() != PullRequestReviewInvitation.Status.EXCLUDED)
				invitations.add(invitation);
		}
		invitations.sort((o1, o2)->{
			if (o1.getId() == null) {
				if (o2.getId() == null)
					return o1.getDate().compareTo(o2.getDate());
				else
					return 1;
			} else {
				if (o2.getId() == null)
					return -1;
				else
					return (int)(o1.getId() - o2.getId());
			}
		});
		return invitations;
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

}
