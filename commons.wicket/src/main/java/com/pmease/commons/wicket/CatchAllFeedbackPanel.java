package com.pmease.commons.wicket;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

@SuppressWarnings("serial")
class CatchAllFeedbackPanel extends FeedbackPanel {

	public CatchAllFeedbackPanel(String id) {
		super(id, new IFeedbackMessageFilter() {
			
			@Override
			public boolean accept(FeedbackMessage message) {
				return !message.isRendered();
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("pmease.commons.showCatchAllFeedback();"));
	}

}
