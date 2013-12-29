package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.util.time.Duration;

/**
 * A panel that displays {@link NotificationMessage}s or {@link FeedbackMessage}s
 * in a list view. The maximum number of messages to show can be set with
 * {@link #setMaxMessages(int)}.
 *
 * @author miha
 */
@SuppressWarnings("serial")
public class NotificationPanel extends FencedFeedbackPanel {

    private Duration duration;
    private boolean showRenderedMessages = false;

    /**
     * Construct.
     *
     * @param id The component id
     */
    public NotificationPanel(String id) {
        this(id, (Component) null);
    }

    /**
     * Construct.
     *
     * @param id The component id
     * @param fence  The component used as fence
     */
    public NotificationPanel(String id, Component fence) {
        this(id, fence, null);
    }

    /**
     * Construct.
     *
     * @param id     The component id
     * @param filter the feedback message filter
     */
    public NotificationPanel(String id, IFeedbackMessageFilter filter) {
        this(id, null, filter);
    }

    /**
     * Construct.
     *
     * @param id     The component id
     * @param fence  The component used as fence
     * @param filter the feedback message filter
     */
    public NotificationPanel(String id, Component fence, IFeedbackMessageFilter filter) {
        super(id, fence, filter);
    }

    /**
     * whether to show already rendered messages or not.
     *
     * @param showRenderedMessages true, if rendered messages should be shown
     * @return this instance for chaining
     */
    public NotificationPanel showRenderedMessages(final boolean showRenderedMessages) {
        this.showRenderedMessages = showRenderedMessages;
        return this;
    }

    /**
     * The amount of time to delay before automatically close all feedback messages.
     * If Duration.NONE or value is 0, messages will not automatically close.
     *
     * @param duration The amount of time as {@link Duration}
     * @return this instance for chaining
     */
    public NotificationPanel hideAfter(final Duration duration) {
        this.duration = duration;
        return this;
    }

    @Override
    protected FeedbackMessagesModel newFeedbackMessagesModel() {
        FeedbackMessagesModel model = new FeedbackMessagesModel(this);

        if (!showRenderedMessages) {
            model.setFilter(new IFeedbackMessageFilter() {
                @Override
                public boolean accept(FeedbackMessage message) {
                    return !message.isRendered();
                }
            });
        }

        return model;
    }

    @Override
    protected String getCSSClass(final FeedbackMessage message) {
        return null;
    }

    @Override
    protected Component newMessageDisplayComponent(String markupId, FeedbackMessage message) {
        return new NotificationAlert(markupId, message, duration);
    }
}