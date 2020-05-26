package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;

@SuppressWarnings("serial")
public abstract class ReviewStatusIcon extends WebComponent {
	
	public ReviewStatusIcon(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DropdownHoverBehavior() {
			
			@Override
			protected Component newContent(String id) {
				String title;
				if (getResult() != null) {
					if (getResult().isApproved())
						title = "Approved";
					else
						title = "Request for changes";
				} else {
					title = "Pending review";
				}
				StringBuilder builder = new StringBuilder();
				builder.append("<div class='title'>").append(title).append("</div>");
				
				if (getResult() != null && getResult().getComment() != null) {
					MarkdownManager markdownManager = OneDev.getInstance(MarkdownManager.class);
					String rendered = markdownManager.render(getResult().getComment());
					builder.append("<div class='comment'>").append(HtmlUtils.clean(rendered).body().html()).append("</div>");
				}
				Label label = new Label(id, builder.toString());
				label.add(AttributeAppender.append("class", "review-detail"));
				label.setEscapeModelStrings(false);
				return label;
			}
			
		});
		
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getResult() == null)
					return "review-status fa fa-clock-o pending";
				else if (getResult().isApproved())
					return "review-status fa fa-check-circle approved";
				else
					return "review-status fa fa-hand-stop-o requested-for-changes";
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
