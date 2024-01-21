package io.onedev.server.model.support.channelnotification;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable
public class ChannelNotificationWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	private ChannelNotification channelNotification = new ChannelNotification();
	
	@Editable
	@OmitName
	@NotNull
	public ChannelNotification getChannelNotification() {
		return channelNotification;
	}

	public void setChannelNotification(ChannelNotification channelNotification) {
		this.channelNotification = channelNotification;
	}

}