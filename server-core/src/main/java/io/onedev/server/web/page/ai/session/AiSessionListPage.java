package io.onedev.server.web.page.ai.session;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.ai.dispatch.AiDispatchAgent;
import io.onedev.server.ai.dispatch.AiDispatchConversation;
import io.onedev.server.ai.dispatch.AiDispatchManager;
import io.onedev.server.ai.dispatch.AiDispatchModelUtils;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AiDispatchRunService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.airuns.PullRequestAiRunsPage;
import io.onedev.server.web.page.project.pullrequests.detail.airuns.PullRequestAiRunsResourceReference;

public class AiSessionListPage extends MyPage {

private static final String PARAM_FILTER = "filter";

private static final String PARAM_TAB = "tab";

private static final String PARAM_AGENT = "agent";

private static final String PARAM_PROJECT = "project";

private static final String PARAM_STATUS = "status";

public AiSessionListPage(PageParameters params) {
super(params);
}

@Override
protected void onInitialize() {
super.onInitialize();

add(tabLink("showSessionsTab", Tab.SESSIONS));
add(tabLink("showAgentsTab", Tab.AGENTS));

var sessionsTab = new WebMarkupContainer("sessionsTab");
sessionsTab.setOutputMarkupPlaceholderTag(true);
sessionsTab.setVisible(getTab() == Tab.SESSIONS);
add(sessionsTab);

		var scopedRunsModel = new LoadableDetachableModel<List<AiDispatchRun>>() {
			@Override
			protected List<AiDispatchRun> load() {
				var projectPath = getProjectPath();
				var project = projectPath != null ? findProjectByPath(projectPath) : null;
				return getRunService().queryRecent(getAgentFilter().toAgent(), getRunStateFilter().toStates()).stream()
						.filter(it -> SecurityUtils.canReadCode(it.getRequest().getProject()))
						.filter(it -> project == null || project.equals(it.getRequest().getProject()))
						.toList();
			}
		};

		var statsModel = new LoadableDetachableModel<SessionStats>() {
			@Override
			protected SessionStats load() {
				long active = 0;
				long completed = 0;
				long failed = 0;
				for (var run: scopedRunsModel.getObject()) {
					if (run.isActive())
						active++;
					else if (run.getState() == AiDispatchRun.State.COMPLETED)
						completed++;
					else if (run.getState() == AiDispatchRun.State.FAILED)
						failed++;
				}
				return new SessionStats(active, completed, failed);
			}
		};

		var runsModel = new LoadableDetachableModel<List<AiDispatchRun>>() {
			@Override
			protected List<AiDispatchRun> load() {
				var filter = getFilter();
				return scopedRunsModel.getObject().stream()
						.filter(it -> switch (filter) {
						case ACTIVE -> it.isActive();
						case HISTORY -> !it.isActive();
						case ALL -> true;
						})
						.toList();
			}
		};

		sessionsTab.add(filterLink("showActive", Filter.ACTIVE));
		sessionsTab.add(filterLink("showHistory", Filter.HISTORY));
		sessionsTab.add(filterLink("showAll", Filter.ALL));

		var selectedAgentFilter = Model.of(getAgentFilter());
		var selectedProjectFilter = Model.of(StringUtils.defaultString(getProjectPath()));
		var selectedRunStateFilter = Model.of(getRunStateFilter());

		var agentFilter = new DropDownChoice<>("agentFilter", selectedAgentFilter,
				Arrays.asList(AgentFilter.values()), new ChoiceRenderer<>("displayName"));
		agentFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(AiSessionListPage.class, paramsOf(getFilter(), getTab(),
						selectedAgentFilter.getObject(), StringUtils.trimToNull(selectedProjectFilter.getObject()),
						selectedRunStateFilter.getObject()));
			}
		});
		sessionsTab.add(agentFilter);

		var projectFilterChoices = new LoadableDetachableModel<List<String>>() {
			@Override
			protected List<String> load() {
				var choices = getReadableProjects().stream()
						.map(Project::getPath)
						.sorted()
						.collect(Collectors.toList());
				choices.add(0, "");
				return choices;
			}
		};
		var projectFilter = new DropDownChoice<>("projectFilter", selectedProjectFilter, projectFilterChoices,
				new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(String object) {
						return StringUtils.isBlank(object) ? _T("All projects") : object;
					}
				});
		projectFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(AiSessionListPage.class, paramsOf(getFilter(), getTab(),
						selectedAgentFilter.getObject(), StringUtils.trimToNull(selectedProjectFilter.getObject()),
						selectedRunStateFilter.getObject()));
			}
		});
		sessionsTab.add(projectFilter);

		var runStateFilter = new DropDownChoice<>("runStateFilter", selectedRunStateFilter,
				Arrays.asList(RunStateFilter.values()), new ChoiceRenderer<>("displayName"));
		runStateFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(AiSessionListPage.class, paramsOf(getFilter(), getTab(),
						selectedAgentFilter.getObject(), StringUtils.trimToNull(selectedProjectFilter.getObject()),
						selectedRunStateFilter.getObject()));
			}
		});
		sessionsTab.add(runStateFilter);

		sessionsTab.add(new AjaxLink<Void>("clearFilters") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(AiSessionListPage.class, paramsOf(getFilter(), getTab(),
						AgentFilter.ALL, null, RunStateFilter.ALL));
			}
		});

