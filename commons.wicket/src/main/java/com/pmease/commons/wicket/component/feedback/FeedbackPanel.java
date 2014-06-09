package com.pmease.commons.wicket.component.feedback;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.util.time.Duration;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationAlert;

@SuppressWarnings("serial")
public class FeedbackPanel extends FencedFeedbackPanel {

	private Duration duration;
    
    public FeedbackPanel(String id) {
		super(id);
	}

	public FeedbackPanel(String id, Component fence) {
		super(id, fence, null);
	}

	public FeedbackPanel(String id, IFeedbackMessageFilter filter) {
		super(id, null, filter);
	}

	public FeedbackPanel(String id, Component fence, IFeedbackMessageFilter filter) {
		super(id, fence, filter);
	}
	
    protected Component newMessageDisplayComponent(String markupId, FeedbackMessage message) {
        return new NotificationAlert(markupId, message, duration);
    }

    public FeedbackPanel hideAfter(final Duration duration) {
        this.duration = duration;
        return this;
    }

}
