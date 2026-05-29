package io.onedev.server.util;

import static io.onedev.k8shelper.KubernetesHelper.ENV_SERVER_URL;
import static io.onedev.k8shelper.KubernetesHelper.unescapeStartCR;
import static io.onedev.k8shelper.Test.TEST_PATH;
import static io.onedev.server.util.CollectionUtils.newLinkedHashMap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.Test;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.util.KubernetesUtils.CollectLogExitCondition.CommandCompleted;
import io.onedev.server.util.KubernetesUtils.CollectLogExitCondition.SeenMessage;
import io.onedev.server.util.KubernetesUtils.CollectLogExitCondition.SeenMessageOrCommandCompleted;

/**
 * Shared helpers for running {@code kubectl} and interpreting pod/container status from the API.
 */
public final class KubernetesUtils {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesUtils.class);

	private static final int POD_WATCH_TIMEOUT_SECONDS = 60;

	private static final String TEST_INIT_CONTAINER_NAME = "k8s-test";

	private static final String TEST_MAIN_CONTAINER_NAME = "main";

	private static final String K8S_HELPER_CLASSPATH = "/k8s-helper/*";

	public static Commandline newKubectl(@Nullable String kubeCtlPath, @Nullable String configFile) {
		String kubectl = kubeCtlPath;
		if (kubectl == null) {
			if (SystemUtils.IS_OS_MAC_OSX && new File("/usr/local/bin/kubectl").exists())
				kubectl = "/usr/local/bin/kubectl";
			else
				kubectl = "kubectl";
		}
		Commandline cmdline = new Commandline(kubectl);
		if (configFile != null)
			cmdline.addArgs("--kubeconfig", configFile);
		return cmdline;
	}

	/**
	 * Namespace manifest with privileged pod security labels (matches cluster defaults used by
	 * the Kubernetes executor and provisioner).
	 */
	public static Map<Object, Object> getPrivilegedNamespaceDefinition(String name) {
		Map<Object, Object> labels = new LinkedHashMap<>();
		labels.put("pod-security.kubernetes.io/audit", "privileged");
		labels.put("pod-security.kubernetes.io/enforce", "privileged");
		labels.put("pod-security.kubernetes.io/warn", "privileged");
		Map<Object, Object> metadata = new LinkedHashMap<>();
		metadata.put("name", name);
		metadata.put("labels", labels);
		Map<Object, Object> def = new LinkedHashMap<>();
		def.put("apiVersion", "v1");
		def.put("kind", "Namespace");
		def.put("metadata", metadata);
		return def;
	}

	public static void logPodUnschedulableWarnings(@Nullable JsonNode statusNode, TaskLogger taskLogger) {
		if (statusNode == null || !statusNode.isObject())
			return;
		JsonNode conditionsNode = statusNode.get("conditions");
		if (conditionsNode == null)
			return;
		for (JsonNode conditionNode : conditionsNode) {
			if (conditionNode.get("type").asText().equals("PodScheduled")
					&& conditionNode.get("status").asText().equals("False")
					&& conditionNode.get("reason").asText().equals("Unschedulable")) {
				taskLogger.warning("Kubernetes: " + conditionNode.get("message").asText());
			}
		}
	}

	public static Map<String, Object> getContainerErrors(Collection<JsonNode> containerStatusNodes) {
		Map<String, Object> containerErrors = new HashMap<>();
		for (JsonNode containerStatusNode : containerStatusNodes) {
			String containerName = containerStatusNode.get("name").asText();

			JsonNode stateNode = containerStatusNode.get("state");
			JsonNode waitingNode = stateNode.get("waiting");
			if (waitingNode != null) {
				String reason = waitingNode.get("reason").asText();
				if (reason.equals("ErrImagePull") || reason.equals("InvalidImageName")
						|| reason.equals("ImageInspectError") || reason.equals("ErrImageNeverPull")
						|| reason.equals("RegistryUnavailable")) {
					JsonNode messageNode = waitingNode.get("message");
					if (messageNode != null)
						containerErrors.put(containerName, messageNode.asText());
					else
						containerErrors.put(containerName, reason);
				}
			}

			if (!containerErrors.containsKey(containerName)) {
				JsonNode terminatedNode = stateNode.get("terminated");
				if (terminatedNode != null) {
					String reason;
					JsonNode reasonNode = terminatedNode.get("reason");
					if (reasonNode != null)
						reason = reasonNode.asText();
					else
						reason = "Unknown reason";

					if (!reason.equals("Completed")) {
						JsonNode messageNode = terminatedNode.get("message");
						if (messageNode != null) {
							containerErrors.put(containerName, messageNode.asText());
						} else {
							JsonNode exitCodeNode = terminatedNode.get("exitCode");
							if (exitCodeNode != null)
								containerErrors.put(containerName, exitCodeNode.asInt());
							else
								containerErrors.put(containerName, reason);
						}
					}
				}
			}
		}
		return containerErrors;
	}

	public static Collection<String> getStartedContainers(Collection<JsonNode> containerStatusNodes) {
		Collection<String> startedContainers = new HashSet<>();
		for (JsonNode containerStatusNode : containerStatusNodes) {
			JsonNode stateNode = containerStatusNode.get("state");
			if (stateNode.get("running") != null || stateNode.get("terminated") != null)
				startedContainers.add(containerStatusNode.get("name").asText());
		}
		return startedContainers;
	}

	public static Collection<String> getStoppedContainers(Collection<JsonNode> containerStatusNodes) {
		Collection<String> stoppedContainers = new ArrayList<>();
		for (JsonNode containerStatusNode : containerStatusNodes) {
			JsonNode stateNode = containerStatusNode.get("state");
			if (stateNode.get("terminated") != null)
				stoppedContainers.add(containerStatusNode.get("name").asText());
		}
		return stoppedContainers;
	}

	public static void collectContainerLog(Supplier<Commandline> kubectlFactory, String namespace,
			String podName, String containerName, CollectLogExitCondition exitCondition, TaskLogger taskLogger) {
		Thread thread = Thread.currentThread();
		AtomicBoolean abortError = new AtomicBoolean(false);
		AtomicReference<Instant> lastInstantRef = new AtomicReference<>(null);
		AtomicBoolean seenMessageRef = new AtomicBoolean(false);

		while (true) {
			Commandline kubectl = kubectlFactory.get();
			kubectl.addArgs("logs", podName, "-c", containerName, "-n", namespace,
					"--follow", "--timestamps=true");
			if (lastInstantRef.get() != null)
				kubectl.addArgs("--since-time=" + DateTimeFormatter.ISO_INSTANT.format(lastInstantRef.get()));

			class Logger extends LineConsumer {

				private final String sessionId = UUID.randomUUID().toString();

				@Override
				public void consume(String line) {
					if (line.contains("rpc error:") && line.contains("No such container:")
							|| line.contains("Unable to retrieve container logs for")) {
						logger.debug(line);
					} else if (exitCondition instanceof SeenMessage seenMessage && line.contains(seenMessage.getMessage())) {
						String lastLogMessage = StringUtils.substringBefore(line, seenMessage.getMessage());
						if (StringUtils.substringAfter(lastLogMessage, " ").length() != 0)
							consume(lastLogMessage);
						if (!seenMessageRef.get()) {
							seenMessageRef.set(true);
							thread.interrupt();
						}
					} else if (line.startsWith("Error from server") || line.startsWith("error:")) {
						taskLogger.error(line);
						if (!abortError.get()) {
							abortError.set(true);
							thread.interrupt();
						}
					} else if (line.contains(" ")) {
						var timestamp = StringUtils.substringBefore(line, " ");
						try {
							Instant instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp));
							if (lastInstantRef.get() == null || lastInstantRef.get().isBefore(instant))
								lastInstantRef.set(instant);
							taskLogger.log(unescapeStartCR(StringUtils.substringAfter(line, " ")), sessionId);
						} catch (DateTimeParseException e) {
							taskLogger.log(unescapeStartCR(line), sessionId);
						}
					} else {
						taskLogger.log(unescapeStartCR(line), sessionId);
					}
				}

			};

			try {
				kubectl.execute(new Logger(), new Logger()).checkReturnCode();
			} catch (Throwable e) {
				if (!seenMessageRef.get() && !abortError.get())
					throw ExceptionUtils.unchecked(e);
			}			
			if (seenMessageRef.get() || abortError.get()) {
				Thread.interrupted();
				break;
			} else if (exitCondition instanceof CommandCompleted || exitCondition instanceof SeenMessageOrCommandCompleted) {
				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static void watchPod(Supplier<Commandline> kubectlFactory, String namespace,
			String podName, PodWatchAbortChecker abortChecker, TaskLogger taskLogger) {
		AtomicReference<PodWatchAbort> abortRef = new AtomicReference<>(null);
		StringBuilder json = new StringBuilder();
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);

		Thread thread = Thread.currentThread();

		while (true) {
			Commandline kubectl = kubectlFactory.get();
			kubectl.addArgs("get", "pod", podName, "-n", namespace, "--watch", "-o", "json");
			kubectl.timeout(POD_WATCH_TIMEOUT_SECONDS);
			try {
				kubectl.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						if (line.startsWith("{")) {
							json.append("{").append("\n");
						} else if (line.startsWith("}")) {
							json.append("}");
							logger.trace("Pod watching output:\n{}", json);
							try {
								process(mapper.readTree(json.toString()));
							} catch (Exception e) {
								logger.error("Error processing pod watching output", e);
							}
							json.setLength(0);
						} else {
							json.append(line).append("\n");
						}
					}

					private void process(JsonNode podNode) {
						JsonNode statusNode = podNode.get("status");
						if (statusNode == null)
							return;
						logPodUnschedulableWarnings(statusNode, taskLogger);

						if (abortRef.get() == null) {
							String nodeName = null;
							JsonNode specNode = podNode.get("spec");
							if (specNode != null) {
								JsonNode nodeNameNode = specNode.get("nodeName");
								if (nodeNameNode != null)
									nodeName = nodeNameNode.asText();
							}

							Collection<JsonNode> containerStatusNodes = new ArrayList<>();
							JsonNode initContainerStatusesNode = statusNode.get("initContainerStatuses");
							if (initContainerStatusesNode != null) {
								for (JsonNode containerStatusNode : initContainerStatusesNode)
									containerStatusNodes.add(containerStatusNode);
							}
							JsonNode containerStatusesNode = statusNode.get("containerStatuses");
							if (containerStatusesNode != null) {
								for (JsonNode containerStatusNode : containerStatusesNode)
									containerStatusNodes.add(containerStatusNode);
							}

							abortRef.set(abortChecker.check(nodeName, containerStatusNodes));

							if (abortRef.get() != null) 
								thread.interrupt();
						}
					}

				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logKubernetesError(taskLogger, line);
					}

				}).checkReturnCode();

				throw new ExplicitException("Unexpected end of pod watching");
			} catch (Exception e) {
				PodWatchAbort abort = abortRef.get();
				if (abort != null) {
					if (abort.getErrorMessage() != null)
						throw new ExplicitException(abort.getErrorMessage());
					else
						break;
				} else if (ExceptionUtils.find(e, TimeoutException.class) == null) {
					throw ExceptionUtils.unchecked(e);
				}
			}
		}
	}

	public static void logKubernetesError(TaskLogger taskLogger, String message) {
		if (!message.contains("Failed to watch *unstructured.Unstructured: unknown")) {
			if (message.startsWith("Warning:"))
				taskLogger.warning("Kubernetes: " + message);
			else
				taskLogger.error("Kubernetes: " + message);
		} else {
			logger.error("Kubernetes: " + message);
		}
	}

	public static boolean resourceExists(Supplier<Commandline> kubectlFactory, String kind, String name,
			String namespace, TaskLogger taskLogger) {
		AtomicBoolean exists = new AtomicBoolean(false);
		Commandline kubectl = kubectlFactory.get();
		kubectl.addArgs("get", kind, name, "-n", namespace, "--ignore-not-found", "-o", "name");
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				exists.set(true);
			}

		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logKubernetesError(taskLogger, line);
			}

		}).checkReturnCode();
		return exists.get();
	}

	public static void checkClusterAccess(Supplier<Commandline> kubectlFactory, TaskLogger taskLogger) {
		taskLogger.log("Checking cluster access...");
		Commandline cmd = kubectlFactory.get();
		cmd.addArgs("cluster-info");
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}

		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				taskLogger.log(line);
			}

		}).checkReturnCode();
	}

	public static boolean namespaceExists(Supplier<Commandline> kubectlFactory, String namespace,
			TaskLogger taskLogger) {
		AtomicBoolean exists = new AtomicBoolean(false);
		Commandline kubectl = kubectlFactory.get();
		kubectl.addArgs("get", "namespaces", "--field-selector", "metadata.name=" + namespace,
				"-o", "name", "--chunk-size=0");
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				exists.set(true);
			}

		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logKubernetesError(taskLogger, line);
			}

		}).checkReturnCode();
		return exists.get();
	}

	public static void ensureNamespace(Supplier<Commandline> kubectlFactory, String namespace,
			TaskLogger taskLogger) {
		if (namespaceExists(kubectlFactory, namespace, taskLogger))
			return;

		Map<Object, Object> namespaceDef = getPrivilegedNamespaceDefinition(namespace);
		createResource(kubectlFactory, namespaceDef, new HashSet<>(), taskLogger);
	}

	public static boolean createTrustCertsConfigMap(Supplier<Commandline> kubectlFactory,
			String name, String namespace, TaskLogger taskLogger) {
		Map<String, String> configMapData = new LinkedHashMap<>();
		File trustCertsDir = new File(Bootstrap.getConfDir(), "trust-certs");
		if (trustCertsDir.exists()) {
			int index = 1;
			for (File file : trustCertsDir.listFiles()) {
				if (file.isFile() && !file.isHidden()) {
					try {
						byte[] fileContent = FileUtils.readFileToByteArray(file);
						configMapData.put((index++) + ".pem", encodeBase64String(fileContent));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		if (!configMapData.isEmpty()) {
			Map<Object, Object> configMapDef = newLinkedHashMap(
					"apiVersion", "v1",
					"kind", "ConfigMap",
					"metadata", newLinkedHashMap(
							"name", name,
							"namespace", namespace),
					"binaryData", configMapData);
			createResource(kubectlFactory, configMapDef, new HashSet<>(), taskLogger);
			return true;
		}
		return false;
	}

	public static void testCluster(Supplier<Commandline> kubectlFactory, String namespace,
				String image, String helperImage, String serverUrl, String testToken,
				@Nullable String storageClass, String storageSize, boolean alwaysPullImage,
				TaskLogger taskLogger) {
		checkClusterAccess(kubectlFactory, taskLogger);
		ensureNamespace(kubectlFactory, namespace, taskLogger);

		String podName = "test-" + System.currentTimeMillis();
		String pvcName = podName + "-pvc";
		String volumeName = podName + "-vol";
		String trustCertsConfigMapName = podName + "-trust-certs";
		boolean trustCertsConfigMapCreated = false;
		try {
			trustCertsConfigMapCreated = createTrustCertsConfigMap(kubectlFactory,
					trustCertsConfigMapName, namespace, taskLogger);

			taskLogger.log("Creating test PVC...");
			Map<Object, Object> pvcSpecDef = newLinkedHashMap(
					"accessModes", Lists.newArrayList("ReadWriteOnce"),
					"resources", newLinkedHashMap(
							"requests", newLinkedHashMap("storage", storageSize)));
			if (StringUtils.isNotBlank(storageClass))
				pvcSpecDef.put("storageClassName", storageClass);
				Map<Object, Object> pvcDef = newLinkedHashMap(
					"apiVersion", "v1",
					"kind", "PersistentVolumeClaim",
					"metadata", newLinkedHashMap(
							"name", pvcName,
							"namespace", namespace),
					"spec", pvcSpecDef);
			createResource(kubectlFactory, pvcDef, new HashSet<>(), taskLogger);

			var containerTestDirPath = TEST_PATH;
			var containerTrustCertsDirPath = containerTestDirPath + "/trust-certs";

			var volumeMounts = new ArrayList<Object>();
			volumeMounts.add(newLinkedHashMap(
					"name", volumeName,
					"mountPath", containerTestDirPath));
			if (trustCertsConfigMapCreated) {
				volumeMounts.add(newLinkedHashMap(
						"name", "trust-certs",
						"mountPath", containerTrustCertsDirPath));
			}

			var initEnvs = new ArrayList<Object>();
			initEnvs.add(newLinkedHashMap("name", ENV_SERVER_URL, "value", serverUrl));
			initEnvs.add(newLinkedHashMap("name", Test.ENV_TEST_TOKEN, "value", testToken));

			Map<Object, Object> initContainerSpec = newLinkedHashMap(
					"name", TEST_INIT_CONTAINER_NAME,
					"image", helperImage,
					"command", Lists.newArrayList("java"),
					"args", Lists.newArrayList("-classpath", K8S_HELPER_CLASSPATH,
							"io.onedev.k8shelper.Test"),
					"env", initEnvs,
					"volumeMounts", volumeMounts);
			if (alwaysPullImage)
				initContainerSpec.put("imagePullPolicy", "Always");
			setupSecurityContext(initContainerSpec, "0:0");

			Map<Object, Object> mainContainerSpec = newLinkedHashMap(
					"name", TEST_MAIN_CONTAINER_NAME,
					"image", image,
					"command", Lists.newArrayList("sh", "-c"),
					"args", Lists.newArrayList("echo hello from container"),
					"volumeMounts", Lists.newArrayList(newLinkedHashMap(
							"name", volumeName,
							"mountPath", containerTestDirPath)));
			if (alwaysPullImage)
				mainContainerSpec.put("imagePullPolicy", "Always");

			var volumes = new ArrayList<Object>();
			volumes.add(newLinkedHashMap(
					"name", volumeName,
					"persistentVolumeClaim", newLinkedHashMap("claimName", pvcName)));
			if (trustCertsConfigMapCreated) {
				volumes.add(newLinkedHashMap(
						"name", "trust-certs",
						"configMap", newLinkedHashMap("name", trustCertsConfigMapName)));
			}

			var podSpec = new LinkedHashMap<String, Object>();
			podSpec.put("initContainers", Lists.<Object>newArrayList(initContainerSpec));
			podSpec.put("containers", Lists.<Object>newArrayList(mainContainerSpec));
			podSpec.put("volumes", volumes);
			podSpec.put("restartPolicy", "Never");

			Map<Object, Object> podDef = newLinkedHashMap(
					"apiVersion", "v1",
					"kind", "Pod",
					"metadata", newLinkedHashMap(
							"name", podName,
							"namespace", namespace),
					"spec", podSpec);
			createResource(kubectlFactory, podDef, new HashSet<>(), taskLogger);

			try {
				waitForContainerStart(kubectlFactory, namespace, podName, TEST_INIT_CONTAINER_NAME, taskLogger);			
			} catch (Throwable t) {
				collectContainerLog(kubectlFactory, namespace, podName, TEST_INIT_CONTAINER_NAME,
						new CommandCompleted(), taskLogger);
				throw t;
			}
			collectContainerLog(kubectlFactory, namespace, podName, TEST_INIT_CONTAINER_NAME,
					new CommandCompleted(), taskLogger);
			waitForContainerStop(kubectlFactory, namespace, podName, TEST_INIT_CONTAINER_NAME, taskLogger);

			try {
				waitForContainerStart(kubectlFactory, namespace, podName, TEST_MAIN_CONTAINER_NAME, taskLogger);
			} catch (Throwable t) {
				collectContainerLog(kubectlFactory, namespace, podName, TEST_MAIN_CONTAINER_NAME,
						new CommandCompleted(), taskLogger);
				throw t;
			}
			collectContainerLog(kubectlFactory, namespace, podName, TEST_MAIN_CONTAINER_NAME,
					new CommandCompleted(), taskLogger);
			waitForContainerStop(kubectlFactory, namespace, podName, TEST_MAIN_CONTAINER_NAME, taskLogger);
		} finally {
			deleteResource(kubectlFactory, "pod", podName, namespace, true, taskLogger);
			deleteResource(kubectlFactory, "pvc", pvcName, namespace, true, taskLogger);
			if (trustCertsConfigMapCreated)
				deleteResource(kubectlFactory, "configmap", trustCertsConfigMapName, namespace, true, taskLogger);
		}
	}

	public static void waitForContainerStart(Supplier<Commandline> kubectlFactory, String namespace,
			String podName, String containerName, TaskLogger taskLogger) {
		watchPod(kubectlFactory, namespace, podName, (nodeName, containerStatusNodes) -> {
			var error = getContainerErrors(containerStatusNodes).get(containerName);
			if (error != null) {
				String msg;
				if (error instanceof Integer)
					msg = "Container '" + containerName + "' exited with code " + error;
				else
					msg = "Container '" + containerName + "': " + error;
				return new PodWatchAbort(msg);
			}
			if (getStartedContainers(containerStatusNodes).contains(containerName))
				return new PodWatchAbort(null);
			return null;
		}, taskLogger);
	}

	public static void waitForContainerStop(Supplier<Commandline> kubectlFactory, String namespace,
			String podName, String containerName, TaskLogger taskLogger) {
		watchPod(kubectlFactory, namespace, podName, (nodeName, containerStatusNodes) -> {
			var error = getContainerErrors(containerStatusNodes).get(containerName);
			if (error != null) {
				String msg;
				if (error instanceof Integer)
					msg = "Container '" + containerName + "' exited with code " + error;
				else
					msg = "Container '" + containerName + "': " + error;
				return new PodWatchAbort(msg);
			}
			if (getStoppedContainers(containerStatusNodes).contains(containerName))
				return new PodWatchAbort(null);
			return null;
		}, taskLogger);
	}

	public static String createResource(Supplier<Commandline> kubectlFactory,
			Map<Object, Object> resourceDef, Collection<String> secretsToMask, TaskLogger taskLogger) {
		File file = null;
		try {
			file = FileUtils.createTempFile("k8s", ".yaml");

			String resourceYaml = new Yaml().dump(resourceDef);

			String maskedYaml = resourceYaml;
			for (String secret : secretsToMask)
				maskedYaml = Strings.CS.replace(maskedYaml, secret, SecretInput.MASK);
			logger.trace("Creating resource:\n{}", maskedYaml);

			FileUtils.writeFile(file, resourceYaml, UTF_8);

			String resourceName = tryCreateResource(kubectlFactory, file, taskLogger);
			if (resourceName != null)
				return resourceName;

			@SuppressWarnings("unchecked")
			Map<Object, Object> metadata = (Map<Object, Object>) resourceDef.get("metadata");
			String kind = ((String) resourceDef.get("kind")).toLowerCase();
			String name = (String) metadata.get("name");
			String namespace = (String) metadata.get("namespace");

			deleteResource(kubectlFactory, kind, name, namespace, true, taskLogger);

			if (kind.equals("pod") && namespace != null) {
				while (resourceExists(kubectlFactory, kind, name, namespace, taskLogger)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

			String retryResourceName = tryCreateResource(kubectlFactory, file, taskLogger);
			if (retryResourceName == null)
				throw new ExplicitException("Failed to create " + kind + " '" + name + "': resource still exists after deletion");
			return retryResourceName;
		} finally {
			if (file != null)
				file.delete();
		}
	}

	@Nullable
	private static String tryCreateResource(Supplier<Commandline> kubectlFactory, File yamlFile,
			TaskLogger taskLogger) {
		AtomicBoolean alreadyExists = new AtomicBoolean(false);
		AtomicReference<String> resourceNameRef = new AtomicReference<>(null);
		Commandline kubectl = kubectlFactory.get();
		kubectl.addArgs("create", "-f", yamlFile.getAbsolutePath(), "-o", "jsonpath={.metadata.name}");
		var result = kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				resourceNameRef.set(line);
			}

		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.contains("(AlreadyExists)") || line.contains("already exists"))
					alreadyExists.set(true);
				else
					logKubernetesError(taskLogger, line);
			}

		});

		if (result.getReturnCode() == 0)
			return Preconditions.checkNotNull(resourceNameRef.get());
		else if (alreadyExists.get())
			return null;
		else
			throw result.buildException();
	}

	public static void deleteResource(Supplier<Commandline> kubectlFactory, String kind, String name,
			@Nullable String namespace,  boolean ignoreNotFound, TaskLogger taskLogger) {
		try {
			Commandline kubectl = kubectlFactory.get();
			kubectl.addArgs("delete", kind, name);
			if (namespace != null)
				kubectl.addArgs("-n", namespace);
			if (ignoreNotFound)
				kubectl.addArgs("--ignore-not-found");
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.debug(line);
				}

			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logKubernetesError(taskLogger, line);
				}

			}).checkReturnCode();
		} catch (Exception e) {
			if (ExceptionUtils.find(e, TimeoutException.class) == null)
				throw ExceptionUtils.unchecked(e);
			else
				taskLogger.error("Timed out deleting " + kind + " " + name);
		}
	}

	public static void setupSecurityContext(Map<Object, Object> containerSpec, String runAs) {
		var securityContext = new HashMap<>();
		var fields = Splitter.on(':').trimResults().splitToList(runAs);
		securityContext.put("runAsUser", Integer.parseInt(fields.get(0)));
		securityContext.put("runAsGroup", Integer.parseInt(fields.get(1)));
		containerSpec.put("securityContext", securityContext);
	}

	public static final class PodWatchAbort {

		private final @Nullable String errorMessage;

		public PodWatchAbort(@Nullable String errorMessage) {
			this.errorMessage = errorMessage;
		}

		@Nullable
		public String getErrorMessage() {
			return errorMessage;
		}

	}

	@FunctionalInterface
	public interface PodWatchAbortChecker {

		@Nullable
		PodWatchAbort check(@Nullable String nodeName, Collection<JsonNode> containerStatusNodes);

	}

	public static interface CollectLogExitCondition {

		public static class CommandCompleted implements CollectLogExitCondition {
		}
		
		public static class SeenMessage implements CollectLogExitCondition {
		 
			private final String message;
	
			public SeenMessage(String message) {
				this.message = message;
			}        
			
			public String getMessage() {
				return message;
			}
	
		}
	
		public static class SeenMessageOrCommandCompleted extends SeenMessage {
	
			public SeenMessageOrCommandCompleted(String message) {
				super(message);
			}
			
		}
	}
		
}
