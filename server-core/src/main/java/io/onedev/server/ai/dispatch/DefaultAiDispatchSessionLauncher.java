package io.onedev.server.ai.dispatch;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import io.onedev.commons.utils.ImmediateFuture;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ProcessTreeKiller;
import io.onedev.commons.utils.command.StreamPumper;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.model.support.administration.AiDispatchAgentSetting;
import io.onedev.server.model.support.administration.AiDispatchBackend;
import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.model.support.administration.CopilotApiSetting;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.AiDispatchRunService;

@Singleton
public class DefaultAiDispatchSessionLauncher implements AiDispatchSessionLauncher {

private final ExecutorService executorService;

private final CopilotApiManager copilotApiManager;

private final AiDispatchRunService runService;

private final TransactionService transactionService;

@Inject
public DefaultAiDispatchSessionLauncher(ExecutorService executorService, CopilotApiManager copilotApiManager,
AiDispatchRunService runService, TransactionService transactionService) {
this.executorService = executorService;
this.copilotApiManager = copilotApiManager;
this.runService = runService;
this.transactionService = transactionService;
}

	@Override
	public LaunchResult launch(AiDispatchRun run, AiDispatchWorktreeManager.PreparedWorktree worktree,
	AiSetting aiSetting, OutputListener listener) {
		var agent = requireAgent(run);
		var agentSetting = aiSetting.getDispatchAgentSetting(agent);
		if (StringUtils.isNotBlank(run.getModelName()))
			listener.onOutput(AiDispatchConversation.newProgressBlock(
					"Using per-session model override: " + run.getModelName()));
		var modelSetting = resolveModelSetting(run, agentSetting, aiSetting,
	message -> listener.onOutput(AiDispatchConversation.newProgressBlock(message)));
if (shouldUseModel(agentSetting, modelSetting)) {
if (modelSetting == null)
throw new IllegalStateException("No AI model configured for @" + run.getAgent().getMentionName());
return new ModelLaunchResult(run.getId(), worktree, modelSetting, listener);
}

Commandline cmdline = buildCommandline(run, worktree, aiSetting, agentSetting);
var initialInput = getInitialCliInput(run, agentSetting);
var acceptingInput = new AtomicBoolean(true);

var stdinRef = new AtomicReference<OutputStream>(null);
var future = executorService.submit(() -> {
var stdoutRef = new AtomicReference<InputStream>(null);
var stderrRef = new AtomicReference<InputStream>(null);
try {
cmdline.processKiller(new ProcessTreeKiller() {

@Override
public void kill(Process process, String executionId) {
closeQuietly(stdoutRef.get());
closeQuietly(stderrRef.get());
super.kill(process, executionId);
}

});
var result = cmdline.execute(newStreamHandler(stdoutRef, listener, acceptingInput),
newStreamHandler(stderrRef, listener, acceptingInput), os -> {
stdinRef.set(os);
if (initialInput != null) {
try {
os.write(initialInput.getBytes(StandardCharsets.UTF_8));
os.flush();
} catch (IOException e) {
throw new RuntimeException(e);
}
}
return new ImmediateFuture<Void>(null);
});
return result.getReturnCode();
} finally {
closeQuietly(stdinRef.get());
stdinRef.set(null);
}
});

return new LaunchResult() {
@Override
public Future<Integer> getFuture() {
return future;
}

@Override
public boolean isInteractive() {
return true;
}

@Override
public boolean isAcceptingInput() {
return acceptingInput.get();
}

@Override
public void sendInput(String input) {
acceptingInput.set(true);
var stdin = stdinRef.get();
if (stdin != null) {
try {
var payload = normalizeInteractiveInput(input);
stdin.write(payload.getBytes(StandardCharsets.UTF_8));
stdin.flush();
} catch (IOException e) {
throw new RuntimeException(e);
}
}
}

	@Override
public void cancel() {
closeQuietly(stdinRef.get());
future.cancel(true);
}
};
}

