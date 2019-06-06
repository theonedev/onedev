package io.onedev.server.web.page.base;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class SessionFeedbackPanel extends FeedbackPanel {

	public SessionFeedbackPanel(String id) {
		super(id, new IFeedbackMessageFilter() {
			
			@Override
			public boolean accept(FeedbackMessage message) {
				return message.getReporter() == null;
			}
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (anyMessage(FeedbackMessage.ERROR) || anyMessage(FeedbackMessage.FATAL))
					return " error";
				else if (anyMessage(FeedbackMessage.WARNING))
					return " warning";
				else if (anyMessage(FeedbackMessage.SUCCESS))
					return " success";
				else
					return " info";
			}
			
		}));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		// we delay show feedback with a timer as some other script may scroll the window 
		response.render(OnDomReadyHeaderItem.forScript("setTimeout('onedev.server.showSessionFeedback();', 1);"));
	}

}
