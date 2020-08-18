package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public abstract class ReviewStatusIcon extends WebComponent {
	
	public ReviewStatusIcon(String id) {
		super(id);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		String icon;
		String cssClass = "icon review-status review-status-";
		if (getResult() == null) {
			icon = "clock-o";
			cssClass += "pending";
		} else if (getResult().isApproved()) {
			icon = "tick-circle-o";
			cssClass += "approved";
		} else {
			icon = "times-circle-o";
			cssClass += "request-for-changes";
		}
		
		String versionedIcon = SpriteImage.getVersionedHref(IconScope.class, icon);

		replaceComponentTagBody(markupStream, openTag, 
				String.format("<svg class='%s'><use xlink:href='%s'></use></svg>", cssClass, versionedIcon));
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
		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ReviewCssResourceReference()));
	}

	protected abstract ReviewResult getResult();
	
}
