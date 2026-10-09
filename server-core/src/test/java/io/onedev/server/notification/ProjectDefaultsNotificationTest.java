package io.onedev.server.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.channelnotification.ChannelNotification;
import io.onedev.server.model.support.channelnotification.ChannelNotificationSetting;
import io.onedev.server.service.ProjectService;

public class ProjectDefaultsNotificationTest {

	private static class NotificationManager extends ChannelNotificationManager<ChannelNotificationSetting> {
		@Override
		protected void post(HttpPost post, String title, ProjectEvent event) {
			fail("This test must not send notifications");
		}
	}

	@Test
	void integrationsInheritDefaultsAndOverrideDuplicateWebhookUrls() throws Exception {
		var defaults = new Project();
		defaults.setId(Project.DEFAULT_ID);
		var root = new Project();
		root.setId(1L);
		var child = new Project();
		child.setId(2L);
		child.setParent(root);
		var defaultNotification = new ChannelNotification();
		defaultNotification.setWebhookUrl("https://example.invalid/default");
		var overriddenNotification = new ChannelNotification();
		overriddenNotification.setWebhookUrl("https://example.invalid/override");
		var childNotification = new ChannelNotification();
		childNotification.setWebhookUrl(overriddenNotification.getWebhookUrl());
		var defaultSettings = new ChannelNotificationSetting();
		defaultSettings.setNotifications(List.of(defaultNotification, overriddenNotification));
		defaults.setContributedSetting(ChannelNotificationSetting.class, defaultSettings);
		root.setContributedSetting(ChannelNotificationSetting.class, new ChannelNotificationSetting());
		var childSettings = new ChannelNotificationSetting();
		childSettings.setNotifications(List.of(childNotification));
		child.setContributedSetting(ChannelNotificationSetting.class, childSettings);
		var method = ChannelNotificationManager.class.getDeclaredMethod("getNotifications", Project.class);
		method.setAccessible(true);
		try (var oneDev = mockStatic(OneDev.class)) {
			var subscription = mock(SubscriptionService.class);
			var projects = mock(ProjectService.class);
			oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			when(projects.load(Project.DEFAULT_ID)).thenReturn(defaults);
			when(subscription.isSubscriptionActive()).thenReturn(true);
			var notifications = (Collection<?>) method.invoke(new NotificationManager(), child);
			assertEquals(2, notifications.size());
			assertTrue(notifications.contains(defaultNotification));
			assertTrue(notifications.contains(childNotification));
			assertFalse(notifications.contains(overriddenNotification));
			when(subscription.isSubscriptionActive()).thenReturn(false);
			notifications = (Collection<?>) method.invoke(new NotificationManager(), child);
			assertEquals(List.of(childNotification), List.copyOf(notifications));
		}
	}
}
