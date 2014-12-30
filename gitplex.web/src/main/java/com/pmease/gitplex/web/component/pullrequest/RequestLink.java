package com.pmease.gitplex.web.component.pullrequest;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestOverviewPage;

@SuppressWarnings("serial")
public class RequestLink extends BookmarkablePageLink<Void> {

	private String title;
	
	public RequestLink(String id, PullRequest request) {
		super(id, RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
		
		title = request.getTitle();
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag) {

		// Draw anything before the body?
		if (!isLinkEnabled() && (getBeforeDisabledLink() != null)) {
			getResponse().write(getBeforeDisabledLink());
		}
		
		replaceComponentTagBody(markupStream, openTag, title);

		// Draw anything after the body?
		if (!isLinkEnabled() && (getAfterDisabledLink() != null)) {
			getResponse().write(getAfterDisabledLink());
		}
	}
	
}
