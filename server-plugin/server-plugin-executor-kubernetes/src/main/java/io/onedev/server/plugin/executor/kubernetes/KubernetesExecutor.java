package io.onedev.server.plugin.executor.kubernetes;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.k8shelper.ExecuteCondition.ALWAYS;
import static io.onedev.k8shelper.KubernetesHelper.ENV_JOB_TOKEN;
import static io.onedev.k8shelper.KubernetesHelper.ENV_SERVER_URL;
import static io.onedev.k8shelper.KubernetesHelper.IMAGE_REPO;
import static io.onedev.k8shelper.KubernetesHelper.LOG_END_MESSAGE;
import static io.onedev.k8shelper.KubernetesHelper.parseStepPosition;
import static io.onedev.k8shelper.KubernetesHelper.stringifyStepPosition;
import static io.onedev.k8shelper.RegistryLoginFacade.merge;
import static io.onedev.server.util.CollectionUtils.newHashMap;
import static io.onedev.server.util.CollectionUtils.newLinkedHashMap;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.PruneBuilderCacheFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.RunImagetoolsFacade;
import io.onedev.k8shelper.ServiceFacade;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.SettingService;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.job.JobRunnable;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.KubernetesAware;
import io.onedev.server.model.support.administration.jobexecutor.NodeSelectorEntry;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.plugin.executor.kubernetes.KubernetesExecutor.TestData;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.web.util.Testable;

@Editable(order=KubernetesExecutor.ORDER, description="This executor runs build jobs as pods in a kubernetes cluster. "
		+ "No any agents are required."
		+ "<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system "
		+ "settings as job pods need to access it to download source and artifacts")
public class KubernetesExecutor extends JobExecutor implements KubernetesAware, Testable<TestData> {

	private static final long serialVersionUID = 1L;

	static final int ORDER = 40;
	
	private static final int POD_WATCH_TIMEOUT = 60;
	
	private static final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
	
	private static final long NAMESPACE_DELETION_TIMEOUT = 120;
	
	private static final String POD_NAME = "job";
	
	private List<NodeSelectorEntry> nodeSelector = new ArrayList<>();
	
	private String clusterRole;
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private boolean buildWithPV;
	
	private String storageClass;
	
	private String storageSize;
	
	private List<ServiceLocator> serviceLocators = new ArrayList<>();

	private String configFile;
	
	private String kubeCtlPath;
	
	private String cpuRequest = "250m";
	
	private String memoryRequest = "256Mi";
	
	private String cpuLimit;
	
	private String memoryLimit;
	
	private boolean alwaysPullImage = true;
	
	private transient volatile String containerName;
	
	@Editable(order=200, description="Specify registry logins if necessary. For built-in registry, " +
			"use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and " +
			"access token for password")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Editable(order=300, name="Build with Persistent Volume", description="Enable this to place intermediate " +
			"files required by job execution on dynamically allocated persistent volume instead of emptyDir")
	public boolean isBuildWithPV() {
		return buildWithPV;
	}

	public void setBuildWithPV(boolean buildWithPV) {
		this.buildWithPV = buildWithPV;
	}
	
	@Editable(order=400, name="Build Volume Storage Class", placeholder = "Use default storage class", description = "" +
			"Optionally specify a storage class to allocate build volume dynamically. Leave empty to use default storage class. " +
			"<b class='text-warning'>NOTE:</b> Reclaim policy of the storage class should be set to <code>Delete</code>, " +
			"as the volume is only used to hold temporary build files")
	@DependsOn(property="buildWithPV")
	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	@Editable(order=500, name="Build Volume Storage Size", description = "Specify storage size to request " +
			"for the build volume. The size should conform to <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#setting-requests-and-limits-for-local-ephemeral-storage' target='_blank'>Kubernetes resource capacity format</a>, " +
			"for instance <i>10Gi</i>")
	@DependsOn(property="buildWithPV")
	@NotEmpty
	public String getStorageSize() {
		return storageSize;
	}

	public void setStorageSize(String storageSize) {
		this.storageSize = storageSize;
	}

	@Editable(order=400, group = "Resource Settings", description = "Specify cpu request for each job/service using this executor. " +
			"Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details")
	@NotEmpty
	public String getCpuRequest() {
		return cpuRequest;
	}

	public void setCpuRequest(String cpuRequest) {
		this.cpuRequest = cpuRequest;
	}

	@Editable(order=500, group="Resource Settings", description = "Specify memory request for each job/service using this executor. " +
			"Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details")
	@NotEmpty
	public String getMemoryRequest() {
		return memoryRequest;
	}

	public void setMemoryRequest(String memoryRequest) {
		this.memoryRequest = memoryRequest;
	}

	@Editable(order=24990, group="Resource Settings", placeholder = "No limit", description = "" +
			"Optionally specify cpu limit for each job/service using this executor. " +
			"Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details")
	public String getCpuLimit() {
		return cpuLimit;
	}

	public void setCpuLimit(String cpuLimit) {
		this.cpuLimit = cpuLimit;
	}

	@Editable(order=24995, group="Resource Settings", placeholder = "No limit", description = "" +
			"Optionally specify memory limit for each job/service using this executor. " +
			"Check <a href='https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/' target='_blank'>Kubernetes resource management</a> for details")
	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	@Editable(order=600, group="Privilege Settings", description = "Whether or not to always pull image when " +
			"run container or build images. This option should be enabled to avoid images being replaced by " +
			"malicious jobs running on same node")
	public boolean isAlwaysPullImage() {
		return alwaysPullImage;
	}

	public void setAlwaysPullImage(boolean alwaysPullImage) {
		this.alwaysPullImage = alwaysPullImage;
	}

