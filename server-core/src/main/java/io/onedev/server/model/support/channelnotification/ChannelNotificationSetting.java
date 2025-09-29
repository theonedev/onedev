package io.onedev.server.model.support.channelnotification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Vertical;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;

@ClassValidating
public class ChannelNotificationSetting implements ContributedProjectSetting, Validatable {

	private static final long serialVersionUID = 1L;

	private List<ChannelNotification> notifications = new ArrayList<>();

	@Editable
	@Vertical
	@OmitName
	@NotNull
	@Valid
	public List<ChannelNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<ChannelNotification> notifications) {
		this.notifications = notifications;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> webhookUrls = new HashSet<>();
		for (ChannelNotification notification: notifications) {
			if (notification.getWebhookUrl() != null
					&& !webhookUrls.add(notification.getWebhookUrl())) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Duplicate webhook url found").addConstraintViolation();
				return false;
			}
		}
		return true;
	}
	
}
