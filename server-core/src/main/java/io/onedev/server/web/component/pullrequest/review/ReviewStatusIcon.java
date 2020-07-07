package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.support.pullrequest.ReviewResult;

@SuppressWarnings("serial")
public abstract class ReviewStatusIcon extends WebComponent {
	
	public ReviewStatusIcon(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(AttributeAppender.append("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String title;
				if (getResult() != null) {
					if (getResult().isApproved())
						title = "Approved";
					else
						title = "Request for changes";
				} else {
					title = "Pending review";
				}
				return title;
			}
			
		}));
		
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getResult() == null)
					return "review-status pending";
				else if (getResult().isApproved())
					return "review-status approved";
				else
					return "review-status requested-for-changes";
			}
			
		}));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ReviewCssResourceReference()));
	}

	protected abstract ReviewResult getResult();
}