	@Editable(order=500, group = "More Settings", description="Optionally specify node selector of the job pods")
	@Valid
	public List<NodeSelectorEntry> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(List<NodeSelectorEntry> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	@Editable(order=600, group="More Settings", description="Optionally specify cluster role the job pods service account "
			+ "binding to. This is necessary if you want to do things such as running other "
			+ "Kubernetes pods in job command")
	public String getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(String clusterRole) {
		this.clusterRole = clusterRole;
	}
	
	@Editable(order=25000, group="More Settings", description="Optionally specify where to run service pods "
			+ "specified in job. The first matching locator will be used. If no any locators are found, "
			+ "node selector of the executor will be used")
	@Valid
	public List<ServiceLocator> getServiceLocators() {
		return serviceLocators;
	}

	public void setServiceLocators(List<ServiceLocator> serviceLocators) {
		this.serviceLocators = serviceLocators;
	}

	@Editable(name="Kubectl Config File", order=26000, group="More Settings", 
			placeholder="Use default", description="Specify absolute path to the config file "
					+ "used by kubectl to access the cluster. Leave empty to have kubectl "
					+ "determining cluster access information automatically")
	public String getConfigFile() {
		return configFile;
	}
 
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	@Editable(name="Path to kubectl", order=27000, group="More Settings", placeholder="Use default", 
			description="Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. "
			+ "If left empty, OneDev will try to find the utility from system path")
	public String getKubeCtlPath() {
		return kubeCtlPath;
	}

	public void setKubeCtlPath(String kubeCtlPath) {
		this.kubeCtlPath = kubeCtlPath;
	}

	@Override
	public boolean execute(JobContext jobContext, TaskLogger jobLogger) {
		var clusterService = OneDev.getInstance(ClusterService.class);
		var servers = clusterService.getServerAddresses();
		var server = servers.get(RandomUtils.nextInt(0, servers.size()));
		return getJobService().runJob(server, ()-> getJobService().runJob(jobContext, new JobRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean run(TaskLogger jobLogger) {
				return execute(jobLogger, jobContext);
			}

			@Override
			public void resume(JobContext jobContext) {
				Commandline kubectl = newKubeCtl();
				kubectl.addArgs("exec", "job", "--container", "sidecar", "--namespace", getNamespace(jobContext), "--");
				kubectl.addArgs("touch", "/onedev-build/continue");
				kubectl.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.debug(line);
					}

				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.error("Kubernetes: " + line);
					}

				}).checkReturnCode();
			}

