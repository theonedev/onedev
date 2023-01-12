package io.onedev.server.plugin.notification.slack;

import io.onedev.server.util.channelnotification.ChannelNotificationSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Slack Notifications", group="Notification", order=100, description="Set up slack notification " +
		"settings. Settings will be inherited by child projects, and can be overriden by defining settings with " +
		"same webhook url")
public class SlackNotificationSetting extends ChannelNotificationSetting {

	private static final long serialVersionUID = 1L;

}
