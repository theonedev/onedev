package io.onedev.server.model;

import static io.onedev.server.model.Build.PROP_CANCELLER_NAME;
import static io.onedev.server.model.Build.PROP_COMMIT;
import static io.onedev.server.model.Build.PROP_FINISH_DATE;
import static io.onedev.server.model.Build.PROP_FINISH_DAY;
import static io.onedev.server.model.Build.PROP_JOB;
import static io.onedev.server.model.Build.PROP_NUMBER;
import static io.onedev.server.model.Build.PROP_PENDING_DATE;
import static io.onedev.server.model.Build.PROP_REF_NAME;
import static io.onedev.server.model.Build.PROP_RUNNING_DATE;
import static io.onedev.server.model.Build.PROP_STATUS;
import static io.onedev.server.model.Build.PROP_SUBMITTER_NAME;
import static io.onedev.server.model.Build.PROP_SUBMIT_DATE;
import static io.onedev.server.model.Build.PROP_VERSION;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
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
import java.util.concurrent.atomic.AtomicBoolean;

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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.VariableInterpolator;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramspec.SecretParam;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.Day;
import io.onedev.server.util.Input;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.util.BuildAware;
import io.onedev.server.web.util.WicketUtils;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_submitter_id"), @Index(columnList="o_canceller_id"),
				@Index(columnList="o_request_id"), @Index(columnList=PROP_SUBMITTER_NAME), 
				@Index(columnList=PROP_CANCELLER_NAME), @Index(columnList=PROP_COMMIT), 
				@Index(columnList=PROP_NUMBER), @Index(columnList=PROP_JOB), 
				@Index(columnList=PROP_STATUS), @Index(columnList=PROP_REF_NAME),  
				@Index(columnList=PROP_SUBMIT_DATE), @Index(columnList=PROP_PENDING_DATE), 
				@Index(columnList=PROP_RUNNING_DATE), @Index(columnList=PROP_FINISH_DATE), 
				@Index(columnList=PROP_FINISH_DAY), @Index(columnList=PROP_VERSION), 
				@Index(columnList="o_numberScope_id"), @Index(columnList="o_project_id, " + PROP_COMMIT)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", PROP_NUMBER})}
)
public class Build extends AbstractEntity implements Referenceable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ID = "id";

	public static final String PROP_NUMBER_SCOPE = "numberScope";
	
	public static final String NAME_NUMBER = "Number";
	
	public static final String PROP_NUMBER = "number";
	
	public static final String NAME_VERSION = "Version";
	
	public static final String PROP_VERSION = "version";
	
	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";
	
	public static final String NAME_JOB = "Job";
	
	public static final String PROP_JOB = "jobName";
	
	public static final String NAME_IMAGE = "Image";
	
	public static final String NAME_STATUS = "Status";
	
	public static final String PROP_STATUS = "status";
	
	public static final String NAME_SUBMITTER = "Submitter";
	
	public static final String PROP_SUBMITTER = "submitter";
	
	public static final String PROP_SUBMITTER_NAME = "submitterName";
	
	public static final String NAME_CANCELLER = "Canceller";
	
	public static final String PROP_CANCELLER = "canceller";
	
	public static final String PROP_CANCELLER_NAME = "cancellerName";
	
	public static final String NAME_SUBMIT_DATE = "Submit Date";
	
	public static final String PROP_SUBMIT_DATE = "submitDate";
	
	public static final String NAME_PENDING_DATE = "Pending Date";
	
	public static final String PROP_PENDING_DATE = "pendingDate";
	
	public static final String NAME_RUNNING_DATE = "Running Date";
	
	public static final String PROP_RUNNING_DATE = "runningDate";
	
	public static final String NAME_FINISH_DATE = "Finish Date";
	
	public static final String PROP_FINISH_DATE = "finishDate";
	
	public static final String PROP_FINISH_DAY = "finishDay";
	
	public static final String NAME_COMMIT = "Commit";
	
	public static final String NAME_BRANCH = "Branch";
	
	public static final String NAME_TAG = "Tag";
	
	public static final String PROP_REF_NAME = "refName";
	
	public static final String PROP_COMMIT = "commitHash";
	
	public static final String PROP_PARAMS = "params";
	
	public static final String NAME_DEPENDENCIES = "Dependencies";
	
	public static final String PROP_DEPENDENCIES = "dependencies";
	
	public static final String NAME_DEPENDENTS = "Dependents";
	
	public static final String PROP_DEPENDENTS = "dependents";
	
	public static final String NAME_PULL_REQUEST = "Pull Request";
	
	public static final String PROP_PULL_REQUEST = "request";
	
	public static final String NAME_ERROR_MESSAGE = "Error Message";
	
	public static final String NAME_LOG = "Log";
	
	public static final String PROP_TRIGGER_ID = "triggerId";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_PROJECT, NAME_NUMBER, NAME_JOB, NAME_STATUS, NAME_SUBMITTER, NAME_CANCELLER, 
			NAME_SUBMIT_DATE, NAME_PENDING_DATE, NAME_RUNNING_DATE, NAME_FINISH_DATE,
			NAME_PULL_REQUEST, NAME_BRANCH, NAME_TAG, NAME_COMMIT, NAME_VERSION, NAME_DEPENDENCIES, 
			NAME_DEPENDENTS, NAME_ERROR_MESSAGE, NAME_LOG, NAME_IMAGE, BuildMetric.NAME_REPORT);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_JOB, NAME_NUMBER, NAME_BRANCH, NAME_TAG, NAME_VERSION, NAME_PULL_REQUEST, 
			NAME_COMMIT, NAME_SUBMIT_DATE, NAME_PENDING_DATE, NAME_RUNNING_DATE, NAME_FINISH_DATE);

	public static final List<String> METRIC_QUERY_FIELDS = Lists.newArrayList(
			NAME_JOB, NAME_BRANCH, NAME_PULL_REQUEST, BuildMetric.NAME_REPORT);
	
	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_JOB, PROP_JOB,
			NAME_STATUS, PROP_STATUS,
			NAME_NUMBER, PROP_NUMBER,
			NAME_SUBMIT_DATE, PROP_SUBMIT_DATE,
			NAME_PENDING_DATE, PROP_PENDING_DATE,
			NAME_RUNNING_DATE, PROP_RUNNING_DATE,
			NAME_FINISH_DATE, PROP_FINISH_DATE,
			NAME_PROJECT, PROP_PROJECT,
			NAME_COMMIT, PROP_COMMIT);	
	
	private static ThreadLocal<Stack<Build>> stack =  new ThreadLocal<Stack<Build>>() {

		@Override
		protected Stack<Build> initialValue() {
			return new Stack<Build>();
		}
	
	};
	
	public static final String ARTIFACTS_DIR = "artifacts";
	
	private static final int MAX_ERROR_MESSAGE_LEN = 12000;
	
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
	private Project numberScope; 
	
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
	
	private String jobWorkspace;
	
	@Column(nullable=false)
	private String refName;
	
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
	
	private Integer finishDay;
	
	@Column(nullable=false, length=1000)
	private String submitReason;
	
	@Column(length=MAX_ERROR_MESSAGE_LEN)
	private String errorMessage;

	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<BuildParam> params = new ArrayList<>();
	
	@OneToMany(mappedBy="dependent", cascade=CascadeType.REMOVE)
	private Collection<BuildDependence> dependencies = new ArrayList<>();
	
	@OneToMany(mappedBy="dependency", cascade=CascadeType.REMOVE)
	private Collection<BuildDependence> dependents= new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<JestTestMetric> jestTestMetrics = new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<CloverMetric> cloverMetrics = new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<CheckstyleMetric> checkstyleMetrics = new ArrayList<>();
	
	@ManyToOne(fetch=FetchType.LAZY)
	private PullRequest request;
	
	private transient Map<String, List<String>> paramMap;
	
	private transient Collection<Long> fixedIssueNumbers;
	
	private transient Map<Build.Status, Collection<RevCommit>> commitsCache;
	
	private transient BuildSpec spec;
	
	private transient Job job;
	
	private transient Map<String, Input> paramInputs;
	
	private transient Map<Build.Status, Build> streamPreviousCache = new HashMap<>();
	
	private transient Map<Integer, Collection<Long>> streamPreviousNumbersCache = new HashMap<>();
	
	public Project getNumberScope() {
		return numberScope;
	}

	public void setNumberScope(Project numberScope) {
		this.numberScope = numberScope;
	}

	@Override
	public Project getProject() {
		return project;
	}
	
	@Override
	public String getPrefix() {
		return "build";
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

	@Nullable
	public String getJobWorkspace() {
		return jobWorkspace;
	}

	public void setJobWorkspace(@Nullable String jobWorkspace) {
		this.jobWorkspace = jobWorkspace;
	}

	@Nullable
	public String getVersion() {
		return version;
	}

	public void setVersion(@Nullable String version) {
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
	
	public boolean isSuccessful() {
		return status == Status.SUCCESSFUL;
	}
	
	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public String getSubmitReason() {
		return submitReason;
	}

	public void setSubmitReason(String submitReason) {
		this.submitReason = submitReason;
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
		if (finishDate != null) {
			finishDay = new Day(finishDate).getValue();
		} else {
			finishDay = null;
		}
	}

	public Date getRetryDate() {
		return retryDate;
	}

	public void setRetryDate(Date retryDate) {
		this.retryDate = retryDate;
	}

	@Nullable
	public Integer getFinishDay() {
		return finishDay;
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
			errorMessage = StringUtils.abbreviate(errorMessage, MAX_ERROR_MESSAGE_LEN);
			for (String secretValue: getSecretValuesToMask())
				errorMessage = StringUtils.replace(errorMessage, secretValue, SecretInput.MASK);
		}
		this.errorMessage = errorMessage;
	}

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}
	
	@Nullable
	public String getBranch() {
		if (refName != null)
			return GitUtils.ref2branch(refName);
		else
			return null;
	}

	@Nullable
	public String getTag() {
		if (refName != null)
			return GitUtils.ref2tag(refName);
		else
			return null;
	}
	
	@Nullable
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
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
						fixedIssueNumbers.addAll(IssueUtils.parseFixedIssueNumbers(getProject(), commit.getFullMessage()));
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
		for (JobSecret secret: getProject().getBuildSetting().getJobSecrets()) {
			if (isOnBranches(secret.getAuthorizedBranches()))
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
		if (secretName.startsWith(SecretInput.LITERAL_VALUE_PREFIX)) {
			return secretName.substring(SecretInput.LITERAL_VALUE_PREFIX.length());
		} else {
			for (JobSecret secret: getProject().getBuildSetting().getJobSecrets()) {
				if (secret.getName().equals(secretName)) {
					if (isOnBranches(secret.getAuthorizedBranches()))				
						return secret.getValue();
					else
						throw new ExplicitException("Job secret not authorized: " + secretName);
				}
			}
			throw new ExplicitException("No job secret found: " + secretName);
		}
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
	
	public String getReportCategoryLockKey(String reportCategory) {
		return "job-report:" + getId() + ":" + reportCategory;
	}
	
	public File getReportCategoryDir(String reportCategory) {
		return new File(getPublishDir(), reportCategory);
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
	
	public Collection<Long> getStreamPreviousNumbers(int limit) {
		if (streamPreviousNumbersCache == null) 
			streamPreviousNumbersCache = new HashMap<>();
		if (!streamPreviousNumbersCache.containsKey(limit)) {
			BuildManager buildManager = OneDev.getInstance(BuildManager.class);
			streamPreviousNumbersCache.put(limit, buildManager.queryStreamPreviousNumbers(
					this, null, EntityCriteria.IN_CLAUSE_LIMIT));
		}
		return streamPreviousNumbersCache.get(limit);
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
			return Interpolative.parse(interpolativeString).interpolateWith(new VariableInterpolator(this));
		else 
			return null;
	}	
	
	@SuppressWarnings("unchecked")
	public <T> T interpolateProperties(T object) {
		Class<T> clazz = VariableInterpolator.getUninterceptedClass(object);
		
		T unintercepted;
		try {
			unintercepted = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		for (Field field: BeanUtils.findFields(clazz)) {
			field.setAccessible(true);
			try {
				field.set(unintercepted, field.get(object));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		for (Method getter: BeanUtils.findGetters(clazz)) {
			if (getter.getAnnotation(Editable.class) != null) {
				Method setter = BeanUtils.findSetter(getter);
				if (setter != null) {
					try {
						Object propertyValue = getter.invoke(unintercepted);
						if (propertyValue != null) {
							Class<?> propertyClass = VariableInterpolator.getUninterceptedClass(propertyValue);
							if (getter.getAnnotation(io.onedev.server.web.editable.annotation.Interpolative.class) != null) {
								try {
									if (propertyValue instanceof String) {
										setter.invoke(unintercepted, interpolate((String) propertyValue));
									} else if (propertyValue instanceof List) {
										List<String> interpolatedList = new ArrayList<>();
										for (String element: (List<String>) propertyValue) 
											interpolatedList.add(interpolate(element));
										setter.invoke(unintercepted, interpolatedList);
									}
								} catch (Exception e) {
									String message = String.format("Error interpolating (class: %s, property: %s)", 
											propertyClass, BeanUtils.getPropertyName(getter));
									throw new RuntimeException(message, e);
								}
							} else if (propertyClass.getAnnotation(Editable.class) != null) {
								setter.invoke(unintercepted, interpolateProperties(propertyValue));
							} else if (propertyValue instanceof List) {
								List<Object> uninterceptedList = new ArrayList<>();
								for (Object element: (List<?>)propertyValue) { 
									if (element != null && VariableInterpolator.getUninterceptedClass(element).getAnnotation(Editable.class) != null)
										uninterceptedList.add(interpolateProperties(element));
									else
										uninterceptedList.add(element);
								}
								setter.invoke(unintercepted, uninterceptedList);
							}
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		return unintercepted;
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
	
	public static void push(@Nullable Build build) {
		stack.get().push(build);
		if (build != null)
			ScriptIdentity.push(new JobIdentity(build.getProject(), build.getCommitId()));
	}

	public static void pop() {
		Build build = stack.get().pop();
		if (build != null)
			ScriptIdentity.pop();
	}
	
	public boolean isValid() {
		try {
			return getProject().getRepository().getObjectDatabase().has(ObjectId.fromString(getCommitHash()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	public boolean isOnBranches(@Nullable String branches) {
		if (branches == null)
			branches = "**";
		if (project.isCommitOnBranches(getCommitId(), branches)) {
			return true;
		} else if (getRequest() != null) {
			PatternSet patternSet = PatternSet.parse(branches);
			PathMatcher matcher = new PathMatcher();
			return project.equals(getRequest().getSourceProject()) 
					&& patternSet.matches(matcher, getRequest().getTargetBranch())
					&& getRequest().getSourceBranch() != null 
					&& patternSet.matches(matcher, getRequest().getSourceBranch());
		} else {
			return false;
		}
	}
	
	public boolean matchParams(List<ParamSupply> paramSupplies) {
		AtomicBoolean matches = new AtomicBoolean(false);
		new MatrixRunner<List<String>>(ParamSupply.getParamMatrix(null, paramSupplies)) {
			
			@Override
			public void run(Map<String, List<String>> params) {
				if (!matches.get()) {
					boolean matching = true;
					for (Map.Entry<String, List<String>> entry: params.entrySet()) {
						if (!(getJob().getParamSpecMap().get(entry.getKey()) instanceof SecretParam)) {
							List<String> paramValues = getParamMap().get(entry.getKey());
							if (paramValues != null) {
								if (!entry.getValue().isEmpty()) {
									if (!Objects.equal(new HashSet<>(entry.getValue()), new HashSet<>(paramValues))) {
										matching = false;
										break;
									}
								} else if (paramValues.size() != 1 || paramValues.iterator().next() != null) {
									matching = false;
									break;
								}
							} else {
								matching = false;
								break;
							}
						}
					}
					if (matching)
						matches.set(true);
				}
			}
			
		}.run();
		
		return matches.get();
	}
}