	static AiDispatchAgent requireAgent(AiDispatchRun run) {
		var agent = run.getAgent();
		if (agent == null)
			throw new IllegalStateException("AI dispatch run is missing agent configuration");
		return agent;
	}

private boolean shouldUseModel(AiDispatchAgentSetting agentSetting, AiModelSetting modelSetting) {
if (agentSetting.getBackend() == AiDispatchBackend.MODEL || agentSetting.getBackend() == AiDispatchBackend.COPILOT_API)
return true;
if (agentSetting.getBackend() == AiDispatchBackend.CLI)
return false;
return modelSetting != null;
}

	private AiModelSetting resolveModelSetting(AiDispatchRun run, AiDispatchAgentSetting agentSetting, AiSetting aiSetting,
	Consumer<String> logger) {
		var modelOverride = StringUtils.trimToNull(run.getModelName());
		if (agentSetting.getBackend() == AiDispatchBackend.COPILOT_API) {
			if (run.getAgent() != AiDispatchAgent.COPILOT)
				throw new IllegalStateException("Copilot API backend can only be used for @copilot");
			var setting = copyCopilotApiSetting(aiSetting.getCopilotApiSetting());
			if (modelOverride != null)
				setting.setModel(modelOverride);
			return copilotApiManager.prepareModelSetting(setting, logger);
		}
		AiModelSetting modelSetting;
		if (agentSetting.getModelSetting() != null)
			modelSetting = agentSetting.getModelSetting();
		else
			modelSetting = aiSetting.getLiteModelSetting();
		if (modelSetting == null)
			return null;
		if (modelOverride != null)
			return copyModelSetting(modelSetting, modelOverride);
		return modelSetting;
	}

private Commandline buildCommandline(AiDispatchRun run, AiDispatchWorktreeManager.PreparedWorktree worktree,
AiSetting aiSetting, AiDispatchAgentSetting agentSetting) {
var command = StringUtils.defaultIfBlank(agentSetting.getCommand(), run.getAgent().getMentionName());
Commandline cmdline = new Commandline(command).workingDir(worktree.getDirectory());
		for (var each: agentSetting.getCommandArgumentList())
			cmdline.addArgs(each);
		if (run.getAgent() == AiDispatchAgent.CLAUDE && run.isExtendedThinking())
			cmdline.addArgs("--think");
		if (StringUtils.isNotBlank(run.getModelName()))
			cmdline.addArgs("--model", run.getModelName());

		var promptOption = StringUtils.trimToNull(agentSetting.getPromptOption());
if (promptOption != null)
cmdline.addArgs(promptOption, buildPrompt(run));

configureEnvironment(cmdline.environments(), run, aiSetting, agentSetting);
return cmdline;
}

private String getInitialCliInput(AiDispatchRun run, AiDispatchAgentSetting agentSetting) {
if (StringUtils.isBlank(agentSetting.getPromptOption()))
return buildPrompt(run) + "\n";
return null;
}

static String normalizeInteractiveInput(String input) {
return input.endsWith("\n") ? input : input + "\n";
}

private void configureEnvironment(Map<String, String> environments, AiDispatchRun run,
AiSetting aiSetting, AiDispatchAgentSetting agentSetting) {
var apiKeyEnvName = StringUtils.trimToNull(agentSetting.getApiKeyEnvName());
var apiKey = StringUtils.trimToNull(agentSetting.getApiKey());
if (apiKey == null && run.getAgent() == AiDispatchAgent.CLAUDE)
apiKey = StringUtils.trimToNull(aiSetting.getClaudeApiKey());
if (apiKeyEnvName != null && apiKey != null)
environments.put(apiKeyEnvName, apiKey);

var baseUrlEnvName = StringUtils.trimToNull(agentSetting.getBaseUrlEnvName());
var baseUrl = StringUtils.trimToNull(agentSetting.getBaseUrl());
if (baseUrlEnvName != null && baseUrl != null)
environments.put(baseUrlEnvName, baseUrl);
}

private Function<InputStream, Future<?>> newStreamHandler(AtomicReference<InputStream> streamRef,
OutputListener listener, AtomicBoolean acceptingInput) {
return input -> {
streamRef.set(input);
return StreamPumper.pump(input, new OutputStream() {
@Override
public void write(byte[] b, int off, int len) {
acceptingInput.set(false);
listener.onOutput(new String(b, off, len, StandardCharsets.UTF_8));
}

@Override
public void write(int b) {
throw new UnsupportedOperationException();
}
});
};
}

