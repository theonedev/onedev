package com.pmease.gitplex.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.ReviewInvitation;

@SuppressWarnings("serial")
public class ReviewersModel extends LoadableDetachableModel<List<ReviewInvitation>>{

	private final IModel<PullRequest> requestModel;
	
	public ReviewersModel(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	protected List<ReviewInvitation> load() {
		List<ReviewInvitation> invitations = new ArrayList<>();
		for (ReviewInvitation invitation: requestModel.getObject().getReviewInvitations()) {
			if (invitation.isPreferred())
				invitations.add(invitation);
		}
		Collections.sort(invitations, new Comparator<ReviewInvitation>() {

			@Override
			public int compare(ReviewInvitation invitation1, ReviewInvitation invitation2) {
				return invitation1.getDate().compareTo(invitation2.getDate());
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
