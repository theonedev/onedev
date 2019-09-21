package io.onedev.server.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.BeanUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.cache.BuildInfoManager;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.VariableInterpolator;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.ci.job.paramspec.SecretParam;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.Input;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Editable;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_submitter_id"), @Index(columnList="o_canceller_id"),
				@Index(columnList="submitterName"), @Index(columnList="cancellerName"), @Index(columnList="commitHash"), 
				@Index(columnList="number"), @Index(columnList="jobName"), @Index(columnList="status"), 
				@Index(columnList="submitDate"), @Index(columnList="pendingDate"), @Index(columnList="runningDate"), 
				@Index(columnList="finishDate"), @Index(columnList="version"), 
				@Index(columnList="o_project_id, commitHash")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "number"})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Build extends AbstractEntity implements Referenceable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Build.class);
	
	public static final String STATUS = "status";
	
	public static final String ARTIFACTS_DIR = "artifacts";
	
	private static final int MAX_STATUS_MESSAGE_LEN = 1024;
	
	public enum Status {
		// Most significant status comes first, refer to getOverallStatus
		WAITING, PENDING, RUNNING, IN_ERROR, FAILED, CANCELLED, TIMED_OUT, SUCCESSFUL;
		
		public String getDisplayName() {
			return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
		}
		
		@Nullable
		public static Status getOverallStatus(Collection<Status> statuses) {
			for (Status status: Status.values()) {
				if (statuses.contains(status))
					return status;
			}
			return null;
		}
		
	};
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User submitter;
	
	private String submitterName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User canceller;
	
	private String cancellerName;
	
	@Column(nullable=false)
	private String jobName;
	
	private String version;
	
	private long number;
	
	@Column(nullable=false)
	private String commitHash;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date submitDate;
	
	private Date pendingDate;
	
	private Date runningDate;

	private Date finishDate;
	
	@Column(length=MAX_STATUS_MESSAGE_LEN)
	private String statusMessage;

	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<BuildParam> params = new ArrayList<>();
	
	@OneToMany(mappedBy="dependent", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<BuildDependence> dependencies = new ArrayList<>();
	
	@OneToMany(mappedBy="dependency", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<BuildDependence> dependents= new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<PullRequestBuild> pullRequestBuilds = new ArrayList<>();
	
	private transient Map<String, List<String>> paramMap;
	
	private transient Optional<Collection<Long>> fixedIssueNumbers;
	
	private transient CISpec ciSpec;
	
	private transient Job job;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Nullable
	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	@Nullable
	public String getSubmitterName() {
		return submitterName;
	}

	public void setSubmitterName(String submitterName) {
		this.submitterName = submitterName;
	}

	@Nullable
	public String getCancellerName() {
		return cancellerName;
	}

	public void setCancellerName(String cancellerName) {
		this.cancellerName = cancellerName;
	}

	@Nullable
	public User getCanceller() {
		return canceller;
	}

	public void setCanceller(User canceller) {
		this.canceller = canceller;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}
	
	public ObjectId getCommitId() {
		return ObjectId.fromString(getCommitHash());
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		setStatus(status, null);
	}
	
	public void setStatus(Status status, @Nullable String statusMessage) {
		this.status = status;
		setStatusMessage(statusMessage);
	}
	
	public boolean isFinished() {
		return status == Status.IN_ERROR || status == Status.FAILED 
				|| status == Status.CANCELLED || status == Status.SUCCESSFUL
				|| status == Status.TIMED_OUT;
	}
	
	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public Date getPendingDate() {
		return pendingDate;
	}

	public void setPendingDate(Date pendingDate) {
		this.pendingDate = pendingDate;
	}

	public Date getRunningDate() {
		return runningDate;
	}

	public void setRunningDate(Date runningDate) {
		this.runningDate = runningDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public Collection<BuildParam> getParams() {
		return params;
	}

	public void setParams(Collection<BuildParam> params) {
		this.params = params;
	}
	
	public Collection<BuildDependence> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Collection<BuildDependence> dependencies) {
		this.dependencies = dependencies;
	}

	public Collection<BuildDependence> getDependents() {
		return dependents;
	}

	public void setDependents(Collection<BuildDependence> dependents) {
		this.dependents = dependents;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		if (statusMessage != null) {
			statusMessage = StringUtils.abbreviate(statusMessage, MAX_STATUS_MESSAGE_LEN);
			for (String secretValue: getSecretValuesToMask())
				statusMessage = StringUtils.replace(statusMessage, secretValue, SecretInput.MASK);
		}
		this.statusMessage = statusMessage;
	}

	public Collection<PullRequestBuild> getPullRequestBuilds() {
		return pullRequestBuilds;
	}

	public void setPullRequestBuilds(Collection<PullRequestBuild> pullRequestBuilds) {
		this.pullRequestBuilds = pullRequestBuilds;
	}
	
	public Map<String, List<String>> getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap<>();
			for (BuildParam param: getParams()) {
				List<String> values = paramMap.get(param.getName());
				if (values == null) {
					values = new ArrayList<>();
					paramMap.put(param.getName(), values);
				}
				values.add(param.getValue());
			}
			for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) { 
				List<String> values = entry.getValue();
				Collections.sort(values);
				entry.setValue(values);
			}
		}
		return paramMap;
	}

	/**
	 * Get fixed issue numbers
	 * 
	 * @return
	 * 			<tt>null</tt> if fixed issue numbers information is not available yet 
	 */
	@Nullable
	public Collection<Long> getFixedIssueNumbers() {
		if (fixedIssueNumbers == null) {
			Collection<ObjectId> prevCommits = OneDev.getInstance(BuildInfoManager.class).getPrevCommits(project, getId());
			if (prevCommits != null) {
				fixedIssueNumbers = Optional.of(new HashSet<>());
				Repository repository = project.getRepository();
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getCommitHash())));
					for (ObjectId prevCommit: prevCommits)
						revWalk.markUninteresting(revWalk.parseCommit(prevCommit));

					RevCommit commit;
					while ((commit = revWalk.next()) != null) 
						fixedIssueNumbers.get().addAll(IssueUtils.parseFixedIssues(project, commit.getFullMessage()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				fixedIssueNumbers = Optional.absent();
			}
		}
		return fixedIssueNumbers.orNull();
	}
	
	public Map<String, Input> getParamInputs() {
		Map<String, Input> inputs = new LinkedHashMap<>();
		List<BuildParam> params = new ArrayList<>(getParams());
		Map<String, Integer> paramOrders = new HashMap<>();
		
		int index = 1;
		for (ParamSpec paramSpec: getJob().getParamSpecs())
			paramOrders.put(paramSpec.getName(), index++);
			
		Collections.sort(params, new Comparator<BuildParam>() {

			@Override
			public int compare(BuildParam o1, BuildParam o2) {
				Integer order1 = paramOrders.get(o1.getName());
				Integer order2 = paramOrders.get(o2.getName());
				if (order1 == null)
					order1 = Integer.MAX_VALUE;
				if (order2 == null)
					order2 = Integer.MAX_VALUE;
				return order1 - order2;
			}
			
		});
		for (BuildParam param: params) {
			Input input = inputs.get(param.getName());
			if (input == null) {
				input = new Input(param.getName(), param.getType(), new ArrayList<>());
				inputs.put(param.getName(), input);
			}
			if (param.getValue() != null)
				input.getValues().add(param.getValue());
		}
		return inputs;
	}
	
	public BuildFacade getFacade() {
		return new BuildFacade(getId(), getProject().getId(), getCommitHash());
	}
	
	public Collection<String> getSecretValuesToMask() {
		Collection<String> secretValuesToMask = new HashSet<>();
		for (BuildParam param: getParams()) {
			if (param.getType().equals(ParamSpec.SECRET) && param.getValue() != null) {		
				try {
					String value = getSecretValue(param.getValue());
					if (value.length() >= SecretInput.MASK.length())
						secretValuesToMask.add(value);
				} catch (OneException e) {
					logger.error("Error retrieving secret value", e);
				}
			}
		}
		return secretValuesToMask;
	}
	
	public boolean isParamVisible(String paramName) {
		return isParamVisible(paramName, Sets.newHashSet());
	}
	
	private boolean isParamVisible(String paramName, Set<String> checkedParamNames) {
		if (!checkedParamNames.add(paramName))
			return false;
		
		ParamSpec paramSpec = Preconditions.checkNotNull(getJob().getParamSpecMap().get(paramName));
		if (paramSpec.getShowCondition() != null) {
			Input dependentInput = getParamInputs().get(paramSpec.getShowCondition().getInputName());
			Preconditions.checkNotNull(dependentInput);
			String value;
			if (!dependentInput.getValues().isEmpty())
				value = dependentInput.getValues().iterator().next();
			else
				value = null;
			if (paramSpec.getShowCondition().getValueMatcher().matches(value)) 
				return isParamVisible(dependentInput.getName(), checkedParamNames);
			else 
				return false;
		} else {
			return true;
		}
	}
	
	public String getSecretValue(String secretName) {
		if (secretName.startsWith(SecretParam.LITERAL_VALUE_PREFIX))
			return secretName.substring(SecretParam.LITERAL_VALUE_PREFIX.length());
		else
			return project.getSecretValue(secretName, ObjectId.fromString(getCommitHash()));
	}
	
	public CISpec getCISpec() {
		if (ciSpec == null) 
			ciSpec = Preconditions.checkNotNull(getProject().getCISpec(getCommitId()));
		return ciSpec;
	}
	
	public Job getJob() {
		if (job == null) 
			job = Preconditions.checkNotNull(getCISpec().getJobMap().get(getJobName()));
		return job;
	}

	public Serializable getParamBean() {
		Serializable paramBean;
		try {
			paramBean = (Serializable) JobParam.defineBeanClass(getJob().getParamSpecs()).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} 
		BeanDescriptor descriptor = new BeanDescriptor(paramBean.getClass());
		for (List<PropertyDescriptor> groupProperties: descriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				ParamSpec paramSpec = getJob().getParamSpecMap().get(property.getDisplayName());
				Preconditions.checkNotNull(paramSpec);
				Input input = Preconditions.checkNotNull(getParamInputs().get(paramSpec.getName()));
				property.setPropertyValue(paramBean, paramSpec.convertToObject(input.getValues()));
			}
		}
		return paramBean;
	}
	
	public File getPublishDir() {
		return OneDev.getInstance(StorageManager.class).getBuildDir(getProject().getId(), getNumber());
	}
	
	public File getArtifactsDir() {
		return new File(getPublishDir(), ARTIFACTS_DIR);
	}
	
	public String getReportLockKey(String reportDir) {
		return "job-report:" + getId() + ":" + reportDir;
	}
	
	public File getReportDir(String reportDir) {
		return new File(getPublishDir(), reportDir);
	}
	
	public void publishArtifacts(File workspaceDir, String artifacts) {
		LockUtils.write(getArtifactsLockKey(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File artifactsDir = getArtifactsDir();
				FileUtils.createDir(artifactsDir);
				PatternSet patternSet = PatternSet.fromString(artifacts);
				int baseLen = workspaceDir.getAbsolutePath().length() + 1;
				for (File file: patternSet.listFiles(workspaceDir)) {
					try {
						FileUtils.copyFile(file, new File(artifactsDir, file.getAbsolutePath().substring(baseLen)));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				return null;
			}
			
		});
	}
	
	public void retrieveArtifacts(Build dependency, String artifacts, File workspaceDir) {
		LockUtils.read(dependency.getArtifactsLockKey(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File artifactsDir = dependency.getArtifactsDir();
				if (artifactsDir.exists()) {
					PatternSet patternSet = PatternSet.fromString(artifacts);
					int baseLen = artifactsDir.getAbsolutePath().length() + 1;
					for (File file: patternSet.listFiles(artifactsDir)) {
						try {
							FileUtils.copyFile(file, new File(workspaceDir, file.getAbsolutePath().substring(baseLen)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				return null;
			}
			
		});
	}
	
	public String getArtifactsLockKey() {
		return "build-artifacts:" + getId();
	}
	
	public String interpolate(@Nullable String interpolativeString) {
		if (interpolativeString != null) 
			return Interpolative.fromString(interpolativeString).interpolateWith(new VariableInterpolator(this));
		else 
			return null;
	}	
	
	@SuppressWarnings("unchecked")
	public void interpolate(Serializable bean) {
		try {
			for (Method getter: BeanUtils.findGetters(bean.getClass())) {
				Method setter = BeanUtils.findSetter(getter);
				if (setter != null && getter.getAnnotation(Editable.class) != null) {
					Serializable value = (Serializable) getter.invoke(bean);
					if (value != null) {
						if (getter.getAnnotation(io.onedev.server.web.editable.annotation.Interpolative.class) != null) {
							if (value instanceof String) {
								setter.invoke(bean, interpolate((String) getter.invoke(bean)));
							} else if (value instanceof List) {
								List<String> list = (List<String>) value;
								for (int i=0; i<list.size(); i++)
									list.set(i, interpolate(list.get(i)));
							}
						} else if (value instanceof Collection) {
							for (Serializable element: (Collection<Serializable>) value) {
								if (element.getClass().getAnnotation(Editable.class) != null)
									interpolate(element);
							}
						} else if (value.getClass().getAnnotation(Editable.class) != null) {
							interpolate(value);
						}
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Collection<String> getOnBranches() {
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		Collection<ObjectId> descendants = commitInfoManager.getDescendants(
				getProject(), Sets.newHashSet(getCommitId()));
		descendants.add(getCommitId());
	
		Collection<String> branches = new ArrayList<>();
		for (RefInfo ref: getProject().getBranches()) {
			String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(ref.getRef().getName()));
			if (descendants.contains(ref.getPeeledObj()))
				branches.add(branchName);
		}
		
		return branches;
	}
	
	public static String getLogWebSocketObservable(Long buildId) {
		return "build-log:" + buildId;
	}
	
	public static String getWebSocketObservable(Long buildId) {
		return Build.class.getName() + ":" + buildId;
	}

}
