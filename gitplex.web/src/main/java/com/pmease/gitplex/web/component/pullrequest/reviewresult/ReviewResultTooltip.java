package com.pmease.gitplex.web.component.pullrequest.reviewresult;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReview;
import com.pmease.gitplex.core.manager.PullRequestReviewManager;
import com.pmease.gitplex.core.security.SecurityUtils;

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
