package io.onedev.server.ai.dispatch;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.AiDispatchRunService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.ManagedFutureService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.websocket.WebSocketService;

@Singleton
public class AiDispatchManager {

private static final Logger logger = LoggerFactory.getLogger(AiDispatchManager.class);

private final AiDispatchCommentParser parser;

private final AiDispatchRunService runService;

private final AiDispatchWorktreeManager worktreeManager;

private final AiDispatchSessionLauncher sessionLauncher;

private final ManagedFutureService managedFutureService;

private final SettingService settingService;

private final WebSocketService webSocketService;

private final ExecutorService executorService;

private final UserService userService;

private final PullRequestCommentService pullRequestCommentService;

private final PullRequestService pullRequestService;

private final IssueCommentService issueCommentService;

private final UrlService urlService;

private final GitService gitService;

private final TransactionService transactionService;

private final Map<Long, ActiveSession> activeSessions = new ConcurrentHashMap<>();

@Inject
	public AiDispatchManager(AiDispatchCommentParser parser, AiDispatchRunService runService,
	AiDispatchWorktreeManager worktreeManager, AiDispatchSessionLauncher sessionLauncher,
	ManagedFutureService managedFutureService, SettingService settingService,
	WebSocketService webSocketService, ExecutorService executorService,
	UserService userService, PullRequestCommentService pullRequestCommentService,
	PullRequestService pullRequestService, IssueCommentService issueCommentService,
	UrlService urlService, GitService gitService,
	TransactionService transactionService) {
this.parser = parser;
this.runService = runService;
this.worktreeManager = worktreeManager;
this.sessionLauncher = sessionLauncher;
this.managedFutureService = managedFutureService;
this.settingService = settingService;
this.webSocketService = webSocketService;
this.executorService = executorService;
this.userService = userService;
	this.pullRequestCommentService = pullRequestCommentService;
	this.pullRequestService = pullRequestService;
	this.issueCommentService = issueCommentService;
	this.urlService = urlService;
	this.gitService = gitService;
	this.transactionService = transactionService;
}

@Listen
public void on(SystemStarted event) {
for (var run: runService.queryUnfinished()) {
if (run.getWorktreePath() != null) {
var prepared = new AiDispatchWorktreeManager.PreparedWorktree(null,
new java.io.File(run.getWorktreePath()), null);
worktreeManager.cleanup(prepared);
}
transactionService.run(() -> {
var managedRun = runService.load(run.getId());
managedRun.appendLog("[system] Session closed because the server restarted.\n");
managedRun.appendLog(AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.SYSTEM,
"Session closed because the server restarted."));
managedRun.setState(AiDispatchRun.State.CANCELLED);
managedRun.setCompletedAt(new Date());
runService.update(managedRun);
notifyChange(managedRun);
});
}
}

@Listen
public void on(SystemStopping event) {
for (var runId: activeSessions.keySet())
cancel(runId, "Session cancelled because the server is stopping.");
}

