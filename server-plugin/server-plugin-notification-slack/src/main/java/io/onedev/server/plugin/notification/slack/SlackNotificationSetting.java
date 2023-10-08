package io.onedev.server.plugin.notification.slack;

import io.onedev.server.model.support.channelnotification.ChannelNotificationSetting;
import io.onedev.server.annotation.Editable;

@Editable(name="Slack Notifications", group="Notification", order=100, description="Set up slack notification " +
		"settings. Settings will be inherited by child projects, and can be overridden by defining settings with " +
		"same webhook url")
public class SlackNotificationSetting extends ChannelNotificationSetting {

	private static final long serialVersionUID = 1L;

}
