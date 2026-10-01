package io.onedev.server.web.component.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.choiceparam.ChoiceParam;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ScriptingChoices;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.RepeatingView;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.xodus.CommitInfoService;

class RunJobLinkTest {

	@TempDir
	Path directory;

	private WicketTester tester;
	private GroovyScript script;
	private ScriptingChoices choices;
	private BuildOptionModalPanel modal;
	private Path marker;

	@BeforeEach
	void setUp() throws Exception {
		tester = new WicketTester();
		marker = directory.resolve("script-executed");
		script = new GroovyScript();
		script.setName("restricted-poc");
		script.setCanBeUsedByBuildJobs(false);
		script.setContent(List.of("new File('" + marker + "').text = 'executed'",
				"return ['RESTRICTED-SCRIPT-OUTPUT-9031']"));
		choices = new ScriptingChoices();
		choices.setScriptName(script.getName());

		var parameter = new ChoiceParam();
		parameter.setName("environment");
		parameter.setChoiceProvider(choices);
		var job = new Job();
		job.setName("required-build");
		job.setParamSpecs(List.of(parameter));
		var buildSpec = new BuildSpec();
		buildSpec.setJobs(List.of(job));
		var commitId = ObjectId.zeroId();
		var project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getBuildSpec(commitId)).thenReturn(buildSpec);
		var request = new PullRequest();
		request.setId(1L);
		request.setSourceProject(project);
		request.setTargetProject(project);
		request.setSourceBranch("feature");
		request.setTargetBranch("main");
		var link = new RunJobLink("run", commitId, job.getName(), null) {
			@Override
			protected Project getProject() {
				return project;
			}

			@Override
			protected PullRequest getPullRequest() {
				return request;
			}

			@Override
			protected ObjectId getSeenBranchTip(String branch) {
				return null;
			}
		};
		var commits = mock(CommitInfoService.class);
		when(commits.getDescendants(eq(1L), any())).thenReturn(new HashSet<>());
		var field = RunJobLink.class.getDeclaredField("commitInfoService");
		field.setAccessible(true);
		field.set(link, commits);

		// Like the PR page, the page itself supplies no job authorization context.
		var page = new WebPage() {
			@Override
			public Markup getAssociatedMarkup() {
				return Markup.of("<html><body><a wicket:id='run'></a><div wicket:id='roots'></div></body></html>");
			}
		};
		var roots = new RepeatingView("roots");
		page.add(link, roots);
		var basePage = mock(BasePage.class);
		when(basePage.getRootComponents()).thenReturn(roots);
		var target = mock(AjaxRequestTarget.class);
		when(target.getPage()).thenReturn(basePage);
		try (var security = mockStatic(SecurityUtils.class); var params = mockStatic(ParamUtils.class)) {
			// Bean generation is unrelated to the later choice-loading callback.
			params.when(() -> ParamUtils.defineBeanClass(job.getParamSpecs())).thenReturn(String.class);
			link.onClick(target);
		}
		modal = (BuildOptionModalPanel) roots.iterator().next();
		assertSame(roots, modal.getParent());
		assertNull(JobAuthorizationContext.get(), "The opening click's context must have ended");
	}

	@AfterEach
	void tearDown() {
		tester.destroy();
	}

	@Test
	void laterChoiceRequestRejectsScriptDisabledForBuildJobs() {
		loadChoices(() -> {
			var exception = assertThrows(ExplicitException.class, () -> {
				var result = choices.getChoices(false);
				fail("Restricted script returned " + result.keySet() + "; marker created: " + Files.exists(marker));
			});
			assertEquals("Unauthorized groovy script: restricted-poc", exception.getMessage());
			assertFalse(Files.exists(marker), "The restricted script must not execute");
		});
	}

	@Test
	void laterChoiceRequestChecksBothPullRequestBranches() {
		script.setCanBeUsedByBuildJobs(true);
		for (var branch : List.of("main", "feature")) {
			script.setJobAuthorization("on branch \"" + branch + "\"");
			loadChoices(() -> {
				assertThrows(ExplicitException.class, () -> choices.getChoices(false));
				assertFalse(Files.exists(marker));
			});
		}
	}

	@Test
	void laterChoiceRequestAllowsAuthorizedScript() {
		script.setCanBeUsedByBuildJobs(true);
		script.setJobAuthorization("on branch \"main\" or on branch \"feature\"");
		loadChoices(() -> {
			assertEquals(List.of("RESTRICTED-SCRIPT-OUTPUT-9031"),
					List.copyOf(choices.getChoices(false).keySet()));
			assertTrue(Files.exists(marker));
		});
	}

	private void loadChoices(Runnable assertions) {
		var settings = mock(SettingService.class);
		when(settings.getGroovyScripts()).thenReturn(List.of(script));
		// Dispatch a later Wicket listener under the real modal, just as the choice
		// control does, without retaining the RunJobLink's opening-click context.
		var callback = new Link<Void>("choices") {
			@Override
			public void onClick() {
				assertions.run();
			}
		};
		modal.addOrReplace(callback);
		try (var oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			new ListenerRequestHandler(new PageAndComponentProvider(callback.getPage(), callback))
					.respond(tester.getRequestCycle());
		}
		assertNull(JobAuthorizationContext.get());
	}
}
