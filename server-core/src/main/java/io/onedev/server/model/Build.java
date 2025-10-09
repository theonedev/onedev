package io.onedev.server.model;

import static io.onedev.server.model.AbstractEntity.PROP_NUMBER;
import static io.onedev.server.model.Build.PROP_COMMIT_HASH;
import static io.onedev.server.model.Build.PROP_FINISH_DATE;
import static io.onedev.server.model.Build.PROP_FINISH_DAY;
import static io.onedev.server.model.Build.PROP_FINISH_MONTH;
import static io.onedev.server.model.Build.PROP_FINISH_WEEK;
import static io.onedev.server.model.Build.PROP_JOB_NAME;
import static io.onedev.server.model.Build.PROP_PENDING_DATE;
import static io.onedev.server.model.Build.PROP_REF_NAME;
import static io.onedev.server.model.Build.PROP_RUNNING_DATE;
import static io.onedev.server.model.Build.PROP_STATUS;
import static io.onedev.server.model.Build.PROP_SUBMIT_DATE;
import static io.onedev.server.model.Build.PROP_VERSION;
import static io.onedev.server.model.Project.BUILDS_DIR;
import static io.onedev.server.model.support.TimeGroups.PROP_DAY;
import static io.onedev.server.model.support.TimeGroups.PROP_MONTH;
import static io.onedev.server.model.support.TimeGroups.PROP_WEEK;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.bootstrap.SecretMasker;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Markdown;
import io.onedev.server.attachment.AttachmentStorageSupport;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.model.support.ProjectBelonging;
import io.onedev.server.model.support.TimeGroups;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.search.entity.SortField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.BuildService;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.util.BuildAware;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.web.util.WicketUtils;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_submitter_id"), 
				@Index(columnList="o_canceller_id"), @Index(columnList="o_request_id"),  
				@Index(columnList= PROP_COMMIT_HASH), 
				@Index(columnList=PROP_NUMBER), @Index(columnList= PROP_JOB_NAME), 
				@Index(columnList=PROP_STATUS), @Index(columnList=PROP_REF_NAME),  
				@Index(columnList=PROP_SUBMIT_DATE), @Index(columnList=PROP_PENDING_DATE), 
				@Index(columnList=PROP_RUNNING_DATE), @Index(columnList=PROP_FINISH_DATE), 
				@Index(columnList=PROP_FINISH_MONTH), @Index(columnList=PROP_FINISH_WEEK),
				@Index(columnList=PROP_FINISH_DAY), @Index(columnList=PROP_VERSION), 
				@Index(columnList="o_numberScope_id"), @Index(columnList="o_project_id, " + PROP_COMMIT_HASH)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", PROP_NUMBER})}
)
@DynamicUpdate
public class Build extends ProjectBelonging 
		implements AttachmentStorageSupport, LabelSupport<BuildLabel> {

	private static final long serialVersionUID = 1L;
	
	public static final String ARTIFACTS_DIR = "artifacts";

	public static final String LOG_FILE = "build.log";

	public static final int MAX_DESCRIPTION_LEN = 12000;
	
	public static final String NAME_VERSION = "Version";
	
	public static final String PROP_VERSION = "version";
	
	public static final String PROP_AGENT = "agent";
	
	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";
	
	public static final String NAME_JOB = "Job";
	
	public static final String NAME_LABEL = "Label";
	
	public static final String PROP_JOB_NAME = "jobName";
	
	public static final String NAME_STATUS = "Status";
	
	public static final String PROP_STATUS = "status";
	
	public static final String NAME_SUBMITTER = "Submitter";
	
	public static final String PROP_SUBMITTER = "submitter";
	
	public static final String NAME_CANCELLER = "Canceller";
	
	public static final String PROP_CANCELLER = "canceller";
	
	public static final String NAME_SUBMIT_DATE = "Submit Date";
	
	public static final String PROP_SUBMIT_DATE = "submitDate";
	
	public static final String NAME_PENDING_DATE = "Pending Date";
	
	public static final String PROP_PENDING_DATE = "pendingDate";
	
	public static final String NAME_RUNNING_DATE = "Running Date";
	
	public static final String PROP_RUNNING_DATE = "runningDate";
	
	public static final String NAME_FINISH_DATE = "Finish Date";
	
	public static final String PROP_FINISH_DATE = "finishDate";
	
	public static final String PROP_FINISH_TIME_GROUPS = "finishTimeGroups";
	
	public static final String PROP_FINISH_MONTH = "finishMonth";

	public static final String PROP_FINISH_WEEK = "finishWeek";

	public static final String PROP_FINISH_DAY = "finishDay";
	
	public static final String PROP_PENDING_DURATION = "pendingDuration";
	
	public static final String PROP_RUNNING_DURATION = "runningDuration";
	
	public static final String NAME_COMMIT = "Commit";
	
	public static final String NAME_BRANCH = "Branch";
	
	public static final String NAME_TAG = "Tag";
	
	public static final String PROP_REF_NAME = "refName";
	
	public static final String PROP_COMMIT_HASH = "commitHash";
	
	public static final String PROP_PARAMS = "params";
	
	public static final String PROP_DEPENDENCIES = "dependencies";
	
	public static final String PROP_DEPENDENTS = "dependents";
	
	public static final String NAME_PULL_REQUEST = "Pull Request";
	
	public static final String PROP_PULL_REQUEST = "request";
	
	public static final String PROP_ISSUE = "issue";
	
	public static final String NAME_LOG = "Log";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_PROJECT, NAME_NUMBER, NAME_JOB, NAME_LABEL, NAME_STATUS, NAME_SUBMITTER, 
			NAME_CANCELLER, NAME_SUBMIT_DATE, NAME_PENDING_DATE, NAME_RUNNING_DATE, 
			NAME_FINISH_DATE, NAME_PULL_REQUEST, NAME_BRANCH, NAME_TAG, NAME_COMMIT, 
			NAME_VERSION, NAME_LOG, BuildMetric.NAME_REPORT);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_JOB, NAME_NUMBER, NAME_BRANCH, NAME_TAG, NAME_VERSION, 
			NAME_LABEL, NAME_PULL_REQUEST, NAME_COMMIT, NAME_SUBMIT_DATE, NAME_PENDING_DATE, 
			NAME_RUNNING_DATE, NAME_FINISH_DATE);

	public static final List<String> METRIC_QUERY_FIELDS = Lists.newArrayList(
			NAME_JOB, NAME_BRANCH, NAME_PULL_REQUEST, BuildMetric.NAME_REPORT);
	
	public static final Map<String, SortField<Build>> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_JOB, new SortField<>(PROP_JOB_NAME));
		SORT_FIELDS.put(NAME_STATUS, new SortField<>(PROP_STATUS));
		SORT_FIELDS.put(NAME_NUMBER, new SortField<>(PROP_NUMBER));
		SORT_FIELDS.put(NAME_SUBMIT_DATE, new SortField<>(PROP_SUBMIT_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_PENDING_DATE, new SortField<>(PROP_PENDING_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_RUNNING_DATE, new SortField<>(PROP_RUNNING_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_FINISH_DATE, new SortField<>(PROP_FINISH_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_PROJECT, new SortField<>(PROP_PROJECT));
		SORT_FIELDS.put(NAME_COMMIT, new SortField<>(PROP_COMMIT_HASH));
	}
	
	private static ThreadLocal<Stack<Build>> stack = ThreadLocal.withInitial(Stack::new);

	public static File getLogFile(Long projectId, Long buildNumber) {
		File buildDir = OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber);
		return new File(buildDir, LOG_FILE);
	}
	
	public static String getProjectRelativeDirPath(Long buildNumber) {
		return BUILDS_DIR + "/" + getRelativeDirPath(buildNumber);		
	}
	
	public static String getRelativeDirPath(Long buildNumber) {
		return String.format("s%03d", buildNumber%1000) + "/" + buildNumber;
	}
	
	public File getLogFile() {
		return getLogFile(getProject().getId(), getNumber());
	}
	
	public static String getLogLockName(Long projectId, Long buildNumber) {
		return "build-log: " + projectId + ":" + buildNumber;
	}
	
	public String getLogLockName() {
		return getLogLockName(getProject().getId(), getNumber());
	}

	public enum Status {
		// Most significant status comes first, refer to getOverallStatus
		WAITING {
			@Override
			public boolean isFinished() {
				return false;
			}
		}, 
		PENDING {
			@Override
			public boolean isFinished() {
				return false;
			}
			
		}, 
		RUNNING {
			@Override
			public boolean isFinished() {
				return false;
			}
			
		}, 
		FAILED {
			@Override
			public boolean isFinished() {
				return true;
			}
			
		}, 
		CANCELLED {
			@Override
			public boolean isFinished() {
				return true;
			}
			
		}, 
		TIMED_OUT {
			@Override
			public boolean isFinished() {
				return true;
			}
		},
		SUCCESSFUL {
			@Override
			public boolean isFinished() {
				return true;
			}
			
		};
		
		@Override
		public String toString() {
			return TextUtils.getDisplayValue(this);
		}
		
		public abstract boolean isFinished();
		
		@Nullable
		public static Status of(String statusName) {
			for (Status status: Status.values()) {
				if (status.toString().toLowerCase().equals(statusName.toLowerCase()))
					return status;
			}
			return null;
		}
		
		@Nullable
		public static Status getOverallStatus(Collection<Status> statuses) {
			for (Status status: Status.values()) {
				if (statuses.contains(status))
					return status;
			}
			return null;
		}
		
		public static Criterion ofFinished() {
			return Restrictions.or(
					Restrictions.eq(PROP_STATUS, Status.FAILED), 
					Restrictions.eq(PROP_STATUS, Status.CANCELLED), 
					Restrictions.eq(PROP_STATUS, Status.TIMED_OUT),
					Restrictions.eq(PROP_STATUS, Status.SUCCESSFUL));
		}
		
	};
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project numberScope; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Agent agent; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User submitter;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User canceller;
	
	@Column(nullable=false)
	private String jobName;
	
	@JsonIgnore
	@Column(nullable=false)
	private String jobToken;
	
	@Column
	private String workspacePath;
	
	@Lob
	@Column(nullable=false)
	private ArrayList<String> checkoutPaths = new ArrayList<>();
	
	@Column(nullable=false)
	private String refName;
	
	private String version;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	private String description;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private long number;

	private long submitSequence = 1L;
	
	@Column(nullable=false)
	private String commitHash;
	
	@Column(nullable=false)
	private Status status; 
	
	private boolean paused;
	
	@Column(nullable=false)
	private Date submitDate;
	
	private Date pendingDate;
	
	private Date runningDate;
	
	private Date finishDate;
	
	private Long pendingDuration;
	
	private Long runningDuration;
	
	private Date retryDate;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name= PROP_MONTH, column=@Column(name=PROP_FINISH_MONTH)),
			@AttributeOverride(name= PROP_WEEK, column=@Column(name=PROP_FINISH_WEEK)),
			@AttributeOverride(name= PROP_DAY, column=@Column(name=PROP_FINISH_DAY))})
	private TimeGroups finishTimeGroups;
	
	@Column(nullable=false, length=1000)
	private String submitReason;
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<BuildLabel> labels = new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<BuildParam> params = new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<Pack> packs = new ArrayList<>();
	
	@OneToMany(mappedBy="dependent", cascade=CascadeType.REMOVE)
	private Collection<BuildDependence> dependencies = new ArrayList<>();
	
	@OneToMany(mappedBy="dependency", cascade=CascadeType.REMOVE)
	private Collection<BuildDependence> dependents= new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<UnitTestMetric> jestTestMetrics = new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<CoverageMetric> cloverMetrics = new ArrayList<>();
	
	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<ProblemMetric> checkstyleMetrics = new ArrayList<>();
	
	@ManyToOne(fetch=FetchType.LAZY)
	private PullRequest request;

	@ManyToOne(fetch=FetchType.LAZY)
	private Issue issue;
	
	private transient Collection<Long> fixedIssueIds;
	
	private transient Map<Build.Status, Collection<RevCommit>> commitsCache;
	
	private transient Optional<BuildSpec> spec;
	
	private transient Map<String, List<String>> paramMap;
	
	private transient ParamCombination paramCombination;
	
	private transient Map<Build.Status, Build> streamPreviousCache = new HashMap<>();
	
	private transient Map<Integer, Collection<Long>> streamPreviousNumbersCache = new HashMap<>();
	
	private transient JobAuthorizationContext jobAuthorizationContext;
	
	private transient List<ArtifactInfo> rootArtifacts;
	
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
	
	public void setProject(Project project) {
		this.project = project;
	}

	@Nullable
	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
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

	public String getJobToken() {
		return jobToken;
	}

	public void setJobToken(String jobToken) {
		this.jobToken = jobToken;
	}

	@Nullable
	public String getWorkspacePath() {
		return workspacePath;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}

	public Collection<String> getCheckoutPaths() {
		return checkoutPaths;
	}

	@Nullable
	public String getVersion() {
		return version;
	}

	public void setVersion(@Nullable String version) {
		this.version = version;
	}

	@Editable
	@Markdown
	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable String description) {
		this.description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LEN);
	}

	public String getFQN() {
		return getReference().toString();
	}
	
	public EntityReference getReference() {
		return new BuildReference(getProject(), getNumber());
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public long getSubmitSequence() {
		return submitSequence;
	}

	public void setSubmitSequence(long submitSequence) {
		this.submitSequence = submitSequence;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUUID(String uuid) {
		this.uuid = uuid;
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
		this.status = status;
		paused = false;
	}
	
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public Date getStatusDate() {
		switch (status) {
			case FAILED:
			case CANCELLED:
			case SUCCESSFUL:
			case TIMED_OUT:
				return getFinishDate();
			case WAITING:
				return getRetryDate() != null ? getRetryDate() : getSubmitDate();
			case RUNNING:
				return getRunningDate();
			case PENDING:
				return getPendingDate();
			default:
				throw new RuntimeException("Unexpected build status: " + status);
		}
	}
	
	public boolean isFinished() {
		return status.isFinished();
	}
	
	public boolean isSuccessful() {
		return status == Status.SUCCESSFUL;
	}

	public boolean isFailed() {
		return status == Status.FAILED;
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

	@Nullable
	public Date getPendingDate() {
		return pendingDate;
	}

	public void setPendingDate(@Nullable Date pendingDate) {
		this.pendingDate = pendingDate;
	}

	@Nullable
	public Date getRunningDate() {
		return runningDate;
	}

	public void setRunningDate(@Nullable Date runningDate) {
		this.runningDate = runningDate;
		if (runningDate != null && pendingDate != null) 
			pendingDuration = runningDate.getTime() - pendingDate.getTime();
		else 
			pendingDuration = null;
	}

	@Nullable
	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(@Nullable Date finishDate) {
		this.finishDate = finishDate;
		if (finishDate != null) {
			if (runningDate != null)
				runningDuration = finishDate.getTime() - runningDate.getTime();
			else 
				runningDuration = null;
			finishTimeGroups = TimeGroups.of(finishDate);
		} else {
			runningDuration = null;
			finishTimeGroups = null;
		}
	}

	@Nullable
	public Long getPendingDuration() {
		return pendingDuration;
	}

	public void setPendingDuration(@Nullable Long pendingDuration) {
		this.pendingDuration = pendingDuration;
	}

	@Nullable
	public Long getRunningDuration() {
		return runningDuration;
	}

	public void setRunningDuration(@Nullable Long runningDuration) {
		this.runningDuration = runningDuration;
	}
	
	@Nullable
	public Date getRetryDate() {
		return retryDate;
	}

	public void setRetryDate(@Nullable Date retryDate) {
		this.retryDate = retryDate;
	}

	@Nullable
	public TimeGroups getFinishTimeGroups() {
		return finishTimeGroups;
	}

	public void setFinishTimeGroups(@Nullable TimeGroups finishTimeGroups) {
		this.finishTimeGroups = finishTimeGroups;
	}

	public Collection<BuildParam> getParams() {
		return params;
	}

	public void setParams(Collection<BuildParam> params) {
		this.params = params;
	}

	public Collection<Pack> getPacks() {
		return packs;
	}

	public void setPacks(Collection<Pack> packs) {
		this.packs = packs;
	}

	public Collection<BuildLabel> getLabels() {
		return labels;
	}

	public void setLabels(Collection<BuildLabel> labels) {
		this.labels = labels;
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

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}
	
	@Nullable
	public String getBranch() {
		return GitUtils.ref2branch(refName);
	}

	@Nullable
	public String getTag() {
		return GitUtils.ref2tag(refName);
	}
	
	@Nullable
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	@Nullable
	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
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
	
	public ParamCombination getParamCombination() {
		if (paramCombination == null) {
			if (getJob() != null)
				paramCombination = new ParamCombination(getJob().getParamSpecs(), getParamMap());
			else
				paramCombination = new ParamCombination(new ArrayList<>(), getParamMap());
		}
		return paramCombination;
	}
	
	public Map<String, Input> getParamInputs() {
		return getParamCombination().getParamInputs();
	}
	
	public boolean isParamVisible(String paramName) {
		return getParamCombination().isParamVisible(paramName);
	}
	
	/**
	 * Get fixed issue numbers
	 * 
	 */
	public Collection<Long> getFixedIssueIds() {
		if (fixedIssueIds == null) {
			fixedIssueIds = new HashSet<>();
			Build prevSuccessfulBuild = getStreamPrevious(Status.SUCCESSFUL);
			if (prevSuccessfulBuild != null) {
				for (RevCommit commit: getCommits(Status.SUCCESSFUL)) 
					fixedIssueIds.addAll(getProject().parseFixedIssueIds(commit.getFullMessage()));
			} 
		}
		return fixedIssueIds;
	}
	 
	public Collection<RevCommit> getCommits(Build.@Nullable Status sincePrevStatus) {
		if (commitsCache == null) 
			commitsCache = new HashMap<>();
		if (!commitsCache.containsKey(sincePrevStatus)) {
			Collection<RevCommit> commits;
			Build prevBuild = getStreamPrevious(sincePrevStatus);
			if (prevBuild != null) {
				commits = getGitService().getReachableCommits(project, 
						Lists.newArrayList(getCommitId()), Lists.newArrayList(prevBuild.getCommitId()));
			} else {
				commits = new ArrayList<>();
			}
			commitsCache.put(sincePrevStatus, commits);
		}
		return commitsCache.get(sincePrevStatus);
	}
	
	public BuildFacade getFacade() {
		return new BuildFacade(getId(), getProject().getId(), getNumber(), getCommitHash());
	}
	
	public JobAuthorizationContext getJobAuthorizationContext() {
		if (jobAuthorizationContext == null) 
			jobAuthorizationContext = new JobAuthorizationContext(project, getCommitId(), request);
		return jobAuthorizationContext;
	}
	
	// For backward compatibility
	public JobAuthorizationContext getJobSecretAuthorizationContext() {
		return getJobAuthorizationContext();
	}

	public SecretMasker getSecretMasker() {
		return SecretMasker.create(getMaskSecrets(), SecretInput.MASK);
	}
	
	public Collection<String> getMaskSecrets() {
		Collection<String> maskSecrets = new HashSet<>();
		maskSecrets.add(getJobToken());
		maskSecrets.add(OneDev.getInstance(ClusterService.class).getCredential());

		for (JobSecret secret: getProject().getHierarchyJobSecrets()) 
			maskSecrets.add(secret.getValue());
		
		for (BuildParam param: getParams()) {
			if (param.getType().equals(ParamSpec.SECRET) && param.getValue() != null) {		
				if (param.getValue().startsWith(SecretInput.LITERAL_VALUE_PREFIX)) 
					maskSecrets.add(param.getValue().substring(SecretInput.LITERAL_VALUE_PREFIX.length()));
			}
		}

		maskSecrets.removeIf(s -> s.length() < SecretInput.MASK.length());
		return maskSecrets;
	}
	
	@Nullable
	public BuildSpec getSpec() {
		if (spec == null) 
			spec = Optional.ofNullable(getProject().getBuildSpec(getCommitId()));
		return spec.orElse(null);
	}
	
	@Nullable
	public Job getJob() {
		JobAuthorizationContext.push(getJobAuthorizationContext());
		try {
			return getSpec()!=null? getSpec().getJobMap().get(getJobName()): null;
		} finally {
			JobAuthorizationContext.pop();
		}
	}

	public Serializable getParamBean() {
		Serializable paramBean;
		try {
			paramBean = (Serializable) ParamUtils.defineBeanClass(getJob().getParamSpecs())
					.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		} 
		BeanDescriptor descriptor = new BeanDescriptor(paramBean.getClass());
		for (List<PropertyDescriptor> groupProperties: descriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				ParamSpec paramSpec = getJob().getParamSpecMap().get(property.getDisplayName());
				Preconditions.checkNotNull(paramSpec);
				Input input = Preconditions.checkNotNull(getParamCombination().getParamInputs().get(paramSpec.getName()));
				property.setPropertyValue(paramBean, paramSpec.convertToObject(input.getValues()));
			}
		}
		return paramBean;
	}
	
	public File getDir() {
		return getBuildService().getBuildDir(getProject().getId(), getNumber());
	}
	
	public File getArtifactsDir() {
		return getBuildService().getArtifactsDir(getProject().getId(), getNumber());
	}
	
	@Nullable
	public Build getStreamPrevious(@Nullable Status status) {
		if (streamPreviousCache == null) 
			streamPreviousCache = new HashMap<>();
		if (!streamPreviousCache.containsKey(status)) 
			streamPreviousCache.put(status, getBuildService().findStreamPrevious(this, status));
		return streamPreviousCache.get(status);
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	private BuildService getBuildService() {
		return OneDev.getInstance(BuildService.class);
	}
	
	public Collection<Long> getStreamPreviousNumbers(int limit) {
		if (streamPreviousNumbersCache == null) 
			streamPreviousNumbersCache = new HashMap<>();
		if (!streamPreviousNumbersCache.containsKey(limit)) {
			streamPreviousNumbersCache.put(limit, getBuildService().queryStreamPreviousNumbers(
					this, null, Criteria.IN_CLAUSE_LIMIT));
		}
		return streamPreviousNumbersCache.get(limit);
	}
	
	public String getArtifactsLockName() {
		return getArtifactsLockName(getProject().getId(), getNumber());
	}
	
	public static String getArtifactsLockName(Long projectId, Long buildNumber) {
		return "build-artifacts:" + projectId + ":" + buildNumber;
	}
	
	public static String getLogChangeObservable(Long buildId) {
		return "build-log:" + buildId;
	}
	
	public static String getDetailChangeObservable(Long buildId) {
		return Build.class.getName() + ":" + buildId;
	}
	
	public static String getCommitStatusChangeObservable(Long projectId, String commitHash) {
		return "commit-status:" + projectId + ":" + commitHash;
	}

	public static String getJobStatusChangeObservable(Long projectId, String commitHash, String jobName) {
		return "job-status:" + projectId + ":" + commitHash + ":" + jobName;
	}
	
	public Collection<String> getChangeObservables() {
		return Sets.newHashSet(
				getDetailChangeObservable(getId()),
				getCommitStatusChangeObservable(getProject().getId(), getCommitHash()),
				getJobStatusChangeObservable(getProject().getId(), getCommitHash(), getJobName()));
	}
	
	public static void push(@Nullable Build build) {
		stack.get().push(build);
	}

	public static void pop() {
		stack.get().pop();
	}

	public boolean canCreateBranch(String accessTokenSecret, String branchName) {
		var project = getProject();
		return project.isCommitOnBranch(getCommitId(), project.getDefaultBranch())
				|| accessTokenSecret != null && SecurityUtils.canCreateBranch(getAccessToken(accessTokenSecret).asSubject(), project, branchName);
	}
	
	public boolean canCreateTag(@Nullable String accessTokenSecret, String tagName) {
		var project = getProject();
		return project.isCommitOnBranch(getCommitId(), project.getDefaultBranch())
				|| accessTokenSecret != null && SecurityUtils.canCreateTag(getAccessToken(accessTokenSecret).asSubject(), project, tagName);
	}
	
	public AccessToken getAccessToken(String accessTokenSecret) {
		String secretValue = getJobAuthorizationContext().getSecretValue(accessTokenSecret);
		var accessToken = OneDev.getInstance(AccessTokenService.class).findByValue(secretValue);
		if (accessToken == null)
			throw new ExplicitException("Invalid access token");
		return accessToken;
	}
	
	public boolean canCloseIteration(@Nullable String accessTokenSecret) {
		var project = getProject();
		return project.isCommitOnBranch(getCommitId(), project.getDefaultBranch())
				|| accessTokenSecret != null && SecurityUtils.canManageIssues(getAccessToken(accessTokenSecret).asSubject(), project);
	}
	
	public boolean isValid() {
		return getGitService().hasObjects(getProject(), ObjectId.fromString(getCommitHash()));
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
	
	@Override
	public Project getAttachmentProject() {
		return getProject();
	}

	@Override
	public String getAttachmentGroup() {
		return uuid;
	}

	public static String getSerialLockName(Long buildId) {
		return "build-" + buildId + "-serial";
	}
	
	public List<ArtifactInfo> getRootArtifacts() {
		if (rootArtifacts == null) {
			DirectoryInfo directory = (DirectoryInfo) getBuildService()
					.getArtifactInfo(this, null);
			if (directory != null)
				rootArtifacts = directory.getChildren();
			else 
				rootArtifacts = new ArrayList<>();
		}
		return rootArtifacts;
	}
	
	@Nullable
	public String getBlobPath(String filePath) {
		if (FilenameUtils.getPrefix(filePath).length() == 0 && workspacePath != null)
			filePath = workspacePath + "/" + filePath;
		filePath = filePath.replace('\\', '/');
		filePath = Paths.get(filePath).normalize().toString();
		filePath = filePath.replace('\\', '/');
		for (var checkoutPath: checkoutPaths) {
			if (filePath.startsWith(checkoutPath + "/")) {
				var blobPath = filePath.substring(checkoutPath.length() + 1);
				if (getProject().findBlobIdent(getCommitId(), blobPath) != null)
					return blobPath;
			} else if (filePath.equals(checkoutPath)) {
				return "";
			}
		}
		return null;
	}
	
	public String getSummary(@Nullable Project currentProject) {
		var summary = getJobName();
		if (getVersion() != null)
			summary += ": " + getVersion();
		return summary + " (" + getReference().toString(currentProject) + ")";
	}
	
}