public void dispatch(PullRequestComment comment) {
var commandOpt = parser.parse(comment.getContent());
if (commandOpt.isEmpty() || comment.getUser().isSystem())
return;

AiSetting aiSetting = settingService.getAiSetting();
if (!aiSetting.isDispatchEnabled())
createRejectedRun(comment, commandOpt.get(), "AI dispatch is disabled by administrator.");
else if (runService.countActive() >= aiSetting.getMaxDispatchSessions())
createRejectedRun(comment, commandOpt.get(), "Maximum concurrent AI dispatch sessions reached.");
	else
	start(comment, commandOpt.get(), aiSetting);
	}

	public void dispatch(IssueComment comment) {
		var commandOpt = parser.parse(comment.getContent());
		if (commandOpt.isEmpty() || comment.getUser().isSystem())
			return;

		AiSetting aiSetting = settingService.getAiSetting();
		if (!aiSetting.isDispatchEnabled()) {
			postIssueDispatchComment(comment.getIssue(),
					"AI task was not started because AI dispatch is disabled by administrator.");
			return;
		}
		if (runService.countActive() >= aiSetting.getMaxDispatchSessions()) {
			postIssueDispatchComment(comment.getIssue(),
					"AI task was not started because maximum concurrent AI dispatch sessions has been reached.");
			return;
		}

		try {
			var command = commandOpt.get();
			var prompt = AiDispatchPromptUtils.buildIssuePrompt(comment.getIssue(), command.getPrompt());
			var request = startFromIssue(comment.getIssue(), command.getAgent(), command.getModelName(), prompt,
					comment.getUser());
			postIssueDispatchComment(comment.getIssue(),
					"Started @" + command.getAgent().getMentionName() + " task in draft pull request ["
							+ request.getReference().toString(comment.getIssue().getProject()) + "]("
							+ urlService.urlFor(request, true) + ").");
		} catch (Exception e) {
			logger.error("Error starting issue AI dispatch session", e);
			postIssueDispatchComment(comment.getIssue(),
					"AI task failed to start: " + StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
		}
	}

/**
 * Start a dispatch session from the AI Console (no existing PR comment required).
 * Creates a new branch, draft PR, and dispatches the agent.
 */
	public void startFromConsole(Project project, String baseBranch, AiDispatchAgent agent, String prompt, User triggeredBy) {
		startFromConsole(project, baseBranch, agent, null, prompt, triggeredBy);
	}

	public void startFromConsole(Project project, String baseBranch, AiDispatchAgent agent,
								 @Nullable String modelName, String prompt, User triggeredBy) {
		AiSetting aiSetting = requireDispatchCapacity();
		var branchName = "ai/" + agent.getMentionName() + "/" + buildTimestamp();

		var firstLine = prompt.contains("\n") ? prompt.substring(0, prompt.indexOf('\n')).strip() : prompt.strip();
		var prTitle = StringUtils.abbreviate("[AI] " + firstLine, 255);
		var pr = createDispatchRequest(project, baseBranch, branchName, prTitle,
				"AI dispatch task started from the AI Console.\n\n**Agent:** @" + agent.getMentionName()
						+ (StringUtils.isNotBlank(modelName) ? "\n**Model:** `" + modelName + "`" : "")
						+ "\n**Prompt:**\n" + prompt,
				triggeredBy);

		// Create an AiDispatchRun without a comment (console-initiated)
		var command = new AiDispatchCommand(agent, List.of(), prompt, modelName);
		start(pr, command, aiSetting, triggeredBy);
	}

	public PullRequest startFromIssue(Issue issue, AiDispatchAgent agent, @Nullable String modelName,
									  String prompt, User triggeredBy) {
		AiSetting aiSetting = requireDispatchCapacity();
		var project = issue.getProject();
		var baseBranch = StringUtils.trimToNull(project.getDefaultBranch());
		if (baseBranch == null)
			throw new IllegalStateException("Project default branch is not configured.");
		var branchName = "ai/" + agent.getMentionName() + "/issue-" + issue.getNumber() + "-" + buildTimestamp();
		var title = StringUtils.abbreviate("[AI] " + issue.getTitle(), 255);
		var issueReference = issue.getReference().toString(project);
		var description = "AI dispatch task started from issue " + issueReference + ".\n\n"
				+ "**Agent:** @" + agent.getMentionName()
				+ (StringUtils.isNotBlank(modelName) ? "\n**Model:** `" + modelName + "`" : "")
				+ "\n**Issue:** " + issue.getTitle()
				+ "\n**Prompt:**\n" + prompt
				+ "\n\nRelated to " + issueReference;
		var request = createDispatchRequest(project, baseBranch, branchName, title, description, triggeredBy);
		var command = new AiDispatchCommand(agent, List.of(), prompt, modelName);
		start(request, command, aiSetting, triggeredBy);
		return request;
	}

public void sendInput(Long runId, String input) {
if (StringUtils.isBlank(input))
return;

var session = activeSessions.get(runId);
if (session == null)
throw new IllegalStateException("AI dispatch session is no longer active");
if (!session.launchResult.isInteractive())
throw new IllegalStateException("Live guidance is not available for this AI dispatch session");
if (!session.launchResult.isAcceptingInput()) {
throw new IllegalStateException(
"Session is still responding. Wait for the current reply to finish before sending another prompt.");
}

var normalizedInput = input.strip();
transactionService.run(() -> {
var run = runService.load(runId);
closeAssistantMessage(run, session);
run.appendLog("> " + normalizedInput + "\n");
run.appendLog(AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, normalizedInput));
runService.update(run);
notifyChange(run);
});
session.launchResult.sendInput(normalizedInput);
}

