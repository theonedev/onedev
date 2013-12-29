package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.time.Duration;

/**
 * Represents a bootstrap styled feedback message.
 *
 * @author miha
 */
public interface INotificationMessage extends IClusterable {

    /**
     * @return The amount of time to delay before automatically close all feedback messages.
     */
    Duration hideAfter();

    /**
     * @return the feedback message
     */
    IModel<String> message();

    /**
     * @return the header of feedback message
     */
    IModel<String> header();

    /**
     * @return whether to render header and message in same line or not
     */
    boolean inlineHeader();

    /**
     * @return whether to escape model {@link String} or not
     */
    boolean escapeModelStrings();
}