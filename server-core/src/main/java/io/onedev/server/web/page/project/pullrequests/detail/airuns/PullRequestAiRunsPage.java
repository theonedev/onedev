package io.onedev.server.web.page.project.pullrequests.detail.airuns;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.ai.dispatch.AiDispatchConversation;
import io.onedev.server.ai.dispatch.AiDispatchManager;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AiDispatchRunService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class PullRequestAiRunsPage extends PullRequestDetailPage {

private static final String PARAM_RUN = "run";

@Inject
private AiDispatchRunService runService;

@Inject
private AiDispatchManager dispatchManager;

public PullRequestAiRunsPage(PageParameters params) {
super(params);
}

@Override
protected String getRobotsMeta() {
return "noindex,nofollow";
}

@Override
protected void onInitialize() {
super.onInitialize();

var runsModel = new LoadableDetachableModel<List<AiDispatchRun>>() {
@Override
protected List<AiDispatchRun> load() {
return runService.query(getPullRequest());
}
};
var selectedRunModel = new LoadableDetachableModel<AiDispatchRun>() {
@Override
protected AiDispatchRun load() {
return selectRun(runsModel.getObject());
}
};
var feedModel = new LoadableDetachableModel<List<AiDispatchConversation.Message>>() {
@Override
protected List<AiDispatchConversation.Message> load() {
var run = selectedRunModel.getObject();
if (run != null)
return AiDispatchConversation.parseFeed(run.getLog());
return List.of();
}
};

var container = new WebMarkupContainer("container");
container.setOutputMarkupId(true);
add(container);

container.add(new FencedFeedbackPanel("feedback", container).setOutputMarkupPlaceholderTag(true));

container.add(new WebMarkupContainer("empty") {
@Override
protected void onConfigure() {
super.onConfigure();
setVisible(runsModel.getObject().isEmpty());
}
});

var workspace = new WebMarkupContainer("workspace") {
@Override
protected void onConfigure() {
super.onConfigure();
setVisible(!runsModel.getObject().isEmpty());
}
};
workspace.setOutputMarkupPlaceholderTag(true);
container.add(workspace);

workspace.add(new ListView<AiDispatchRun>("runs", runsModel) {
@Override
protected void populateItem(ListItem<AiDispatchRun> item) {
var run = item.getModelObject();
var summary = new BookmarkablePageLink<Void>("summary", PullRequestAiRunsPage.class,
paramsOf(getPullRequest(), run.getId()));
summary.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
@Override
protected String load() {
return isSelected(run, runsModel.getObject()) ? "active" : "";
}
}));
summary.add(new Label("agent", run.getAgent().getMentionName()));
summary.add(new Label("state", getStateLabel(run))
.add(AttributeAppender.append("class", getStateClass(run))));
summary.add(new Label("started", run.getStartedAt() != null
? DateUtils.formatAge(run.getStartedAt())
: DateUtils.formatAge(run.getCreatedAt())));
summary.add(new Label("commitCount", String.valueOf(run.getCommitShaList().size())));
summary.add(new Label("triggeredBy", run.getTriggeredBy().getDisplayName()));
summary.add(new Label("promptPreview",
StringUtils.abbreviate(StringUtils.defaultString(run.getPrompt()), 120)));
item.add(summary);
}
});

var selectedRun = new WebMarkupContainer("selectedRun") {
@Override
protected void onConfigure() {
super.onConfigure();
setVisible(selectedRunModel.getObject() != null);
}
};
selectedRun.setMarkupId("pull-request-ai-run-detail");
selectedRun.setOutputMarkupPlaceholderTag(true);
workspace.add(selectedRun);

selectedRun.add(new Label("agent", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? run.getAgent().getMentionName() : "";
}
}));
selectedRun.add(new Label("state", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? getStateLabel(run) : "";
}
}).add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? getStateClass(run) : "";
}
})));
selectedRun.add(new Label("requestReference", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
if (run == null)
return "";
return run.getRequest().getReference().toString(run.getRequest().getProject());
}
}));
selectedRun.add(new Label("started", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
if (run == null)
return "";
return run.getStartedAt() != null
? DateUtils.formatAge(run.getStartedAt())
: DateUtils.formatAge(run.getCreatedAt());
}
}));
selectedRun.add(new Label("commitCount", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? String.valueOf(run.getCommitShaList().size()) : "0";
}
}));
selectedRun.add(new Label("triggeredBy", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? run.getTriggeredBy().getDisplayName() : "";
}
}));
selectedRun.add(new Label("promptPreview", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? StringUtils.defaultString(run.getPrompt()) : "";
}
}));
selectedRun.add(new Link<Void>("openRequest") {
@Override
public void onClick() {
setResponsePage(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(getPullRequest()));
}
});
selectedRun.add(new Label("statusBanner", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
if (run == null)
return "";
return getStatusBannerText(run, feedModel.getObject());
}
}).add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? getStatusBannerClass(run) : "";
}
})));
selectedRun.add(new ListView<AiDispatchConversation.Message>("messages", feedModel) {
@Override
protected void populateItem(ListItem<AiDispatchConversation.Message> messageItem) {
var message = messageItem.getModelObject();
var run = selectedRunModel.getObject();
messageItem.add(AttributeAppender.append("class", getMessageClass(message)));
messageItem.add(new Label("speaker", run != null ? getMessageSpeaker(run, message) : "System"));
var eventTagText = getEventTag(message);
var eventTag = new Label("eventTag", StringUtils.defaultString(eventTagText));
eventTag.setVisible(StringUtils.isNotBlank(eventTagText));
eventTag.add(AttributeAppender.append("class", getEventTagClass(message)));
messageItem.add(eventTag);
messageItem.add(newMessageContent("content", message));
}
});