public void cancel(Long runId, String reason) {
var session = activeSessions.remove(runId);
managedFutureService.removeFuture(getFutureId(runId));
if (session != null) {
session.launchResult.cancel();
worktreeManager.cleanup(session.worktree);
}
transactionService.run(() -> {
var run = runService.load(runId);
if (!run.isActive())
return;
closeAssistantMessage(run, session);
run.appendLog(AiDispatchConversation.newProgressBlock(reason));
run.setState(AiDispatchRun.State.CANCELLED);
run.setCompletedAt(new Date());
runService.update(run);
notifyChange(run);
postReviewReply(run);
});
}

public boolean isInteractive(Long runId) {
var session = activeSessions.get(runId);
return session != null && session.launchResult.isInteractive();
}

public boolean isAcceptingInput(Long runId) {
var session = activeSessions.get(runId);
return session != null && session.launchResult.isAcceptingInput();
}

private void start(PullRequestComment comment, AiDispatchCommand command, AiSetting aiSetting) {
start(comment.getRequest(), command, aiSetting, comment.getUser(), comment);
}

	private AiSetting requireDispatchCapacity() {
		AiSetting aiSetting = settingService.getAiSetting();
		if (!aiSetting.isDispatchEnabled())
			throw new IllegalStateException("AI dispatch is disabled by administrator.");
		if (runService.countActive() >= aiSetting.getMaxDispatchSessions())
			throw new IllegalStateException("Maximum concurrent AI dispatch sessions reached.");
		return aiSetting;
	}

	private String buildTimestamp() {
		return new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
	}

	private PullRequest createDispatchRequest(Project project, String baseBranch, String branchName,
											  String title, String description, User triggeredBy) {
		gitService.createBranch(project, branchName, baseBranch);

		var target = new ProjectAndBranch(project, baseBranch);
		var source = new ProjectAndBranch(project, branchName);

		ObjectId mergeBase = gitService.getMergeBase(project, target.getObjectId(), project, source.getObjectId());
		if (mergeBase == null)
			throw new IllegalStateException("Cannot determine merge base for branch " + baseBranch);

		var pr = new PullRequest();
		pr.setTitle(title);
		pr.setDescription(description);
		pr.setSubmitter(triggeredBy);
		pr.setTarget(target);
		pr.setSource(source);
		pr.setBaseCommitHash(mergeBase.name());
		pr.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);

		var update = new PullRequestUpdate();
		update.setRequest(pr);
		update.setHeadCommitHash(source.getObjectName());
		update.setTargetHeadCommitHash(target.getObjectName());
		update.setDate(new DateTime(new Date()).plusSeconds(1).toDate());
		pr.getUpdates().add(update);

		pullRequestService.open(pr);
		return pr;
	}

	private void postIssueDispatchComment(Issue issue, String content) {
		transactionService.run(() -> {
			var comment = new IssueComment();
			comment.setIssue(issue);
			comment.setUser(userService.getSystem());
			comment.setContent(content);
			issueCommentService.create(comment);
		});
	}

private void start(PullRequest request, AiDispatchCommand command, AiSetting aiSetting,
User triggeredBy) {
start(request, command, aiSetting, triggeredBy, null);
}