// --- New Task Form ---
var newTaskPanel = new WebMarkupContainer("newTaskPanel");
newTaskPanel.setOutputMarkupId(true);
sessionsTab.add(newTaskPanel);

sessionsTab.add(new AjaxLink<Void>("newTaskToggle") {
@Override
public void onClick(AjaxRequestTarget target) {
target.appendJavaScript(String.format(
"var p=document.getElementById('%s');if(p){p.style.display=p.style.display==='none'?'block':'none';}",
newTaskPanel.getMarkupId()));
}
});

var newTaskForm = new Form<Void>("newTaskForm");
newTaskPanel.add(newTaskForm);
newTaskForm.add(new FencedFeedbackPanel("newTaskFeedback", newTaskForm).setOutputMarkupPlaceholderTag(true));

// Project selector
var writableProjects = getWritableProjects();
var projectNames = writableProjects.stream().map(Project::getPath).sorted().collect(Collectors.toList());
var selectedProject = Model.of(projectNames.isEmpty() ? "" : projectNames.get(0));
var projectSelect = new DropDownChoice<>("projectSelect", selectedProject, projectNames);
projectSelect.setOutputMarkupId(true);
newTaskForm.add(projectSelect);

// Branch selector — updates when project changes
var branchChoices = new LoadableDetachableModel<List<String>>() {
@Override
protected List<String> load() {
var projName = selectedProject.getObject();
if (StringUtils.isBlank(projName)) return List.of();
var project = findProjectByPath(projName);
if (project == null) return List.of();
try {
return project.getBranchRefs().stream()
.map(ref -> io.onedev.server.git.GitUtils.ref2branch(ref.getName()))
.sorted()
.collect(Collectors.toList());
} catch (Exception e) {
return List.of();
}
}
};
var selectedBranch = Model.of("");
var branchSelect = new DropDownChoice<>("branchSelect", selectedBranch, branchChoices);
branchSelect.setOutputMarkupId(true);
newTaskForm.add(branchSelect);

// Update branch list when project changes
projectSelect.add(new AjaxFormComponentUpdatingBehavior("change") {
@Override
protected void onUpdate(AjaxRequestTarget target) {
branchChoices.detach();
var branches = branchChoices.getObject();
if (!branches.isEmpty()) {
var project = findProjectByPath(selectedProject.getObject());
var defaultBranch = project != null ? project.getDefaultBranch() : null;
selectedBranch.setObject(defaultBranch != null && branches.contains(defaultBranch)
? defaultBranch : branches.get(0));
} else {
selectedBranch.setObject("");
}
target.add(branchSelect);
}
});

