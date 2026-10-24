package io.onedev.server.plugin.notification.ntfy;

import java.io.Serial;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.channelnotification.ChannelNotificationSetting;

@Editable(name="Ntfy.sh Notifications", group="Notification", order=300, description="Set up ntfy.sh notification " +
		"settings. Settings will be inherited by child projects, and can be overridden by defining settings with " +
		"same webhook url")
public class NtfyNotificationSetting extends ChannelNotificationSetting {

	@Serial
	private static final long serialVersionUID = 1L;
	
}
