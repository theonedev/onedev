package io.onedev.server.buildspec.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.validation.HibernateValidationTestSupport;

class PublishSiteStepTest extends HibernateValidationTestSupport {

	@TempDir
	Path directory;

	@Test
	void validatesOptionalSiteName() {
		var step = new PublishSiteStep();
		assertTrue(validator.validateProperty(step, "siteName").isEmpty());
		for (var name : new String[] {"public", "PUBLIC", "PuBlIc", "../secret", "a/b", "a\\b", ".", "", "bad name"}) {
			step.setSiteName(name);
			assertFalse(validator.validateProperty(step, "siteName").isEmpty(), name);
		}
		for (var name : new String[] {"docs", "preview-1", "docs..v2", "public-docs"}) {
			step.setSiteName(name);
			assertTrue(validator.validateProperty(step, "siteName").isEmpty(), name);
		}
	}

	@Test
	void publishingReplacesOnlyTheSelectedSite() throws Exception {
		var sites = Files.createDirectory(directory.resolve("sites"));
		var input = Files.createDirectory(directory.resolve("input"));
		Files.createDirectory(sites.resolve("public"));
		Files.writeString(sites.resolve("public/index.html"), "public");
		Files.createDirectory(sites.resolve("docs"));
		Files.writeString(sites.resolve("docs/old.html"), "old");
		Files.writeString(input.resolve("index.html"), "updated");
		var project = new Project();
		project.setId(1L);
		project.setName("test");
		project.setPath("test");
		var build = new Build();
		build.setId(2L);
		build.setProject(project);
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		var builds = mock(BuildService.class);
		when(builds.load(2L)).thenReturn(build);
		var jobs = mock(JobService.class);
		var context = mock(JobContext.class, RETURNS_DEEP_STUBS);
		when(jobs.getJobContext(2L)).thenReturn(context);
		when(context.getJobExecutor().isSitePublishEnabled()).thenReturn(true);
		var projects = mock(ProjectService.class);
		when(projects.getSitesDir(1L)).thenReturn(sites.toFile());
		var settings = mock(SettingService.class, RETURNS_DEEP_STUBS);
		when(settings.getSystemSetting().getServerUrl()).thenReturn("https://onedev.example");
		try (var oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(SessionService.class)).thenReturn(sessions);
			oneDev.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
			oneDev.when(() -> OneDev.getInstance(JobService.class)).thenReturn(jobs);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			var step = new PublishSiteStep();
			step.setSiteName("docs");
			step.run(2L, input.toFile(), mock(TaskLogger.class));
			assertEquals("public", Files.readString(sites.resolve("public/index.html")));
			assertEquals("updated", Files.readString(sites.resolve("docs/index.html")));
			assertFalse(Files.exists(sites.resolve("docs/old.html")));
			Files.writeString(input.resolve("index.html"), "new public");
			step.setSiteName(null);
			step.run(2L, input.toFile(), mock(TaskLogger.class));
			assertEquals("new public", Files.readString(sites.resolve("public/index.html")));
			assertEquals("updated", Files.readString(sites.resolve("docs/index.html")));
			verify(projects, times(2)).directoryModified(1L, sites.toFile());
		}
	}
}