// Set initial branch
if (!projectNames.isEmpty()) {
var initProject = findProjectByPath(projectNames.get(0));
if (initProject != null) {
var defaultBranch = initProject.getDefaultBranch();
selectedBranch.setObject(defaultBranch != null ? defaultBranch : "");
}
}

		// Agent selector
		var agentNames = Arrays.stream(AiDispatchAgent.values())
		.map(AiDispatchAgent::getMentionName)
		.collect(Collectors.toList());
		var selectedAgent = Model.of(agentNames.isEmpty() ? "" : agentNames.get(0));
		var agentSelect = new DropDownChoice<>("agentSelect", selectedAgent, agentNames);
		agentSelect.setOutputMarkupId(true);
		newTaskForm.add(agentSelect);

		var selectedModel = Model.of("");
		var modelChoices = new LoadableDetachableModel<List<String>>() {
			@Override
			protected List<String> load() {
				var choices = getAvailableModels(AiDispatchAgent.fromMentionName(selectedAgent.getObject()));
				choices.add(0, "");
				return choices;
			}
		};
		var modelSelect = new DropDownChoice<>("modelSelect", selectedModel, modelChoices,
				new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(String object) {
						return StringUtils.isBlank(object) ? _T("Default configured model") : object;
					}
				});
		modelSelect.setOutputMarkupId(true);
		newTaskForm.add(modelSelect);
		var modelHelp = new Label("modelHelp", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				var agent = AiDispatchAgent.fromMentionName(selectedAgent.getObject());
				if (agent == null)
					return _T("Leave unset to use the configured default model.");
				return _T("Leave unset to use the configured default for @") + agent.getMentionName()
						+ _T(". CLI backends receive --model when supported.");
			}
		});
		modelHelp.setOutputMarkupId(true);
		newTaskForm.add(modelHelp);
		agentSelect.add(new AjaxFormComponentUpdatingBehavior("change") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				modelChoices.detach();
				if (!modelChoices.getObject().contains(selectedModel.getObject()))
					selectedModel.setObject("");
				target.add(modelSelect);
				target.add(modelHelp);
			}
		});

		// Prompt
		var promptModel = Model.of("");
		newTaskForm.add(new TextArea<>("promptInput", promptModel));

// Start button
newTaskForm.add(new AjaxButton("startTask", newTaskForm) {
@Override
protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
try {
				var projName = selectedProject.getObject();
				var branch = selectedBranch.getObject();
				var agentName = selectedAgent.getObject();
				var modelName = StringUtils.trimToNull(selectedModel.getObject());
				var prompt = promptModel.getObject();

if (StringUtils.isBlank(projName))
throw new IllegalStateException("Please select a project.");
if (StringUtils.isBlank(branch))
throw new IllegalStateException("Please select a base branch.");
if (StringUtils.isBlank(prompt))
throw new IllegalStateException("Please enter a task prompt.");

var project = findProjectByPath(projName);
if (project == null)
throw new IllegalStateException("Project not found: " + projName);

var agent = AiDispatchAgent.fromMentionName(agentName);
if (agent == null)
throw new IllegalStateException("Unknown agent: " + agentName);

var user = SecurityUtils.getUser();
if (user == null)
throw new IllegalStateException("You must be logged in.");

				getDispatchManager().startFromConsole(project, branch, agent, modelName, prompt, user);
				promptModel.setObject("");
				selectedModel.setObject("");
				getSession().success(_T("AI task started. It will appear in the sessions list."));
				setResponsePage(AiSessionListPage.class, paramsOf(Filter.ACTIVE, getTab(),
						getAgentFilter(), getProjectPath(), getRunStateFilter()));
} catch (Exception e) {
newTaskForm.error(e.getMessage());
target.add(newTaskForm);
}
}

@Override
protected void onError(AjaxRequestTarget target, Form<?> form) {
target.add(newTaskForm);
}
});

newTaskForm.add(new AjaxLink<Void>("cancelNewTask") {
@Override
public void onClick(AjaxRequestTarget target) {
target.appendJavaScript(String.format(
"var p=document.getElementById('%s');if(p){p.style.display='none';}",
newTaskPanel.getMarkupId()));
}
});
// --- End New Task Form ---

		var container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		sessionsTab.add(container);

		container.add(new Label("activeCount", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return String.valueOf(statsModel.getObject().active());
			}
		}));
		container.add(new Label("completedCount", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return String.valueOf(statsModel.getObject().completed());
			}
		}));
		container.add(new Label("failedCount", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return String.valueOf(statsModel.getObject().failed());
			}
		}));

		container.add(new FencedFeedbackPanel("feedback", container).setOutputMarkupPlaceholderTag(true));

container.add(new WebMarkupContainer("empty") {
@Override
protected void onConfigure() {
super.onConfigure();
setVisible(runsModel.getObject().isEmpty());
}
});