var terminalToggle = new AjaxLink<Void>("terminalToggle") {
@Override
public void onClick(AjaxRequestTarget target) {
target.appendJavaScript(
"var w=document.getElementById('pull-request-ai-run-tw');"
+ "if(w){w.style.display=w.style.display==='none'?'block':'none';"
+ "onedev.server.aiRun.initTerminalIfNeeded('pull-request-ai-run-detail');}");
}
};
selectedRun.add(terminalToggle);

var terminalWrapper = new WebMarkupContainer("terminalWrapper");
terminalWrapper.setMarkupId("pull-request-ai-run-tw");
terminalWrapper.setOutputMarkupId(true);
selectedRun.add(terminalWrapper);

var logData = new Label("logData", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
if (run == null)
return "";
return StringUtils.defaultString(AiDispatchConversation.stripMarkup(run.getLog()));
}
});
logData.setOutputMarkupId(true);
terminalWrapper.add(logData);

var terminal = new WebMarkupContainer("terminal");
terminal.setOutputMarkupId(true);
terminal.add(AttributeAppender.append("data-log-source", logData.getMarkupId()));
terminalWrapper.add(terminal);

var inputModel = Model.of("");
var inputForm = new Form<Void>("inputForm") {
@Override
protected void onConfigure() {
super.onConfigure();
setVisible(canGuide(selectedRunModel.getObject()));
}
};
inputForm.setOutputMarkupPlaceholderTag(true);
var input = new TextArea<String>("input", inputModel);
input.add(AttributeAppender.append("data-ai-guidance-key", new LoadableDetachableModel<String>() {
@Override
protected String load() {
var run = selectedRunModel.getObject();
return run != null ? String.valueOf(run.getId()) : "";
}
}));
inputForm.add(input);
inputForm.add(new AjaxButton("send", inputForm) {
@Override
protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
var run = selectedRunModel.getObject();
if (run != null) {
try {
dispatchManager.sendInput(run.getId(), inputModel.getObject());
inputModel.setObject("");
} catch (Exception e) {
error(e.getMessage());
}
}
target.add(container);
}

@Override
protected void onError(AjaxRequestTarget target, Form<?> form) {
target.add(container);
}
});
inputForm.add(new AjaxLink<Void>("end") {
@Override
public void onClick(AjaxRequestTarget target) {
var run = selectedRunModel.getObject();
if (run != null)
dispatchManager.cancel(run.getId(), "Session ended by user.");
target.add(container);
}
});
inputForm.add(AttributeAppender.append("class", "ai-run-composer"));
selectedRun.add(inputForm);
selectedRun.add(new WebMarkupContainer("guidanceUnavailable") {
@Override
protected void onConfigure() {
super.onConfigure();
var run = selectedRunModel.getObject();
setVisible(run != null && run.isActive() && SecurityUtils.canWriteCode(run.getRequest().getProject())
&& !canGuide(run));
}
});

container.add(new ChangeObserver() {
@Override
public void onObservableChanged(org.apache.wicket.core.request.handler.IPartialPageRequestHandler handler,
Collection<String> changedObservables) {
handler.prependJavaScript(
String.format("onedev.server.aiRun.preserveGuidance('%s');", container.getMarkupId()));
handler.add(container);
handler.appendJavaScript(String.format(
"onedev.server.aiRun.onDomReady('%s');onedev.server.aiRun.restoreGuidance('%s');",
container.getMarkupId(), container.getMarkupId()));
}

@Override
public Collection<String> findObservables() {
return Sets.newHashSet(AiDispatchRun.getChangeObservable(getPullRequest().getId()));
}
});
}

@Override
public void renderHead(IHeaderResponse response) {
super.renderHead(response);
response.render(JavaScriptHeaderItem.forReference(new PullRequestAiRunsResourceReference()));
response.render(OnDomReadyHeaderItem.forScript("onedev.server.aiRun.onDomReady();"));
}

