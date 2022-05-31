package io.onedev.server.plugin.executor.kubernetes;

import static io.onedev.k8shelper.KubernetesHelper.ENV_JOB_TOKEN;
import static io.onedev.k8shelper.KubernetesHelper.ENV_OS_INFO;
import static io.onedev.k8shelper.KubernetesHelper.ENV_SERVER_URL;
import static io.onedev.k8shelper.KubernetesHelper.IMAGE_REPO_PREFIX;
import static io.onedev.k8shelper.KubernetesHelper.LOG_END_MESSAGE;
import static io.onedev.k8shelper.KubernetesHelper.stringifyPosition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.WordUtils;
import org.hibernate.validator.constraints.NotEmpty;
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

import io.onedev.agent.job.FailedException;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.LeafVisitor;
import io.onedev.k8shelper.OsContainer;
import io.onedev.k8shelper.OsExecution;
import io.onedev.k8shelper.OsInfo;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.NodeSelectorEntry;
import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.plugin.executor.kubernetes.KubernetesExecutor.TestData;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.PKCS12CertExtractor;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=KubernetesExecutor.ORDER, description="This executor runs build jobs as pods in a kubernetes cluster. "
		+ "No any agents are required."
		+ "<b class='text-danger'>Note:</b> Make sure server url is specified correctly in system "
		+ "setting as job pods need to access it to download source and artifacts")
@Horizontal
public class KubernetesExecutor extends JobExecutor implements Testable<TestData> {

	private static final long serialVersionUID = 1L;

	static final int ORDER = 40;
	
	private static final int POD_WATCH_TIMEOUT = 60;
	
	private static final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
	
	private static final long NAMESPACE_DELETION_TIMEOUT = 120;
	
	private List<NodeSelectorEntry> nodeSelector = new ArrayList<>();
	
	private String clusterRole;
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private List<ServiceLocator> serviceLocators = new ArrayList<>();

	private String configFile;
	
	private String kubeCtlPath;
	
	private boolean mountContainerSock;
	
	@Editable(order=20, description="Optionally specify node selector of the job pods")
	public List<NodeSelectorEntry> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(List<NodeSelectorEntry> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	@Editable(order=40, description="Optionally specify cluster role the job pods service account "
			+ "binding to. This is necessary if you want to do things such as running other "
			+ "Kubernetes pods in job command")
	public String getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(String clusterRole) {
		this.clusterRole = clusterRole;
	}

	@Editable(order=200, description="Specify login information of docker registries if necessary. "
			+ "These logins will be used to create image pull secrets of the job pods")
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}
	
	@Editable(order=300, description="Whether or not to mount docker/containerd sock into job "
			+ "container to support container operations in job commands, for instance to build "
			+ "container image.<br>"
			+ "<b class='text-danger'>WARNING</b>: Malicious jobs can take control of k8s node "
			+ "running the job by operating the mounted container sock. You should configure "
			+ "job authorization below to make sure the executor can only be used by trusted "
			+ "jobs if this option is enabled")
	public boolean isMountContainerSock() {
		return mountContainerSock;
	}

	public void setMountContainerSock(boolean mountContainerSock) {
		this.mountContainerSock = mountContainerSock;
	}

	@Editable(order=25000, group="More Settings", description="Optionally specify where to run service pods "
			+ "specified in job. The first matching locator will be used. If no any locators are found, "
			+ "node selector of the executor will be used")
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
	public void execute(String jobToken, JobContext jobContext) {
		execute(jobToken, jobContext.getLogger(), jobContext);
	}
	