container.add(new ListView<AiDispatchRun>("runs", runsModel) {
@Override
protected void populateItem(ListItem<AiDispatchRun> item) {
var run = item.getModelObject();
item.setMarkupId(run.getAnchor());
item.setOutputMarkupId(true);

// Summary header (always visible, click to expand)
var summary = new AjaxLink<Void>("summary") {
    @Override
    public void onClick(AjaxRequestTarget target) {
        target.appendJavaScript(String.format(
            "var d=document.getElementById('%s');if(d){d.classList.toggle('expanded');"
            + "onedev.server.aiRun.initTerminalIfNeeded('%s');}",
            item.getMarkupId() + "-detail", item.getMarkupId() + "-detail"));
    }
};
summary.add(new Label("agent", run.getAgent().getMentionName()));
summary.add(new Label("state", getStateLabel(run))
    .add(AttributeAppender.append("class", getStateClass(run))));
var requestLink = new BookmarkablePageLink<Void>("requestLink", PullRequestActivitiesPage.class,
    PullRequestActivitiesPage.paramsOf(run.getRequest()));
requestLink.add(new Label("projectPath", run.getRequest().getProject().getPath()));
requestLink.add(new Label("requestReference",
    run.getRequest().getReference().toString(run.getRequest().getProject())));
summary.add(requestLink);
summary.add(new Label("started", run.getStartedAt() != null
		    ? DateUtils.formatAge(run.getStartedAt())
		    : DateUtils.formatAge(run.getCreatedAt())));
		summary.add(new Label("commitCount", String.valueOf(run.getCommitShaList().size())));
		summary.add(new Label("modelMeta",
				StringUtils.isNotBlank(run.getModelName()) ? "model " + run.getModelName() + " ·" : ""));
		summary.add(new Label("triggeredBy", run.getTriggeredBy().getDisplayName()));
		summary.add(new Label("promptPreview",
		    StringUtils.abbreviate(StringUtils.defaultString(run.getPrompt()), 120)));
item.add(summary);

// Detail panel (hidden by default, expanded for active runs)
var detail = new WebMarkupContainer("detail");
detail.setMarkupId(item.getMarkupId() + "-detail");
detail.setOutputMarkupId(true);
if (run.isActive())
    detail.add(AttributeAppender.append("class", "expanded"));
item.add(detail);

var feedModel = new LoadableDetachableModel<List<AiDispatchConversation.Message>>() {
    @Override
    protected List<AiDispatchConversation.Message> load() {
        return AiDispatchConversation.parseFeed(run.getLog());
    }
};

detail.add(new Label("statusBanner", new LoadableDetachableModel<String>() {
    @Override
    protected String load() {
        return getStatusBannerText(run, feedModel.getObject());
    }
}).add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
    @Override
    protected String load() {
        return getStatusBannerClass(run);
    }
})));
detail.add(new BookmarkablePageLink<Void>("openRequest", PullRequestActivitiesPage.class,
    PullRequestActivitiesPage.paramsOf(run.getRequest())));
detail.add(new BookmarkablePageLink<Void>("openAiRun", PullRequestAiRunsPage.class,
    PullRequestAiRunsPage.paramsOf(run.getRequest(), run.getId())));

detail.add(new ListView<AiDispatchConversation.Message>("messages", feedModel) {
    @Override
    protected void populateItem(ListItem<AiDispatchConversation.Message> messageItem) {
        var message = messageItem.getModelObject();
        messageItem.add(AttributeAppender.append("class", getMessageClass(message)));
        messageItem.add(new Label("speaker", getMessageSpeaker(run, message)));
        var eventTag = new Label("eventTag", StringUtils.defaultString(getEventTag(message)));
        eventTag.setVisible(StringUtils.isNotBlank(getEventTag(message)));
        eventTag.add(AttributeAppender.append("class", getEventTagClass(message)));
        messageItem.add(eventTag);
        messageItem.add(newMessageContent("content", message));
    }
});

