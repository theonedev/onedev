package io.onedev.server.plugin.notification.discord;

import io.onedev.server.util.channelnotification.ChannelNotificationSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Discord Notifications", order=200, description="Set up discord notification settings. Settings will be inherited by "
		+ "child projects, and can be overriden by defining setting with same webhook url")
public class DiscordNotificationSetting extends ChannelNotificationSetting {

	private static final long serialVersionUID = 1L;

}