	private String buildPrompt(AiDispatchRun run) {
var builder = new StringBuilder();
if (run.isReviewOnly())
builder.append("Review-only mode. Do not push or commit any changes.\n");
if (run.isExtendedThinking())
builder.append("Think carefully before responding and show your reasoning when appropriate.\n");
		builder.append(run.getPrompt());
		return builder.toString();
	}

	private AiModelSetting copyModelSetting(AiModelSetting source, String modelName) {
		var copy = new AiModelSetting();
		copy.setApiKey(source.getApiKey());
		copy.setBaseUrl(source.getBaseUrl());
		copy.setTimeoutSeconds(source.getTimeoutSeconds());
		copy.setName(modelName);
		return copy;
	}

	private CopilotApiSetting copyCopilotApiSetting(CopilotApiSetting source) {
		var copy = new CopilotApiSetting();
		copy.setEndpoint(source.getEndpoint());
		copy.setModel(source.getModel());
		copy.setAutoStart(source.isAutoStart());
		copy.setProjectPath(source.getProjectPath());
		copy.setDockerImage(source.getDockerImage());
		copy.setContainerName(source.getContainerName());
		copy.setAuthDataDir(source.getAuthDataDir());
		copy.setGitHubToken(source.getGitHubToken());
		copy.setStartupTimeoutSeconds(source.getStartupTimeoutSeconds());
		return copy;
	}

static List<ChatMessage> buildConversationMessages(ConversationContext context,
AiDispatchWorktreeManager.PreparedWorktree worktree) {
	List<ChatMessage> messages = new ArrayList<>();
	messages.add(new SystemMessage(buildConversationSystemPrompt(context, worktree)));
	for (var message: context.transcript()) {
	if (StringUtils.isBlank(message.getContent()) || !message.isTranscriptMessage())
	continue;
	if (message.isUser())
	messages.add(new UserMessage(message.getContent()));
else if (message.isAssistant())
messages.add(new AiMessage(message.getContent()));
}
return messages;
}

private static String buildConversationSystemPrompt(ConversationContext context,
AiDispatchWorktreeManager.PreparedWorktree worktree) {
return """
You are acting as an AI dispatch assistant inside OneDev for an active pull request conversation.
Agent label: %s
Pull request #%s
Source branch: %s
Target branch: %s
Worktree path: %s
Mode: %s

Continue the conversation using the transcript supplied after this message.
When the user sends another prompt, treat it as a follow-up instruction in the same session.
If review-only mode is requested, do not suggest committing or pushing changes.
""".formatted(
 context.agentMentionName(),
 context.requestNumber(),
 context.sourceBranch(),
 context.targetBranch(),
 worktree.getDirectory().getAbsolutePath(),
 context.reviewOnly() ? "review-only" : "standard");
}

static record ConversationContext(
String agentMentionName,
Long requestNumber,
String sourceBranch,
String targetBranch,
boolean reviewOnly,
List<AiDispatchConversation.Message> transcript) {
}

private class ModelLaunchResult implements LaunchResult {

private final Long runId;

private final AiDispatchWorktreeManager.PreparedWorktree worktree;

private final AiModelSetting modelSetting;

private final OutputListener listener;

private final CompletableFuture<Integer> future = new CompletableFuture<>();

private final AtomicBoolean acceptingInput = new AtomicBoolean(false);

private final AtomicBoolean cancelled = new AtomicBoolean(false);

private final AtomicReference<StreamingHandle> activeHandle = new AtomicReference<>();