// Terminal toggle
var terminalToggle = new AjaxLink<Void>("terminalToggle") {
    @Override
    public void onClick(AjaxRequestTarget target) {
        target.appendJavaScript(String.format(
            "var w=document.getElementById('%s');"
            + "if(w){w.style.display=w.style.display==='none'?'block':'none';"
            + "onedev.server.aiRun.initTerminalIfNeeded('%s');}",
            item.getMarkupId() + "-tw", item.getMarkupId() + "-detail"));
    }
};
detail.add(terminalToggle);

var terminalWrapper = new WebMarkupContainer("terminalWrapper");
terminalWrapper.setMarkupId(item.getMarkupId() + "-tw");
terminalWrapper.setOutputMarkupId(true);
detail.add(terminalWrapper);

var logData = new Label("logData",
    StringUtils.defaultString(AiDispatchConversation.stripMarkup(run.getLog())));
logData.setOutputMarkupId(true);
terminalWrapper.add(logData);
var terminal = new WebMarkupContainer("terminal");
terminal.setOutputMarkupId(true);
terminal.add(AttributeAppender.append("data-log-source", logData.getMarkupId()));
terminalWrapper.add(terminal);

var inputModel = Model.of("");
var inputForm = new Form<Void>("inputForm");
inputForm.setOutputMarkupPlaceholderTag(true);
var input = new TextArea<String>("input", inputModel);
input.add(AttributeAppender.append("data-ai-guidance-key", String.valueOf(run.getId())));
inputForm.add(input);
inputForm.add(new AjaxButton("send", inputForm) {
    @Override
    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        try {
            getDispatchManager().sendInput(run.getId(), inputModel.getObject());
            inputModel.setObject("");
        } catch (Exception e) {
            error(e.getMessage());
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
        getDispatchManager().cancel(run.getId(), "Session ended by user.");
        target.add(container);
    }
});
inputForm.setVisible(canGuide(run));
detail.add(inputForm);
detail.add(new WebMarkupContainer("guidanceUnavailable") {
    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(run.isActive() && SecurityUtils.canWriteCode(run.getRequest().getProject())
            && !canGuide(run));
    }
});
}
});

container.add(new ChangeObserver() {
@Override
public void onObservableChanged(org.apache.wicket.core.request.handler.IPartialPageRequestHandler handler,
Collection<String> changedObservables) {
handler.prependJavaScript(
String.format("onedev.server.aiRun.preserveGuidance('%s');", container.getMarkupId()));
handler.add(container);
handler.appendJavaScript(
String.format("onedev.server.aiRun.restoreGuidance('%s');", container.getMarkupId()));
}

@Override
protected Collection<String> findObservables() {
return Sets.newHashSet(AiDispatchRun.getSessionsChangeObservable());
}
});

var agentsTab = new WebMarkupContainer("agentsTab");
agentsTab.setOutputMarkupPlaceholderTag(true);
agentsTab.setVisible(getTab() == Tab.AGENTS);
add(agentsTab);

agentsTab.add(new FencedFeedbackPanel("agentsFeedback", agentsTab).setOutputMarkupPlaceholderTag(true));
agentsTab.add(new WebMarkupContainer("adminRequired") {
@Override
protected void onConfigure() {
super.onConfigure();
setVisible(!SecurityUtils.isAdministrator());
}
});

AiSetting aiSetting = getSettingService().getAiSetting();
var oldAuditContent = VersionedXmlDoc.fromBean(aiSetting).toXML();
var agentForm = new Form<Void>("agentForm") {
@Override
protected void onSubmit() {
super.onSubmit();
var newAuditContent = VersionedXmlDoc.fromBean(aiSetting).toXML();
getSettingService().saveAiSetting(aiSetting);
auditService.audit(null, "changed AI dispatch settings via AI console",
oldAuditContent, newAuditContent);
getSession().success(_T("AI agent settings have been saved"));
setResponsePage(AiSessionListPage.class, paramsOf(getFilter(), Tab.AGENTS));
}
};
agentForm.setVisible(SecurityUtils.isAdministrator());
agentForm.add(BeanContext.edit("agentEditor", aiSetting, Set.of(
AiSetting.PROP_DISPATCH_ENABLED,
AiSetting.PROP_LITE_MODEL_SETTING,
AiSetting.PROP_CLAUDE_DISPATCH_SETTING,
AiSetting.PROP_COPILOT_DISPATCH_SETTING,
AiSetting.PROP_COPILOT_API_SETTING,
AiSetting.PROP_CODEX_DISPATCH_SETTING,
AiSetting.PROP_MAX_DISPATCH_SESSIONS,
AiSetting.PROP_DISPATCH_TIMEOUT_MINUTES), false));
agentsTab.add(agentForm);
}

