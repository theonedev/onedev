package io.onedev.server.model;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static io.onedev.server.model.AbstractEntity.PROP_NUMBER;
import static io.onedev.server.model.PullRequest.PROP_CLOSE_DAY;
import static io.onedev.server.model.PullRequest.PROP_CLOSE_MONTH;
import static io.onedev.server.model.PullRequest.PROP_CLOSE_WEEK;
import static io.onedev.server.model.PullRequest.PROP_COMMENT_COUNT;
import static io.onedev.server.model.PullRequest.PROP_CONFUSED_COUNT;
import static io.onedev.server.model.PullRequest.PROP_EYES_COUNT;
import static io.onedev.server.model.PullRequest.PROP_HEART_COUNT;
import static io.onedev.server.model.PullRequest.PROP_ROCKET_COUNT;
import static io.onedev.server.model.PullRequest.PROP_SMILE_COUNT;
import static io.onedev.server.model.PullRequest.PROP_STATUS;
import static io.onedev.server.model.PullRequest.PROP_SUBMIT_DATE;
import static io.onedev.server.model.PullRequest.PROP_SUBMIT_DAY;
import static io.onedev.server.model.PullRequest.PROP_SUBMIT_MONTH;
import static io.onedev.server.model.PullRequest.PROP_SUBMIT_WEEK;
import static io.onedev.server.model.PullRequest.PROP_TADA_COUNT;
import static io.onedev.server.model.PullRequest.PROP_THUMBS_DOWN_COUNT;
import static io.onedev.server.model.PullRequest.PROP_THUMBS_UP_COUNT;
import static io.onedev.server.model.PullRequest.PROP_TITLE;
import static io.onedev.server.model.PullRequest.PROP_UUID;
import static io.onedev.server.model.support.TimeGroups.PROP_DAY;
import static io.onedev.server.model.support.TimeGroups.PROP_MONTH;
import static io.onedev.server.model.support.TimeGroups.PROP_WEEK;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.web.translation.Translation._T;
import static java.lang.ThreadLocal.withInitial;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentStorageSupport;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.UserService;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.PullRequestReference;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.CommitMessageError;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.ProjectBelonging;
import io.onedev.server.model.support.TimeGroups;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.BuildRequirement;
import io.onedev.server.model.support.pullrequest.AutoMerge;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.search.entity.SortField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.util.PullRequestAware;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.xodus.VisitInfoService;

