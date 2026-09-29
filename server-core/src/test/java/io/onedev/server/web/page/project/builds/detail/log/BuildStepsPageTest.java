package io.onedev.server.web.page.project.builds.detail.log;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.string.StringValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.ChangeObserver;

class BuildStepsPageTest {
	@ParameterizedTest
	@EnumSource(value = Build.Status.class, mode = EnumSource.Mode.EXCLUDE, names = "RUNNING")
	void hidesProgressForBuildsThatAreNotRunning(Build.Status status) throws Exception {
		var build = mock(Build.class);
		when(build.getStatus()).thenReturn(status);
		assertNull(progressData(build));
		verify(build, never()).getStreamPrevious(any());
	}

	@Test
	void hidesProgressWithoutUsableSuccessfulStreamHistory() throws Exception {
		var build = mock(Build.class);
		when(build.getStatus()).thenReturn(Build.Status.RUNNING);
		when(build.getRunningDate()).thenReturn(new Date());
		assertNull(progressData(build));
		var previous = mock(Build.class);
		when(build.getStreamPrevious(Build.Status.SUCCESSFUL)).thenReturn(previous);
		when(previous.getRunningDuration()).thenReturn(null, 0L, -1L);
		assertNull(progressData(build));
		assertNull(progressData(build));
		assertNull(progressData(build));
	}

