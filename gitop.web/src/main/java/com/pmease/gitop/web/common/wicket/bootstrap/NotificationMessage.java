package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 * Default implementation of {@link INotificationMessage}.
 *
 * @author miha
 */
@SuppressWarnings("serial")
public class NotificationMessage implements INotificationMessage {

    private final IModel<String> message;
    private final IModel<String> header;
    private boolean inlineHeader;
    private boolean escapeModelStrings;
    private Duration duration;

    /**
     * Construct.
     *
     * @param message The feedback message
     */
    public NotificationMessage(final IModel<String> message) {
        this(message, Model.of(""), true);
    }

    /**
     * Construct.
     *
     * @param message The feedback message
     * @param header  The header of feedback message
     */
    public NotificationMessage(final IModel<String> message, final IModel<String> header) {
        this(message, header, true);
    }

    /**
     * Construct.
     *
     * @param message      The feedback message
     * @param header       The header of feedback message
     * @param inlineHeader whether to render header and message in same line or not
     */
    public NotificationMessage(final IModel<String> message, final IModel<String> header, final boolean inlineHeader) {
        this.message = message;
        this.header = header;
        this.inlineHeader = inlineHeader;
    }

    /**
     * whether to escape model {@link String} or not
     *
     * @param escapeModelStrings true (default), if model values should be escaped.
     * @return this instance for chaining
     */
    public NotificationMessage escapeModelStrings(boolean escapeModelStrings) {
        this.escapeModelStrings = escapeModelStrings;
        return this;
    }

    /**
     * The amount of time to delay before automatically close all feedback messages.
     * If Duration.NONE or value is 0, messages will not automatically close.
     *
     * @param duration The amount of time as {@link Duration}
     * @return this instance for chaining
     */
    public NotificationMessage hideAfter(Duration duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public Duration hideAfter() {
        return duration;
    }

    @Override
    public IModel<String> message() {
        return message;
    }

    @Override
    public IModel<String> header() {
        return header;
    }

    @Override
	public boolean inlineHeader() {
        return inlineHeader;
    }

    @Override
    public boolean escapeModelStrings() {
        return escapeModelStrings;
    }

    @Override
    public String toString() {
        return Strings.isEmpty(header.getObject()) ? message.getObject()
                                                   : header.getObject() + " " + message.getObject();
    }
}