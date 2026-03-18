package io.onedev.server.ai.dispatch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.model.support.administration.CopilotApiSetting;


import javax.inject.Singleton;


@Singleton
public class DefaultCopilotApiManager implements CopilotApiManager {

	private static final int READY_TIMEOUT_SECONDS = 5;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	@Override
	public AiModelSetting prepareModelSetting(CopilotApiSetting setting, Consumer<String> logger) {
		if (!isEndpointReady(setting)) {
			if (!setting.isAutoStart()) {
				throw new IllegalStateException("Copilot API is not reachable at " + setting.getEndpoint()
						+ ". Start the local proxy or enable Docker auto-start.");
			}
			startContainer(setting, logger);
			waitForEndpoint(setting, logger);
		}
		return newModelSetting(setting);
	}

	static AiModelSetting newModelSetting(CopilotApiSetting setting) {
		var modelSetting = new AiModelSetting();
		modelSetting.setBaseUrl(setting.getEndpoint());
		modelSetting.setApiKey("dummy");
		modelSetting.setName(setting.getModel());
		modelSetting.setTimeoutSeconds(Math.max(5, setting.getStartupTimeoutSeconds()));
		return modelSetting;
	}

	static String getModelsUrl(CopilotApiSetting setting) {
		var endpoint = StringUtils.stripEnd(setting.getEndpoint(), "/");
		return endpoint + "/models";
	}

	static String getAuthVolumeTokenPath(CopilotApiSetting setting) {
		return Path.of(setting.getAuthDataDir(), "github_token").toString();
	}

	static List<String> buildDockerRunArgs(CopilotApiSetting setting) {
		var args = new ArrayList<String>();
		args.add("run");
		args.add("-d");
		args.add("--name");
		args.add(setting.getContainerName());
		args.add("-p");
		args.add(resolvePort(setting) + ":4141");
		args.add("-v");
		args.add(setting.getAuthDataDir() + ":/root/.local/share/copilot-api");
		if (StringUtils.isNotBlank(setting.getGitHubToken())) {
			args.add("-e");
			args.add("GH_TOKEN=" + setting.getGitHubToken());
		}
		args.add(setting.getDockerImage());
		return args;
	}

	static int resolvePort(CopilotApiSetting setting) {
		var endpoint = URI.create(setting.getEndpoint());
		if (endpoint.getPort() > 0)
			return endpoint.getPort();
		return "https".equalsIgnoreCase(endpoint.getScheme()) ? 443 : 80;
	}

	private boolean isEndpointReady(CopilotApiSetting setting) {
		try {
			var request = HttpRequest.newBuilder()
					.uri(URI.create(getModelsUrl(setting)))
					.timeout(Duration.ofSeconds(READY_TIMEOUT_SECONDS))
					.GET()
					.build();
			var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
			return response.statusCode() == 200;
		} catch (Exception e) {
			return false;
		}
	}

	private void startContainer(CopilotApiSetting setting, Consumer<String> logger) {
		try {
			Files.createDirectories(Path.of(setting.getAuthDataDir()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!Files.isDirectory(Path.of(setting.getProjectPath())))
			throw new IllegalStateException("Copilot API project path does not exist: " + setting.getProjectPath());

		if (StringUtils.isBlank(setting.getGitHubToken()) && !hasPersistedToken(setting)) {
			throw new IllegalStateException("Copilot API auto-start needs either a GitHub token or a persisted OAuth token at "
					+ getAuthVolumeTokenPath(setting) + ". Pre-authorize the mounted auth directory with the container auth flow.");
		}

		logger.accept("Ensuring copilot-api Docker image " + setting.getDockerImage() + " is available.");
		runDockerCommand(logger, List.of("image", "inspect", setting.getDockerImage()), true);
		if (getLastExitCode() != 0) {
			logger.accept("Building local copilot-api Docker image from " + setting.getProjectPath() + ".");
			runDockerCommand(logger, List.of("build", "-t", setting.getDockerImage(), setting.getProjectPath()), false);
		}

		logger.accept("Starting copilot-api container " + setting.getContainerName() + ".");
		runDockerCommand(logger, List.of("rm", "-f", setting.getContainerName()), true);
		runDockerCommand(logger, buildDockerRunArgs(setting), false);
	}

	private final ThreadLocal<Integer> lastExitCode = ThreadLocal.withInitial(() -> 0);

	private int getLastExitCode() {
		return lastExitCode.get();
	}

	private void waitForEndpoint(CopilotApiSetting setting, Consumer<String> logger) {
		var timeoutAt = System.currentTimeMillis() + setting.getStartupTimeoutSeconds() * 1000L;
		while (System.currentTimeMillis() < timeoutAt) {
			if (isEndpointReady(setting)) {
				logger.accept("Copilot API is reachable at " + setting.getEndpoint() + ".");
				return;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
		throw new IllegalStateException("Timed out waiting for Copilot API at " + setting.getEndpoint() + ".");
	}

	private boolean hasPersistedToken(CopilotApiSetting setting) {
		var tokenPath = Path.of(getAuthVolumeTokenPath(setting));
		if (!Files.exists(tokenPath))
			return false;
		try {
			return StringUtils.isNotBlank(Files.readString(tokenPath, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void runDockerCommand(Consumer<String> logger, List<String> args, boolean ignoreFailure) {
		var cmdline = new Commandline("docker");
		for (var each: args)
			cmdline.addArgs(each);
		var stderr = new StringBuilder();
		var result = cmdline.execute(new LineConsumer() {
			@Override
			public void consume(String line) {
				logger.accept(line);
			}
		}, new LineConsumer() {
			@Override
			public void consume(String line) {
				if (stderr.length() != 0)
					stderr.append('\n');
				stderr.append(line);
			}
		});
		lastExitCode.set(result.getReturnCode());
		if (result.getReturnCode() != 0 && !ignoreFailure) {
			throw new IllegalStateException("Failed to execute docker " + String.join(" ", args)
					+ " for Copilot API backend. Exit code: " + result.getReturnCode()
					+ (stderr.length() != 0 ? ". " + stderr : ""));
		}
		if (result.getReturnCode() != 0 && ignoreFailure)
			logger.accept("Ignoring failed docker " + String.join(" ", args) + " while preparing Copilot API.");
	}

}
