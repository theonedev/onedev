package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

/**
 * An {@link Alert} implementation that shows an {@link INotificationMessage}.
 *
 * @author miha
 */
@SuppressWarnings("serial")
public class NotificationAlert extends Alert {

    private final IModel<INotificationMessage> notificationModel;
    private final Duration duration;
    private Component message;

    /**
     * Construct.
     *
     * @param markupId The component id
     * @param message  The feedback message
     */
    public NotificationAlert(final String markupId, final FeedbackMessage message) {
        this(markupId, message, Duration.NONE);
    }

    /**
     * Construct.
     *
     * @param markupId The component id
     * @param message  The feedback message
     * @param duration amount of time when message should be closed
     */
    public NotificationAlert(final String markupId, final FeedbackMessage message, final Duration duration) {
        super(markupId, Model.of(""));

        this.duration = duration;
        this.notificationModel = Model.of();

        type(Alert.Type.from(message.getLevelAsString()));

        if (message.getMessage() instanceof INotificationMessage) {
            withNotificationMessage((INotificationMessage) message.getMessage());
        } else {
            withMessage(Model.of(String.valueOf(message.getMessage())));
            hideAfter(duration);
        }
    }

    /**
     * sets the notification message
     *
     * @param notificationMessage The notification message
     * @return this instance for chaining.
     */
    private NotificationAlert withNotificationMessage(final INotificationMessage notificationMessage) {
        notificationModel.setObject(notificationMessage);

        hideAfter(notificationMessage.hideAfter() == null ? duration : notificationMessage.hideAfter());
        useInlineHeader(notificationMessage.inlineHeader());
        withHeader(notificationMessage.header());
        withMessage(notificationMessage.message());

        this.message.setEscapeModelStrings(notificationMessage.escapeModelStrings());

        return this;
    }

    @Override
    protected Component createMessage(String markupId, IModel<String> message) {
        this.message = super.createMessage(markupId, message);
        return this.message;
    }

    @Override
    public void detachModels() {
        super.detachModels();

        notificationModel.detach();
    }
}