@Entity
@Table(
		indexes={
				@Index(columnList=PROP_TITLE), @Index(columnList=PROP_UUID), 
				@Index(columnList=PROP_NUMBER), @Index(columnList="o_targetProject_id"), 
				@Index(columnList=PROP_SUBMIT_DATE), @Index(columnList= LastActivity.COLUMN_DATE), 
				@Index(columnList="o_sourceProject_id"), @Index(columnList="o_submitter_id"), 
				@Index(columnList=MergePreview.COLUMN_HEAD_COMMIT_HASH), @Index(columnList=PROP_SUBMIT_MONTH), 
				@Index(columnList=PROP_SUBMIT_WEEK), @Index(columnList=PROP_SUBMIT_DAY), 
				@Index(columnList=PROP_CLOSE_MONTH), @Index(columnList=PROP_CLOSE_WEEK), 
				@Index(columnList=PROP_CLOSE_DAY), @Index(columnList=PROP_STATUS), 
				@Index(columnList=PROP_THUMBS_UP_COUNT), @Index(columnList=PROP_THUMBS_DOWN_COUNT),
				@Index(columnList=PROP_SMILE_COUNT), @Index(columnList=PROP_TADA_COUNT),
				@Index(columnList=PROP_CONFUSED_COUNT), @Index(columnList=PROP_HEART_COUNT),
				@Index(columnList=PROP_ROCKET_COUNT), @Index(columnList=PROP_EYES_COUNT),
				@Index(columnList=PROP_COMMENT_COUNT), @Index(columnList="o_numberScope_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", PROP_NUMBER})})
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate
public class PullRequest extends ProjectBelonging 
		implements AttachmentStorageSupport, LabelSupport<PullRequestLabel> {

	private static final long serialVersionUID = 1L;
		
	public static final int MAX_TITLE_LEN = 255;
	
	public static final int MAX_DESCRIPTION_LEN = 100000;
	
	public static final String NAME_STATUS = "Status";
	
	public static final String NAME_TARGET_PROJECT = "Target Project";
	
	public static final String PROP_TARGET_PROJECT = "targetProject";
	
	public static final String NAME_TARGET_BRANCH = "Target Branch";
	
	public static final String PROP_TARGET_BRANCH = "targetBranch";
	
	public static final String NAME_SOURCE_PROJECT = "Source Project";
	
	public static final String PROP_SOURCE_PROJECT = "sourceProject";
	
	public static final String NAME_SOURCE_BRANCH = "Source Branch";
	
	public static final String PROP_SOURCE_BRANCH = "sourceBranch";
	
	public static final String PROP_BUILD_COMMIT_HASH = "buildCommitHash";
	
	public static final String NAME_TITLE = "Title";
	
	public static final String PROP_TITLE = "title";
	
	public static final String NAME_LABEL = "Label";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String NAME_COMMENT = "Comment";
	
	public static final String NAME_COMMENT_COUNT = "Comment Count";
	
	public static final String PROP_COMMENT_COUNT = "commentCount";

	public static final String NAME_THUMBS_UP_COUNT = "Reaction: Thumbs Up Count";

	public static final String PROP_THUMBS_UP_COUNT = "thumbsUpCount";

	public static final String NAME_THUMBS_DOWN_COUNT = "Reaction: Thumbs Down Count";

	public static final String PROP_THUMBS_DOWN_COUNT = "thumbsDownCount";

	public static final String NAME_SMILE_COUNT = "Reaction: Smile Count";

	public static final String PROP_SMILE_COUNT = "smileCount";

	public static final String NAME_TADA_COUNT = "Reaction: Tada Count";

	public static final String PROP_TADA_COUNT = "tadaCount";

	public static final String NAME_CONFUSED_COUNT = "Reaction: Confused Count";

	public static final String PROP_CONFUSED_COUNT = "confusedCount";

	public static final String NAME_HEART_COUNT = "Reaction: Heart Count";

	public static final String PROP_HEART_COUNT = "heartCount";

	public static final String NAME_ROCKET_COUNT = "Reaction: Rocket Count";

	public static final String PROP_ROCKET_COUNT = "rocketCount";

	public static final String NAME_EYES_COUNT = "Reaction: Eyes Count";
	
	public static final String PROP_EYES_COUNT = "eyesCount";

	public static final String PROP_SUBMITTER = "submitter";
	
	public static final String NAME_SUBMIT_DATE = "Submit Date";
	
	public static final String PROP_SUBMIT_DATE = "submitDate";
	
	public static final String NAME_LAST_ACTIVITY_DATE = "Last Activity Date";
	
	public static final String PROP_LAST_ACTIVITY = "lastActivity";
	
	public static final String NAME_CLOSE_DATE = "Close Date";
	
	public static final String PROP_CLOSE_DATE = "closeDate";
	
	public static final String NAME_MERGE_STRATEGY = "Merge Strategy";
	
	public static final String PROP_MERGE_STRATEGY = "mergeStrategy";
	
	public static final String PROP_STATUS = "status";
	
	public static final String PROP_MERGE_PREVIEW = "mergePreview";
	
	public static final String PROP_UUID = "uuid";
	
	public static final String PROP_DURATION = "duration";

	public static final String PROP_SUBMIT_TIME_GROUPS = "submitTimeGroups";
	
	public static final String PROP_CLOSE_TIME_GROUPS = "closeTimeGroups";
	
	public static final String PROP_SUBMIT_MONTH = "submitMonth";

	public static final String PROP_SUBMIT_WEEK = "submitWeek";

	public static final String PROP_SUBMIT_DAY = "submitDay";
	
	public static final String PROP_CLOSE_MONTH = "closeMonth";

	public static final String PROP_CLOSE_WEEK = "closeWeek";

	public static final String PROP_CLOSE_DAY = "closeDay";
	
	public static final String REFS_PREFIX = "refs/pulls/";

	private static final int MAX_CHECK_ERROR_LEN = 1024;

	private static final List<String> CONVENTIONAL_COMMIT_TYPES = List.of(
		"feat", "fix", "docs", "style", "refactor", "perf", "test", "build", "ci", "chore", "revert");

	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_NUMBER, NAME_TITLE, NAME_TARGET_PROJECT, NAME_TARGET_BRANCH, 
			NAME_SOURCE_PROJECT, NAME_SOURCE_BRANCH, NAME_LABEL, NAME_DESCRIPTION, 
			NAME_COMMENT, NAME_SUBMIT_DATE, NAME_LAST_ACTIVITY_DATE, NAME_CLOSE_DATE, 
			NAME_MERGE_STRATEGY, NAME_COMMENT_COUNT, NAME_THUMBS_UP_COUNT, NAME_THUMBS_DOWN_COUNT, 
			NAME_SMILE_COUNT, NAME_TADA_COUNT, NAME_CONFUSED_COUNT, NAME_HEART_COUNT, 
			NAME_ROCKET_COUNT, NAME_EYES_COUNT);

	public static final Map<String, SortField<PullRequest>> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_SUBMIT_DATE, new SortField<>(PROP_SUBMIT_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_CLOSE_DATE, new SortField<>(PROP_CLOSE_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_LAST_ACTIVITY_DATE, new SortField<>(PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_NUMBER, new SortField<>(PROP_NUMBER));
		SORT_FIELDS.put(NAME_STATUS, new SortField<>(PROP_STATUS));
		SORT_FIELDS.put(NAME_TARGET_PROJECT, new SortField<>(PROP_TARGET_PROJECT));
		SORT_FIELDS.put(NAME_TARGET_BRANCH, new SortField<>(PROP_TARGET_BRANCH));
		SORT_FIELDS.put(NAME_SOURCE_PROJECT, new SortField<>(PROP_SOURCE_PROJECT));
		SORT_FIELDS.put(NAME_SOURCE_BRANCH, new SortField<>(PROP_SOURCE_BRANCH));
		SORT_FIELDS.put(NAME_COMMENT_COUNT, new SortField<>(PROP_COMMENT_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_THUMBS_UP_COUNT, new SortField<>(PROP_THUMBS_UP_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_THUMBS_DOWN_COUNT, new SortField<>(PROP_THUMBS_DOWN_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_SMILE_COUNT, new SortField<>(PROP_SMILE_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_TADA_COUNT, new SortField<>(PROP_TADA_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_CONFUSED_COUNT, new SortField<>(PROP_CONFUSED_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_HEART_COUNT, new SortField<>(PROP_HEART_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_ROCKET_COUNT, new SortField<>(PROP_ROCKET_COUNT, DESCENDING));
		SORT_FIELDS.put(NAME_EYES_COUNT, new SortField<>(PROP_EYES_COUNT, DESCENDING));
	}
	
	private static ThreadLocal<Stack<PullRequest>> stack = withInitial(Stack::new);
	
	public enum Status {
		OPEN, MERGED, DISCARDED;
		
		@Override
		public String toString() {
			return TextUtils.getDisplayValue(this);
		}
		
	}
	
	@Column(nullable=false)
	private Status status = Status.OPEN;
	
	@Api(order=100)
	@Column(nullable=false, length=MAX_TITLE_LEN)
	private String title;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	@Api(description = "May be empty")
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User submitter;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project numberScope;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project targetProject;
	
	@Column(nullable=false)
	private String targetBranch;

	@ManyToOne(fetch=FetchType.LAZY)
	@Api(description = "Null if source project is deleted")
	private Project sourceProject;
	
	@Column(nullable=false)
	private String sourceBranch;
	
	@Column(nullable=false)
	private String baseCommitHash;
	
	@Column
	private String buildCommitHash;
		
	@JsonIgnore
	@Embedded
	private MergePreview mergePreview;
	
	@Column(nullable=false)
	private Date submitDate = new Date();
	
	private Date closeDate;
	
	private Long duration;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name= PROP_MONTH, column=@Column(name= PROP_SUBMIT_MONTH)),
			@AttributeOverride(name= PROP_WEEK, column=@Column(name= PROP_SUBMIT_WEEK)),
			@AttributeOverride(name= PROP_DAY, column=@Column(name= PROP_SUBMIT_DAY))})
	private TimeGroups submitTimeGroups;
	
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name= PROP_MONTH, column=@Column(name= PROP_CLOSE_MONTH)),
			@AttributeOverride(name= PROP_WEEK, column=@Column(name= PROP_CLOSE_WEEK)),
			@AttributeOverride(name= PROP_DAY, column=@Column(name= PROP_CLOSE_DAY))})
	private TimeGroups closeTimeGroups;
	
	@Column(nullable=false)
	private MergeStrategy mergeStrategy;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private long number;
	
	private int commentCount;
	
	private int thumbsUpCount;

	private int thumbsDownCount;
	
	private int smileCount;
	
	private int tadaCount;
	
	private int confusedCount;
	
	private int heartCount;
	
	private int rocketCount;
	
	private int eyesCount;

	@JsonProperty(access = READ_ONLY)
	private int descriptionRevisionCount;

	@Embedded
	private LastActivity lastActivity;

	@Embedded
	private AutoMerge autoMerge = new AutoMerge();
	
	@Column(name="CODE_COMMENTS_UPDT")
	private Date codeCommentsUpdateDate;
	
	@Column(length=MAX_CHECK_ERROR_LEN)
	private String checkError;
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestLabel> labels = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestUpdate> updates = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReview> reviews = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestAssignment> assignments = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<Build> builds = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestComment> comments = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestChange> changes = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestWatch> watches = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestMention> mentions = new ArrayList<>();
	
	@OneToMany(mappedBy="compareContext.pullRequest")
	private Collection<CodeComment> codeComments = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PendingSuggestionApply> pendingSuggestionApplies = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReaction> reactions = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestDescriptionRevision> descriptionRevisions = new ArrayList<>();

	private transient Boolean mergedIntoTarget;

	private transient Boolean sourceOutdated;

	private transient Collection<ObjectId> missingCommits;
	
	private transient Collection<Long> fixedIssueIds;
	
	private transient Collection<User> participants;
	
	private transient Collection<RevCommit> pendingCommits;
	
	private transient BuildRequirement buildRequirement;
	
	private transient Collection<Build> currentBuilds;
	
	private transient Collection<User> assignees;
	
	private transient Optional<CommitMessageError> commitMessageErrorOpt;

	private transient Optional<Collection<String>> violatedFileTypesOpt;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = StringUtils.abbreviate(title, MAX_TITLE_LEN);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LEN);
	}
	
	/**
	 * Get the user submitting the pull request.
	 * 
	 * @return
	 * 			the user submitting the pull request
	 */
	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	public Project getNumberScope() {
		return numberScope;
	}

	public void setNumberScope(Project numberScope) {
		this.numberScope = numberScope;
	}

	public Project getTargetProject() {
		return targetProject;
	}
	
	public ProjectAndBranch getTarget() {
		return new ProjectAndBranch(getTargetProject(), getTargetBranch());
	}
	
	public void setTarget(ProjectAndBranch target) {
		setTargetProject(target.getProject());
		setTargetBranch(target.getBranch());
	}
	
	public void setSource(ProjectAndBranch source) {
		setSourceProject(source.getProject());
		setSourceBranch(source.getBranch());
	}
	
	public void setTargetProject(Project targetProject) {
		this.targetProject = targetProject;
	}

	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	@Nullable
	public Project getSourceProject() {
		return sourceProject;
	}

	public void setSourceProject(Project sourceProject) {
		this.sourceProject = sourceProject;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	@Nullable
	public ProjectAndBranch getSource() {
		Project sourceProject = getSourceProject();
		if (sourceProject != null)
			return new ProjectAndBranch(sourceProject, getSourceBranch());
		else
			return null;
	}
	
	public String getTargetRef() {
		return GitUtils.branch2ref(getTargetBranch());
	}
	
	public String getSourceRef() {
		return GitUtils.branch2ref(getSourceBranch());
	}
	
	public Project getWorkProject() {
		if (isNew()) 
			return getSourceProject();
		else
			return getTargetProject();
	}
	
	public String getBaseCommitHash() {
		return baseCommitHash;
	}

	public void setBaseCommitHash(String baseCommitHash) {
		this.baseCommitHash = baseCommitHash;
	}

	@Nullable
	public String getBuildCommitHash() {
		return buildCommitHash;
	}

	public void setBuildCommitHash(@Nullable String buildCommitHash) {
		this.buildCommitHash = buildCommitHash;
	}

	public RevCommit getBaseCommit() {
		return getTargetProject().getRevCommit(ObjectId.fromString(getBaseCommitHash()), true);
	}
	
	@Override
	public Collection<PullRequestLabel> getLabels() {
		return labels;
	}

	public void setLabels(Collection<PullRequestLabel> labels) {
		this.labels = labels;
	}

	public Collection<PullRequestUpdate> getUpdates() {
		return updates;
	}
	
	public void setUpdates(Collection<PullRequestUpdate> updates) {
		this.updates = updates;
	}
	
	public Collection<PullRequestComment> getComments() {
		return comments;
	}

	public void setComments(Collection<PullRequestComment> comments) {
		this.comments = comments;
	}

	public Collection<PullRequestChange> getChanges() {
		return changes;
	}

	public void setChanges(Collection<PullRequestChange> changes) {
		this.changes = changes;
	}

	public Collection<PullRequestReaction> getReactions() {
		return reactions;
	}

	public void setReactions(Collection<PullRequestReaction> reactions) {
		this.reactions = reactions;
	}
	public Collection<PullRequestMention> getMentions() {
		return mentions;
	}

	public void setMentions(Collection<PullRequestMention> mentions) {
		this.mentions = mentions;
	}

	@Override
	public Collection<PullRequestWatch> getWatches() {
		return watches;
	}

	public void setWatches(Collection<PullRequestWatch> watches) {
		this.watches = watches;
	}

	@Override
	public EntityWatch getWatch(User user, boolean createIfNotExist) {
		if (createIfNotExist) {
			PullRequestWatch watch = (PullRequestWatch) super.getWatch(user, false);
			if (watch == null) {
				watch = new PullRequestWatch();
				watch.setRequest(this);
				watch.setUser(user);
				getWatches().add(watch);
			}
			return watch;
		} else {
			return super.getWatch(user, false);
		}
	}
	
	public Collection<CodeComment> getCodeComments() {
		return codeComments;
	}

	public void setCodeComments(Collection<CodeComment> codeComments) {
		this.codeComments = codeComments;
	}

	public Collection<PendingSuggestionApply> getPendingSuggestionApplies() {
		return pendingSuggestionApplies;
	}

	public void setPendingSuggestionApplies(Collection<PendingSuggestionApply> pendingSuggestionApplies) {
		this.pendingSuggestionApplies = pendingSuggestionApplies;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isOpen() {
		return status == Status.OPEN;
	}
	
	/**
	 * Get last merge preview of this pull request. Note that this method may return an 
	 * outdated merge preview. Refer to {@link this#getMergePreview()}
	 * if you'd like to get an update-to-date merge preview
	 *  
	 * @return
	 * 			merge preview of this pull request, or <tt>null</tt> if merge 
	 * 			preview has not been calculated yet. 
	 */
	@Nullable
	public MergePreview getMergePreview() {
		return mergePreview;
	}
	
	public void setMergePreview(MergePreview mergePreview) {
		this.mergePreview = mergePreview;
	}

	/**
	 * Check merge preview of this pull request.
	 * 
	 * @return
	 * 			update to date merge preview of this pull request, or <tt>null</tt> if 
	 * 			the merge preview has not been calculated or outdated
	 */
	@Nullable
	public MergePreview checkMergePreview() {
		if (isOpen()) {
			if (mergePreview != null && mergePreview.isUpToDate(this))
				return mergePreview;
			else
				return null;
		} else {
			return mergePreview;
		}
	}
	
	/**
	 * Get list of sorted updates.
	 * 
	 * @return 
	 * 			list of sorted updates ordered by id
	 */
	public List<PullRequestUpdate> getSortedUpdates() {
		List<PullRequestUpdate> sortedUpdates = new ArrayList<>(updates);
		Collections.sort(sortedUpdates);
		return sortedUpdates;
	}
	
	public List<PullRequestReview> getSortedReviews() {
		List<PullRequestReview> sortedReviews = new ArrayList<>(getReviews());
		Collections.sort(sortedReviews, new Comparator<PullRequestReview>() {

			@Override
			public int compare(PullRequestReview o1, PullRequestReview o2) {
				if (o1.getId() != null && o2.getId() != null)
					return o1.getId().compareTo(o2.getId());
				else
					return 0;
			}
			
		});
		return sortedReviews;
	}
	
	public MergeStrategy getMergeStrategy() {
		return mergeStrategy;
	}

	public void setMergeStrategy(MergeStrategy mergeStrategy) {
		this.mergeStrategy = mergeStrategy;
	}

	public PullRequestUpdate getLatestUpdate() {
		return getSortedUpdates().get(getSortedUpdates().size()-1);
	}
	
	public String getBaseRef() {
		Preconditions.checkNotNull(getId());
		return PullRequest.REFS_PREFIX + getNumber() + "/base";
	}

	public String getMergeRef() {
		Preconditions.checkNotNull(getId());
		return PullRequest.REFS_PREFIX + getNumber() + "/merge";
	}

	public String getBuildRef() {
		Preconditions.checkNotNull(getId());
		return PullRequest.REFS_PREFIX + getNumber() + "/build";
	}
	
	public String getHeadRef() {
		Preconditions.checkNotNull(getId());
		return PullRequest.REFS_PREFIX + getNumber() + "/head";
	}
	
	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
		submitTimeGroups = TimeGroups.of(submitDate);
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
		closeTimeGroups = TimeGroups.of(closeDate);
		duration = closeDate.getTime() - submitDate.getTime();
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Collection<PullRequestReview> getReviews() {
		return reviews;
	}
	
	public void setReviews(Collection<PullRequestReview> reviews) {
		this.reviews = reviews;
	}

	public Collection<PullRequestAssignment> getAssignments() {
		return assignments;
	}

	public void setAssignments(Collection<PullRequestAssignment> assignments) {
		this.assignments = assignments;
	}
	
	public Collection<User> getAssignees() {
		if (assignees == null) 
			assignees = getAssignments().stream().map(it->it.getUser()).collect(Collectors.toSet());
		return assignees;
	}

	public Collection<Build> getBuilds() {
		return builds;
	}

	public void setBuilds(Collection<Build> builds) {
		this.builds = builds;
	}
	
	public Collection<Build> getCurrentBuilds() {
		if (currentBuilds == null) {
			currentBuilds = builds.stream()
						.filter(it->it.getCommitHash().equals(getBuildCommitHash()))
						.collect(Collectors.toList());
		} 
		return currentBuilds;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}
	
	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public LastActivity getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(LastActivity lastActivity) {
		this.lastActivity = lastActivity;
	}

	public AutoMerge getAutoMerge() {
		return autoMerge;
	}

	public void setAutoMerge(AutoMerge autoMerge) {
		this.autoMerge = autoMerge;
	}

	@Nullable
	public Date getCodeCommentsUpdateDate() {
		return codeCommentsUpdateDate;
	}

	public void setCodeCommentsUpdateDate(Date codeCommentsUpdateDate) {
		this.codeCommentsUpdateDate = codeCommentsUpdateDate;
	}

	@Nullable
	public String getCheckError() {
		return checkError;
	}

	public void setCheckError(@Nullable String checkError) {
		if (checkError != null) 
			checkError = StringUtils.abbreviate(checkError, MAX_CHECK_ERROR_LEN);
		this.checkError = checkError;
	}

	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getAuthUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitInfoService.class).getPullRequestVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public boolean isCodeCommentsVisitedAfter(Date date) {
		User user = SecurityUtils.getAuthUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitInfoService.class).getPullRequestCodeCommentsVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public boolean isMerged() {
		return status == Status.MERGED;
	}
	
	public boolean isDiscarded() {
		return status == Status.DISCARDED;
	}
	
	public boolean isMergedIntoTarget() {
		if (mergedIntoTarget == null) { 
			mergedIntoTarget = getGitService().isMergedInto(getTargetProject(), 
					null, ObjectId.fromString(getLatestUpdate().getHeadCommitHash()), 
					getTarget().getObjectId());
		}
		return mergedIntoTarget;
	}

	public boolean isSourceOutdated() {
		if (sourceOutdated == null) {
			var sourceHead = Preconditions.checkNotNull(getSourceHead());
			sourceOutdated = !getGitService().isMergedInto(getTargetProject(),
					null, getTarget().getObjectId(), sourceHead);
		}
		return sourceOutdated;
	}
	
	@Nullable
	public ObjectId getSourceHead() {
		ProjectAndBranch projectAndBranch = getSource();
		if (projectAndBranch != null)
			return projectAndBranch.getObjectId(false);
		else
			return null;
	}
	
	@Nullable
	public PullRequestReview getReview(User user) {
		for (PullRequestReview review: getReviews()) {
			if (review.getUser().equals(user))
				return review;
		}
		return null;
	}
	
	public boolean canCommentOnCommit(String commitHash) {
		if (commitHash.equals(baseCommitHash))
			return true;
		for (PullRequestUpdate update: getUpdates()) {
			for (RevCommit commit: update.getCommits())
				if (commit.name().equals(commitHash))
					return true;
		}
		return false;
	}
	
	public Collection<RevCommit> getPendingCommits() {
		if (pendingCommits == null) {
			pendingCommits = new HashSet<>();
			Project project = getTargetProject();
			ObjectId headCommitId = ObjectId.fromString(getLatestUpdate().getHeadCommitHash());
			ObjectId targetHeadCommitId = ObjectId.fromString(getLatestUpdate().getTargetHeadCommitHash());
			pendingCommits = getGitService().getReachableCommits(project, Lists.newArrayList(headCommitId), 
					Lists.newArrayList(targetHeadCommitId)); 
		}
		return pendingCommits;
	}

	public List<User> getParticipants() {
		if (participants == null) {
			participants = new LinkedHashSet<>();
			participants.add(getSubmitter());
			for (PullRequestComment comment: getComments()) 
				participants.add(comment.getUser());
			for (PullRequestChange change: getChanges()) {
				if (change.getUser() != null)
					participants.add(change.getUser());
			}
			var userService = OneDev.getInstance(UserService.class);
			participants.remove(userService.getSystem());
			participants.remove(userService.getUnknown());
		}
		return new ArrayList<>(participants);
	}
	
	public boolean isValid() {
		return getMissingCommits().isEmpty();
	}

	public Collection<ObjectId> getMissingCommits() {
		if (missingCommits == null) {
			Set<ObjectId> objIds = new LinkedHashSet<>();
			objIds.add(ObjectId.fromString(baseCommitHash));
			for (PullRequestUpdate update: updates) {
				objIds.add(ObjectId.fromString(update.getTargetHeadCommitHash()));
				objIds.add(ObjectId.fromString(update.getHeadCommitHash()));
			}
			missingCommits = getGitService().filterNonExistants(targetProject, objIds);
		}
		return missingCommits;
	}

	/*
	 * Do not remove this method. Used in email notification templates
	 * @return
	 */
	public String getFQN() {
		return getReference().toString();
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	public static String getChangeObservable(Long requestId) {
		return PullRequest.class.getName() + ":" + requestId;
	}

	public Collection<PullRequestDescriptionRevision> getDescriptionRevisions() {
		return descriptionRevisions;
	}

	public void setDescriptionRevisions(Collection<PullRequestDescriptionRevision> descriptionRevisions) {
		this.descriptionRevisions = descriptionRevisions;
	}

	public int getDescriptionRevisionCount() {
		return descriptionRevisionCount;
	}

	public void setDescriptionRevisionCount(int descriptionRevisionCount) {
		this.descriptionRevisionCount = descriptionRevisionCount;
	}

	public Collection<Long> getFixedIssueIds() {
		if (fixedIssueIds == null) {
			fixedIssueIds = new HashSet<>();
			for (PullRequestUpdate update: getUpdates()) {
				for (RevCommit commit: update.getCommits())
					fixedIssueIds.addAll(getProject().parseFixedIssueIds(commit.getFullMessage()));
			}
			fixedIssueIds.addAll(getProject().parseFixedIssueIds(getTitle()));
			if (getDescription() != null)
				fixedIssueIds.addAll(getProject().parseFixedIssueIds(getDescription()));
		}
		return fixedIssueIds;
	}
	
	public boolean isAllReviewsApproved() {
		for (PullRequestReview review: getReviews()) {
			if (review.getStatus() != PullRequestReview.Status.APPROVED
					&& review.getStatus() != PullRequestReview.Status.EXCLUDED) { 
				return false;
			}
		}
		return true;
	}
	
	public boolean isBuildCommitOutdated() {
		MergePreview mergePreview = checkMergePreview();
		if (getBuildCommitHash() != null) {
			return mergePreview == null
					|| mergePreview.getMergeCommitHash() == null
					|| !mergePreview.getMergeCommitHash().equals(getBuildCommitHash());
		} else {
			return mergePreview != null && mergePreview.getMergeCommitHash() != null;
		}
	}
	
	public boolean isBuildRequirementSatisfied() {
		BuildRequirement requirement = getBuildRequirement();
		if (requirement.isStrictMode() && !requirement.getRequiredJobs().isEmpty() && isBuildCommitOutdated())
			return false;
		
		for (Build build: getCurrentBuilds()) {
			if (requirement.getRequiredJobs().contains(build.getJobName()) && build.getStatus() != Build.Status.SUCCESSFUL)
				return false;
		}
		
		return getCurrentBuilds().stream()
				.map(it -> it.getJobName())
				.collect(Collectors.toSet())
				.containsAll(requirement.getRequiredJobs());
	}
	
	public boolean isSignatureRequirementSatisfied() {
		return getProject().isCommitSignatureRequirementSatisfied(getSubmitter(), 
				getTargetBranch(), getLatestUpdate().getHeadCommit());
	}
	
	@Override
	public Project getAttachmentProject() {
		return targetProject;
	}
	
	@Override
	public String getAttachmentGroup() {
		return uuid;
	}
	
	@Override
	public Project getProject() {
		return getTargetProject();
	}
	
	public EntityReference getReference() {
		return new PullRequestReference(getProject(), getNumber());
	}
	
	public static void push(PullRequest request) {
		stack.get().push(request);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	@Nullable
	public static PullRequest get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				PullRequestAware pullRequestAware = WicketUtils.findInnermost(
						componentContext.getComponent(), PullRequestAware.class);
				if (pullRequestAware != null) 
					return pullRequestAware.getPullRequest();
			}
			return null;
		}
	}
	
	public BuildRequirement getBuildRequirement() {
		if (buildRequirement == null) {
			BranchProtection protection = getTargetProject().getBranchProtection(getTargetBranch(), getSubmitter());
			ObjectId requestHeadId = ObjectId.fromString(getLatestUpdate().getHeadCommitHash());
			var mergeBaseId = getGitService().getMergeBase(
					getProject(), ObjectId.fromString(getLatestUpdate().getTargetHeadCommitHash()),
					getProject(), requestHeadId);
			buildRequirement = protection.getBuildRequirement(getTargetProject(), mergeBaseId,
					requestHeadId, new HashMap<>());
		}
		return buildRequirement;
	}
	
	@Nullable
	public String checkMergeCondition() {
    	if (!isOpen())
    		return _T("Pull request already closed");
    	String checkError = getCheckError();
    	if (checkError != null)
    		return MessageFormat.format(_T("Pull request is in error: {0}"), checkError);
		if (isWorkInProgress())
			return _T("Pull request is work in progress");
			
    	MergePreview preview = checkMergePreview();
    	if (preview == null)
    		return _T("Merge preview not calculated yet");
    	if (preview.getMergeCommitHash() == null)
    		return _T("There are merge conflicts");
    	if (!isAllReviewsApproved())
    		return _T("Waiting for approvals");
    	if (!isBuildRequirementSatisfied())
    		return _T("Some required builds not passed");
    	if (!isSignatureRequirementSatisfied())
    		return _T("No valid signature for head commit");
		var commitMessageError = checkCommitMessages();
		if (commitMessageError != null)
			return commitMessageError.toString();		
		var violatedFileTypes = getViolatedFileTypes();
		if (!violatedFileTypes.isEmpty())
			return MessageFormat.format(_T("Disallowed file type(s): {0}"), StringUtils.join(violatedFileTypes, ", "));
		return null;
	}
	
	public String getDefaultMergeCommitMessage() {
		if (getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS) {
			return _T("Merges pull request") + " #" + getNumber() + "\n\n" + getTitle();
		} else {
			var commitMessage = getTitle();
			if (getDescription() != null)
				commitMessage += "\n\n" + getDescription();
			commitMessage += "\n\n" + _T("Merges pull request") + " #" + getNumber();
			return commitMessage;
		}
	}

	@Nullable
	public String checkMergeCommitMessage(User user, @Nullable String commitMessage) {
		if (isMergeCommitMessageRequired()) {
			var branchProtection = getProject().getBranchProtection(getTargetBranch(), user);
			if (commitMessage == null)
				commitMessage = getDefaultMergeCommitMessage();
			return branchProtection.checkCommitMessage(commitMessage, getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS);
		}
		return null;
	}

	@Nullable
	public String checkReopenCondition() {
		if (isOpen())
			return _T("Pull request already opened");
		if (getTarget().getObjectName(false) == null)
			return _T("Target branch no longer exists");
		if (getSourceProject() == null)
			return _T("Source project no longer exists");
		if (getSource().getObjectName(false) == null)
			return _T("Source branch no longer exists");
		PullRequestService manager = OneDev.getInstance(PullRequestService.class);
		PullRequest request = manager.findEffective(getTarget(), getSource());
		if (request != null) {
			if (request.isOpen())
				return _T("Another pull request already open for this change");
			else
				return _T("Change already merged");
		}
		
		if (getGitService().isMergedInto(getTargetProject(), null, 
				getSource().getObjectId(), getTarget().getObjectId())) {
			return _T("Source branch already merged into target branch");
		}
		
		return null;
	}
	
	@Nullable
	public String checkDeleteSourceBranchCondition() {
		if (!isMerged())
			return _T("Pull request not merged");
		if (getSourceProject() == null)
			return _T("Source project no longer exists");
		if (getSource().getObjectName(false) == null)
			return _T("Source branch no longer exists");
		if (getSource().isDefault())
			return _T("Source branch is default branch");
		MergePreview preview = checkMergePreview();
		if (preview == null)
			return _T("Merge preview not calculated yet");
		if (!getSource().getObjectName().equals(preview.getHeadCommitHash()) 
				&& !getSource().getObjectName().equals(preview.getMergeCommitHash())) {
			return _T("Change not updated yet");
		}
		PullRequestService manager = OneDev.getInstance(PullRequestService.class);
		if (!manager.queryOpenTo(getSource()).isEmpty())
			return _T("Some other pull requests are opening to this branch");
		return null;
	}
	
	@Nullable
	public String checkRestoreSourceBranchCondition() {
		if (getSourceProject() == null)
			return _T("Source project no longer exists");
		if (getSource().getObjectName(false) != null)
			return _T("Source branch already exists");
		return null;
	}

	public boolean isMergeCommitMessageRequired() {
		if (mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS || mergeStrategy == CREATE_MERGE_COMMIT)
			return true;
		if (mergeStrategy == CREATE_MERGE_COMMIT_IF_NECESSARY) {
			var preview = checkMergePreview();
			if (preview != null && preview.getMergeCommitHash() != null) {
				return !preview.getMergeCommitHash().equals(preview.getHeadCommitHash());
			} else {
				ObjectId requestHead = getLatestUpdate().getHeadCommit().copy();
				ObjectId targetHead = getTarget().getObjectId();
				return !getGitService().isMergedInto(getProject(), null, targetHead, requestHead);
			}
		}
		return false;
	}

	@Nullable
	public CommitMessageError checkCommitMessages() {
		if (commitMessageErrorOpt == null) {
			CommitMessageError error = null;
			if (mergeStrategy != SQUASH_SOURCE_BRANCH_COMMITS) {
				error = getProject().checkCommitMessages(getTargetBranch(), getSubmitter(),
						getBaseCommit().copy(), getLatestUpdate().getHeadCommit().copy(), new HashMap<>());
			}
			if (error == null && autoMerge.isEnabled()) {
				var system = OneDev.getInstance(UserService.class).getSystem();
				var errorMessage = checkMergeCommitMessage(system, autoMerge.getCommitMessage());
				if (errorMessage != null)
					error = new CommitMessageError(null, errorMessage);
			}
			commitMessageErrorOpt = Optional.ofNullable(error);
		}
		return commitMessageErrorOpt.orElse(null);
	}

	public Collection<String> getViolatedFileTypes() {
		if (violatedFileTypesOpt == null) {
			var violatedFileTypes = getProject().getViolatedFileTypes(getTargetBranch(), getSubmitter(),
					getBaseCommit().copy(), getLatestUpdate().getHeadCommit().copy(), new HashMap<>());
			violatedFileTypesOpt = Optional.of(violatedFileTypes);
		}
		return violatedFileTypesOpt.get();
	}

	public static String getSerialLockName(Long requestId) {
		return "pull-request-" + requestId + "-serial";
	}
	
	public String getSummary(@Nullable Project currentProject) {
		return Emojis.getInstance().apply(getTitle()) + " (" + getReference().toString(currentProject)+ ")";
	}
	
	public int getThumbsUpCount() {
		return thumbsUpCount;
	}

	public void setThumbsUpCount(int thumbsUpCount) {
		this.thumbsUpCount = thumbsUpCount;
	}

	public int getThumbsDownCount() {
		return thumbsDownCount;
	}

	public void setThumbsDownCount(int thumbsDownCount) {
		this.thumbsDownCount = thumbsDownCount;
	}

	public int getSmileCount() {
		return smileCount;
	}

	public void setSmileCount(int smileCount) {
		this.smileCount = smileCount;
	}

	public int getTadaCount() {
		return tadaCount;
	}

	public void setTadaCount(int tadaCount) {
		this.tadaCount = tadaCount;
	}

	public int getConfusedCount() {
		return confusedCount;
	}

	public void setConfusedCount(int confusedCount) {
		this.confusedCount = confusedCount;
	}

	public int getHeartCount() {
		return heartCount;
	}

	public void setHeartCount(int heartCount) {
		this.heartCount = heartCount;
	}

	public int getRocketCount() {
		return rocketCount;
	}

	public void setRocketCount(int rocketCount) {
		this.rocketCount = rocketCount;
	}

	public int getEyesCount() {
		return eyesCount;
	}

	public void setEyesCount(int eyesCount) {
		this.eyesCount = eyesCount;
	}
	
	public boolean isWorkInProgress() {
		var lowerTitle = title.toLowerCase();
		return lowerTitle.startsWith("wip") || lowerTitle.startsWith("[wip]");
	}

	// Remove issue number suffix if there is any
	public void cleanTitle() {
        var cleanedTitle = title.replaceFirst("\\s*\\([a-zA-Z]+-\\d+\\)$", "");
        cleanedTitle = cleanedTitle.replaceFirst("\\s*\\(#\\d+\\)$", "").trim();
        if (cleanedTitle.length() != 0)
            title = cleanedTitle;
	}

	public void generateTitleAndDescriptionIfEmpty() {
		if (title == null) {
			var commits = getLatestUpdate().getCommits();
			if (commits.size() == 1) {
				title = commits.get(0).getShortMessage();
				cleanTitle();
			} else {
				title = getSource().getBranch().toLowerCase();
				boolean wip = false;
				if (title.startsWith("wip-") || title.startsWith("wip_") || title.startsWith("wip/")) {
					wip = true;
					title = title.substring(4);
				}
				var commitTypes = CONVENTIONAL_COMMIT_TYPES;
				var branchProtection = getProject().getBranchProtection(getTargetBranch(), getSubmitter());
				if (branchProtection.isEnforceConventionalCommits() && !branchProtection.getCommitTypes().isEmpty()) {
					commitTypes = branchProtection.getCommitTypes();
				}
				boolean found = false;
				for (var commitType: commitTypes) {
					if (title.startsWith(commitType + "-") || title.startsWith(commitType + "_") || title.startsWith(commitType + "/")) {
						title = commitType + ": " + StringUtils.capitalize(title.substring(commitType.length() + 1).replace('-', ' ').replace('_', ' ').replace('/', ' '));
						found = true;
						break;
					}
				}
				if (!found) {
					title = StringUtils.capitalize(title.replace('-', ' ').replace('_', ' ').replace('/', ' '));
				}
				if (wip)
					title = "[WIP] " + title;
			}
		}
		if (description == null) {
			var commits = getLatestUpdate().getCommits();
			if (commits.size() == 1) {
				var commit = commits.get(0);
				var shortMessage = commits.get(0).getShortMessage();
				description = commit.getFullMessage().substring(shortMessage.length()).trim();
				if (description.length() == 0)
					description = null;
			}
		}		
	}

}