private void start(PullRequest request, AiDispatchCommand command, AiSetting aiSetting,
User triggeredBy, PullRequestComment comment) {
var run = new AiDispatchRun();
run.setRequest(request);
		run.setComment(comment);
		run.setTriggeredBy(triggeredBy);
run.setAgent(command.getAgent());
run.setFlags(String.join(" ", command.getPersistedFlags()));
run.setPrompt(command.getPrompt());
run.setState(AiDispatchRun.State.QUEUED);
run.appendLog(AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, command.getPrompt()));
run.appendLog(AiDispatchConversation.newProgressBlock(
		"Queued @" + command.getAgent().getMentionName() + " session."));
runService.create(run);
notifyChange(run);

try {
var worktree = worktreeManager.prepare(run);
transactionService.run(() -> {
var managedRun = runService.load(run.getId());
managedRun.setWorktreePath(worktree.getDirectory().getAbsolutePath());
managedRun.setStartedAt(new Date());
managedRun.setState(AiDispatchRun.State.RUNNING);
managedRun.appendLog(AiDispatchConversation.newProgressBlock(
		"Worktree ready at " + worktree.getDirectory().getAbsolutePath()));
runService.update(managedRun);
notifyChange(managedRun);
});

var launchResult = sessionLauncher.launch(runService.load(run.getId()), worktree,
aiSetting, text -> appendOutput(run.getId(), text));
activeSessions.put(run.getId(), new ActiveSession(worktree, launchResult));
managedFutureService.addFuture(getFutureId(run.getId()), launchResult.getFuture(),
aiSetting.getDispatchTimeoutMinutes() * 60,
future -> cancel(run.getId(), "Session timed out."));
executorService.submit(() -> {
int exitCode;
Throwable error = null;
try {
exitCode = launchResult.getFuture().get();
} catch (CancellationException e) {
return null;
} catch (Exception e) {
exitCode = -1;
error = e;
}
finish(run.getId(), exitCode, error);
return null;
});
} catch (Exception e) {
logger.error("Error starting AI dispatch session", e);
transactionService.run(() -> {
var managedRun = runService.load(run.getId());
managedRun.appendLog(AiDispatchConversation.newErrorBlock("SESSION_INIT_FAILED",
		StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName())));
managedRun.setState(AiDispatchRun.State.FAILED);
managedRun.setCompletedAt(new Date());
managedRun.setExitCode(-1);
runService.update(managedRun);
notifyChange(managedRun);
});
}
}

private void createRejectedRun(PullRequestComment comment, AiDispatchCommand command, String reason) {
var run = new AiDispatchRun();
run.setRequest(comment.getRequest());
run.setComment(comment);
run.setTriggeredBy(comment.getUser());
run.setAgent(command.getAgent());
run.setFlags(String.join(" ", command.getFlags()));
run.setPrompt(command.getPrompt());
run.setState(AiDispatchRun.State.FAILED);
run.setCompletedAt(new Date());
run.setExitCode(-1);
run.appendLog(AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, command.getPrompt()));
run.appendLog(AiDispatchConversation.newErrorBlock("SESSION_REJECTED", reason));
runService.create(run);
notifyChange(run);
}

private void appendOutput(Long runId, String text) {
var session = activeSessions.get(runId);
transactionService.run(() -> {
var run = runService.load(runId);
if (session != null && (AiDispatchConversation.containsMarkup(text) || AiDispatchConversation.isSystemOutput(text)))
	closeAssistantMessage(run, session);
else if (session != null && session.assistantMessageOpen.compareAndSet(false, true))
	run.appendLog(AiDispatchConversation.startAssistantBlock());
run.appendLog(text);
runService.update(run);
notifyChange(run);
});
}