	@Override
	public boolean isPlaceholderAllowed() {
		return false;
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		execute(UUID.randomUUID().toString(), jobLogger, testData.getDockerImage());
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
	
	private String createResource(Map<Object, Object> resourceDef, Collection<String> secretsToMask, TaskLogger jobLogger) {
		Commandline kubectl = newKubeCtl();
		File file = null;
		try {
			AtomicReference<String> resourceNameRef = new AtomicReference<String>(null);
			file = File.createTempFile("k8s", ".yaml");
			
			String resourceYaml = new Yaml().dump(resourceDef);
			
			String maskedYaml = resourceYaml;
			for (String secret: secretsToMask) 
				maskedYaml = StringUtils.replace(maskedYaml, secret, SecretInput.MASK);
			logger.trace("Creating resource:\n" + maskedYaml);
			
			FileUtils.writeFile(file, resourceYaml, StandardCharsets.UTF_8.name());
			kubectl.addArgs("create", "-f", file.getAbsolutePath(), "-o", "jsonpath={.metadata.name}");
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					resourceNameRef.set(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					jobLogger.error("Kubernetes: " + line);
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
					jobLogger.error("Kubernetes: " + line);
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
				jobLogger.error("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
	}
	
	private String createNamespace(String namespace, @Nullable JobContext jobContext, TaskLogger jobLogger) {
		AtomicBoolean namespaceExists = new AtomicBoolean(false);
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("get", "namespaces", "--field-selector", "metadata.name=" + namespace, 
				"-o", "name");
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				namespaceExists.set(true);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				jobLogger.error("Kubernetes: " + line);
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
				jobLogger.error("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
		
		return namespace;
	}
	
	private OsInfo getBaselineOsInfo(Collection<NodeSelectorEntry> nodeSelector, TaskLogger jobLogger) {
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("get", "nodes", "-o", "jsonpath={range .items[*]}{.status.nodeInfo.operatingSystem} {.status.nodeInfo.kernelVersion} {.status.nodeInfo.architecture} {.spec.unschedulable}{'|'}{end}");
		for (NodeSelectorEntry entry: nodeSelector) 
			kubectl.addArgs("-l", entry.getLabelName() + "=" + entry.getLabelValue());
		
		Collection<OsInfo> osInfos = new ArrayList<>();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		kubectl.execute(baos, new LineConsumer() {

			@Override
			public void consume(String line) {
				jobLogger.error("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
		
		for (String osInfoString: Splitter.on('|').trimResults().omitEmptyStrings().splitToList(baos.toString())) {
			osInfoString = osInfoString.replace('\n', ' ').replace('\r', ' ');
			List<String> fields = Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(osInfoString);
			if (fields.size() == 3 || fields.get(3).equals("false")) {
				String osName = WordUtils.capitalize(fields.get(0));
				String osVersion = fields.get(1);
				if (osName.equals("Windows"))
					osVersion = StringUtils.substringBeforeLast(osVersion, ".");
				osInfos.add(new OsInfo(osName, osVersion, fields.get(2)));
			}
		}

		if (!osInfos.isEmpty()) {
			return OsInfo.getBaseline(osInfos);
		} else {
			throw new ExplicitException("No applicable working nodes found");
		}
	}
	
	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl().toString();
	}
	
	@Nullable
	private String createImagePullSecret(String namespace, TaskLogger jobLogger) {
		if (!getRegistryLogins().isEmpty()) {
			Map<Object, Object> auths = new LinkedHashMap<>();
			for (RegistryLogin login: getRegistryLogins()) {
				String auth = login.getUserName() + ":" + login.getPassword();
				String registryUrl = login.getRegistryUrl();
				if (registryUrl == null)
					registryUrl = "https://index.docker.io/v1/";
				auths.put(registryUrl, CollectionUtils.newLinkedHashMap(
						"auth", Base64.encodeBase64String(auth.getBytes(StandardCharsets.UTF_8))));
			}
			ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
			try {
				String dockerConfig = mapper.writeValueAsString(CollectionUtils.newLinkedHashMap("auths", auths));
				
				String secretName = "image-pull-secret";
				Map<String, String> encodedSecrets = new LinkedHashMap<>();
				Map<Object, Object> secretDef = CollectionUtils.newLinkedHashMap(
						"apiVersion", "v1", 
						"kind", "Secret", 
						"metadata", CollectionUtils.newLinkedHashMap(
								"name", secretName, 
								"namespace", namespace), 
						"data", CollectionUtils.newLinkedHashMap(
								".dockerconfigjson", Base64.encodeBase64String(dockerConfig.getBytes(StandardCharsets.UTF_8))));
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
				jobLogger.error("Kubernetes: " + line);
			}
			
		}).checkReturnCode();
		
		if (clusterRoleBindingExists.get())
			deleteClusterRoleBinding(namespace, jobLogger);
		
		Map<Object, Object> clusterRoleBindingDef = CollectionUtils.newLinkedHashMap(
				"apiVersion", "rbac.authorization.k8s.io/v1", 
				"kind", "ClusterRoleBinding", 
				"metadata", CollectionUtils.newLinkedHashMap(
						"name", namespace), 
				"subjects", Lists.<Object>newArrayList(CollectionUtils.newLinkedHashMap(
						"kind", "ServiceAccount", 
						"name", "default", 
						"namespace", namespace)), 
				"roleRef", CollectionUtils.newLinkedHashMap(
						"apiGroup", "rbac.authorization.k8s.io",
						"kind", "ClusterRole", 
						"name", getClusterRole()));
		createResource(clusterRoleBindingDef, new HashSet<>(), jobLogger);
	}	
	
	@Nullable
	private String createTrustCertsConfigMap(String namespace, TaskLogger jobLogger) {
		Map<String, String> configMapData = new LinkedHashMap<>();
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class); 
		File keystoreFile = serverConfig.getKeystoreFile();
		if (keystoreFile != null) {
			String password = serverConfig.getKeystorePassword();
			for (Map.Entry<String, String> entry: new PKCS12CertExtractor(keystoreFile, password).extact().entrySet()) 
				configMapData.put(entry.getKey(), entry.getValue());
		}
		File trustCertsDir = serverConfig.getTrustCertsDir();
		if (trustCertsDir != null) {
			for (File file: trustCertsDir.listFiles()) {
				if (file.isFile()) {
					try {
						configMapData.put("specified-cert-" + file.getName(), 
								FileUtils.readFileToString(file, StandardCharsets.UTF_8));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		if (!configMapData.isEmpty()) {
			Map<Object, Object> configMapDef = CollectionUtils.newLinkedHashMap(
					"apiVersion", "v1", 
					"kind", "ConfigMap",
					"metadata", CollectionUtils.newLinkedHashMap(
							"name", "trust-certs", 
							"namespace", namespace), 
					"data", configMapData);
			return createResource(configMapDef, new HashSet<>(), jobLogger);			
		} else {
			return null;
		}
	}
	
	private void startService(String namespace, JobContext jobContext, Service jobService, 
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
		Map<Object, Object> containerSpec = CollectionUtils.newHashMap(
				"name", "default", 
				"image", jobService.getImage());
		containerSpec.put("resources", CollectionUtils.newLinkedHashMap("requests", CollectionUtils.newLinkedHashMap(
				"cpu", jobService.getCpuRequirement() + "m", 
				"memory", jobService.getMemoryRequirement() + "Mi")));
		List<Map<Object, Object>> envs = new ArrayList<>();
		for (EnvVar envVar: jobService.getEnvVars()) {
			envs.add(CollectionUtils.newLinkedHashMap(
					"name", envVar.getName(), 
					"value", envVar.getValue()));
		}
		if (jobService.getArguments() != null) {
			List<String> argList = new ArrayList<>();
			for (String arg: StringUtils.parseQuoteTokens(jobService.getArguments()))
				argList.add(arg);
			containerSpec.put("args", argList);			
		}
		containerSpec.put("env", envs);
		
		podSpec.put("containers", Lists.<Object>newArrayList(containerSpec));
		if (imagePullSecretName != null)
			podSpec.put("imagePullSecrets", Lists.<Object>newArrayList(CollectionUtils.newLinkedHashMap("name", imagePullSecretName)));
		podSpec.put("restartPolicy", "Never");		
		
		if (!nodeSelector.isEmpty())
			podSpec.put("nodeSelector", toMap(nodeSelector));
		
		String podName = "service-" + jobService.getName();
		
		Map<Object, Object> podDef = CollectionUtils.newLinkedHashMap(
				"apiVersion", "v1", 
				"kind", "Pod", 
				"metadata", CollectionUtils.newLinkedHashMap(
						"name", podName, 
						"namespace", namespace, 
						"labels", CollectionUtils.newLinkedHashMap(
								"service", jobService.getName())), 
				"spec", podSpec);
		createResource(podDef, Sets.newHashSet(), jobLogger);		
		
		Map<Object, Object> serviceDef = CollectionUtils.newLinkedHashMap(
				"apiVersion", "v1", 
				"kind", "Service", 
				"metadata", CollectionUtils.newLinkedHashMap(
						"name", jobService.getName(),
						"namespace", namespace), 
				"spec", CollectionUtils.newLinkedHashMap(
						"clusterIP", "None", 
						"selector", CollectionUtils.newLinkedHashMap(
								"service", jobService.getName())));
		createResource(serviceDef, Sets.newHashSet(), jobLogger);
		
		jobLogger.log("Waiting for service to be ready...");
		
		OsInfo baselineOsInfo = getBaselineOsInfo(nodeSelector, jobLogger);
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
		while (true) {
			Commandline kubectl = newKubeCtl();
			kubectl.addArgs("get", "pod", podName, "-n", namespace, "-o", "json");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			kubectl.execute(baos, new LineConsumer() {

				@Override
				public void consume(String line) {
					jobLogger.error("Kubernetes: " + line);
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
			
			Map<String, ContainerError> containerErrors = getContainerErrors(containerStatusNodes);
			if (!containerErrors.isEmpty()) {
				ContainerError error = containerErrors.values().iterator().next();
				if (!error.isFatal()) 
					collectContainerLog(namespace, podName, "default", null, jobLogger);
				throw new ExplicitException("Service " + jobService.getName() + ": " + error.getMessage());
			} 
			
			if (!getStoppedContainers(containerStatusNodes).isEmpty()) {
				collectContainerLog(namespace, podName, "default", null, jobLogger);
				throw new ExplicitException("Service " + jobService.getName() + " is stopped unexpectedly");
			}
		
			if (!getStartedContainers(containerStatusNodes).isEmpty()) {
				kubectl = newKubeCtl();
				kubectl.addArgs("exec", podName, "-n", namespace, "--");
				if (baselineOsInfo.isLinux())
					kubectl.addArgs("sh", "-c");
				else 
					kubectl.addArgs("cmd.exe", "/c");
				kubectl.addArgs(jobService.getReadinessCheckCommand());
				ExecutionResult result = kubectl.execute(new LineConsumer() {

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
	
	private void execute(String jobToken, TaskLogger jobLogger, Object executionContext) {
		jobLogger.log("Checking cluster access...");
		JobContext jobContext;
		if (executionContext instanceof JobContext)
			jobContext = (JobContext) executionContext;
		else
			jobContext = null;
		
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
		
		String namespace ;
		if (jobContext != null) {
			namespace = getName() + "-" + jobContext.getProjectId() + "-" 
					+ jobContext.getBuildNumber() + "-" + jobContext.getRetried();
		} else {
			namespace = getName() + "-executor-test";
		}
		
		if (getClusterRole() != null)
			createClusterRoleBinding(namespace, jobLogger);
		
		try {
			createNamespace(namespace, jobContext, jobLogger);
			
			jobLogger.log(String.format("Executing job (executor: %s, namespace: %s)...", 
					getName(), namespace));
			try {
				String imagePullSecretName = createImagePullSecret(namespace, jobLogger);
				if (jobContext != null) {
					for (Service jobService: jobContext.getServices()) {
						jobLogger.log("Starting service (name: " + jobService.getName() + ", image: " + jobService.getImage() + ")...");
						startService(namespace, jobContext, jobService, imagePullSecretName, jobLogger);
					}
				}
				
				String trustCertsConfigMapName = createTrustCertsConfigMap(namespace, jobLogger);
				
				OsInfo osInfo = getBaselineOsInfo(getNodeSelector(), jobLogger);
				
				Map<String, Object> podSpec = new LinkedHashMap<>();

				List<Map<Object, Object>> containerSpecs = new ArrayList<>();
				
				String containerBuildHome;
				String containerCommandHome;
				String containerCacheHome;
				String containerAuthInfoHome;
				String trustCertsHome;
				String dockerSock;
				String containerdSock;
				String containerWorkspace;
				if (osInfo.isWindows()) {
					containerBuildHome = "C:\\onedev-build";
					containerWorkspace = containerBuildHome + "\\workspace";
					containerCacheHome = containerBuildHome + "\\cache";
					containerCommandHome = containerBuildHome + "\\command";
					containerAuthInfoHome = "C:\\Users\\ContainerAdministrator\\auth-info";
					trustCertsHome = containerBuildHome + "\\trust-certs";
					dockerSock = "\\\\.\\pipe\\docker_engine";
					containerdSock = "\\\\.\\pipe\\containerd-containerd";
				} else {
					containerBuildHome = "/onedev-build";
					containerWorkspace = containerBuildHome +"/workspace";
					containerCacheHome = containerBuildHome + "/cache";
					containerCommandHome = containerBuildHome + "/command";
					containerAuthInfoHome = "/root/auth-info";
					trustCertsHome = containerBuildHome + "/trust-certs";
					dockerSock = "/var/run/docker.sock";
					containerdSock = "/run/containerd/containerd.sock";
				}

				Map<String, String> buildHomeMount = CollectionUtils.newLinkedHashMap(
						"name", "build-home", 
						"mountPath", containerBuildHome);
				Map<String, String> authInfoMount = CollectionUtils.newLinkedHashMap(
						"name", "auth-info-home", 
						"mountPath", containerAuthInfoHome);
				
				// Windows nanoserver default user is ContainerUser
				Map<String, String> authInfoMount2 = CollectionUtils.newLinkedHashMap(
						"name", "auth-info-home", 
						"mountPath", "C:\\Users\\ContainerUser\\auth-info");
				
				Map<String, String> cacheHomeMount = CollectionUtils.newLinkedHashMap(
						"name", "cache-home", 
						"mountPath", containerCacheHome);
				Map<String, String> trustCertsMount = CollectionUtils.newLinkedHashMap(
						"name", "trust-certs-home", 
						"mountPath", trustCertsHome);
				Map<String, String> dockerSockMount = CollectionUtils.newLinkedHashMap(
						"name", "docker-sock", 
						"mountPath", dockerSock);
				Map<String, String> containerdSockMount = CollectionUtils.newLinkedHashMap(
						"name", "containerd-sock", 
						"mountPath", containerdSock);
				
				List<Object> commonVolumeMounts = Lists.<Object>newArrayList(buildHomeMount, authInfoMount, cacheHomeMount);
				if (osInfo.isWindows())
					commonVolumeMounts.add(authInfoMount2);
				if (trustCertsConfigMapName != null)
					commonVolumeMounts.add(trustCertsMount);
				
				if (isMountContainerSock()) {
					commonVolumeMounts.add(dockerSockMount);
					commonVolumeMounts.add(containerdSockMount);
				}

				CompositeFacade entryFacade;
				if (jobContext != null) {
					entryFacade = new CompositeFacade(jobContext.getActions());
				} else {
					List<Action> actions = new ArrayList<>();
					CommandFacade facade = new CommandFacade((String) executionContext, 
							Lists.newArrayList("this does not matter"), false);
					actions.add(new Action("test", facade, ExecuteCondition.ALWAYS));
					entryFacade = new CompositeFacade(actions);
				}
				
				List<String> containerNames = Lists.newArrayList("init");
				
				String helperImageSuffix;
				if (osInfo.isWindows()) {  
					String windowsVersion = OsInfo.WINDOWS_VERSIONS.get(osInfo.getWindowsBuild());
					if (windowsVersion != null)
						helperImageSuffix = "windows-" + windowsVersion.toLowerCase();
					else
						throw new ExplicitException("Unsupported windows build number: " + osInfo.getWindowsBuild());
				} else {
					helperImageSuffix = "linux";
				}
				
				String helperImage = IMAGE_REPO_PREFIX + "-" + helperImageSuffix + ":" + KubernetesHelper.getVersion();
				
				List<Map<Object, Object>> commonEnvs = new ArrayList<>();
				commonEnvs.add(CollectionUtils.newLinkedHashMap(
						"name", ENV_SERVER_URL, 
						"value", getServerUrl()));
				commonEnvs.add(CollectionUtils.newLinkedHashMap(
						"name", ENV_JOB_TOKEN, 
						"value", jobToken));
				commonEnvs.add(CollectionUtils.newLinkedHashMap(
						"name", ENV_OS_INFO,
						"value", Hex.encodeHexString(SerializationUtils.serialize(osInfo))
						));
				commonEnvs.add(CollectionUtils.newLinkedHashMap(
						"name", "ONEDEV_WORKSPACE",
						"value", containerWorkspace
						));

				entryFacade.traverse(new LeafVisitor<Void>() {

					@Override
					public Void visit(LeafFacade facade, List<Integer> position) {
						String containerName = getContainerName(position);
						containerNames.add(containerName);
						Map<Object, Object> stepContainerSpec;
						if (facade instanceof CommandFacade) {
							CommandFacade commandFacade = (CommandFacade) facade;
							OsExecution execution = commandFacade.getExecution(osInfo);
							if (execution.getImage() == null) {
								throw new ExplicitException("This step can only be executed by server shell "
										+ "executor or remote shell executor");
							}
							
							stepContainerSpec = CollectionUtils.newHashMap(
									"name", containerName, 
									"image", execution.getImage());
							if (commandFacade.isUseTTY())
								stepContainerSpec.put("tty", true);
							stepContainerSpec.put("volumeMounts", commonVolumeMounts);
							stepContainerSpec.put("env", commonEnvs);
						} else if (facade instanceof BuildImageFacade) {
							stepContainerSpec = CollectionUtils.newHashMap(
									"name", containerName, 
									"image", helperImage);
							stepContainerSpec.put("volumeMounts", commonVolumeMounts);
							stepContainerSpec.put("env", commonEnvs);
						} else if (facade instanceof RunContainerFacade) {
							RunContainerFacade runContainerFacade = (RunContainerFacade) facade;
							OsContainer container = runContainerFacade.getContainer(osInfo); 
							stepContainerSpec = CollectionUtils.newHashMap(
									"name", containerName, 
									"image", container.getImage());
							if (runContainerFacade.isUseTTY())
								stepContainerSpec.put("tty", true);
							
							List<Object> volumeMounts = new ArrayList<>(commonVolumeMounts);
							for (Map.Entry<String, String> entry: container.getVolumeMounts().entrySet()) {
								if (entry.getKey().contains(".."))
									throw new ExplicitException("Volume mount source path should not container '..'");
								String subPath = StringUtils.stripStart(entry.getKey(), "/\\");
								if (osInfo.isWindows())
									subPath = "workspace\\" + subPath;
								else
									subPath = "workspace/" + subPath;
								volumeMounts.add(CollectionUtils.newLinkedHashMap(
										"name", "build-home", 
										"mountPath", entry.getValue(),
										"subPath", subPath));
							}
							stepContainerSpec.put("volumeMounts", volumeMounts);
							
							List<Map<Object, Object>> envs = new ArrayList<>(commonEnvs);
							for (Map.Entry<String, String> entry: container.getEnvMap().entrySet()) {
								envs.add(CollectionUtils.newLinkedHashMap(
										"name", entry.getKey(), 
										"value", entry.getValue()));
							}
							stepContainerSpec.put("env", envs);
						} else { 
							stepContainerSpec = CollectionUtils.newHashMap(
									"name", containerName, 
									"image", helperImage);
							stepContainerSpec.put("volumeMounts", commonVolumeMounts);
							stepContainerSpec.put("env", commonEnvs);
						}
						
						String positionStr = stringifyPosition(position);
						if (osInfo.isLinux()) {
							stepContainerSpec.put("command", Lists.newArrayList("sh"));
							stepContainerSpec.put("args", Lists.newArrayList(containerCommandHome + "/" + positionStr + ".sh"));
						} else {
							stepContainerSpec.put("command", Lists.newArrayList("cmd"));
							stepContainerSpec.put("args", Lists.newArrayList("/c", containerCommandHome + "\\" + positionStr + ".bat"));
						}

						containerSpecs.add(stepContainerSpec);
						
						return null;
					}
					
				}, new ArrayList<>());
				
				String k8sHelperClassPath;
				if (osInfo.isLinux()) {
					k8sHelperClassPath = "/k8s-helper/*";
				} else {
					k8sHelperClassPath = "C:\\k8s-helper\\*";
				}
				
				List<String> sidecarArgs = Lists.newArrayList(
						"-classpath", k8sHelperClassPath,
						"io.onedev.k8shelper.SideCar");
				List<String> initArgs = Lists.newArrayList(
						"-classpath", k8sHelperClassPath, 
						"io.onedev.k8shelper.Init");
				if (jobContext == null) {
					sidecarArgs.add("test");
					initArgs.add("test");
				}
				
				List<Map<Object, Object>> initEnvs = new ArrayList<>(commonEnvs);
				List<RegistryLoginFacade> registryLogins = new ArrayList<>();
				for (RegistryLogin login: getRegistryLogins())
					registryLogins.add(new RegistryLoginFacade(login.getRegistryUrl(), login.getUserName(), login.getPassword()));
				initEnvs.add(CollectionUtils.newLinkedHashMap(
						"name", KubernetesHelper.ENV_REGISTRY_LOGINS,
						"value", Hex.encodeHexString(SerializationUtils.serialize((Serializable) registryLogins))
						));
				Map<Object, Object> initContainerSpec = CollectionUtils.newHashMap(
						"name", "init", 
						"image", helperImage, 
						"command", Lists.newArrayList("java"), 
						"args", initArgs,
						"env", initEnvs,
						"volumeMounts", commonVolumeMounts);
				
				Map<Object, Object> sidecarContainerSpec = CollectionUtils.newHashMap(
						"name", "sidecar", 
						"image", helperImage, 
						"command", Lists.newArrayList("java"), 
						"args", sidecarArgs, 
						"env", commonEnvs, 
						"volumeMounts", commonVolumeMounts);
				
				if (jobContext != null) {
					sidecarContainerSpec.put("resources", CollectionUtils.newLinkedHashMap("requests", CollectionUtils.newLinkedHashMap(
							"cpu", jobContext.getCpuRequirement() + "m", 
							"memory", jobContext.getMemoryRequirement() + "Mi")));
				}
				
				containerSpecs.add(sidecarContainerSpec);
				containerNames.add("sidecar");
				
				podSpec.put("containers", containerSpecs);
				podSpec.put("initContainers", Lists.<Object>newArrayList(initContainerSpec));

				if (imagePullSecretName != null)
					podSpec.put("imagePullSecrets", Lists.<Object>newArrayList(CollectionUtils.newLinkedHashMap("name", imagePullSecretName)));
				podSpec.put("restartPolicy", "Never");		
				
				if (!getNodeSelector().isEmpty())
					podSpec.put("nodeSelector", toMap(getNodeSelector()));
				
				Map<Object, Object> buildHomeVolume = CollectionUtils.newLinkedHashMap(
						"name", "build-home", 
						"emptyDir", CollectionUtils.newLinkedHashMap());
				Map<Object, Object> userHomeVolume = CollectionUtils.newLinkedHashMap(
						"name", "auth-info-home", 
						"emptyDir", CollectionUtils.newLinkedHashMap());
				Map<Object, Object> cacheHomeVolume = CollectionUtils.newLinkedHashMap(
						"name", "cache-home", 
						"hostPath", CollectionUtils.newLinkedHashMap(
								"path", osInfo.getCacheHome(), 
								"type", "DirectoryOrCreate"));
				List<Object> volumes = Lists.<Object>newArrayList(buildHomeVolume, userHomeVolume, cacheHomeVolume);
				if (trustCertsConfigMapName != null) {
					volumes.add(CollectionUtils.newLinkedHashMap(
							"name", "trust-certs-home", 
							"configMap", CollectionUtils.newLinkedHashMap(
									"name", trustCertsConfigMapName)));
				}
				
				if (isMountContainerSock()) {
					volumes.add(CollectionUtils.newLinkedHashMap(
							"name", "docker-sock", 
							"hostPath", CollectionUtils.newLinkedHashMap(
									"path", dockerSock)));
					volumes.add(CollectionUtils.newLinkedHashMap(
							"name", "containerd-sock", 
							"hostPath", CollectionUtils.newLinkedHashMap(
									"path", containerdSock)));
				}
				podSpec.put("volumes", volumes);

				String podName = "job";
				
				Map<Object, Object> podDef = CollectionUtils.newLinkedHashMap(
						"apiVersion", "v1", 
						"kind", "Pod", 
						"metadata", CollectionUtils.newLinkedHashMap(
								"name", podName, 
								"namespace", namespace), 
						"spec", podSpec);
				
				createResource(podDef, Sets.newHashSet(), jobLogger);
				String podFQN = namespace + "/" + podName;
				
				AtomicReference<String> nodeNameRef = new AtomicReference<>(null);
				
				watchPod(namespace, podName, new AbortChecker() {

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
					jobContext.notifyJobRunning(null);

				String nodeName = Preconditions.checkNotNull(nodeNameRef.get());
				jobLogger.log("Running job on node " + nodeName + "...");
				
				jobLogger.log("Starting job containers...");
				
				AtomicBoolean failed = new AtomicBoolean(false);
				
				for (String containerName: containerNames) {
					logger.debug("Waiting for start of container (pod: {}, container: {})...", 
							podFQN, containerName);
					
					watchPod(namespace, podName, new AbortChecker() {

						@Override
						public Abort check(String nodeName, Collection<JsonNode> containerStatusNodes) {
							ContainerError error = getContainerErrors(containerStatusNodes).get(containerName);
							if (error != null) {
								/*
								 * For non-fatal errors (command exited with non-zero code), we abort the watch 
								 * without an exception, and will continue to collect the container log which 
								 * might contain error details
								 */
								if (error.isFatal()) {
									String errorMessage;
									if (containerName.startsWith("step-")) {
										List<Integer> position = KubernetesHelper.parsePosition(containerName.substring("step-".length()));
										errorMessage = "Step \"" + entryFacade.getNamesAsString(position) 
												+ ": " + error.getMessage();
									} else {
										errorMessage = containerName + ": " + error.getMessage();
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
					
					logger.debug("Collecting log of container (pod: {}, container: {})...", 
							podFQN, containerName);
					
					collectContainerLog(namespace, podName, containerName, LOG_END_MESSAGE, jobLogger);
					
					logger.debug("Waiting for stop of container (pod: {})...", 
							podFQN, containerName);
					
					watchPod(namespace, podName, new AbortChecker() {

						@Override
						public Abort check(String nodeName, Collection<JsonNode> containerStatusNodes) {
							ContainerError error = getContainerErrors(containerStatusNodes).get(containerName);
							if (error != null) {
								String errorMessage;
								if (containerName.startsWith("step-")) {
									List<Integer> position = KubernetesHelper.parsePosition(containerName.substring("step-".length()));
									errorMessage = "Step \"" + entryFacade.getNamesAsString(position) 
											+ " is failed: " + error.getMessage();
								} else {
									errorMessage = containerName + ": " + error.getMessage();
								}
								
								/*
								 * We abort the watch with an exception for two reasons:
								 * 
								 * 1. Init container error will prevent other containers to start. 
								 * 2. Step containers may not run command in case of fatal error and sidecar 
								 *    container will wait indefinitely on the successful/failed mark file in 
								 *    this case, causing log following last indefinitely 
								 */
								if (error.isFatal() || containerName.equals("init")) {
									return new Abort(errorMessage);
								} else { 
									jobLogger.error(errorMessage);
									failed.set(true);
									return new Abort(null);
								} 
							} else if (getStoppedContainers(containerStatusNodes).contains(containerName)) {
								return new Abort(null);
							} else {
								return null;
							}
						}
						
					}, jobLogger);
				}
				
				if (failed.get())
					throw new FailedException();
			} finally {
				deleteNamespace(namespace, jobLogger);
			}			
		} finally {
			if (getClusterRole() != null)
				deleteClusterRoleBinding(namespace, jobLogger);
		}
	}
	
	private String getContainerName(List<Integer> stepPosition) {
		return "step-" + stringifyPosition(stepPosition);
	}
	
	private Map<String, ContainerError> getContainerErrors(Collection<JsonNode> containerStatusNodes) {
		Map<String, ContainerError> containerErrors = new HashMap<>();
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
						containerErrors.put(containerName, new ContainerError(messageNode.asText(), true));
					else
						containerErrors.put(containerName, new ContainerError(reason, true));
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
							containerErrors.put(containerName, new ContainerError(messageNode.asText(), true));
						} else {
							JsonNode exitCodeNode = terminatedNode.get("exitCode");
							if (exitCodeNode != null && exitCodeNode.asInt() != 0)
								containerErrors.put(containerName, new ContainerError("Command failed with exit code " + exitCodeNode.asText(), false));
							else
								containerErrors.put(containerName, new ContainerError(reason, true));
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
	
	private void watchPod(String namespace, String podName, AbortChecker abortChecker, TaskLogger jobLogger) {
		Commandline kubectl = newKubeCtl();
		
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
		
		AtomicReference<Abort> abortRef = new AtomicReference<>(null);
		
		StringBuilder json = new StringBuilder();
		kubectl.addArgs("get", "pod", podName, "-n", namespace, "--watch", "-o", "json");
		
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
						jobLogger.error("Kubernetes: " + line);
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
	
	private static class ContainerError {
		
		private final String message;
		
		private final boolean fatal;
		
		public ContainerError(String message, boolean fatal) {
			this.message = message;
			this.fatal = fatal;
		}

		public String getMessage() {
			return message;
		}

		public boolean isFatal() {
			return fatal;
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