private Component newMessageContent(String componentId, AiDispatchConversation.Message message) {
if (message.isAssistant() && message.getEventType() == AiDispatchConversation.EventType.MESSAGE)
return new MarkdownViewer(componentId, Model.of(message.getContent()), null);
return new MultilineLabel(componentId, message.getContent());
}

private String getMessageClass(AiDispatchConversation.Message message) {
return switch (message.getEventType()) {
case MESSAGE -> message.isUser() ? "user" : message.isAssistant() ? "assistant" : "system";
case PROGRESS -> "system progress";
case THINKING -> "assistant thinking";
case ERROR -> "system error";
case OUTPUT -> "system output";
case COMMAND -> "user command";
};
}

private String getMessageSpeaker(AiDispatchRun run, AiDispatchConversation.Message message) {
if (message.getEventType() == AiDispatchConversation.EventType.OUTPUT)
return "Terminal";
if (message.isUser())
return run.getTriggeredBy().getDisplayName();
if (message.isAssistant())
return "@" + run.getAgent().getMentionName();
return "System";
}

private String getStatusBannerText(AiDispatchRun run, List<AiDispatchConversation.Message> feed) {
if (run.isActive()) {
if (canGuide(run))
return "Awaiting guidance. The session is live and ready for follow-up prompts or slash commands.";
return "Live turn in progress. Streaming output will continue here until the agent yields control.";
}
if (run.getState() == AiDispatchRun.State.FAILED) {
for (int i = feed.size() - 1; i >= 0; i--) {
var message = feed.get(i);
if (message.getEventType() == AiDispatchConversation.EventType.ERROR)
return "Session failed: " + message.getContent();
}
return "Session failed. Open the raw terminal for provider and backend details.";
}
if (run.getState() == AiDispatchRun.State.CANCELLED)
return "Session ended before completion. If it was waiting for guidance, re-run it or start a follow-up task.";
if (run.getState() == AiDispatchRun.State.COMPLETED)
return "Session completed successfully. Review the live feed, raw terminal, and any PR changes.";
return "Session is queued and waiting for a worker.";
}

private String getStatusBannerClass(AiDispatchRun run) {
return "state-" + run.getState().name().toLowerCase();
}

private String getStateLabel(AiDispatchRun run) {
if (run.getState() == AiDispatchRun.State.RUNNING)
return canGuide(run) ? "AWAITING INPUT" : "RESPONDING";
return run.getState().name();
}

private String getEventTag(AiDispatchConversation.Message message) {
return switch (message.getEventType()) {
case MESSAGE -> null;
case PROGRESS -> "progress";
case THINKING -> "thinking";
case ERROR -> StringUtils.defaultIfBlank(message.getTag(), "error").toLowerCase().replace('_', ' ');
case OUTPUT -> "output";
case COMMAND -> "/" + StringUtils.defaultIfBlank(message.getTag(), "command");
};
}

private String getEventTagClass(AiDispatchConversation.Message message) {
return "event-" + message.getEventType().name().toLowerCase();
}

private String getStateClass(AiDispatchRun run) {
return switch (run.getState()) {
case QUEUED -> "badge badge-warning";
case RUNNING -> canGuide(run) ? "badge badge-primary" : "badge badge-info";
case COMPLETED -> "badge badge-success";
case FAILED -> "badge badge-danger";
case CANCELLED -> "badge badge-secondary";
};
}

private boolean canGuide(AiDispatchRun run) {
return run != null
&& run.isActive()
&& SecurityUtils.canWriteCode(run.getRequest().getProject())
&& dispatchManager.isInteractive(run.getId())
&& dispatchManager.isAcceptingInput(run.getId());
}

private boolean isSelected(AiDispatchRun run, List<AiDispatchRun> runs) {
var selected = selectRun(runs);
return selected != null && selected.getId().equals(run.getId());
}

private AiDispatchRun selectRun(List<AiDispatchRun> runs) {
var selectedRunId = resolveSelectedRunId(getPageParameters());
if (selectedRunId != null) {
for (var run : runs) {
if (run.getId().equals(selectedRunId))
return run;
}
}
for (var run : runs) {
if (run.isActive())
return run;
}
return runs.isEmpty() ? null : runs.get(0);
}

private static Long resolveSelectedRunId(PageParameters params) {
var value = StringUtils.trimToNull(params.get(PARAM_RUN).toOptionalString());
if (value == null)
return null;
try {
return Long.valueOf(value);
} catch (NumberFormatException ignored) {
return null;
}
}

public static PageParameters paramsOf(PullRequest request) {
return paramsOf(request, null);
}

public static PageParameters paramsOf(PullRequest request, Long runId) {
var params = PullRequestDetailPage.paramsOf(request);
if (runId != null)
params.add(PARAM_RUN, runId);
return params;
}

}
