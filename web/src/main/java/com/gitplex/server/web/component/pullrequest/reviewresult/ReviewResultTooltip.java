package com.gitplex.server.web.component.pullrequest.reviewresult;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReview;
import com.gitplex.server.manager.PullRequestReviewManager;
import com.gitplex.server.security.SecurityUtils;

@SuppressWarnings("serial")
class ReviewResultTooltip extends GenericPanel<PullRequestReview> {

	public ReviewResultTooltip(String id, IModel<PullRequestReview> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestReview review = getModelObject();
		String description;
		if (review.getUpdate().equals(review.getUpdate().getRequest().getLatestUpdate())) { 
			if (review.getResult() == PullRequestReview.Result.APPROVE) {
				description = "Approved";
			} else {
				description = "Disapproved";
			}
		} else if (review.getResult() == PullRequestReview.Result.APPROVE) {
			description = "Approved on a previous update";
		} else {
			description = "Disapproved on a previous update";
		}
		
		add(new Label("description", description));
		
		add(new Link<Void>("withdraw") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequestReview review = ReviewResultTooltip.this.getModelObject();
				setVisible(review.getUser().equals(SecurityUtils.getAccount()) 
						|| SecurityUtils.canManage(review.getUpdate().getRequest().getTargetDepot()));
			}

			@Override
			public void onClick() {
				PullRequestReview review = ReviewResultTooltip.this.getModelObject();
				PullRequest request = review.getUpdate().getRequest();
				GitPlex.getInstance(PullRequestReviewManager.class).deleteAll(review.getUser(), request);
			}

		});
	}

}