@Override
public void renderHead(IHeaderResponse response) {
super.renderHead(response);
response.render(JavaScriptHeaderItem.forReference(new PullRequestAiRunsResourceReference()));
}

@Override
protected Component newTopbarTitle(String componentId) {
return new Label(componentId, _T("AI Console"));
}

@Override
protected String getPageTitle() {
return _T("AI Console");
}

@Override
protected String getRobotsMeta() {
return "noindex,nofollow";
}

	private BookmarkablePageLink<Void> filterLink(String componentId, Filter filter) {
	var link = new BookmarkablePageLink<Void>(componentId, AiSessionListPage.class,
			paramsOf(filter, getTab(), getAgentFilter(), getProjectPath(), getRunStateFilter()));
	if (getFilter() == filter)
	link.add(AttributeAppender.append("class", "active"));
	return link;
	}

	private BookmarkablePageLink<Void> tabLink(String componentId, Tab tab) {
	var link = new BookmarkablePageLink<Void>(componentId, AiSessionListPage.class,
			paramsOf(getFilter(), tab, getAgentFilter(), getProjectPath(), getRunStateFilter()));
	if (getTab() == tab)
	link.add(AttributeAppender.append("class", "active"));
	return link;
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

@Nullable
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

private String getStatusBannerText(AiDispatchRun run, List<AiDispatchConversation.Message> feed) {
	if (run.isActive()) {
		if (canGuide(run))
			return _T("Awaiting guidance. The session is live and ready for follow-up prompts or slash commands.");
		return _T("Live turn in progress. Streaming output will continue here until the agent yields control.");
	}
	if (run.getState() == AiDispatchRun.State.FAILED) {
		for (int i = feed.size() - 1; i >= 0; i--) {
			var message = feed.get(i);
			if (message.getEventType() == AiDispatchConversation.EventType.ERROR)
				return _T("Session failed: ") + message.getContent();
		}
		return _T("Session failed. Open the raw terminal for provider and backend details.");
	}
	if (run.getState() == AiDispatchRun.State.CANCELLED)
		return _T("Session ended before completion. If it was waiting for guidance, re-run it or start a follow-up task.");
	if (run.getState() == AiDispatchRun.State.COMPLETED)
		return _T("Session completed successfully. Review the live feed, raw terminal, and any PR changes.");
	return _T("Session is queued and waiting for a worker.");
}

private String getStatusBannerClass(AiDispatchRun run) {
	return "state-" + run.getState().name().toLowerCase();
}

private String getStateLabel(AiDispatchRun run) {
	if (run.getState() == AiDispatchRun.State.RUNNING)
		return canGuide(run) ? "AWAITING INPUT" : "RESPONDING";
	return run.getState().name();
}

private boolean canGuide(AiDispatchRun run) {
return run.isActive()
&& SecurityUtils.canWriteCode(run.getRequest().getProject())
&& getDispatchManager().isInteractive(run.getId())
&& getDispatchManager().isAcceptingInput(run.getId());
}

private AiDispatchRunService getRunService() {
return OneDev.getInstance(AiDispatchRunService.class);
}

private AiDispatchManager getDispatchManager() {
return OneDev.getInstance(AiDispatchManager.class);
}

private SettingService getSettingService() {
return OneDev.getInstance(SettingService.class);
}

private ProjectService getProjectService() {
return OneDev.getInstance(ProjectService.class);
}

	private List<Project> getWritableProjects() {
	return getProjectService().query(true).stream()
	.filter(p -> SecurityUtils.canWriteCode(p))
	.collect(Collectors.toList());
	}

	private List<Project> getReadableProjects() {
		return getProjectService().query(true).stream()
				.filter(SecurityUtils::canReadCode)
				.collect(Collectors.toList());
	}

	private List<String> getAvailableModels(@Nullable AiDispatchAgent agent) {
		return AiDispatchModelUtils.availableModels(getSettingService().getAiSetting(), agent);
	}

@Nullable
private Project findProjectByPath(String path) {
return getProjectService().findByPath(path);
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

	private Filter getFilter() {
	return resolveFilter(getPageParameters());
	}

	private AgentFilter getAgentFilter() {
		return resolveAgentFilter(getPageParameters());
	}

	@Nullable
	private String getProjectPath() {
		return resolveProjectPath(getPageParameters());
	}

	private RunStateFilter getRunStateFilter() {
		return resolveRunStateFilter(getPageParameters());
	}

	private Tab getTab() {
	return resolveTab(getPageParameters());
	}

static Filter resolveFilter(PageParameters params) {
return findEnum(Filter.class, params.get(PARAM_FILTER).toOptionalString(), Filter.ACTIVE);
}

	static Tab resolveTab(PageParameters params) {
	return findEnum(Tab.class, params.get(PARAM_TAB).toOptionalString(), Tab.SESSIONS);
	}

	static AgentFilter resolveAgentFilter(PageParameters params) {
		return findEnum(AgentFilter.class, params.get(PARAM_AGENT).toOptionalString(), AgentFilter.ALL);
	}

	static RunStateFilter resolveRunStateFilter(PageParameters params) {
		return findEnum(RunStateFilter.class, params.get(PARAM_STATUS).toOptionalString(), RunStateFilter.ALL);
	}

	@Nullable
	static String resolveProjectPath(PageParameters params) {
		return StringUtils.trimToNull(params.get(PARAM_PROJECT).toOptionalString());
	}

private static <T extends Enum<T>> T findEnum(Class<T> enumClass, @Nullable String value, T defaultValue) {
if (value != null) {
for (var each: enumClass.getEnumConstants()) {
if (each.name().equalsIgnoreCase(value))
return each;
}
}
return defaultValue;
}

	public static PageParameters paramsOf(Filter filter) {
	return paramsOf(filter, Tab.SESSIONS);
	}

	public static PageParameters paramsOf(Filter filter, Tab tab) {
		return paramsOf(filter, tab, AgentFilter.ALL, null, RunStateFilter.ALL);
	}

	public static PageParameters paramsOf(Filter filter, Tab tab, AgentFilter agentFilter,
										  @Nullable String projectPath, RunStateFilter runStateFilter) {
		var params = new PageParameters();
		params.add(PARAM_FILTER, filter.name().toLowerCase());
		params.add(PARAM_TAB, tab.name().toLowerCase());
		if (agentFilter != AgentFilter.ALL)
			params.add(PARAM_AGENT, agentFilter.name().toLowerCase());
		if (StringUtils.isNotBlank(projectPath))
			params.add(PARAM_PROJECT, projectPath);
		if (runStateFilter != RunStateFilter.ALL)
			params.add(PARAM_STATUS, runStateFilter.name().toLowerCase());
		return params;
	}

enum Filter {
ACTIVE,
HISTORY,
ALL
}

	enum Tab {
	SESSIONS,
	AGENTS
	}

	enum AgentFilter {
		ALL(null, "All agents"),
		CLAUDE(AiDispatchAgent.CLAUDE, "@claude"),
		COPILOT(AiDispatchAgent.COPILOT, "@copilot"),
		CODEX(AiDispatchAgent.CODEX, "@codex");

		private final AiDispatchAgent agent;

		private final String displayName;

		AgentFilter(AiDispatchAgent agent, String displayName) {
			this.agent = agent;
			this.displayName = displayName;
		}

		@Nullable
		AiDispatchAgent toAgent() {
			return agent;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	enum RunStateFilter {
		ALL(null, "All statuses"),
		QUEUED(AiDispatchRun.State.QUEUED, "Queued"),
		RUNNING(AiDispatchRun.State.RUNNING, "Running"),
		COMPLETED(AiDispatchRun.State.COMPLETED, "Completed"),
		FAILED(AiDispatchRun.State.FAILED, "Failed"),
		CANCELLED(AiDispatchRun.State.CANCELLED, "Cancelled");

		private final AiDispatchRun.State state;

		private final String displayName;

		RunStateFilter(AiDispatchRun.State state, String displayName) {
			this.state = state;
			this.displayName = displayName;
		}

		@Nullable
		Collection<AiDispatchRun.State> toStates() {
			return state != null ? List.of(state) : null;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	private record SessionStats(long active, long completed, long failed) {
	}

}
