package io.onedev.server.web.editable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.ProjectAiSetting;
import io.onedev.server.model.support.code.GitPackConfig;

public class EditableUtilsTest {

	@Test
	public void inheritanceHelpMatchesProjectContext() throws Exception {
		var property = ProjectAiSetting.class.getMethod("getExcludedReviewFiles");
		var root = new Project();
		root.setId(1L);
		var child = new Project();
		child.setId(2L);
		child.setParent(root);
		var defaults = new Project();
		defaults.setId(Project.DEFAULT_ID);

		try (var projects = mockStatic(Project.class); var oneDev = mockStatic(OneDev.class)) {
			var subscription = mock(SubscriptionService.class);
			oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
			var baseDescription = EditableUtils.getDescription(property);
			assertFalse(baseDescription.contains("Leave empty"));

			projects.when(Project::get).thenReturn(child);
			assertTrue(EditableUtils.getDescription(property).endsWith("Leave empty to inherit from the parent project."));

			projects.when(Project::get).thenReturn(root);
			assertEquals(baseDescription, EditableUtils.getDescription(property));
			when(subscription.isSubscriptionActive()).thenReturn(true);
			assertTrue(EditableUtils.getDescription(property).contains(
					"<a href='https://docs.onedev.io/administration-guide/project-defaults' target='_blank'>default setting</a>"));

			projects.when(Project::get).thenReturn(child);
			assertTrue(EditableUtils.getDescription(property).endsWith("Leave empty to inherit from the parent project."));
			assertFalse(EditableUtils.getDescription(property).contains("project-defaults"));

			projects.when(Project::get).thenReturn(defaults);
			assertEquals(baseDescription, EditableUtils.getDescription(property));

			projects.when(Project::get).thenReturn(root);
			assertFalse(EditableUtils.getDescription(GitPackConfig.class.getMethod("getWindow")).contains("Leave empty"));
		}
	}
}
