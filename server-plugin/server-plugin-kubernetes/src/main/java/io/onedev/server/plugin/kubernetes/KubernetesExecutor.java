package io.onedev.server.plugin.kubernetes;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.codec.Charsets;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecuteResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.model.support.JobExecutor;
import io.onedev.server.model.support.SourceSnapshot;
import io.onedev.server.plugin.kubernetes.KubernetesExecutor.TestData;
import io.onedev.server.util.Maps;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=300)
public class KubernetesExecutor extends JobExecutor implements Testable<TestData> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
	
	private String configFile;
	
	private String kubeCtlPath;
	
	private String namespace = "onedev-ci";
	
	private List<NodeSelectorEntry> nodeSelector = new ArrayList<>();
	
	private String imagePullSecrets;
	
	private String serviceAccount;
	
	@Editable(name="Kubectl Config File", order=100, description=
			"Specify absolute path to the config file used by kubectl to access the "
			+ "cluster. Leave empty to have kubectl determining cluster access "
			+ "information automatically")
	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	@Editable(name="Path to kubectl", order=200, description=
			"Specify absolute path to the kubectl utility, for instance: <i>/usr/bin/kubectl</i>. "
			+ "If left empty, OneDev will try to find the utility from system path")
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

	@Override
	public void execute(String environment, File workspace, Map<String, String> envVars, 
			List<String> commands, SourceSnapshot snapshot, Collection<JobCache> caches, 
			PatternSet collectFiles, Logger logger) {
	}

	@Override
	public void checkCaches() {
	}

	@Override
	public void cleanDir(File dir) {
		FileUtils.cleanDir(dir);
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
	
	private String createResource(Map<Object, Object> resourceData, Logger logger) {
		Commandline kubectl = newKubeCtl();
		File file = null;
		try {
			AtomicReference<String> resourceNameRef = new AtomicReference<String>(null);
			file = File.createTempFile("k8s", ".yaml");
			FileUtils.writeFile(file, new Yaml().dump(resourceData), Charsets.UTF_8.name());
			kubectl.addArgs("create", "-f", file.getAbsolutePath());
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.info(line);
					line = StringUtils.substringAfter(line, "/");
					resourceNameRef.set(StringUtils.substringBefore(line, " "));
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
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
	
	private void deleteResource(String resourceType, String resourceName, Logger logger) {
		Commandline cmd = newKubeCtl();
		cmd.addArgs("delete", resourceType, resourceName, "--namespace=" + getNamespace());
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.info(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
	}
	
	private void createNamespaceIfNotExist(Logger logger) {
		Commandline cmd = newKubeCtl();
		cmd.addArgs("get", "namespaces");
		
		AtomicBoolean hasNamespace = new AtomicBoolean(false);
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
				if (line.startsWith(getNamespace() + " "))
					hasNamespace.set(true);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		if (!hasNamespace.get()) {
			cmd = newKubeCtl();
			cmd.addArgs("create", "namespace", getNamespace());
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.debug(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();
		}
	}
	
	private String getResourceNamePrefix() {
		try {
			return "onedev-ci-" + InetAddress.getLocalHost().getHostName() + "-" + getName() + "-";
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
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
	
	private String getOS(Logger logger) {
		logger.info("Checking OS...");
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("get", "nodes", "-o=jsonpath={..nodeInfo.operatingSystem}");
		for (NodeSelectorEntry entry: getNodeSelector()) 
			kubectl.addArgs("-l", entry.getLabelName() + "=" + entry.getLabelValue());
		
		AtomicReference<String> osRef = new AtomicReference<>(null);
		kubectl.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				osRef.set(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return Preconditions.checkNotNull(osRef.get(), "No applicable working nodes for this executor");
	}
	
	@Override
	public void test(TestData testData) {
		createNamespaceIfNotExist(logger);

		String os = getOS(logger);
		
		Map<String, Object> podSpec = new LinkedHashMap<>();
		Map<Object, Object> containerSpec = Maps.newHashMap(
				"name", "test", 
				"image", testData.getDockerImage());

		if (os.equalsIgnoreCase("linux")) {
			containerSpec.put("command", Lists.newArrayList("sh"));
			containerSpec.put("args", Lists.newArrayList("-c", "echo hello from container"));
		} else {
			containerSpec.put("command", Lists.newArrayList("cmd"));
			containerSpec.put("args", Lists.newArrayList("/c", "echo hello from container"));
		}
		
		podSpec.put("containers", Lists.<Object>newArrayList(containerSpec));
		
		Map<String, String> nodeSelectorData = getNodeSelectorData();
		if (!nodeSelectorData.isEmpty())
			podSpec.put("nodeSelector", nodeSelectorData);
		List<Object> imagePullSecretsData = getImagePullSecretsData();
		if (!imagePullSecretsData.isEmpty())
			podSpec.put("imagePullSecrets", imagePullSecretsData);
		if (getServiceAccount() != null)
			podSpec.put("serviceAccountName", getServiceAccount());
		podSpec.put("restartPolicy", "Never");		
		Map<Object, Object> podData = Maps.newLinkedHashMap(
				"apiVersion", "v1", 
				"kind", "Pod", 
				"metadata", Maps.newLinkedHashMap(
						"generateName", getResourceNamePrefix() + "test-", 
						"namespace", getNamespace()), 
				"spec", podSpec);
		
		String podName = createResource(podData, logger);
		try {
			waitForPod(podName, logger);
		} finally {
			deleteResource("pod", podName, logger);
		}
	}
	
	private void waitForPod(String podName, Logger logger) {
		Thread thread = Thread.currentThread();

		AtomicBoolean podStartedRef = new AtomicBoolean(false);
		AtomicReference<String> podErrorRef = new AtomicReference<String>(null);
		
		Commandline kubectl = newKubeCtl();
		kubectl.addArgs("get", "event", "-n", getNamespace(), "--no-headers",
				"--field-selector", "involvedObject.name=" + podName, "--watch");
		try {
			kubectl.execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					tokenizer.nextToken();
					String type = tokenizer.nextToken();
					tokenizer.nextToken();
					tokenizer.nextToken();
					String message = tokenizer.nextToken("\n").trim();
					if (type.equals("Normal"))
						logger.info(message);
					else
						logger.error(message);
					
					if (!type.equals("Normal") && !message.contains("Insufficient cpu")) {
						podErrorRef.set(message);
						thread.interrupt();
					} else if (message.startsWith("Started container")) {
						podStartedRef.set(true);
						thread.interrupt();
					}
				}
				
			}, new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			});
			
			throw new OneException("Unexpected end of pod event watching");
		} catch (Exception e) {
			if (e.getCause() instanceof InterruptedException) {
				if (podStartedRef.get()) {
					kubectl = newKubeCtl();
					kubectl.addArgs("logs", podName, "-n", getNamespace(), "--follow");
					while (true) {
						AtomicReference<Boolean> containerCreatingRef = new AtomicReference<Boolean>(false);
						ExecuteResult result = kubectl.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.info(line);
							}
							
						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								if (line.contains("is waiting to start: ContainerCreating"))
									containerCreatingRef.set(true);
								else
									logger.error(line);
							}
							
						});
						if (containerCreatingRef.get()) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e2) {
							}
						} else {
							result.checkReturnCode();
							break;
						}
					}
				} else if (podErrorRef.get() != null) {
					throw new OneException(podErrorRef.get());
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
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