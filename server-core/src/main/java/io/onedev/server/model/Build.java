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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.VariableInterpolator;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.support.JobSecret;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.Input;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.util.BuildAware;
import io.onedev.server.web.util.WicketUtils;

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
	
	private static ThreadLocal<Stack<Build>> stack =  new ThreadLocal<Stack<Build>>() {

		@Override
		protected Stack<Build> initialValue() {
			return new Stack<Build>();
		}
	
	};
	
	public static final String STATUS = "status";
	
	public static final String ARTIFACTS_DIR = "artifacts";
	
	private static final int MAX_STATUS_MESSAGE_LEN = 1024;
	
	public enum Status {
		// Most significant status comes first, refer to getOverallStatus
		WAITING, PENDING, RUNNING, FAILED, CANCELLED, TIMED_OUT, SUCCESSFUL;
		
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
	
	private Date retryDate;
	
	@Column(length=MAX_STATUS_MESSAGE_LEN)
	private String errorMessage;

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
	
	private transient Collection<Long> fixedIssueNumbers;
	
	private transient Map<Build.Status, Collection<RevCommit>> commitsCache;
	
	private transient BuildSpec spec;
	
	private transient Job job;
	
	private transient Map<String, Input> paramInputs;
	
	private transient Map<Build.Status, Build> streamPreviousCache = new HashMap<>();
	
	private transient Map<Integer, Collection<Long>> numbersOfStreamPreviousCache = new HashMap<>();
	
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
	
	public void setStatus(Status status, @Nullable String errorMessage) {
		this.status = status;
		setErrorMessage(errorMessage);
	}
	
	public Date getStatusDate() {
		switch (status) {
		case FAILED:
		case CANCELLED:
		case SUCCESSFUL:
		case TIMED_OUT:
			return getFinishDate();
		case WAITING:
			return getRetryDate()!=null?getRetryDate():getSubmitDate();
		case RUNNING:
			return getRunningDate();
		case PENDING:
			return getPendingDate();
		default:
			throw new RuntimeException("Unexpected build status: " + status);
		}
	}
	
	public boolean isFinished() {
		return status == Status.FAILED 
				|| status == Status.CANCELLED 
				|| status == Status.SUCCESSFUL
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

	public Date getRetryDate() {
		return retryDate;
	}

	public void setRetryDate(Date retryDate) {
		this.retryDate = retryDate;
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		if (errorMessage != null) {
			errorMessage = StringUtils.abbreviate(errorMessage, MAX_STATUS_MESSAGE_LEN);
			for (String secretValue: getSecretValuesToMask())
				errorMessage = StringUtils.replace(errorMessage, secretValue, SecretInput.MASK);
		}
		this.errorMessage = errorMessage;
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
	 */
	public Collection<Long> getFixedIssueNumbers() {
		if (fixedIssueNumbers == null) {
			fixedIssueNumbers = new HashSet<>();
			Build prevBuild = getStreamPrevious(null);
			if (prevBuild != null) {
				Repository repository = project.getRepository();
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getCommitHash())));
					revWalk.markUninteresting(revWalk.parseCommit(prevBuild.getCommitId()));

					RevCommit commit;
					while ((commit = revWalk.next()) != null) 
						fixedIssueNumbers.addAll(IssueUtils.parseFixedIssueNumbers(commit.getFullMessage()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} 
		}
		return fixedIssueNumbers;
	}
	
	public Collection<RevCommit> getCommits(@Nullable Build.Status sincePrevStatus) {
		if (commitsCache == null) 
			commitsCache = new HashMap<>();
		if (!commitsCache.containsKey(sincePrevStatus)) {
			Collection<RevCommit> commits = new ArrayList<>();
			Build prevBuild = getStreamPrevious(sincePrevStatus);
			if (prevBuild != null) {
				Repository repository = project.getRepository();
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getCommitHash())));
					revWalk.markUninteresting(revWalk.parseCommit(prevBuild.getCommitId()));

					RevCommit commit;
					while ((commit = revWalk.next()) != null) 
						commits.add(commit);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} 
			commitsCache.put(sincePrevStatus, commits);
		}
		return commitsCache.get(sincePrevStatus);
	}
	
	public Map<String, Input> getParamInputs() {
		if (paramInputs == null) {
			paramInputs = new LinkedHashMap<>();
			Map<String, List<BuildParam>> paramMap = new HashMap<>(); 
			for (BuildParam param: getParams()) {
				List<BuildParam> paramsOfName = paramMap.get(param.getName());
				if (paramsOfName == null) {
					paramsOfName = new ArrayList<>();
					paramMap.put(param.getName(), paramsOfName);
				}
				paramsOfName.add(param);
			}
			for (ParamSpec paramSpec: getJob().getParamSpecs()) {
				String paramName = paramSpec.getName();
				List<BuildParam> params = paramMap.get(paramName);
				if (params != null) {
					String type = params.iterator().next().getType();
					List<String> values = new ArrayList<>();
					for (BuildParam param: params) {
						if (param.getValue() != null)
							values.add(param.getValue());
					}
					Collections.sort(values, new Comparator<String>() {

						@Override
						public int compare(String o1, String o2) {
							return (int) (paramSpec.getOrdinal(o1) - paramSpec.getOrdinal(o2));
						}
						
					});
					if (!paramSpec.isAllowMultiple() && values.size() > 1) 
						values = Lists.newArrayList(values.iterator().next());
					paramInputs.put(paramName, new Input(paramName, type, values));
				}
			}		
		}
		return paramInputs;
	}
	
	public BuildFacade getFacade() {
		return new BuildFacade(getId(), getProject().getId(), getCommitHash());
	}
	
	public Collection<String> getSecretValuesToMask() {
		Collection<String> secretValuesToMask = new HashSet<>();
		for (JobSecret secret: getProject().getBuildSetting().getHierarchySecrets(getProject())) {
			if (secret.isAuthorized(getProject(), getCommitId()))
				secretValuesToMask.add(secret.getValue());
		}
		
		for (BuildParam param: getParams()) {
			if (param.getType().equals(ParamSpec.SECRET) && param.getValue() != null) {		
				if (param.getValue().startsWith(SecretInput.LITERAL_VALUE_PREFIX)) 
					secretValuesToMask.add(param.getValue().substring(SecretInput.LITERAL_VALUE_PREFIX.length()));
			}
		}
		
		for (Iterator<String> it = secretValuesToMask.iterator(); it.hasNext();) {
			if (it.next().length() < SecretInput.MASK.length())
				it.remove();
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
			if (paramSpec.getShowCondition().getValueMatcher().matches(dependentInput.getValues())) 
				return isParamVisible(dependentInput.getName(), checkedParamNames);
			else 
				return false;
		} else {
			return true;
		}
	}
	
	public String getSecretValue(String secretName) {
		if (secretName.startsWith(SecretInput.LITERAL_VALUE_PREFIX))
			return secretName.substring(SecretInput.LITERAL_VALUE_PREFIX.length());
		else
			return project.getBuildSetting().getSecretValue(project, secretName, ObjectId.fromString(getCommitHash()));
	}
	
	public BuildSpec getSpec() {
		if (spec == null) 
			spec = Preconditions.checkNotNull(getProject().getBuildSpec(getCommitId()));
		return spec;
	}
	
	public Job getJob() {
		if (job == null) 
			job = Preconditions.checkNotNull(getSpec().getJobMap().get(getJobName()));
		return job;
	}

	public Serializable getParamBean() {
		Serializable paramBean;
		try {
			paramBean = (Serializable) ParamSupply.defineBeanClass(getJob().getParamSpecs()).newInstance();
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
				PatternSet patternSet = PatternSet.parse(artifacts);
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
	
	@Nullable
	public Build getStreamPrevious(@Nullable Status status) {
		if (streamPreviousCache == null) 
			streamPreviousCache = new HashMap<>();
		if (!streamPreviousCache.containsKey(status)) 
			streamPreviousCache.put(status, OneDev.getInstance(BuildManager.class).findStreamPrevious(this, status));
		return streamPreviousCache.get(status);
	}
	
	public Collection<Long> getNumbersOfStreamPrevious(int limit) {
		if (numbersOfStreamPreviousCache == null) 
			numbersOfStreamPreviousCache = new HashMap<>();
		if (!numbersOfStreamPreviousCache.containsKey(limit)) {
			BuildManager buildManager = OneDev.getInstance(BuildManager.class);
			numbersOfStreamPreviousCache.put(limit, buildManager.queryNumbersOfStreamPrevious(
					this, null, EntityCriteria.IN_CLAUSE_LIMIT));
		}
		return numbersOfStreamPreviousCache.get(limit);
	}
	
	public void retrieveArtifacts(Build dependency, String artifacts, File workspaceDir) {
		LockUtils.read(dependency.getArtifactsLockKey(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File artifactsDir = dependency.getArtifactsDir();
				if (artifactsDir.exists()) {
					PatternSet patternSet = PatternSet.parse(artifacts);
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
			return StringUtils.unescape(Interpolative.fromString(interpolativeString).interpolateWith(new VariableInterpolator(this)));
		else 
			return null;
	}	
	
	@SuppressWarnings("unchecked")
	public void interpolate(Serializable bean) {
		Build.push(this);
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
		} finally {
			Build.pop();
		}
	}
	
	public ProjectScopedNumber getFQN() {
		return new ProjectScopedNumber(getProject(), getNumber());
	}
	
	public Collection<String> getOnBranches() {
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		Collection<ObjectId> descendants = commitInfoManager.getDescendants(
				getProject(), Sets.newHashSet(getCommitId()));
		descendants.add(getCommitId());
	
		Collection<String> branches = new ArrayList<>();
		for (RefInfo ref: getProject().getBranchRefInfos()) {
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
	
	public static void push(Build build) {
		stack.get().push(build);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	public boolean isValid() {
		return getProject().getRepository().hasObject(ObjectId.fromString(getCommitHash()));
	}
	
	@Nullable
	public static Build get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				BuildAware buildAware = WicketUtils.findInnermost(componentContext.getComponent(), BuildAware.class);
				if (buildAware != null) 
					return buildAware.getBuild();
			}
			return null;
		}
	}
	
}