private void finish(Long runId, int exitCode, Throwable error) {
var session = activeSessions.remove(runId);
managedFutureService.removeFuture(getFutureId(runId));
var commitShas = session != null ? worktreeManager.collectNewCommits(session.worktree) : java.util.List.<String>of();
if (session != null)
worktreeManager.cleanup(session.worktree);

transactionService.run(() -> {
var run = runService.load(runId);
if (!run.isActive())
return;
closeAssistantMessage(run, session);
if (error != null && error.getMessage() != null) {
run.appendLog(AiDispatchConversation.newErrorBlock(classifyErrorTag(error),
		StringUtils.defaultIfBlank(error.getMessage(), error.getClass().getSimpleName())));
}
run.setCommitShaList(commitShas);
run.setExitCode(exitCode);
run.setCompletedAt(new Date());
run.setState(exitCode == 0 ? AiDispatchRun.State.COMPLETED : AiDispatchRun.State.FAILED);
runService.update(run);
notifyChange(run);
postReviewReply(run);
});
}

private void closeAssistantMessage(AiDispatchRun run, ActiveSession session) {
if (session != null && session.assistantMessageOpen.compareAndSet(true, false))
run.appendLog(AiDispatchConversation.endMessageBlock());
}

private void postReviewReply(AiDispatchRun run) {
var messages = AiDispatchConversation.parseMessages(run.getLog());
var statusEmoji = switch (run.getState()) {
    case COMPLETED -> "✅";
    case FAILED -> "❌";
    case CANCELLED -> "⚠️";
    default -> "ℹ️";
};
var sb = new StringBuilder();
sb.append(statusEmoji).append(" **@").append(run.getAgent().getMentionName())
    .append("** session ").append(run.getState().name().toLowerCase());
if (!run.getCommitShaList().isEmpty())
    sb.append(" · ").append(run.getCommitShaList().size()).append(" commit(s)");
sb.append("\n\n");
var summary = AiDispatchConversation.buildSummary(messages, 6000);
if (!summary.isEmpty())
    sb.append(summary).append("\n\n");
sb.append("---\n*View full session in the [AI Run tab](")
    .append(run.getRequest().getProject().getPath())
    .append("/~pulls/").append(run.getRequest().getNumber())
    .append("/ai-runs#").append(run.getAnchor()).append(")*");

var reply = new PullRequestComment();
reply.setRequest(run.getRequest());
reply.setUser(userService.getSystem());
reply.setDate(new Date());
reply.setContent(sb.toString());
pullRequestCommentService.create(reply);
}

private void notifyChange(AiDispatchRun run) {
webSocketService.notifyObservableChange(AiDispatchRun.getChangeObservable(run.getRequest().getId()), null);
webSocketService.notifyObservableChange(AiDispatchRun.getSessionsChangeObservable(), null);
}

private String getFutureId(Long runId) {
return AiDispatchRun.class.getName() + ":" + runId;
}

static boolean isHandledTermination(Throwable error) {
return error instanceof CancellationException;
}

private static String classifyErrorTag(Throwable error) {
if (isHandledTermination(error))
return "SESSION_CANCELLED";
var message = StringUtils.defaultString(error.getMessage()).toLowerCase();
if (message.contains("unauthorized") || message.contains("api key") || message.contains("token"))
return "AUTH_FAILED";
if (message.contains("rate limit") || message.contains("quota"))
return "RATE_LIMITED";
if (message.contains("timed out") || message.contains("timeout"))
return "TIMEOUT";
if (message.contains("not reachable") || message.contains("connection"))
return "NETWORK_ERROR";
return "SESSION_ERROR";
}

private static class ActiveSession {

private final AiDispatchWorktreeManager.PreparedWorktree worktree;

private final AiDispatchSessionLauncher.LaunchResult launchResult;

private final AtomicBoolean assistantMessageOpen = new AtomicBoolean(false);

private ActiveSession(AiDispatchWorktreeManager.PreparedWorktree worktree,
AiDispatchSessionLauncher.LaunchResult launchResult) {
this.worktree = worktree;
this.launchResult = launchResult;
}

}

}
