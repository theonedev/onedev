package io.onedev.server.web.page.project.builds.detail.log;

import static io.onedev.k8shelper.JobHelper.FINALIZATION;
import static io.onedev.k8shelper.JobHelper.INITIALIZATION;
import static io.onedev.server.web.translation.Translation._T;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.List;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.job.JobService;
import io.onedev.server.logging.build.BuildLogService;
import io.onedev.server.logging.build.BuildLoggingSupport;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.build.StepExecution;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.progress.TimeProgressBar;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.resource.BuildLogResource;
import io.onedev.server.web.resource.BuildLogResourceReference;

public class BuildStepsPage extends BuildDetailPage {
	private static final long serialVersionUID = 1L;

	private static final int MAX_ENTRIES = 5000;

	@Inject
	private BuildLogService buildLogService;
	@Inject
	private JobService jobService;
	@Inject
	private ObjectMapper objectMapper;

	private WebMarkupContainer steps;
	private AbstractPostAjaxBehavior refresh;
	private AbstractPostAjaxBehavior resume;
	private final Map<String, Integer> nextOffsets = new LinkedHashMap<>();
	private long submitSequence;
	private Date retryDate;
	private boolean buildFinished;
	private boolean autoUpdate = true;

	public BuildStepsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		submitSequence = getBuild().getSubmitSequence();
		buildFinished = getBuild().isFinished();
		retryDate = getBuild().getRetryDate();
		steps = new WebMarkupContainer("steps");
		steps.setOutputMarkupId(true);
		add(steps);
		var progressData = getProgressData();
		add(new TimeProgressBar("progress", progressData != null ? progressData.get("expected") / 1000.0 : 0,
				"var(--info)", false).setElapsed(progressData != null ? progressData.get("elapsed") / 1000.0 : 0));
		var autoUpdateContainer = new WebMarkupContainer("autoUpdateContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getStatus() == Build.Status.RUNNING);
			}
		};
		autoUpdateContainer.setOutputMarkupPlaceholderTag(true);
		autoUpdateContainer.add(new AjaxLink<Void>("autoUpdate") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("aria-pressed", String.valueOf(autoUpdate));
				tag.put("data-tippy-content", autoUpdate
						? _T("Steps and logs are updating and scrolling automatically. Click to pause.")
						: _T("Automatic updates and scrolling are paused. Click to resume."));
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getDynamicExtraParameters().add("return {offsets: onedev.server.buildSteps.getOffsets()};");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				changeAutoUpdate(target, !autoUpdate);
				target.add(this);
			}
		}.setOutputMarkupId(true));
		add(autoUpdateContainer);

		refresh = new AbstractPostAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AjaxCallListener()
						.onFailure("onedev.server.buildSteps.busy = false;"));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				readOffsets();
				updateSteps(target, true);
			}
		};
		add(refresh);
		resume = new AbstractPostAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				if (!SecurityUtils.canRunJob(getProject(), getBuild().getJobName()))
					throw new UnauthorizedException();
				jobService.resume(getBuild());
				updateSteps(target, false);
			}
		};
		add(resume);
		add(new ChangeObserver() {
			private static final long serialVersionUID = 1L;

			@Override
			public Collection<String> findObservables() {
				return Set.of(Build.getLogChangeObservable(getBuild().getId()), Build.getDetailChangeObservable(getBuild().getId()));
			}
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changed) {
				if (autoUpdateContainer.isVisible() != (getBuild().getStatus() == Build.Status.RUNNING))
					handler.add(autoUpdateContainer);
				updateSteps(handler, false);
			}
		});
	}

	private void readOffsets() {
		try {
			var offsets = objectMapper.readTree(getRequest().getRequestParameters().getParameterValue("offsets").toString("{}"));
			nextOffsets.clear();
			offsets.properties().forEach(it -> nextOffsets.put(it.getKey(), Math.max(0, it.getValue().asInt())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void notifyObservablesChange(IPartialPageRequestHandler handler, Collection<String> observables) {
		if (!buildFinished && submitSequence == getBuild().getSubmitSequence() && getBuild().isFinished()) {
			buildFinished = true;
			handler.appendJavaScript("window.location.reload();");
		} else {
			super.notifyObservablesChange(handler, observables);
		}
	}

	private void changeAutoUpdate(AjaxRequestTarget target, boolean enabled) {
		autoUpdate = enabled;
		target.prependJavaScript("onedev.server.buildSteps.setAutoUpdate(" + enabled + ");");
		if (enabled) {
			readOffsets();
			updateSteps(target, false);
		}
	}

	private String stepUpdateScript(boolean requested) {
		try {
			return "if (onedev.server.buildSteps && onedev.server.buildSteps.container === document.getElementById('"
					+ steps.getMarkupId() + "')) { onedev.server.buildSteps.setProgress("
					+ objectMapper.writeValueAsString(getProgressData()) + "); onedev.server.buildSteps.update("
					+ objectMapper.writeValueAsString(getStepData())
					+ "," + getBuild().isPaused() + "," + submitSequence + "," + requested + "); }";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Long> getProgressData() {
		var build = getBuild();
		if (build.getStatus() != Build.Status.RUNNING || build.getRunningDate() == null)
			return null;
		var previous = build.getStreamPrevious(Build.Status.SUCCESSFUL);
		if (previous == null)
			return null;
		var expected = previous.getRunningDuration();
		if (expected == null || expected <= 0)
			return null;
		return Map.of("elapsed", Math.max(0, System.currentTimeMillis() - build.getRunningDate().getTime()),
				"expected", expected);
	}

	private void updateSteps(IPartialPageRequestHandler handler, boolean requested) {
		// A new submission belongs to a new page, never to this page's step view.
		if (submitSequence == getBuild().getSubmitSequence()) {
			if (!Objects.equals(retryDate, getBuild().getRetryDate()))
				handler.appendJavaScript("window.location.reload();");
			else if (autoUpdate || requested)
				handler.appendJavaScript(stepUpdateScript(requested));
		}
	}

	private List<Map<String, Object>> getStepData() {
		var data = new ArrayList<Map<String, Object>>();
		var build = getBuild();
		var stepExecutions = build.getStepExecutions();
		var currentLogStage = build.getCurrentLogStage();
		int stepIndex = 0;
		for (var name : build.getLogStages()) {
			var stepExecution = stepExecutions.get(name);
			var stage = new LinkedHashMap<String, Object>();
			stage.put("name", name);
			stage.put("title", switch (name) {
				case INITIALIZATION -> _T("Initialization");
				case FINALIZATION -> _T("Finalization");
				default -> build.getLogStageName(name);
			});
			boolean active = stepExecution != null ? stepExecution.getStatus() == StepExecution.Status.RUNNING
					: name.equals(currentLogStage) && !build.isFinished();
			stage.put("active", active);
			if (stepExecution != null) {
				stepIndex++;
				// Older builds only have the instances recorded in their step logs.
				stage.put("stepIndex", stepExecution.getStepCount() != 0 ? stepExecution.getStepIndex() : stepIndex);
				stage.put("stepCount", stepExecution.getStepCount() != 0 ? stepExecution.getStepCount() : stepExecutions.size());
				stage.put("stepPositionText", _T("Current step index / total number of steps"));
				stage.put("status", stepExecution.getStatus());
				stage.put("statusText", stepExecution.isSkipped() ? _T("Skipped") : switch (stepExecution.getStatus()) {
					case RUNNING -> _T("Running");
					case SUCCESSFUL -> _T("Successful");
					case FAILED -> _T("Failed");
					case CANCELLED -> _T("Cancelled");
					case UNKNOWN -> _T("Unknown");
				});
				stage.put("skipped", stepExecution.isSkipped());
				stage.put("duration", stepExecution.getDuration());
			}
			stage.put("downloadText", _T("Download log"));
			stage.put("download", urlFor(new BuildLogResourceReference(), BuildLogResource.paramsOf(
					getProject().getId(), build.getNumber(), name)).toString());
			appendLogEntries(stage, name, active);
			data.add(stage);
		}
		return data;
	}

	private void appendLogEntries(Map<String, Object> step, String name, boolean active) {
		if (autoUpdate && active)
			nextOffsets.putIfAbsent(name, 0);
		if (nextOffsets.containsKey(name)) {
			int from = nextOffsets.get(name);
			var snippet = buildLogService.readLogSnippetReversely(new BuildLoggingSupport(getBuild(), name), MAX_ENTRIES);
			step.put("from", from);
			step.put("offset", Math.max(from, snippet.offset));
			step.put("next", snippet.offset + snippet.entries.size());
			nextOffsets.put(name, snippet.offset + snippet.entries.size());
			step.put("entries", snippet.entries.stream().skip(Math.max(0, from - snippet.offset))
					.map(it -> it.transformEmojis()).toList());
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		autoUpdate = true;
		buildFinished = getBuild().isFinished();
		retryDate = getBuild().getRetryDate();
		nextOffsets.clear();
		response.render(JavaScriptHeaderItem.forReference(new BuildStepsResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(String.format(
				"onedev.server.buildSteps.init('%s', %s, %s, %s, '%s', '%s', '%s', '%s', '%s', '%s');", steps.getMarkupId(),
				refresh.getCallbackFunction(CallbackParameter.explicit("offsets")),
				SecurityUtils.canRunJob(getProject(), getBuild().getJobName()) ? resume.getCallbackFunction() : "null", MAX_ENTRIES,
				escapeJavaScript(MessageFormat.format(_T("Showing the latest {0} entries. Download the log for all entries."),
						String.valueOf(MAX_ENTRIES))), escapeJavaScript(_T("No log entries")),
				escapeJavaScript(_T("Execution paused")), escapeJavaScript(_T("Resume")),
				escapeJavaScript(_T("No step logs available")), escapeJavaScript(_T("Loading..."))) + stepUpdateScript(false)));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessLog(getBuild());
	}

	public Component renderOptions(String componentId) {
		var fragment = new Fragment(componentId, "optionsFrag", this);
		var stickyActions = new WebMarkupContainer("stickyActions");
		stickyActions.add(newCancelLink("cancel"), newTerminalLink("terminal"));
		stickyActions.add(newBuildObserver(getBuild().getId()));
		fragment.add(stickyActions);
		fragment.add(new ResourceLink<Void>("download", new BuildLogResourceReference(),
				BuildLogResource.paramsOf(getProject().getId(), getBuild().getNumber(), null)));
		return fragment;
	}
}