	private ModelLaunchResult(Long runId, AiDispatchWorktreeManager.PreparedWorktree worktree,
	AiModelSetting modelSetting, OutputListener listener) {
this.runId = runId;
this.worktree = worktree;
this.modelSetting = modelSetting;
this.listener = listener;
 listener.onOutput(AiDispatchConversation.newProgressBlock("Using configured model-backed dispatch session."));
 submitTurn();
}

@Override
public Future<Integer> getFuture() {
return future;
}

@Override
public boolean isInteractive() {
return true;
}

@Override
public boolean isAcceptingInput() {
return acceptingInput.get();
}

@Override
public void sendInput(String input) {
if (!acceptingInput.compareAndSet(true, false)) {
throw new IllegalStateException(
"Session is still responding. Wait for the current reply to finish before sending another prompt.");
}
submitTurn();
}

@Override
public void cancel() {
cancelled.set(true);
acceptingInput.set(false);
var handle = activeHandle.get();
if (handle != null)
handle.cancel();
future.cancel(true);
}

private void submitTurn() {
if (cancelled.get() || future.isDone())
return;
acceptingInput.set(false);
executorService.submit(() -> {
try {
 var context = transactionService.call(() -> {
 var managedRun = runService.load(runId);
 return new ConversationContext(
 managedRun.getAgent().getMentionName(),
 managedRun.getRequest().getNumber(),
 managedRun.getRequest().getSourceBranch(),
 managedRun.getRequest().getTargetBranch(),
 managedRun.isReviewOnly(),
 AiDispatchConversation.parseMessages(managedRun.getLog()));
 });
 var request = ChatRequest.builder()
 .messages(buildConversationMessages(context, worktree))
 .build();
var turnDone = new CompletableFuture<Void>();
var error = new AtomicReference<Throwable>();
modelSetting.getStreamingChatModel().chat(request, new StreamingChatResponseHandler() {
@Override
public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
activeHandle.set(context.streamingHandle());
var text = partialResponse.text();
if (StringUtils.isNotEmpty(text))
listener.onOutput(text);
}

@Override
public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
activeHandle.set(context.streamingHandle());
var text = partialThinking.text();
if (StringUtils.isNotEmpty(text))
 listener.onOutput(AiDispatchConversation.newThinkingBlock(text));
}

@Override
public void onCompleteResponse(ChatResponse completeResponse) {
turnDone.complete(null);
}

@Override
public void onError(Throwable throwable) {
error.set(throwable);
turnDone.complete(null);
}
});
turnDone.get();
if (error.get() != null)
 listener.onOutput(AiDispatchConversation.newErrorBlock(classifyModelErrorTag(error.get()),
 		StringUtils.defaultIfBlank(error.get().getMessage(), error.get().getClass().getSimpleName())));
else if (!cancelled.get() && !future.isDone())
 listener.onOutput(AiDispatchConversation.newProgressBlock(
 		"Response complete. Send the next prompt or end the session."));
} catch (Exception e) {
 listener.onOutput(AiDispatchConversation.newErrorBlock(classifyModelErrorTag(e),
 		StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName())));
} finally {
activeHandle.set(null);
if (!cancelled.get() && !future.isDone())
acceptingInput.set(true);
}
return null;
});
	}

	}

	private static String classifyModelErrorTag(Throwable error) {
		var message = StringUtils.defaultString(error.getMessage()).toLowerCase();
		if (message.contains("unauthorized") || message.contains("api key") || message.contains("token"))
			return "AUTH_FAILED";
		if (message.contains("rate limit") || message.contains("quota"))
			return "RATE_LIMITED";
		if (message.contains("timed out") || message.contains("timeout"))
			return "TIMEOUT";
		if (message.contains("not reachable") || message.contains("connection"))
			return "NETWORK_ERROR";
		return "MODEL_ERROR";
	}

}
