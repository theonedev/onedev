package com.pmease.commons.wicket;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

@SuppressWarnings("serial")
class SessionFeedbackPanel extends FeedbackPanel {

	public SessionFeedbackPanel(String id) {
		super(id, new IFeedbackMessageFilter() {
			
			@Override
			public boolean accept(FeedbackMessage message) {
				return message.getReporter() == null;
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("pmease.commons.showSessionFeedback();"));
	}

}
