package io.onedev.server.plugin.kubernetes;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraints.NotEmpty;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.json.JsonEscape;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.Maps;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.JobContext;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.JobExecutor;
import io.onedev.server.plugin.kubernetes.KubernetesExecutor.TestData;
import io.onedev.server.util.JobLogger;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=300)
@ClassValidating
public class KubernetesExecutor extends JobExecutor implements Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
	
	private String configFile;
	
	private String kubeCtlPath;
	
	private String namespace = "onedev-ci";
	
	private List<NodeSelectorEntry> nodeSelector = new ArrayList<>();
	
	private String imagePullSecrets;
	
	private String serviceAccount;
	
	private String cpuRequest = "500m";
	
	private String memoryRequest = "128m";
	
	private String cacheHome;
	
	@Editable(name="Kubectl Config File", order=100, description=
			"Specify absolute path to the config file used by kubectl to access the "
			+ "cluster. Leave empty to have kubectl determining cluster access "
			+ "information automatically")
	@NameOfEmptyValue("Use default")
	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	@Editable(name="Path to kubectl", order=200, description=
			"Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. "
			+ "If left empty, OneDev will try to find the utility from system path")
	@NameOfEmptyValue("Use default")
	public String getKubeCtlPath() {
		return kubeCtlPath;
	}

	public void setKubeCtlPath(String kubeCtlPath) {
		this.kubeCtlPath = kubeCtlPath;
	}

	@Editable(order=20000, group="More Settings", description="Optionally specify Kubernetes namespace "
			+ "used by this executor to place created Kubernetes resources (such as job pods)")
	@NotEmpty
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Editable(order=21000, group="More Settings", description="Optionally specify node selectors of the "
			+ "job pods created by this executor")
	public List<NodeSelectorEntry> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(List<NodeSelectorEntry> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	@Editable(order=22000, group="More Settings", description="Optionally specify space-separated image "
			+ "pull secrets in above namespace for job pods to access private docker registries. "
			+ "Refer to <a href='https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/'>kubernetes "
			+ "documentation</a> on how to set up image pull secrets")
	public String getImagePullSecrets() {
		return imagePullSecrets;
	}

	public void setImagePullSecrets(String imagePullSecrets) {
		this.imagePullSecrets = imagePullSecrets;
	}

	@Editable(order=23000, group="More Settings", description="Optionally specify a service account in above namespace to run the job "
			+ "pod. Refer to <a href='https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/'>"
			+ "kubernetes documentation</a> on how to set up service accounts")
	public String getServiceAccount() {
		return serviceAccount;
	}

	public void setServiceAccount(String serviceAccount) {
		this.serviceAccount = serviceAccount;
	}

	@Editable(order=24000, group="More Settings", description="Specify cpu requirement of jobs using this executor. "
			+ "Refer to <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-cpu'>"
			+ "kubernetes documentation</a> for details")
	@NotEmpty
	public String getCpuRequest() {
		return cpuRequest;
	}

	public void setCpuRequest(String cpuRequest) {
		this.cpuRequest = cpuRequest;
	}

	@Editable(order=25000, group="More Settings", description="Specify memory requirement of jobs using this executor. "
			+ "Refer to <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-memory'>"
			+ "kubernetes documentation</a> for details")
	@NotEmpty
	public String getMemoryRequest() {
		return memoryRequest;
	}

	public void setMemoryRequest(String memoryRequest) {
		this.memoryRequest = memoryRequest;
	}

	@Editable(order=40000, group="More Settings", description="Optionally specify an absolute directory on Kubernetes "
			+ "working nodes to store job caches of this executor. If leave empty, OneDev will use directory "
			+ "<tt>/onedev-cache</tt> on Linux, and <tt>C:\\onedev-cache</tt> on Windows")
	@NameOfEmptyValue("Use default")
	public String getCacheHome() {
		return cacheHome;
	}

	public void setCacheHome(String cacheHome) {
		this.cacheHome = cacheHome;
	}

	private String getEffectiveCacheHome(String osName) {
		if (getCacheHome() != null)
			return cacheHome;
		else if (osName.equalsIgnoreCase("linux"))
			return "/onedev-cache";
		else
			return "C:\\onedev-cache";
	}
	
	@Override
	public void execute(String jobToken, JobContext jobContext) {
		execute(jobContext.getEnvironment(), jobToken, jobContext.getLogger(), jobContext);
	}
	
	@Override
	public void test(TestData testData, JobLogger logger) {
		execute(testData.getDockerImage(), KubernetesResource.TEST_JOB_TOKEN, logger, null);
	}
	
	private Commandline newKubeCtl() {
		String kubectl = getKubeCtlPath();
		if (kubectl == null)
			kubectl = "kubectl";
		Commandline cmdline = new Commandline(kubectl); 
		if (getConfigFile() != null)
			cmdline.addArgs("--kubeconfig", getConfigFile());
		return cmdline;
	}
	
	private String createResource(Map<Object, Object> resourceDef, Collection<String> secretsToMask, JobLogger logger) {
		Commandline kubectl = newKubeCtl();
		File file = null;
		try {
			AtomicReference<String> resourceNameRef = new AtomicReference<String>(null);
			file = File.createTempFile("k8s", ".yaml");
			
			String resourceYaml = new Yaml().dump(resourceDef);
			String maskedYaml = resourceYaml;
			for (String secret: secretsToMask) 
				maskedYaml = StringUtils.replace(maskedYaml, secret, SecretInput.MASK);
			KubernetesExecutor.logger.trace("Creating resource:\n" + maskedYaml);
			
			FileUtils.writeFile(file, resourceYaml, Charsets.UTF_8.name());
			kubectl.addArgs("create", "-f", file.getAbsolutePath(), "-o", "jsonpath={.metadata.name}");
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					resourceNameRef.set(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.log("Kubernetes: " + line);
				}
				
			}).checkReturnCode();
			
			return Preconditions.checkNotNull(resourceNameRef.get());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (file != null)
				file.delete();
		}
	}
	
	private void deleteResource(String resourceType, String resourceName, JobLogger logger) {
		Commandline cmd = newKubeCtl();
		cmd.addArgs("delete", resourceType, resourceName, "--namespace=" + getNamespace());
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				KubernetesExecutor.logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.log("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
	}
	
	private void createNamespaceIfNotExist(JobLogger logger) {
		Commandline cmd = newKubeCtl();
		String query = String.format("{.items[?(@.metadata.name=='%s')]}", getNamespace());
		cmd.addArgs("get", "namespaces", "-o", "jsonpath=" + query);
		
		AtomicBoolean hasNamespace = new AtomicBoolean(false);
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				hasNamespace.set(true);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.log("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
		
		if (!hasNamespace.get()) {
			cmd = newKubeCtl();
			cmd.addArgs("create", "namespace", getNamespace());
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					KubernetesExecutor.logger.debug(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.log("Kubernetes: " + line);
				}
				
			}).checkReturnCode();
		}
	}
	
	private List<Object> getImagePullSecretsData() {
		List<Object> data = new ArrayList<>();
		if (getImagePullSecrets() != null) {
			for (String imagePullSecret: Splitter.on(" ").trimResults().omitEmptyStrings().split(getImagePullSecrets()))
				data.add(Maps.newLinkedHashMap("name", imagePullSecret));
		}
		return data;
	}
	
	private Map<String, String> getNodeSelectorData() {
		Map<String, String> data = new LinkedHashMap<>();
		for (NodeSelectorEntry selector: getNodeSelector())
			data.put(selector.getLabelName(), selector.getLabelValue());
		return data;
	}
	
	private String getOSName(JobLogger logger) {
		logger.log("Checking working node OS...");
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("get", "nodes", "-o", "jsonpath={range .items[*]}{.status.nodeInfo.operatingSystem}{'\\n'}{end}");
		for (NodeSelectorEntry entry: getNodeSelector()) 
			kubectl.addArgs("-l", entry.getLabelName() + "=" + entry.getLabelValue());
		
		AtomicReference<String> osNameRef = new AtomicReference<>(null);
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				osNameRef.set(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.log("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
		
		String osName = osNameRef.get();
		if (osName != null) {
			logger.log(String.format("OS of working node is '%s'", osName));
			return osName;
		} else {
			throw new OneException("No applicable working nodes found for executor '" + getEffectiveCacheHome(osName) + "'");
		}
	}
	
	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
	}
	
	private List<Map<Object, Object>> getSecretEnvs(String secretName, Collection<String> secretKeys) {
		List<Map<Object, Object>> secretEnvs = new ArrayList<>();
		for (String secretKey: secretKeys) {
			Map<Object, Object> secretEnv = new LinkedHashMap<>();
			secretEnv.put("name", secretKey);
			secretEnv.put("valueFrom", Maps.newLinkedHashMap("secretKeyRef", Maps.newLinkedHashMap(
							"name", secretName, 
							"key", secretKey)));
			secretEnvs.add(secretEnv);
		}
		return secretEnvs;
	}
	
	private void execute(String dockerImage, String jobToken, JobLogger logger, @Nullable JobContext jobContext) {
		createNamespaceIfNotExist(logger);

		Map<String, String> secrets = Maps.newLinkedHashMap(KubernetesHelper.ENV_JOB_TOKEN, jobToken);
		String secretName = createSecret(secrets, logger);
		try {
			String osName = getOSName(logger);
			
			Map<String, Object> podSpec = new LinkedHashMap<>();
			
			Map<Object, Object> mainContainerSpec = Maps.newHashMap(
					"name", "main", 
					"image", dockerImage);
	
			String k8sHelperClassPath;
			String containerCIHome;
			String containerWorkspace;
			String containerCacheHome;
			if (osName.equalsIgnoreCase("linux")) {
				containerCIHome = "/onedev-ci";
				containerWorkspace = containerCIHome + "/workspace";
				k8sHelperClassPath = "/k8s-helper/*";
				containerCacheHome = "/onedev-cache";
				mainContainerSpec.put("command", Lists.newArrayList("sh"));
				mainContainerSpec.put("args", Lists.newArrayList(containerCIHome + "/commands.sh"));
			} else {
				containerCIHome = "C:\\onedev-ci";
				containerWorkspace = containerCIHome + "\\workspace";
				k8sHelperClassPath = "C:\\k8s-helper\\*";
				containerCacheHome = "C:\\onedev-cache";
				mainContainerSpec.put("command", Lists.newArrayList("cmd"));
				mainContainerSpec.put("args", Lists.newArrayList("/c", containerCIHome + "\\commands.bat"));
			}

			Map<String, String> ciPathMount = Maps.newLinkedHashMap(
					"name", "ci-home", 
					"mountPath", containerCIHome);
			Map<String, String> cacheHomeMount = Maps.newLinkedHashMap(
					"name", "cache-home", 
					"mountPath", containerCacheHome);
			
			List<Object> volumeMounts = Lists.<Object>newArrayList(ciPathMount, cacheHomeMount);
			
			mainContainerSpec.put("workingDir", containerWorkspace);
			mainContainerSpec.put("volumeMounts", volumeMounts);
			
			mainContainerSpec.put("resources", Maps.newLinkedHashMap("requests", Maps.newLinkedHashMap(
					"cpu", getCpuRequest(), 
					"memory", getMemoryRequest())));
	
			List<Map<Object, Object>> envs = new ArrayList<>();
			Map<Object, Object> serverUrlEnv = Maps.newLinkedHashMap(
					"name", KubernetesHelper.ENV_SERVER_URL, 
					"value", getServerUrl());
			envs.add(serverUrlEnv);
			envs.addAll(getSecretEnvs(secretName, secrets.keySet()));
			
			List<String> sidecarArgs = Lists.newArrayList("-classpath", k8sHelperClassPath, "io.onedev.k8shelper.SideCar");
			List<String> initArgs = Lists.newArrayList("-classpath", k8sHelperClassPath, "io.onedev.k8shelper.Init");
			if (jobContext == null) {
				sidecarArgs.add("test");
				initArgs.add("test");
			}
			Map<Object, Object> sidecarContainerSpec = Maps.newHashMap(
					"name", "sidecar", 
					"image", "1dev/k8s-helper", 
					"command", Lists.newArrayList("java"), 
					"args", sidecarArgs, 
					"env", envs, 
					"volumeMounts", volumeMounts);
			
			Map<Object, Object> initContainerSpec = Maps.newHashMap(
					"name", "init", 
					"image", "1dev/k8s-helper", 
					"command", Lists.newArrayList("java"), 
					"args", initArgs,
					"env", envs,
					"volumeMounts", volumeMounts);
			
			podSpec.put("containers", Lists.<Object>newArrayList(mainContainerSpec, sidecarContainerSpec));
			podSpec.put("initContainers", Lists.<Object>newArrayList(initContainerSpec));
			
			Map<String, String> nodeSelectorData = getNodeSelectorData();
			if (!nodeSelectorData.isEmpty())
				podSpec.put("nodeSelector", nodeSelectorData);
			List<Object> imagePullSecretsData = getImagePullSecretsData();
			if (!imagePullSecretsData.isEmpty())
				podSpec.put("imagePullSecrets", imagePullSecretsData);
			if (getServiceAccount() != null)
				podSpec.put("serviceAccountName", getServiceAccount());
			podSpec.put("restartPolicy", "Never");		
			
			Map<Object, Object> ciHomeVolume = Maps.newLinkedHashMap(
					"name", "ci-home", 
					"emptyDir", Maps.newLinkedHashMap());
			Map<Object, Object> cacheHomeVolume = Maps.newLinkedHashMap(
					"name", "cache-home", 
					"hostPath", Maps.newLinkedHashMap(
							"path", JsonEscape.escapeJson(getEffectiveCacheHome(osName)), 
							"type", "DirectoryOrCreate"));
			
			podSpec.put("volumes", Lists.<Object>newArrayList(ciHomeVolume, cacheHomeVolume));
			
			Map<Object, Object> podDef = Maps.newLinkedHashMap(
					"apiVersion", "v1", 
					"kind", "Pod", 
					"metadata", Maps.newLinkedHashMap(
							"generateName", "job-", 
							"namespace", getNamespace()), 
					"spec", podSpec);
			
			String podName = createResource(podDef, Sets.newHashSet(), logger);
			try {
				logger.log("Preparing job environment...");
				
				KubernetesExecutor.logger.debug("Checking error events (pod: {})...", podName);
				// Some errors only reported via events
				checkEventError(podName, logger);
				
				KubernetesExecutor.logger.debug("Waiting for init container to start (pod: {})...", podName);
				watchPod(podName, new StatusChecker() {

					@Override
					public StopWatch check(JsonNode statusNode) {
						JsonNode initContainerStatusesNode = statusNode.get("initContainerStatuses");
						if (initContainerStatusesNode != null) {
							for (JsonNode initContainerStatusNode: initContainerStatusesNode) {
								JsonNode stateNode = initContainerStatusNode.get("state");
								if (initContainerStatusNode.get("name").asText().equals("init") 
										&& (stateNode.get("running") != null || stateNode.get("terminated") != null)) {
									return new StopWatch(null);
								}
							}
						}
						return null;
					}
					
				}, logger);
				
				if (jobContext != null)
					jobContext.notifyJobRunning();
				
				KubernetesExecutor.logger.debug("Collecting init container log (pod: {})...", podName);
				collectContainerLog(podName, "init", logger);
				
				KubernetesExecutor.logger.debug("Waiting for main container to start (pod: {})...", podName);
				watchPod(podName, new StatusChecker() {

					@Override
					public StopWatch check(JsonNode statusNode) {
						JsonNode initContainerStatusesNode = statusNode.get("initContainerStatuses");
						String errorMessage = getContainerError(initContainerStatusesNode, "init");
						if (errorMessage != null)
							return new StopWatch(new OneException("Error executing init logic: " + errorMessage));
						
						JsonNode containerStatusesNode = statusNode.get("containerStatuses");
						if (isContainerStarted(containerStatusesNode, "main")) 
							return new StopWatch(null);
						else
							return null;
					}
					
				}, logger);
				
				KubernetesExecutor.logger.debug("Collecting main container log (pod: {})...", podName);
				collectContainerLog(podName, "main", logger);
				
				KubernetesExecutor.logger.debug("Waiting for sidecar container to start (pod: {})...", podName);
				watchPod(podName, new StatusChecker() {

					@Override
					public StopWatch check(JsonNode statusNode) {
						JsonNode containerStatusesNode = statusNode.get("containerStatuses");
						String errorMessage = getContainerError(containerStatusesNode, "main");
						if (errorMessage != null)
							return new StopWatch(new OneException(errorMessage));
						
						if (isContainerStarted(containerStatusesNode, "sidecar"))
							return new StopWatch(null);
						else
							return null;
					}
					
				}, logger);
				
				KubernetesExecutor.logger.debug("Collecting sidecar container log (pod: {})...", podName);
				collectContainerLog(podName, "sidecar", logger);
				
				KubernetesExecutor.logger.debug("Checking sidecar container result (pod: {})...", podName);
				watchPod(podName, new StatusChecker() {

					@Override
					public StopWatch check(JsonNode statusNode) {
						JsonNode containerStatusesNode = statusNode.get("containerStatuses");
						String errorMessage = getContainerError(containerStatusesNode, "sidecar");
						if (errorMessage != null)
							return new StopWatch(new OneException("Error executing sidecar logic: " + errorMessage));
						else if (isContainerStopped(containerStatusesNode, "sidecar"))
							return new StopWatch(null);
						else
							return null;
					}
					
				}, logger);
				
			} finally {
				deleteResource("pod", podName, logger);
			}
		} finally {
			deleteResource("secret", secretName, logger);
		}
	}
	
	@Nullable
	private String getContainerError(@Nullable JsonNode containerStatusesNode, String containerName) {
		if (containerStatusesNode != null) {
			for (JsonNode containerStatusNode: containerStatusesNode) {
				JsonNode stateNode = containerStatusNode.get("state");
				if (containerStatusNode.get("name").asText().equals(containerName)) {
					JsonNode terminatedNode = stateNode.get("terminated");
					if (terminatedNode != null) {
						String reason;
						JsonNode reasonNode = terminatedNode.get("reason");
						if (reasonNode != null)
							reason = reasonNode.asText();
						else
							reason = "terminated for unknown reason";
						
						if (!reason.equals("Completed")) {
							JsonNode messageNode = terminatedNode.get("message");
							if (messageNode != null) {
								return messageNode.asText();
							} else {
								JsonNode exitCodeNode = terminatedNode.get("exitCode");
								if (exitCodeNode != null && exitCodeNode.asInt() != 0)
									return "exit code: " + exitCodeNode.asText();
								else
									return reason;
							}
						}
					}
					break;
				}
			}
		}
		return null;
	}
	
	private boolean isContainerStarted(@Nullable JsonNode containerStatusesNode, String containerName) {
		if (containerStatusesNode != null) {
			for (JsonNode containerStatusNode: containerStatusesNode) {
				if (containerStatusNode.get("name").asText().equals(containerName)) {
					JsonNode stateNode = containerStatusNode.get("state");
					if (stateNode.get("running") != null || stateNode.get("terminated") != null)
						return true;
					break;
				}
			}
		}
		return false;
	}
	
	private boolean isContainerStopped(@Nullable JsonNode containerStatusesNode, String containerName) {
		if (containerStatusesNode != null) {
			for (JsonNode containerStatusNode: containerStatusesNode) {
				if (containerStatusNode.get("name").asText().equals(containerName)) {
					JsonNode stateNode = containerStatusNode.get("state");
					if (stateNode.get("terminated") != null)
						return true;
					break;
				}
			}
		}
		return false;
	}
	
	private String createSecret(Map<String, String> secrets, JobLogger logger) {
		Map<String, String> encodedSecrets = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry: secrets.entrySet())
			encodedSecrets.put(entry.getKey(), Base64.getEncoder().encodeToString(entry.getValue().getBytes(Charsets.UTF_8)));
		Map<Object, Object> secretDef = Maps.newLinkedHashMap(
				"apiVersion", "v1", 
				"kind", "Secret", 
				"metadata", Maps.newLinkedHashMap(
						"generateName", "secret-", 
						"namespace", getNamespace()), 
				"data", encodedSecrets);
		return createResource(secretDef, encodedSecrets.values(), logger);
	}
	
	private void watchPod(String podName, StatusChecker statusChecker, JobLogger logger) {
		Commandline kubectl = newKubeCtl();
		
		ObjectMapper mapper = new ObjectMapper();
		
		AtomicReference<StopWatch> stopWatchRef = new AtomicReference<>(null); 
		
		StringBuilder json = new StringBuilder();
		kubectl.addArgs("get", "pod", podName, "-n", getNamespace(), "--watch", "-o", "json");
		Thread thread = Thread.currentThread();
		try {
			kubectl.execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.startsWith("{")) {
						json.append("{").append("\n");
					} else if (line.startsWith("}")) {
						json.append("}");
						KubernetesExecutor.logger.trace("Watching pod:\n" + json.toString());
						try {
							process(mapper.readTree(json.toString()));
						} catch (Exception e) {
							KubernetesExecutor.logger.error("Error processing pod watching record", e);
						}
						json.setLength(0);
					} else {
						json.append(line).append("\n");
					}
				}

				private void process(JsonNode podNode) {
					String errorMessage = null;
					JsonNode statusNode = podNode.get("status");
					JsonNode conditionsNode = statusNode.get("conditions");
					if (conditionsNode != null) {
						for (JsonNode conditionNode: conditionsNode) {
							if (conditionNode.get("type").asText().equals("PodScheduled") 
									&& conditionNode.get("status").asText().equals("False")
									&& conditionNode.get("reason").asText().equals("Unschedulable")) {
								logger.log("Kubernetes: " + conditionNode.get("message").asText());
							}
						}
					}
					
					Collection<JsonNode> containerStatusNodes = new ArrayList<>();
					JsonNode initContainerStatusesNode = statusNode.get("initContainerStatuses");
					if (initContainerStatusesNode != null) {
						for (JsonNode containerStatusNode: initContainerStatusesNode)
							containerStatusNodes.add(containerStatusNode);
					}
					JsonNode containerStatusesNode = statusNode.get("containerStatuses");
					if (containerStatusesNode != null) {
						for (JsonNode containerStatusNode: containerStatusesNode)
							containerStatusNodes.add(containerStatusNode);
					}
					
					for (JsonNode containerStatusNode: containerStatusNodes) {
						JsonNode stateNode = containerStatusNode.get("state");
						JsonNode waitingNode = stateNode.get("waiting");
						if (waitingNode != null) {
							String reason = waitingNode.get("reason").asText();
							if (reason.equals("ErrImagePull") || reason.equals("InvalidImageName") 
									|| reason.equals("ImageInspectError") || reason.equals("ErrImageNeverPull")
									|| reason.equals("RegistryUnavailable")) {
								JsonNode messageNode = waitingNode.get("message");
								if (messageNode != null)
									errorMessage = messageNode.asText();
								else
									errorMessage = reason;
								break;
							}
						} 
					}
					if (errorMessage != null) 
						stopWatchRef.set(new StopWatch(new OneException(errorMessage)));
					else 
						stopWatchRef.set(statusChecker.check(statusNode));
					if (stopWatchRef.get() != null) 
						thread.interrupt();
				}
				
			}, new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logger.log("Kubernetes: " + line);
				}
				
			}).checkReturnCode();
			
			throw new OneException("Unexpected end of pod watching");
		} catch (Exception e) {
			StopWatch stopWatch = stopWatchRef.get();
			if (stopWatch != null) {
				if (stopWatch.getException() != null)
					throw stopWatch.getException();
			} else { 
				throw ExceptionUtils.unchecked(e);
			}
		}		
	}
	
	private void checkEventError(String podName, JobLogger logger) {
		Commandline kubectl = newKubeCtl();
		
		ObjectMapper mapper = new ObjectMapper();
		
		StringBuilder json = new StringBuilder();
		kubectl.addArgs("get", "event", "-n", getNamespace(), "--field-selector", 
				"involvedObject.kind=Pod,involvedObject.name=" + podName, "--watch", 
				"-o", "json");
		Thread thread = Thread.currentThread();
		AtomicReference<StopWatch> stopWatchRef = new AtomicReference<>(null);
		try {
			kubectl.execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.startsWith("{")) {
						json.append("{").append("\n");
					} else if (line.startsWith("}")) {
						json.append("}");
						KubernetesExecutor.logger.trace("Watching event:\n" + json.toString());
						try {
							JsonNode eventNode = mapper.readTree(json.toString()); 
							String type = eventNode.get("type").asText();
							String reason = eventNode.get("reason").asText();
							JsonNode messageNode = eventNode.get("message");
							String message = messageNode!=null? messageNode.asText(): reason;
							if (type.equals("Warning")) {
								if (reason.equals("FailedScheduling"))
									logger.log("Kubernetes: " + message);
								else
									stopWatchRef.set(new StopWatch(new OneException(message)));
							} else if (type.equals("Normal") && reason.equals("Started")) {
								stopWatchRef.set(new StopWatch(null));
							}
							if (stopWatchRef.get() != null)
								thread.interrupt();
						} catch (Exception e) {
							KubernetesExecutor.logger.error("Error processing event watching record", e);
						}
						json.setLength(0);
					} else {
						json.append(line).append("\n");
					}
				}
				
			}, new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logger.log("Kubernetes: " + line);
				}
				
			}).checkReturnCode();
			
			throw new OneException("Unexpected end of pod watching");
		} catch (Exception e) {
			StopWatch stopWatch = stopWatchRef.get();
			if (stopWatch != null) {
				if (stopWatch.getException() != null)
					throw stopWatch.getException();
			} else { 
				throw ExceptionUtils.unchecked(e);
			}
		}		
	}
	
	private void collectContainerLog(String podName, String containerName, JobLogger logger) {
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("logs", podName, "-c", containerName, "-n", getNamespace(), "--follow");
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.log(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.log(line);
			}
			
		}).checkReturnCode();
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		
		if (getCacheHome() != null && FilenameUtils.getPrefixLength(getCacheHome()) == 0) {
			isValid = false;
			context.buildConstraintViolationWithTemplate("An absolute path is expected")
					.addPropertyNode("cacheHome").addConstraintViolation();
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}

	@Editable
	public static class NodeSelectorEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		private String labelName;
		
		private String labelValue;

		@Editable(order=100)
		public String getLabelName() {
			return labelName;
		}

		public void setLabelName(String labelName) {
			this.labelName = labelName;
		}

		@Editable(order=200)
		public String getLabelValue() {
			return labelValue;
		}

		public void setLabelValue(String labelValue) {
			this.labelValue = labelValue;
		}
		
	}
	
	private static interface StatusChecker {
		
		StopWatch check(JsonNode statusNode);
		
	}
	
	private static class StopWatch {
		
		private final RuntimeException exception;
		
		public StopWatch(@Nullable RuntimeException exception) {
			this.exception = exception;
		}
		
		@Nullable
		public RuntimeException getException() {
			return exception;
		}
		
	}
	
	@Editable(name="Specify a Docker Image to Test Against")
	public static class TestData implements Serializable {

		private static final long serialVersionUID = 1L;

		private String dockerImage;

		@Editable
		@OmitName
		@NotEmpty
		public String getDockerImage() {
			return dockerImage;
		}

		public void setDockerImage(String dockerImage) {
			this.dockerImage = dockerImage;
		}
		
	}

}