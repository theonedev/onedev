package com.pmease.gitplex.web.component.pullrequest;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.model.Review;

@SuppressWarnings("serial")
public class ReviewResultIcon extends WebComponent {

	public ReviewResultIcon(String id, IModel<Review> reviewModel) {
		super(id, reviewModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Review review = (Review) getDefaultModelObject();
		String css;
		String tooltip;
		if (review.getUpdate().equals(review.getUpdate().getRequest().getLatestUpdate())) { 
			if (review.getResult() == Review.Result.APPROVE) {
				css = " review-result approved current pa pa-arrow-up";
				tooltip = "Approved";
			} else {
				css = " review-result disapproved current pa pa-arrow-down";
				tooltip = "Disapproved";
			}
		} else if (review.getResult() == Review.Result.APPROVE) {
			css = " review-result approved previous pa pa-arrow-up";
			tooltip = "Approved on previous updates";
		} else {
			css = " review-result disapproved previous pa pa-arrow-down";
			tooltip = "Disapproved on previous updates";
		}
		add(AttributeAppender.append("class", css));
		add(new TooltipBehavior(Model.of(tooltip)));
	}

}
