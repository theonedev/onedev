package io.onedev.server.ai.dispatch;

import java.util.concurrent.Future;

import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.support.administration.AiSetting;

public interface AiDispatchSessionLauncher {

LaunchResult launch(AiDispatchRun run, AiDispatchWorktreeManager.PreparedWorktree worktree,
AiSetting aiSetting, OutputListener listener);

interface OutputListener {

void onOutput(String text);

}

interface LaunchResult {

Future<Integer> getFuture();

boolean isInteractive();

boolean isAcceptingInput();

void sendInput(String input);

void cancel();

}

}
