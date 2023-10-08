package io.onedev.server.model.support.channelnotification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;

import io.onedev.server.validation.Validatable;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;

@ClassValidating
public class ChannelNotificationSetting implements ContributedProjectSetting, Validatable {

	private static final long serialVersionUID = 1L;

	private List<ChannelNotificationWrapper> notifications = new ArrayList<>();

	@Editable
	@OmitName
	@NotNull
	public List<ChannelNotificationWrapper> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<ChannelNotificationWrapper> notifications) {
		this.notifications = notifications;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> webhookUrls = new HashSet<>();
		for (ChannelNotificationWrapper notification: notifications) {
			if (notification.getChannelNotification().getWebhookUrl() != null 
					&& !webhookUrls.add(notification.getChannelNotification().getWebhookUrl())) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Duplicate webhook url found").addConstraintViolation();
				return false;
			}
		}
		return true;
	}
	
}