			@Override
			public Shell openShell(JobContext jobContext, Terminal terminal) {
				String containerNameCopy = containerName;
				if (containerNameCopy != null) {
					Commandline kubectl = newKubeCtl();
					kubectl.addArgs("exec", "-it", POD_NAME, "-c", containerNameCopy,
							"--namespace", getNamespace(jobContext), "--");

					String workingDir;
					if (containerNameCopy.startsWith("step-")) {
						List<Integer> stepPosition = parseStepPosition(containerNameCopy.substring("step-".length()));
						LeafFacade step = Preconditions.checkNotNull(jobContext.getStep(stepPosition));
						if (step instanceof RunContainerFacade)
							workingDir = ((RunContainerFacade)step).getWorkingDir();
						else 
							workingDir = "/onedev-build/workspace";
					} else {
						workingDir = "/onedev-build/workspace";
					}

					String[] shell = null;
					if (containerNameCopy.startsWith("step-")) {
						List<Integer> stepPosition = parseStepPosition(containerNameCopy.substring("step-".length()));
						LeafFacade step = Preconditions.checkNotNull(jobContext.getStep(stepPosition));
						if (step instanceof CommandFacade)
							shell = ((CommandFacade)step).getShell(false, workingDir);
					}
					if (shell == null) {
						if (workingDir != null) 
							shell = new String[]{"sh", "-c", String.format("cd '%s' && sh", workingDir)};
						else 
							shell = new String[]{"sh"};
					}
					kubectl.addArgs(shell);
					return new CommandlineShell(terminal, kubectl);
				} else {
					throw new ExplicitException("Shell not ready");
				}
			}
		}));
	}
	
	private JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}
	
	private String getNamespace(@Nullable JobContext jobContext) {
		if (jobContext != null) {
			return getName() + "-" + jobContext.getProjectId() + "-" 
					+ jobContext.getBuildNumber() + "-" + jobContext.getSubmitSequence();
		} else {
			return getName() + "-executor-test";
		}
	}

	@Override
	public List<RegistryLoginFacade> getRegistryLogins(String jobToken) {
		return getRegistryLogins().stream().map(it->it.getFacade(jobToken)).collect(toList());
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		execute(jobLogger, testData.getDockerImage());
	}
	
	private Commandline newKubeCtl() {
		String kubectl = getKubeCtlPath();
		if (kubectl == null) {
			if (SystemUtils.IS_OS_MAC_OSX && new File("/usr/local/bin/kubectl").exists())
				kubectl = "/usr/local/bin/kubectl";
			else
				kubectl = "kubectl";
		}
		Commandline cmdline = new Commandline(kubectl); 
		if (getConfigFile() != null)
			cmdline.addArgs("--kubeconfig", getConfigFile());
		return cmdline;
	}
	
	private void logKubernetesError(TaskLogger jobLogger, String message) {
		if (!message.contains("Failed to watch *unstructured.Unstructured: unknown"))
			jobLogger.error("Kubernetes: " + message);
		else 
			logger.error("Kubernetes: " + message);
	}
	
	private String createResource(Map<Object, Object> resourceDef, Collection<String> secretsToMask, TaskLogger jobLogger) {
		Commandline kubectl = newKubeCtl();
		File file = null;
		try {
			AtomicReference<String> resourceNameRef = new AtomicReference<String>(null);
			file = FileUtils.createTempFile("k8s", ".yaml");
			
			String resourceYaml = new Yaml().dump(resourceDef);
			
			String maskedYaml = resourceYaml;
			for (String secret: secretsToMask) 
				maskedYaml = StringUtils.replace(maskedYaml, secret, SecretInput.MASK);
			logger.trace("Creating resource:\n" + maskedYaml);
			
			FileUtils.writeFile(file, resourceYaml, UTF_8);
			kubectl.addArgs("create", "-f", file.getAbsolutePath(), "-o", "jsonpath={.metadata.name}");
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					resourceNameRef.set(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logKubernetesError(jobLogger, line);
				}
				
			}).checkReturnCode();
			
			return Preconditions.checkNotNull(resourceNameRef.get());
		} finally {
			if (file != null)
				file.delete();
		}
	}
	
	private void deleteNamespace(String namespace, TaskLogger jobLogger) {
		try {
			Commandline cmd = newKubeCtl();
			cmd.timeout(NAMESPACE_DELETION_TIMEOUT).addArgs("delete", "namespace", namespace);
			cmd.execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logger.debug(line);
				}
				
			}, new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logKubernetesError(jobLogger, line);
				}
				
			}).checkReturnCode();
		} catch (Exception e) {
			if (ExceptionUtils.find(e, TimeoutException.class) == null)
				throw ExceptionUtils.unchecked(e);
			else
				jobLogger.error("Timed out deleting namespace");
		}
	}
	
	private void deleteClusterRoleBinding(String namespace, TaskLogger jobLogger) {
		Commandline cmd = newKubeCtl();
		cmd.addArgs("delete", "clusterrolebinding", namespace);
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logKubernetesError(jobLogger, line);
			}
			
		}).checkReturnCode();
	}
	
	private void createNamespace(String namespace, @Nullable JobContext jobContext, TaskLogger jobLogger) {
		AtomicBoolean namespaceExists = new AtomicBoolean(false);
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("get", "namespaces", "--field-selector", "metadata.name=" + namespace, 
				"-o", "name", "--chunk-size=0");
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				namespaceExists.set(true);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logKubernetesError(jobLogger, line);
			}
			
		}).checkReturnCode();
		
		if (namespaceExists.get())
			deleteNamespace(namespace, jobLogger);
		
		kubectl = newKubeCtl();
		kubectl.addArgs("create", "namespace", namespace);
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logKubernetesError(jobLogger, line);
			}
			
		}).checkReturnCode();
	}
	
	private String getServerUrl() {
		return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl().toString();
	}
	
	private void mergeAndEnsureUnique(Collection<RegistryLoginFacade> logins, Collection<RegistryLoginFacade> otherLogins) {
		for (var login: logins) {
			if (otherLogins.stream().anyMatch(it->it.getRegistryUrl().equals(login.getRegistryUrl()) && !it.equals(login))) {
				var errorMessage = String.format("Login for registry '%s' should be the same across all " +
						"command steps and services executing via Kubernetes executor", login.getRegistryUrl());
				throw new ExplicitException(errorMessage);
			}
		}
		logins.addAll(otherLogins);
	}
	
	@Nullable
	private String createImagePullSecret(String namespace, @Nullable JobContext jobContext, TaskLogger jobLogger) {
		List<RegistryLoginFacade> registryLogins;
		if (jobContext != null) {
			registryLogins = getRegistryLogins(jobContext.getJobToken());
			var jobRegistryLogins = new ArrayList<RegistryLoginFacade>();
			new CompositeFacade(jobContext.getActions()).traverse((facade, position) -> {
				if (facade instanceof CommandFacade) {
					CommandFacade commandFacade = (CommandFacade) facade;
					mergeAndEnsureUnique(jobRegistryLogins, commandFacade.getRegistryLogins());
				}
				return null;
			}, new ArrayList<>());

			for (var service: jobContext.getServices()) 
				mergeAndEnsureUnique(jobRegistryLogins, service.getRegistryLogins());
			
			registryLogins = merge(jobRegistryLogins, registryLogins);
		} else {
			registryLogins = getRegistryLogins(UUID.randomUUID().toString());
		}

		Map<Object, Object> auths = new LinkedHashMap<>();
		for (var login: registryLogins) {
			String auth = login.getUserName() + ":" + login.getPassword();
			auths.put(login.getRegistryUrl(), newLinkedHashMap(
					"auth", encodeBase64String(auth.getBytes(UTF_8))));
		}
		if (!auths.isEmpty()) {
			ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
			try {
				String dockerConfig = mapper.writeValueAsString(newLinkedHashMap("auths", auths));

				String secretName = "image-pull-secret";
				Map<String, String> encodedSecrets = new LinkedHashMap<>();
				Map<Object, Object> secretDef = newLinkedHashMap(
						"apiVersion", "v1",
						"kind", "Secret",
						"metadata", newLinkedHashMap(
								"name", secretName,
								"namespace", namespace),
						"data", newLinkedHashMap(
								".dockerconfigjson", encodeBase64String(dockerConfig.getBytes(UTF_8))));
				secretDef.put("type", "kubernetes.io/dockerconfigjson");
				createResource(secretDef, encodedSecrets.values(), jobLogger);
				return secretName;
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}
	
	private void createClusterRoleBinding(String namespace, TaskLogger jobLogger) {
		AtomicBoolean clusterRoleBindingExists = new AtomicBoolean(false);
		Commandline cmd = newKubeCtl();
		cmd.addArgs("get", "clusterrolebindings", "--field-selector", "metadata.name=" + namespace, 
				"-o", "name");
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				clusterRoleBindingExists.set(true);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logKubernetesError(jobLogger, line);
			}
			
		}).checkReturnCode();
		
		if (clusterRoleBindingExists.get())
			deleteClusterRoleBinding(namespace, jobLogger);
		
		Map<Object, Object> clusterRoleBindingDef = newLinkedHashMap(
				"apiVersion", "rbac.authorization.k8s.io/v1", 
				"kind", "ClusterRoleBinding", 
				"metadata", newLinkedHashMap(
						"name", namespace), 
				"subjects", Lists.<Object>newArrayList(newLinkedHashMap(
						"kind", "ServiceAccount", 
						"name", "default", 
						"namespace", namespace)), 
				"roleRef", newLinkedHashMap(
						"apiGroup", "rbac.authorization.k8s.io",
						"kind", "ClusterRole", 
						"name", getClusterRole()));
		createResource(clusterRoleBindingDef, new HashSet<>(), jobLogger);
	}	
	
	@Nullable
	private String createTrustCertsConfigMap(String namespace, TaskLogger jobLogger) {
		Map<String, String> configMapData = new LinkedHashMap<>();
		File trustCertsDir = new File(Bootstrap.getConfDir(), "trust-certs");
		if (trustCertsDir.exists()) {
			int index = 1;
			for (File file: trustCertsDir.listFiles()) {
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
							"name", "trust-certs", 
							"namespace", namespace), 
					"binaryData", configMapData);
			return createResource(configMapDef, new HashSet<>(), jobLogger);			
		} else {
			return null;
		}
	}

	private void startService(String namespace, JobContext jobContext, ServiceFacade jobService, 
			@Nullable String imagePullSecretName, TaskLogger jobLogger) {
		jobLogger.log("Creating service pod from image " + jobService.getImage() + "...");
		
		List<NodeSelectorEntry> nodeSelector = getNodeSelector();
		for (ServiceLocator locator: getServiceLocators()) {
			if (locator.isApplicable(jobService)) {
				nodeSelector = locator.getNodeSelector();
				break;
			}
		}
		
		Map<String, Object> podSpec = new LinkedHashMap<>();
		Map<Object, Object> containerSpec = newLinkedHashMap(
				"name", "default", 
				"image", jobService.getImage());
		if (isAlwaysPullImage())
			containerSpec.put("imagePullPolicy", "Always");
		Map<Object, Object> resourcesSpec = newLinkedHashMap(
				"requests", newLinkedHashMap(
						"cpu", getCpuRequest(),
						"memory", getMemoryRequest()));
		Map<Object, Object>	limitsSpec = new LinkedHashMap<>();
		if (getCpuLimit() != null)
			limitsSpec.put("cpu", getCpuLimit());
		if (getMemoryLimit() != null)
			limitsSpec.put("memory", getMemoryLimit());
		if (!limitsSpec.isEmpty())
			resourcesSpec.put("limits", limitsSpec);
		containerSpec.put("resources", resourcesSpec);
		List<Map<Object, Object>> envs = new ArrayList<>();
		for (var entry: jobService.getEnvs().entrySet()) {
			envs.add(newLinkedHashMap(
					"name", entry.getKey(), 
					"value", entry.getValue()));
		}
		if (jobService.getArguments() != null) {
			List<String> argList = new ArrayList<>();
			for (String arg: StringUtils.parseQuoteTokens(jobService.getArguments()))
				argList.add(arg);
			containerSpec.put("args", argList);			
		}
		containerSpec.put("env", envs);
		setupSecurityContext(containerSpec, jobService.getRunAs());
		
		podSpec.put("containers", Lists.<Object>newArrayList(containerSpec));
		if (imagePullSecretName != null)
			podSpec.put("imagePullSecrets", Lists.<Object>newArrayList(newLinkedHashMap("name", imagePullSecretName)));
		podSpec.put("restartPolicy", "Never");		
		
		if (!nodeSelector.isEmpty())
			podSpec.put("nodeSelector", toMap(nodeSelector));
		
		String podName = "service-" + jobService.getName();
		
		Map<Object, Object> podDef = newLinkedHashMap(
				"apiVersion", "v1", 
				"kind", "Pod", 
				"metadata", newLinkedHashMap(
						"name", podName, 
						"namespace", namespace, 
						"labels", newLinkedHashMap(
								"service", jobService.getName())), 
				"spec", podSpec);
		createResource(podDef, Sets.newHashSet(), jobLogger);		
		
		Map<Object, Object> serviceDef = newLinkedHashMap(
				"apiVersion", "v1", 
				"kind", "Service", 
				"metadata", newLinkedHashMap(
						"name", jobService.getName(),
						"namespace", namespace), 
				"spec", newLinkedHashMap(
						"clusterIP", "None", 
						"selector", newLinkedHashMap(
								"service", jobService.getName())));
		createResource(serviceDef, Sets.newHashSet(), jobLogger);
		
		jobLogger.log("Waiting for service to be ready...");
		
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
		while (true) {
			Commandline kubectl = newKubeCtl();
			kubectl.addArgs("get", "pod", podName, "-n", namespace, "-o", "json");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			kubectl.execute(baos, new LineConsumer() {

				@Override
				public void consume(String line) {
					logKubernetesError(jobLogger, line);
				}
				
			}).checkReturnCode();

			JsonNode statusNode;
			try {
				statusNode = mapper.readTree(baos.toString()).get("status");				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			checkConditions(statusNode, jobLogger);
			
			List<JsonNode> containerStatusNodes = new ArrayList<>();
			JsonNode containerStatusesNode = statusNode.get("containerStatuses");
			if (containerStatusesNode != null)
				containerStatusNodes.add(containerStatusesNode.iterator().next());				
			
			Map<String, Object> containerErrors = getContainerErrors(containerStatusNodes);
			if (!containerErrors.isEmpty()) {
				Object error = containerErrors.values().iterator().next();
				String errorMessage;
				if (error instanceof Integer) {
					collectContainerLog(namespace, podName, "default", null, jobLogger);
					errorMessage = "Exited with code " + error;
				} else {
					errorMessage = (String) error;
				}
				throw new ExplicitException("Service " + jobService.getName() + ": " + errorMessage);
			} 
			
			if (!getStoppedContainers(containerStatusNodes).isEmpty()) {
				collectContainerLog(namespace, podName, "default", null, jobLogger);
				throw new ExplicitException("Service " + jobService.getName() + " is stopped unexpectedly");
			}
		
			if (!getStartedContainers(containerStatusNodes).isEmpty()) {
				kubectl = newKubeCtl();
				kubectl.addArgs("exec", podName, "-n", namespace, "--", "sh", "-c");
				kubectl.addArgs(jobService.getReadinessCheckCommand());
				var result = kubectl.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						jobLogger.log("Service readiness check: " + line);
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						jobLogger.log("Service readiness check: " + line);
					}
					
				});
				if (result.getReturnCode() == 0) {
					jobLogger.log("Service is ready");
					break;
				}
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private Map<String, String> toMap(List<NodeSelectorEntry> nodeSelector) {
		Map<String, String> map = new LinkedHashMap<>();
		for (NodeSelectorEntry entry: nodeSelector)
			map.put(entry.getLabelName(), entry.getLabelValue());
		return map;
	}
	
	private void setupSecurityContext(Map<Object, Object> containerSpec, @Nullable String runAs) {
		if (runAs != null) {
			var securityContext = new HashMap<>();
			var fields = Splitter.on(':').trimResults().splitToList(runAs);
			securityContext.put("runAsUser", parseInt(fields.get(0)));
			securityContext.put("runAsGroup", parseInt(fields.get(1)));
			containerSpec.put("securityContext", securityContext);
		}
	}
	
	private boolean execute(TaskLogger jobLogger, Object executionContext) {
		jobLogger.log("Checking cluster access...");
		JobContext jobContext;
		String jobToken;
		if (executionContext instanceof JobContext) {
			jobContext = (JobContext) executionContext;
			jobToken = jobContext.getJobToken();
		} else {
			jobContext = null;
			jobToken = UUID.randomUUID().toString();
		}
		
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("cluster-info");
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				jobLogger.log(line);
			}
			
		}).checkReturnCode();
		
		String namespace = getNamespace(jobContext);
		if (getClusterRole() != null)
			createClusterRoleBinding(namespace, jobLogger);
		
		try {
			createNamespace(namespace, jobContext, jobLogger);
			
			jobLogger.log(String.format("Preparing job (executor: %s, namespace: %s)...", 
					getName(), namespace));
			try {
				String imagePullSecretName = createImagePullSecret(namespace, jobContext, jobLogger);
				if (jobContext != null) {
					for (var jobService: jobContext.getServices()) {
						jobLogger.log("Starting service (name: " + jobService.getName() + ", image: " + jobService.getImage() + ")...");
						startService(namespace, jobContext, jobService, imagePullSecretName, jobLogger);
					}
				}
				
				var trustCertsConfigMapName = createTrustCertsConfigMap(namespace, jobLogger);
				
				if (isBuildWithPV()) {
					Map<Object, Object> pvcDef = newLinkedHashMap(
							"apiVersion", "v1",
							"kind", "PersistentVolumeClaim",
							"metadata", newLinkedHashMap(
									"name", "build-home",
									"namespace", namespace));
					Map<Object, Object> pvcSpecDef = newLinkedHashMap(
							"accessModes", newArrayList("ReadWriteOnce"),
							"resources", newLinkedHashMap(
									"requests", newLinkedHashMap(
												"storage", getStorageSize())));
					if (getStorageClass() != null)
						pvcSpecDef.put("storageClassName", getStorageClass());
					pvcDef.put("spec", pvcSpecDef);
					createResource(pvcDef, Sets.newHashSet(), jobLogger);
				}
				
				Map<String, Object> podSpec = new LinkedHashMap<>();

				List<Map<Object, Object>> containerSpecs = new ArrayList<>();
				
				var containerBuildHome = "/onedev-build";
				var containerWorkspace = containerBuildHome +"/workspace";
				var containerCommandDir = containerBuildHome + "/command";
				var containerTrustCertsDir = containerBuildHome + "/trust-certs";

				Map<String, String> buildHomeMount = newLinkedHashMap(
						"name", "build-home", 
						"mountPath", containerBuildHome);
				Map<String, String> trustCertsMount = newLinkedHashMap(
						"name", "trust-certs", 
						"mountPath", containerTrustCertsDir);
				
				var commonVolumeMounts = newArrayList(buildHomeMount);
				if (trustCertsConfigMapName != null)
					commonVolumeMounts.add(trustCertsMount);
				
				CompositeFacade entryFacade;
				if (jobContext != null) {
					entryFacade = new CompositeFacade(jobContext.getActions());
				} else {
					List<Action> actions = new ArrayList<>();
					CommandFacade facade = new CommandFacade((String) executionContext, null, null,
							"this does not matter", new HashMap<>(), false);
					actions.add(new Action("test", facade, ALWAYS, false));
					entryFacade = new CompositeFacade(actions);
				}
				
				List<String> containerNames = newArrayList("init");
				
				String helperImage = IMAGE_REPO + ":" + KubernetesHelper.getVersion();
				
				ArrayList<Map<Object, Object>> commonEnvs = new ArrayList<>();
				commonEnvs.add(newLinkedHashMap(
						"name", ENV_SERVER_URL, 
						"value", getServerUrl()));
				commonEnvs.add(newLinkedHashMap(
						"name", ENV_JOB_TOKEN, 
						"value", jobToken));
				commonEnvs.add(newLinkedHashMap(
						"name", "ONEDEV_WORKSPACE",
						"value", containerWorkspace
						));

				Collection<String> cachePaths = new HashSet<>();
				entryFacade.traverse((facade, position) -> {
					String containerName = getContainerName(position);
					containerNames.add(containerName);
					Map<Object, Object> stepContainerSpec;
					if (facade instanceof CommandFacade) {
						CommandFacade commandFacade = (CommandFacade) facade;
						if (commandFacade.getImage() == null) {
							throw new ExplicitException("This step can only be executed by server shell "
									+ "executor or remote shell executor");
						}
						
						stepContainerSpec = newHashMap(
								"name", containerName, 
								"image", commandFacade.getImage());
						if (isAlwaysPullImage())
							stepContainerSpec.put("imagePullPolicy", "Always");
						if (commandFacade.isUseTTY())
							stepContainerSpec.put("tty", true);
						var volumeMounts = buildVolumeMounts(cachePaths);
						volumeMounts.addAll(commonVolumeMounts);
						stepContainerSpec.put("volumeMounts", SerializationUtils.clone(volumeMounts));
						stepContainerSpec.put("env", SerializationUtils.clone(commonEnvs));
						setupSecurityContext(stepContainerSpec, commandFacade.getRunAs());
					} else if (facade instanceof BuildImageFacade) {
						throw new ExplicitException("This step can only be executed by server docker executor or " +
								"remote docker executor. Use kaniko step instead to build image in kubernetes cluster");
					} else if (facade instanceof RunContainerFacade || facade instanceof RunImagetoolsFacade 
							|| facade instanceof PruneBuilderCacheFacade) {
						throw new ExplicitException("This step can only be executed by server docker executor or " +
								"remote docker executor");
					} else {
						if (facade instanceof SetupCacheFacade) 
							cachePaths.addAll(((SetupCacheFacade) facade).getPaths());
						stepContainerSpec = newHashMap(
								"name", containerName, 
								"image", helperImage);
						if (isAlwaysPullImage())
							stepContainerSpec.put("imagePullPolicy", "Always");
						var volumeMounts = buildVolumeMounts(cachePaths);
						volumeMounts.addAll(commonVolumeMounts);
						stepContainerSpec.put("volumeMounts", SerializationUtils.clone(volumeMounts));
						stepContainerSpec.put("env", SerializationUtils.clone(commonEnvs));
					}
					
					if (stepContainerSpec != null) {
						String positionStr = stringifyStepPosition(position);
						stepContainerSpec.put("command", newArrayList("sh"));
						stepContainerSpec.put("args", newArrayList(containerCommandDir + "/" + positionStr + ".sh"));

						Map<Object, Object> requestsSpec = newLinkedHashMap(
								"cpu", "0",
								"memory", "0");
						Map<Object, Object> limitsSpec = new LinkedHashMap<>();
						if (getCpuLimit() != null)
							limitsSpec.put("cpu", getCpuLimit());
						if (getMemoryLimit() != null)
							limitsSpec.put("memory", getMemoryLimit());
						if (!limitsSpec.isEmpty()) {
							stepContainerSpec.put(
									"resources", newLinkedHashMap(
											"limits", limitsSpec,
											"requests", requestsSpec));
						}

						containerSpecs.add(stepContainerSpec);
					}
					return null;
				}, new ArrayList<>());
				
				String k8sHelperClassPath = "/k8s-helper/*";
				
				List<String> sidecarArgs = newArrayList(
						"-classpath", k8sHelperClassPath,
						"io.onedev.k8shelper.SideCar");
				List<String> initArgs = newArrayList(
						"-classpath", k8sHelperClassPath, 
						"io.onedev.k8shelper.Init");
				if (jobContext == null) {
					sidecarArgs.add("test");
					initArgs.add("test");
				}

				ArrayList<Object> volumeMounts = buildVolumeMounts(cachePaths);
				volumeMounts.addAll(commonVolumeMounts);
				
				Map<Object, Object> initContainerSpec = newHashMap(
						"name", "init", 
						"image", helperImage, 
						"command", newArrayList("java"), 
						"args", initArgs,
						"env", SerializationUtils.clone(commonEnvs),
						"volumeMounts", SerializationUtils.clone(volumeMounts));
				if (isAlwaysPullImage())
					initContainerSpec.put("imagePullPolicy", "Always");
				
				Map<Object, Object> sidecarContainerSpec = newLinkedHashMap(
						"name", "sidecar", 
						"image", helperImage, 
						"command", newArrayList("java"), 
						"args", sidecarArgs, 
						"env", SerializationUtils.clone(commonEnvs), 
						"volumeMounts", SerializationUtils.clone(volumeMounts));
				if (isAlwaysPullImage())
					sidecarContainerSpec.put("imagePullPolicy", "Always");
				
				sidecarContainerSpec.put("resources", newLinkedHashMap("requests", newLinkedHashMap(
						"cpu", getCpuRequest(), 
						"memory", getMemoryRequest())));
				
				containerSpecs.add(sidecarContainerSpec);
				containerNames.add("sidecar");
				
				podSpec.put("containers", containerSpecs);
				podSpec.put("initContainers", Lists.<Object>newArrayList(initContainerSpec));

				if (imagePullSecretName != null)
					podSpec.put("imagePullSecrets", Lists.<Object>newArrayList(newLinkedHashMap("name", imagePullSecretName)));
				podSpec.put("restartPolicy", "Never");		
				
				if (!getNodeSelector().isEmpty())
					podSpec.put("nodeSelector", toMap(getNodeSelector()));
				
				Map<Object, Object> buildHomeVolume;
				if (isBuildWithPV()) {
					buildHomeVolume = newLinkedHashMap(
							"name", "build-home", 
							"persistentVolumeClaim", newLinkedHashMap(
									"claimName", "build-home"));
				} else {
					buildHomeVolume = newLinkedHashMap(
							"name", "build-home",
							"emptyDir", newLinkedHashMap());
				}
				List<Object> volumes = newArrayList(buildHomeVolume);
				if (trustCertsConfigMapName != null) {
					volumes.add(newLinkedHashMap(
							"name", "trust-certs", 
							"configMap", newLinkedHashMap(
									"name", trustCertsConfigMapName)));
				}
				podSpec.put("volumes", volumes);

				Map<Object, Object> podDef = newLinkedHashMap(
						"apiVersion", "v1", 
						"kind", "Pod", 
						"metadata", newLinkedHashMap(
								"name", POD_NAME, 
								"namespace", namespace), 
						"spec", podSpec);
				
				createResource(podDef, Sets.newHashSet(), jobLogger);
				
				String podFQN = namespace + "/" + POD_NAME;
				
				AtomicReference<String> nodeNameRef = new AtomicReference<>(null);
				
				watchPod(namespace, new AbortChecker() {

					@Override
					public Abort check(String nodeName, Collection<JsonNode> containerStatusNodes) {
						if (nodeName != null) {
							nodeNameRef.set(nodeName);
							return new Abort(null);
						} else {
							return null;
						}
					}
					
				}, jobLogger);
				
				if (jobContext != null)
					notifyJobRunning(jobContext.getBuildId(), null);				
				
				String nodeName = Preconditions.checkNotNull(nodeNameRef.get());
				jobLogger.log("Running job on node " + nodeName + "...");
				
				jobLogger.log("Starting job containers...");
				
				var successful = new AtomicBoolean(true);
				for (String containerName: containerNames) {
					logger.debug("Waiting for start of container (pod: {}, container: {})...", 
							podFQN, containerName);
					
					watchPod(namespace, new AbortChecker() {

						@Override
						public Abort check(String nodeName, Collection<JsonNode> containerStatusNodes) {
							var error = getContainerErrors(containerStatusNodes).get(containerName);
							if (error != null) {
								/*
								 * For non-fatal errors (command exited with non-zero code), we abort the watch 
								 * without an exception, and will continue to collect the container log which 
								 * might contain error details
								 */
								if (error instanceof String) {
									String errorMessage;
									if (containerName.startsWith("step-")) {
										List<Integer> position = parseStepPosition(containerName.substring("step-".length()));
										errorMessage = "Step \"" + entryFacade.getPathAsString(position) + "\": " + error;
									} else {
										errorMessage = "Container \"" + containerName + "\": " + error;
									}
									return new Abort(errorMessage);
								} else {
									return new Abort(null);
								}
							} else if (getStartedContainers(containerStatusNodes).contains(containerName)) {
								return new Abort(null);
							} else {
								return null;
							}
						}
						
					}, jobLogger);
					
					KubernetesExecutor.this.containerName = containerName; 
					try {
						logger.debug("Collecting log of container (pod: {}, container: {})...", 
								podFQN, containerName);
						
						collectContainerLog(namespace, POD_NAME, containerName, LOG_END_MESSAGE, jobLogger);
						
						logger.debug("Waiting for stop of container (pod: {}, container: {})...", 
								podFQN, containerName);
						
						watchPod(namespace, new AbortChecker() {
	
							@Override
							public Abort check(String nodeName, Collection<JsonNode> containerStatusNodes) {
								var error = getContainerErrors(containerStatusNodes).get(containerName);
								if (error != null) {
									// init container error will prevent other containers to start.
									if (containerName.equals("init")) {
										String errorMessage;
										if (error instanceof Integer)
											errorMessage = "Container \"init\": Exited with code " + error;
										else
											errorMessage = "Container \"init\": " + error;
										return new Abort(errorMessage);
									} else if (containerName.startsWith("step-")) {
										/*
										 * Step containers may not run command in case of errors and sidecar container 
										 * will wait indefinitely on the successful/failed mark file in this case, so 
										 * we just abort with error
										 */
										List<Integer> position = parseStepPosition(containerName.substring("step-".length()));
										var stepPath = entryFacade.getPathAsString(position);
										if (error instanceof Integer)
											return new Abort("Step \"" + stepPath + "\": Exited with code " + error);
										else
											return new Abort("Step \"" + stepPath + "\": " + error);
									} else {
										if (error instanceof Integer) {
											if ((int)error == 1) {
												successful.set(false);
												return new Abort(null);
											} else {
												return new Abort("Container \"sidecar\": Exited with code " + error);												
											}
										} else {
											return new Abort("Container \"sidecar\": " + error);
										}
									}
								} else if (getStoppedContainers(containerStatusNodes).contains(containerName)) {
									return new Abort(null);
								} else {
									return null;
								}
							}
							
						}, jobLogger);
					} finally {
						KubernetesExecutor.this.containerName = null;
					}
				}
				return successful.get();
			} finally {
				deleteNamespace(namespace, jobLogger);
			}			
		} finally {
			if (getClusterRole() != null)
				deleteClusterRoleBinding(namespace, jobLogger);
		}
	}
	
	private ArrayList<Object> buildVolumeMounts(Collection<String> cachePaths) {
		var volumeMounts = new ArrayList<>();
		int index = 1;
		for (var cachePath: cachePaths) {
			if (FilenameUtils.getPrefixLength(cachePath) > 0) {
				var volumeMount = newLinkedHashMap(
						"name", "build-home",
						"mountPath", cachePath,
						"subPath", "cache/" + index);
				volumeMounts.add(volumeMount);
			}
			index++;
		}
		return volumeMounts;
	}
	
	private String getContainerName(List<Integer> stepPosition) {
		return "step-" + stringifyStepPosition(stepPosition);
	}
	
	private Map<String, Object> getContainerErrors(Collection<JsonNode> containerStatusNodes) {
		Map<String, Object> containerErrors = new HashMap<>();
		for (JsonNode containerStatusNode: containerStatusNodes) {
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
	
	private Collection<String> getStartedContainers(Collection<JsonNode> containerStatusNodes) {
		Collection<String> startedContainers = new HashSet<>();
		for (JsonNode containerStatusNode: containerStatusNodes) {
			JsonNode stateNode = containerStatusNode.get("state");
			if (stateNode.get("running") != null || stateNode.get("terminated") != null) 
				startedContainers.add(containerStatusNode.get("name").asText());					
		}
		return startedContainers;
	}
	
	private Collection<String> getStoppedContainers(Collection<JsonNode> containerStatusNodes) {
		Collection<String> stoppedContainers = new ArrayList<>();
		for (JsonNode containerStatusNode: containerStatusNodes) {
			JsonNode stateNode = containerStatusNode.get("state");
			if (stateNode.get("terminated") != null)
				stoppedContainers.add(containerStatusNode.get("name").asText());
		}
		return stoppedContainers;
	}
	
	private void checkConditions(JsonNode statusNode, TaskLogger jobLogger) {
		JsonNode conditionsNode = statusNode.get("conditions");
		if (conditionsNode != null) {
			for (JsonNode conditionNode: conditionsNode) {
				if (conditionNode.get("type").asText().equals("PodScheduled") 
						&& conditionNode.get("status").asText().equals("False")
						&& conditionNode.get("reason").asText().equals("Unschedulable")) {
					jobLogger.warning("Kubernetes: " + conditionNode.get("message").asText());
				}
			}
		}
	}
	
	private void watchPod(String namespace, AbortChecker abortChecker, TaskLogger jobLogger) {
		Commandline kubectl = newKubeCtl();
		
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
		
		AtomicReference<Abort> abortRef = new AtomicReference<>(null);
		
		StringBuilder json = new StringBuilder();
		kubectl.addArgs("get", "pod", POD_NAME, "-n", namespace, "--watch", "-o", "json");
		kubectl.timeout(POD_WATCH_TIMEOUT);
		
		Thread thread = Thread.currentThread();
		
		while (true) {
			try {
				kubectl.execute(new LineConsumer() {
		
					@Override
					public void consume(String line) {
						if (line.startsWith("{")) {
							json.append("{").append("\n");
						} else if (line.startsWith("}")) {
							json.append("}");
							logger.trace("Pod watching output:\n" + json.toString());
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
						checkConditions(statusNode, jobLogger);

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
								for (JsonNode containerStatusNode: initContainerStatusesNode)
									containerStatusNodes.add(containerStatusNode);
							}
							JsonNode containerStatusesNode = statusNode.get("containerStatuses");
							if (containerStatusesNode != null) {
								for (JsonNode containerStatusNode: containerStatusesNode)
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
						logKubernetesError(jobLogger, line);
					}
					
				}).checkReturnCode();
				
				throw new ExplicitException("Unexpected end of pod watching");
			} catch (Exception e) {
				Abort abort = abortRef.get();
				if (abort != null) {
					if (abort.getErrorMessage() != null)
						throw new ExplicitException(abort.getErrorMessage());
					else 
						break;
				} else if (ExceptionUtils.find(e, TimeoutException.class) == null) { 
					// If there is no output for some time, let's re-watch as sometimes 
					// pod status update is not pushed
					throw ExceptionUtils.unchecked(e);
				}
			}		
		}
	}

	private void collectContainerLog(String namespace, String podName, String containerName, 
			@Nullable String logEndMessage, TaskLogger jobLogger) {
		Thread thread = Thread.currentThread();
		AtomicReference<Boolean> abortError = new AtomicReference<>(false);
		AtomicReference<Instant> lastInstantRef = new AtomicReference<>(null);
		AtomicBoolean endOfLogSeenRef = new AtomicBoolean(false);
		
		while (true) {
			Commandline kubectl = newKubeCtl();
			kubectl.addArgs("logs", podName, "-c", containerName, "-n", namespace, "--follow", "--timestamps=true");
			if (lastInstantRef.get() != null)
				kubectl.addArgs("--since-time=" + DateTimeFormatter.ISO_INSTANT.format(lastInstantRef.get()));
			
			class Logger extends LineConsumer {

				private final String sessionId = UUID.randomUUID().toString();
				
				@Override
				public void consume(String line) {
					if (line.contains("rpc error:") && line.contains("No such container:") 
							|| line.contains("Unable to retrieve container logs for")) { 
						logger.debug(line);
					} else if (logEndMessage != null && line.contains(logEndMessage)) {
						endOfLogSeenRef.set(true);
						String lastLogMessage = StringUtils.substringBefore(line, logEndMessage);
						if (StringUtils.substringAfter(lastLogMessage, " ").length() != 0)
							consume(lastLogMessage);
					} else if (line.startsWith("Error from server") || line.startsWith("error:")) {
						jobLogger.error(line);
						if (!abortError.get()) {
							abortError.set(true);
							thread.interrupt();
						}
					} else if (line.contains(" ")) {
						String timestamp = StringUtils.substringBefore(line, " ");
						try {
							Instant instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp));
							if (lastInstantRef.get() == null || lastInstantRef.get().isBefore(instant))
								lastInstantRef.set(instant);
							jobLogger.log(StringUtils.substringAfter(line, " "), sessionId);
						} catch (DateTimeParseException e) {
							jobLogger.log(line, sessionId);
						}
					} else {
						jobLogger.log(line, sessionId);
					}
				}
				
			};
			
			try {
				kubectl.execute(new Logger(), new Logger()).checkReturnCode();
			} catch (Exception e) {
				if (!abortError.get()) 
					throw ExceptionUtils.unchecked(e);
			}		
			
			if (logEndMessage == null || endOfLogSeenRef.get() || abortError.get() != null) {
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
	
	private static interface AbortChecker {
		
		@Nullable
		Abort check(@Nullable String nodeName, Collection<JsonNode> containerStatusNodes);
		
	}
	
	private static class Abort {
		
		private final String errorMessage;
		
		public Abort(@Nullable String errorMessage) {
			this.errorMessage = errorMessage;
		}
		
		@Nullable
		public String getErrorMessage() {
			return errorMessage;
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