	@Test
	void estimatesProgressUsingRunningTimeOfSuccessfulStreamPrevious() throws Exception {
		var build = mock(Build.class);
		when(build.getStatus()).thenReturn(Build.Status.RUNNING);
		var started = System.currentTimeMillis() - 30_000;
		when(build.getRunningDate()).thenReturn(new Date(started));
		var previous = mock(Build.class);
		when(previous.getRunningDuration()).thenReturn(60_000L);
		when(build.getStreamPrevious(Build.Status.SUCCESSFUL)).thenReturn(previous);
		var progress = progressData(build);
		assertEquals(60_000L, progress.get("expected"));
		assertTrue(progress.get("elapsed") >= 30_000);
		assertTrue(progress.get("elapsed") <= System.currentTimeMillis() - started);
		verify(build).getStreamPrevious(Build.Status.SUCCESSFUL);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Long> progressData(Build build) throws Exception {
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		var method = BuildStepsPage.class.getDeclaredMethod("getProgressData");
		method.setAccessible(true);
		return (Map<String, Long>) method.invoke(page);
	}

	@Test
	void pushesUpdatesAfterCompletionAndIgnoresNewSubmission() throws Exception {
		var build = mock(Build.class);
		when(build.getProject()).thenReturn(new Project());
		when(build.getSubmitSequence()).thenReturn(1L);
		when(build.isFinished()).thenReturn(true);
		when(build.getStepExecutions()).thenReturn(new LinkedHashMap<>());
		var observer = mock(ChangeObserver.class);
		var steps = mock(WebMarkupContainer.class);
		when(steps.getMarkupId()).thenReturn("steps");
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		doReturn(page).when(page).remove(observer);
		set(page, "submitSequence", 1L);
		set(page, "nextOffsets", new LinkedHashMap<>());
		set(page, "objectMapper", new ObjectMapper());
		set(page, "steps", steps);
		set(page, "autoUpdate", true);
		var handler = mock(IPartialPageRequestHandler.class);
		var update = BuildStepsPage.class.getDeclaredMethod("updateSteps", IPartialPageRequestHandler.class, boolean.class);
		update.setAccessible(true);

		// Keep handling late output after completion, as the original LogPanel did.
		update.invoke(page, handler, false);
		verify(page, never()).remove(observer);
		verify(handler).appendJavaScript(contains("buildSteps.update([],false,1,false)"));
		verify(handler, never()).appendJavaScript(contains("buildSteps.refresh"));

		update.invoke(page, handler, false);
		verify(page, never()).remove(observer);
		verify(handler, times(2)).appendJavaScript(contains("buildSteps.update([],false,1,false)"));

		// A late notification from a rebuild must not repaint the old step view.
		when(build.getSubmitSequence()).thenReturn(2L);
		clearInvocations(handler);
		update.invoke(page, handler, false);
		verifyNoInteractions(handler);
	}

	@Test
	void disablingAutoUpdateKeepsBuildStatusUpdatingButStopsSteps() throws Exception {
		var build = mock(Build.class);
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		var detail = mock(ChangeObserver.class);
		var logs = mock(ChangeObserver.class);
		var detailObservable = Build.getDetailChangeObservable(1L);
		var logObservable = Build.getLogChangeObservable(1L);
		when(detail.getObservables()).thenReturn(Set.of(detailObservable));
		when(logs.getObservables()).thenReturn(Set.of(logObservable));
		doReturn(List.of(detail, logs)).when(page).findBehaviors(ChangeObserver.class);
		doAnswer(invocation -> {
			var update = BuildStepsPage.class.getDeclaredMethod("updateSteps", IPartialPageRequestHandler.class, boolean.class);
			update.setAccessible(true);
			update.invoke(page, invocation.getArgument(0), false);
			return null;
		}).when(logs).onObservableChanged(any(), any());
		var target = mock(AjaxRequestTarget.class);
		changeAutoUpdate(page, target, false);
		verify(target).prependJavaScript("onedev.server.buildSteps.setAutoUpdate(false);");
		clearInvocations(target);

		page.notifyObservablesChange(target, Set.of(detailObservable, logObservable));
		verify(detail).onObservableChanged(target, Set.of(detailObservable));
		verify(logs).onObservableChanged(target, Set.of(logObservable));
		verifyNoInteractions(target);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void finishingBuildReloadsWholePageRegardlessOfAutoUpdate(boolean autoUpdate) throws Exception {
		var build = mock(Build.class);
		when(build.getSubmitSequence()).thenReturn(1L);
		when(build.isFinished()).thenReturn(true);
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		doReturn(List.of()).when(page).findBehaviors(ChangeObserver.class);
		set(page, "submitSequence", 1L);
		set(page, "autoUpdate", autoUpdate);
		var target = mock(AjaxRequestTarget.class);

		page.notifyObservablesChange(target, Set.of(Build.getDetailChangeObservable(1L)));
		verify(target).appendJavaScript("window.location.reload();");
		clearInvocations(target);

		// Late log notifications must not repeatedly reload an already finished page.
		page.notifyObservablesChange(target, Set.of(Build.getLogChangeObservable(1L)));
		verifyNoInteractions(target);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void reEnablingOnlyCatchesUpStepsWithoutCheckingBuildCompletion(boolean finished) throws Exception {
		var build = mock(Build.class);
		when(build.getId()).thenReturn(1L);
		when(build.getSubmitSequence()).thenReturn(2L);
		when(build.isFinished()).thenReturn(finished);
		when(build.getStepExecutions()).thenReturn(new LinkedHashMap<>());
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		set(page, "submitSequence", 2L);
		var steps = mock(WebMarkupContainer.class);
		when(steps.getMarkupId()).thenReturn("steps");
		set(page, "steps", steps);
		var offsets = new LinkedHashMap<String, Integer>(Map.of("old-step", 500));
		set(page, "nextOffsets", offsets);
		set(page, "objectMapper", new ObjectMapper());
		var request = mock(Request.class, RETURNS_DEEP_STUBS);
		when(request.getRequestParameters().getParameterValue("offsets"))
				.thenReturn(StringValue.valueOf("{\"running-step\":42,\"expanded-step\":7}"));
		doReturn(request).when(page).getRequest();
		var target = mock(AjaxRequestTarget.class);

		changeAutoUpdate(page, target, true);
		assertEquals(Map.of("running-step", 42, "expanded-step", 7), offsets);
		verify(target).prependJavaScript("onedev.server.buildSteps.setAutoUpdate(true);");
		verify(target).appendJavaScript(contains("buildSteps.update([],false,2,false)"));
		verify(target, never()).appendJavaScript(contains("location.reload"));
		verify(build, never()).isFinished();
		verify(page, never()).findBehaviors(ChangeObserver.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void phasesHaveNoStatusOrDurationWhileStepsKeepTheirMetadata() throws Exception {
		var build = spy(new Build());
		var project = new Project();
		project.setId(1L);
		build.setProject(project);
		build.setNumber(2L);
		build.setStatus(Build.Status.RUNNING);
		doReturn(null).when(build).getJob();
		var stepExecution = new io.onedev.server.model.support.build.StepExecution();
		stepExecution.complete(io.onedev.server.model.support.build.StepExecution.Status.FAILED);
		build.getStepExecutions().put("step-0", stepExecution);
		build.setFinalization(true);
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		doReturn(project).when(page).getProject();
		doReturn("/log").when(page).urlFor(any(org.apache.wicket.request.resource.ResourceReference.class),
				any(org.apache.wicket.request.mapper.parameter.PageParameters.class));
		set(page, "nextOffsets", new LinkedHashMap<>());
		var method = BuildStepsPage.class.getDeclaredMethod("getStepData");
		method.setAccessible(true);
		var data = (List<Map<String, Object>>) method.invoke(page);
		assertEquals(List.of("initialization", "step-0", "finalization"),
				data.stream().map(it -> it.get("name")).toList());
		for (var phase : List.of(data.get(0), data.get(2))) {
			assertTrue(!phase.containsKey("status"));
			assertTrue(!phase.containsKey("duration"));
			assertTrue(!phase.containsKey("stepIndex"));
			assertEquals("/log", phase.get("download"));
		}
		assertEquals(false, data.get(0).get("active"));
		assertEquals(true, data.get(2).get("active"));
		assertEquals(stepExecution.getStatus(), data.get(1).get("status"));
		assertEquals(stepExecution.getDuration(), data.get(1).get("duration"));
		assertEquals(1, data.get(1).get("stepIndex"));

		// A retry hides the previous attempt's finalization until cleanup starts again.
		build.setFinalization(false);
		build.getStepExecutions().clear();
		data = (List<Map<String, Object>>) method.invoke(page);
		assertEquals(List.of("initialization"), data.stream().map(it -> it.get("name")).toList());
		assertEquals(true, data.get(0).get("active"));
		build.setStatus(Build.Status.FAILED);
		data = (List<Map<String, Object>>) method.invoke(page);
		assertEquals(List.of("initialization"), data.stream().map(it -> it.get("name")).toList());
		build.setStatus(Build.Status.RUNNING);
		build.getStepExecutions().put("step-0", new io.onedev.server.model.support.build.StepExecution());
		data = (List<Map<String, Object>>) method.invoke(page);
		assertEquals(List.of("initialization", "step-0"), data.stream().map(it -> it.get("name")).toList());
		assertEquals(false, data.get(0).get("active"));
		assertEquals(true, data.get(1).get("active"));
		build.setFinalization(true);
		data = (List<Map<String, Object>>) method.invoke(page);
		assertEquals("finalization", data.get(2).get("name"));
	}

	private void changeAutoUpdate(BuildStepsPage page, AjaxRequestTarget target, boolean enabled) throws Exception {
		var method = BuildStepsPage.class.getDeclaredMethod("changeAutoUpdate", AjaxRequestTarget.class, boolean.class);
		method.setAccessible(true);
		method.invoke(page, target, enabled);
	}

	private void set(BuildStepsPage page, String name, Object value) throws Exception {
		var field = BuildStepsPage.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(page, value);
	}

	@Test
	void retryReloadsThePageRegardlessOfAutoUpdateInsteadOfReusingPreviousLogOffsets() throws Exception {
		var build = mock(Build.class);
		when(build.getSubmitSequence()).thenReturn(1L);
		when(build.getRetryDate()).thenReturn(new Date(1000));
		var page = mock(BuildStepsPage.class, CALLS_REAL_METHODS);
		doReturn(build).when(page).getBuild();
		set(page, "submitSequence", 1L);
		set(page, "autoUpdate", true);
		var handler = mock(IPartialPageRequestHandler.class);
		var update = BuildStepsPage.class.getDeclaredMethod("updateSteps", IPartialPageRequestHandler.class, boolean.class);
		update.setAccessible(true);
		update.invoke(page, handler, false);
		verify(handler).appendJavaScript("window.location.reload();");
		verifyNoMoreInteractions(handler);
		clearInvocations(handler);
		set(page, "autoUpdate", false);
		update.invoke(page, handler, false);
		verify(handler).appendJavaScript("window.location.reload();");
		verifyNoMoreInteractions(handler);
		clearInvocations(handler);
		update.invoke(page, handler, true);
		verify(handler).appendJavaScript("window.location.reload();");
	}
}
