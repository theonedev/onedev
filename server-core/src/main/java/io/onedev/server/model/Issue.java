package io.onedev.server.model;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static io.onedev.server.model.AbstractEntity.PROP_NUMBER;
import static io.onedev.server.model.Issue.PROP_BOARD_POSITION;
import static io.onedev.server.model.Issue.PROP_COMMENT_COUNT;
import static io.onedev.server.model.Issue.PROP_CONFUSED_COUNT;
import static io.onedev.server.model.Issue.PROP_EYES_COUNT;
import static io.onedev.server.model.Issue.PROP_HEART_COUNT;
import static io.onedev.server.model.Issue.PROP_ROCKET_COUNT;
import static io.onedev.server.model.Issue.PROP_SMILE_COUNT;
import static io.onedev.server.model.Issue.PROP_STATE;
import static io.onedev.server.model.Issue.PROP_SUBMIT_DATE;
import static io.onedev.server.model.Issue.PROP_TADA_COUNT;
import static io.onedev.server.model.Issue.PROP_THUMBS_DOWN_COUNT;
import static io.onedev.server.model.Issue.PROP_THUMBS_UP_COUNT;
import static io.onedev.server.model.Issue.PROP_TITLE;
import static io.onedev.server.model.Issue.PROP_UUID;
import static io.onedev.server.model.Issue.PROP_VOTE_COUNT;
import static io.onedev.server.model.IssueSchedule.NAME_ITERATION;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.mail.internet.InternetAddress;
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
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.attachment.AttachmentStorageSupport;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.ProjectBelonging;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.IssueSortField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.facade.IssueFacade;
import io.onedev.server.web.UrlService;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.component.iteration.burndown.BurndownIndicators;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.util.IssueAware;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.xodus.CommitInfoService;
import io.onedev.server.xodus.PullRequestInfoService;
import io.onedev.server.xodus.VisitInfoService;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList=PROP_STATE), 
				@Index(columnList=PROP_UUID), @Index(columnList = PROP_BOARD_POSITION),
				@Index(columnList=PROP_TITLE), @Index(columnList=PROP_NUMBER), 
				@Index(columnList=PROP_SUBMIT_DATE), @Index(columnList="o_submitter_id"), 
				@Index(columnList=PROP_VOTE_COUNT), @Index(columnList=PROP_COMMENT_COUNT), 
				@Index(columnList=PROP_THUMBS_UP_COUNT), @Index(columnList=PROP_THUMBS_DOWN_COUNT),
				@Index(columnList=PROP_SMILE_COUNT), @Index(columnList=PROP_TADA_COUNT),
				@Index(columnList=PROP_CONFUSED_COUNT), @Index(columnList=PROP_HEART_COUNT),
				@Index(columnList=PROP_ROCKET_COUNT), @Index(columnList=PROP_EYES_COUNT),
				@Index(columnList= LastActivity.COLUMN_DATE), @Index(columnList="o_numberScope_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", PROP_NUMBER})})
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate
@Editable
public class Issue extends ProjectBelonging implements AttachmentStorageSupport {

	private static final long serialVersionUID = 1L;
	
	public static final int MAX_TITLE_LEN = 255;
	
	public static final int MAX_DESCRIPTION_LEN = 100000;

	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";
	
	public static final String NAME_STATE = "State";
	
	public static final String PROP_STATE = "state";
	
	public static final String PROP_STATE_ORDINAL = "stateOrdinal";
	
	public static final String NAME_TITLE = "Title";
	
	public static final String PROP_TITLE = "title";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String NAME_COMMENT = "Comment";
	
	public static final String NAME_SUBMITTER = "Submitter";
	
	public static final String PROP_SUBMITTER = "submitter";
	
	public static final String NAME_SUBMIT_DATE = "Submit Date";
	
	public static final String PROP_SUBMIT_DATE = "submitDate";
	
	public static final String NAME_VOTE_COUNT = "Vote Count";
	
	public static final String PROP_VOTE_COUNT = "voteCount";
	
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

	public static final String NAME_LAST_ACTIVITY_DATE = "Last Activity Date";
	
	public static final String PROP_LAST_ACTIVITY = "lastActivity";
	
	public static final String PROP_FIELDS = "fields";
	
	public static final String PROP_SCHEDULES = "schedules";
	
	public static final String PROP_UUID = "uuid";
		
	public static final String PROP_CONFIDENTIAL = "confidential";
	
	public static final String PROP_PIN_DATE = "pinDate";

	public static final String NAME_ESTIMATED_TIME = "Estimated Time";
	
	public static final String NAME_SPENT_TIME = "Spent Time";

	public static final String PROP_TOTAL_ESTIMATED_TIME = "totalEstimatedTime";

	public static final String PROP_TOTAL_SPENT_TIME = "totalSpentTime";
	
	public static final String PROP_OWN_ESTIMATED_TIME = "ownEstimatedTime";
	
	public static final String PROP_OWN_SPENT_TIME = "ownSpentTime";
	
	public static final String NAME_PROGRESS = "Progress";
	
	public static final String PROP_PROGRESS = "progress";

	public static final String NAME_BOARD_POSITION = "Board Position";

	public static final String PROP_BOARD_POSITION = "boardPosition";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_PROJECT, NAME_NUMBER, NAME_STATE, NAME_TITLE, NAME_SUBMITTER, 
			NAME_DESCRIPTION, NAME_COMMENT, NAME_SUBMIT_DATE, NAME_LAST_ACTIVITY_DATE, 
			NAME_VOTE_COUNT, NAME_COMMENT_COUNT, NAME_ITERATION,
			NAME_THUMBS_UP_COUNT, NAME_THUMBS_DOWN_COUNT, NAME_SMILE_COUNT, NAME_TADA_COUNT, 
			NAME_CONFUSED_COUNT, NAME_HEART_COUNT, NAME_ROCKET_COUNT, NAME_EYES_COUNT,
			NAME_ESTIMATED_TIME, NAME_SPENT_TIME, NAME_PROGRESS,
			BurndownIndicators.ISSUE_COUNT, BurndownIndicators.REMAINING_TIME);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_NUMBER, NAME_STATE, NAME_TITLE, NAME_DESCRIPTION,
			NAME_ESTIMATED_TIME, NAME_SPENT_TIME, NAME_PROGRESS,
			NAME_COMMENT, NAME_SUBMIT_DATE, NAME_LAST_ACTIVITY_DATE, NAME_VOTE_COUNT, 
			NAME_COMMENT_COUNT, NAME_ITERATION,
			NAME_THUMBS_UP_COUNT, NAME_THUMBS_DOWN_COUNT, NAME_SMILE_COUNT, NAME_TADA_COUNT, 
			NAME_CONFUSED_COUNT, NAME_HEART_COUNT, NAME_ROCKET_COUNT, NAME_EYES_COUNT);

	public static final Map<String, IssueSortField> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_VOTE_COUNT, new IssueSortField(PROP_VOTE_COUNT, DESCENDING, comparingInt(Issue::getVoteCount)));
		SORT_FIELDS.put(NAME_COMMENT_COUNT, new IssueSortField(PROP_COMMENT_COUNT, DESCENDING, comparingInt(Issue::getCommentCount)));
		SORT_FIELDS.put(NAME_NUMBER, new IssueSortField(PROP_NUMBER, (o1, o2) -> (int)(o1.getNumber() - o2.getNumber())));
		SORT_FIELDS.put(NAME_STATE, new IssueSortField(PROP_STATE_ORDINAL, comparingInt(Issue::getStateOrdinal)));
		SORT_FIELDS.put(NAME_SUBMIT_DATE, new IssueSortField(PROP_SUBMIT_DATE, DESCENDING, comparing(Issue::getSubmitDate)));
		SORT_FIELDS.put(NAME_PROJECT, new IssueSortField(PROP_PROJECT, comparing(o -> o.getProject().getId())));
		SORT_FIELDS.put(NAME_LAST_ACTIVITY_DATE, new IssueSortField(PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE, DESCENDING, comparing(o -> o.getLastActivity().getDate())));
		SORT_FIELDS.put(NAME_ESTIMATED_TIME, new IssueSortField(PROP_TOTAL_ESTIMATED_TIME, comparingInt(Issue::getTotalEstimatedTime)));
		SORT_FIELDS.put(NAME_SPENT_TIME, new IssueSortField(PROP_TOTAL_SPENT_TIME, comparingInt(Issue::getTotalSpentTime)));
		SORT_FIELDS.put(NAME_PROGRESS, new IssueSortField(PROP_PROGRESS, comparingInt(Issue::getProgress)));
		SORT_FIELDS.put(NAME_BOARD_POSITION, new IssueSortField(PROP_BOARD_POSITION, comparingInt(Issue::getBoardPosition)));
		SORT_FIELDS.put(NAME_THUMBS_UP_COUNT, new IssueSortField(PROP_THUMBS_UP_COUNT, DESCENDING, comparingInt(Issue::getThumbsUpCount)));
		SORT_FIELDS.put(NAME_THUMBS_DOWN_COUNT, new IssueSortField(PROP_THUMBS_DOWN_COUNT, DESCENDING, comparingInt(Issue::getThumbsDownCount)));
		SORT_FIELDS.put(NAME_SMILE_COUNT, new IssueSortField(PROP_SMILE_COUNT, DESCENDING, comparingInt(Issue::getSmileCount)));
		SORT_FIELDS.put(NAME_TADA_COUNT, new IssueSortField(PROP_TADA_COUNT, DESCENDING, comparingInt(Issue::getTadaCount)));
		SORT_FIELDS.put(NAME_CONFUSED_COUNT, new IssueSortField(PROP_CONFUSED_COUNT, DESCENDING, comparingInt(Issue::getConfusedCount)));
		SORT_FIELDS.put(NAME_HEART_COUNT, new IssueSortField(PROP_HEART_COUNT, DESCENDING, comparingInt(Issue::getHeartCount)));
		SORT_FIELDS.put(NAME_ROCKET_COUNT, new IssueSortField(PROP_ROCKET_COUNT, DESCENDING, comparingInt(Issue::getRocketCount)));
		SORT_FIELDS.put(NAME_EYES_COUNT, new IssueSortField(PROP_EYES_COUNT, DESCENDING, comparingInt(Issue::getEyesCount)));
	}
	
	private static ThreadLocal<Stack<Issue>> stack = ThreadLocal.withInitial(() -> new Stack<>());
	
	@Column(nullable=false)
	private String state;
	
	@Column(nullable=false)
	private int stateOrdinal;
	
	@Column(nullable=false, length=MAX_TITLE_LEN)
	private String title;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	@Api(description = "May be empty")
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project numberScope;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueSchedule> schedules = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<Build> builds = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueDescriptionRevision> descriptionRevisions = new ArrayList<>();
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User submitter;
	
	@Column(nullable=false)
	private Date submitDate = new Date();

	@Lob
	private InternetAddress onBehalfOf;

	private int voteCount;
	
	private int commentCount;

	private int thumbsUpCount;

	private int thumbsDownCount;

	private int smileCount;

	private int tadaCount;

	private int confusedCount;
	
	private int heartCount;
	
	private int rocketCount;

	private int eyesCount;
	
	private int totalEstimatedTime;
	
	private int totalSpentTime;
	
	private int ownEstimatedTime;
	
	private int ownSpentTime;
	
	private int progress = -1;

	@JsonProperty(access = READ_ONLY)
	private int descriptionRevisionCount;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();

	private long number;
	
	private String messageId;
	
	@Embedded
	private LastActivity lastActivity;
		
	private boolean confidential;
	
	private Date pinDate;
	
	private int boardPosition;
	
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<InternetAddress> externalParticipants = new LinkedHashSet<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueField> fields = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueStateHistory> stateHistories = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueComment> comments = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueChange> changes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueVote> votes = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueWork> works = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<Stopwatch> stopwatches = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueWatch> watches = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueMention> mentions = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<IssueAuthorization> authorizations = new ArrayList<>();
	
	@OneToMany(mappedBy=IssueLink.PROP_TARGET, cascade=CascadeType.REMOVE)
	private Collection<IssueLink> sourceLinks = new ArrayList<>();
	
	@OneToMany(mappedBy=IssueLink.PROP_SOURCE, cascade=CascadeType.REMOVE)
	private Collection<IssueLink> targetLinks = new ArrayList<>();

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueReaction> reactions = new ArrayList<>();
	
	private transient List<ProjectScopedCommit> headFixCommits;

	private transient List<ProjectScopedCommit> fixCommits;
	
	private transient List<PullRequest> pullRequests;
	
	private transient Map<String, Input> fieldInputs;
	
	private transient Collection<User> participants;
	
	private transient Collection<User> authorizedUsers;
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
		stateOrdinal = getIssueSetting().getStateOrdinal(state);
		if (stateOrdinal == -1)
			throw new ExplicitException("Undefined state: " + state);
	}

	public int getStateOrdinal() {
		return stateOrdinal;
	}

	public void setStateOrdinal(int stateOrdinal) {
		this.stateOrdinal = stateOrdinal;
	}

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

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public Date getPinDate() {
		return pinDate;
	}

	public void setPinDate(Date pinDate) {
		this.pinDate = pinDate;
	}

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

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	@Nullable
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getThreadingReferences() {
		var threadingReferences = "<" + getUUID() + "@onedev>";
		if (getMessageId() != null)
			threadingReferences = getMessageId() + " " + threadingReferences;
		return threadingReferences;
	}

	public EntityReference getReference() {
		return new IssueReference(getProject(), getNumber());
	}

	/*
	 * Do not remove this method. Used in email notification templates
	 * @return
	 */
	public String getFQN() {
		return getReference().toString();
	}
	
	public long getNumber() {
		return number;
	}
	
	public void setNumber(long number) {
		this.number = number;
	}

	public LastActivity getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(LastActivity lastActivity) {
		this.lastActivity = lastActivity;
	}

	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	@Nullable
	public InternetAddress getOnBehalfOf() {
		return onBehalfOf;
	}

	public void setOnBehalfOf(@Nullable InternetAddress onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
	}

	public Collection<IssueSchedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(Collection<IssueSchedule> schedules) {
		this.schedules = schedules;
	}

	public Collection<IssueComment> getComments() {
		return comments;
	}

	public void setComments(Collection<IssueComment> comments) {
		this.comments = comments;
	}
	public Collection<IssueReaction> getReactions() {
		return reactions;
	}

	public void setReactions(Collection<IssueReaction> reactions) {
		this.reactions = reactions;
	}

	public Collection<IssueChange> getChanges() {
		return changes;
	}

	public void setChanges(Collection<IssueChange> changes) {
		this.changes = changes;
	}

	public Collection<IssueVote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<IssueVote> votes) {
		this.votes = votes;
	}

	public Collection<IssueDescriptionRevision> getDescriptionRevisions() {
		return descriptionRevisions;
	}

	public void setDescriptionRevisions(Collection<IssueDescriptionRevision> descriptionRevisions) {
		this.descriptionRevisions = descriptionRevisions;
	}

	public int getDescriptionRevisionCount() {
		return descriptionRevisionCount;
	}

	public void setDescriptionRevisionCount(int descriptionRevisionCount) {
		this.descriptionRevisionCount = descriptionRevisionCount;
	}

	@Override
	public Collection<IssueWatch> getWatches() {
		return watches;
	}

	public void setWatches(Collection<IssueWatch> watches) {
		this.watches = watches;
	}
	
	@Override
	public EntityWatch getWatch(User user, boolean createIfNotExist) {
		if (createIfNotExist) {
			IssueWatch watch = (IssueWatch) super.getWatch(user, false);
			if (watch == null) {
				watch = new IssueWatch();
				watch.setIssue(this);
				watch.setUser(user);
				getWatches().add(watch);
			}
			return watch;
		} else {
			return super.getWatch(user, false);
		}
	}

	public Collection<IssueWork> getWorks() {
		return works;
	}

	public void setWorks(Collection<IssueWork> works) {
		this.works = works;
	}

	public Collection<Stopwatch> getStopwatches() {
		return stopwatches;
	}

	public void setStopwatches(Collection<Stopwatch> stopwatches) {
		this.stopwatches = stopwatches;
	}

	public Collection<IssueMention> getMentions() {
		return mentions;
	}

	public void setMentions(Collection<IssueMention> mentions) {
		this.mentions = mentions;
	}

	public Collection<IssueAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<IssueAuthorization> authorizations) {
		this.authorizations = authorizations;
	}
	
	public Collection<User> getAuthorizedUsers() {
		if (authorizedUsers == null)
			authorizedUsers = getAuthorizations().stream().map(it->it.getUser()).collect(Collectors.toSet());
		return authorizedUsers;
	}

	public int getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
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

	public int getTotalEstimatedTime() {
		return totalEstimatedTime;
	}

	public void setTotalEstimatedTime(int totalEstimatedTime) {
		this.totalEstimatedTime = totalEstimatedTime;
		calcProgress();
	}
	
	private void calcProgress() {
		if (totalEstimatedTime != 0)
			progress = totalSpentTime * 100 / totalEstimatedTime;
		else
			progress = -1;
	}

	public int getTotalSpentTime() {
		return totalSpentTime;
	}

	public void setTotalSpentTime(int totalSpentTime) {
		this.totalSpentTime = totalSpentTime;
		calcProgress();
	}

	public int getOwnEstimatedTime() {
		return ownEstimatedTime;
	}

	public int getOwnSpentTime() {
		return ownSpentTime;
	}

	public void setOwnSpentTime(int ownSpentTime) {
		this.ownSpentTime = ownSpentTime;
	}

	public void setOwnEstimatedTime(int ownEstimatedTime) {
		this.ownEstimatedTime = ownEstimatedTime;
	}

	public int getProgress() {
		return progress;
	}

	public int getBoardPosition() {
		return boardPosition;
	}

	public void setBoardPosition(int boardPosition) {
		this.boardPosition = boardPosition;
	}

	public LinkedHashSet<InternetAddress> getExternalParticipants() {
		return externalParticipants;
	}

	public void setExternalParticipants(LinkedHashSet<InternetAddress> externalParticipants) {
		this.externalParticipants = externalParticipants;
	}

	public Collection<IssueField> getFields() {
		return fields;
	}

	public void setFields(Collection<IssueField> fields) {
		this.fields = fields;
	}

	public Collection<IssueStateHistory> getStateHistories() {
		return stateHistories;
	}

	public void setStateHistories(Collection<IssueStateHistory> stateHistories) {
		this.stateHistories = stateHistories;
	}

	public Collection<IssueLink> getSourceLinks() {
		return sourceLinks;
	}
	
	public void setSourceLinks(Collection<IssueLink> sourceLinks) {
		this.sourceLinks = sourceLinks;
	}

	public Collection<IssueLink> getTargetLinks() {
		return targetLinks;
	}

	public void setTargetLinks(Collection<IssueLink> targetLinks) {
		this.targetLinks = targetLinks;
	}
	
	public Collection<IssueLink> getLinks() {
		var links = new ArrayList<IssueLink>();
		links.addAll(sourceLinks);
		links.addAll(targetLinks);
		return links;
	}
	
	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getAuthUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitInfoService.class).getIssueVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public Collection<String> getFieldNames() {
		return getFields().stream().map(IssueField::getName).collect(Collectors.toSet());
	}
	
	public Map<String, Input> getFieldInputs() {
		if (fieldInputs == null) {
			fieldInputs = new LinkedHashMap<>();
	
			Map<String, List<IssueField>> fieldMap = new HashMap<>(); 
			for (IssueField field: getFields()) 
				fieldMap.computeIfAbsent(field.getName(), k -> new ArrayList<>()).add(field);
			for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
				String fieldName = fieldSpec.getName();
				List<IssueField> fields = fieldMap.get(fieldName);
				if (fields != null) {
					Collections.sort(fields, (Comparator<IssueField>) (o1, o2) -> {
						long result = o1.getOrdinal() - o2.getOrdinal();
						if (result > 0)
							return 1;
						else if (result < 0)
							return -1;
						else if (o1.getValue() != null && o2.getValue() != null)
							return o1.getValue().compareTo(o2.getValue());
						else if (o1.getValue() != null && o2.getValue() == null)
							return -1;
						else if (o1.getValue() == null && o2.getValue() != null)
							return 1;
						else
							return 0;
					});
					String type = fields.iterator().next().getType();
					List<String> values = new ArrayList<>();
					for (IssueField field: fields) {
						if (field.getValue() != null)
							values.add(field.getValue());
					}
					if (!fieldSpec.isAllowMultiple() && values.size() > 1) 
						values = Lists.newArrayList(values.iterator().next());
					fieldInputs.put(fieldName, new Input(fieldName, type, values));
				}
			}
		}
		return fieldInputs;
	}
	
	public static String getDetailChangeObservable(Long issueId) {
		return Issue.class.getName() + ":" + issueId;
	}
	
	public static String getListChangeObservable(Long projectId, Long issueId) {
		return Issue.class.getName() + ":list:" + projectId + ":" + issueId;
	}

	public static String getListChangeObservable(Long projectId) {
		return Issue.class.getName() + ":list:" + projectId;
	}
	
	public Collection<String> getChangeObservables(boolean withList) {
		Collection<String> observables = Sets.newHashSet(getDetailChangeObservable(getId()));
		if (withList)
			observables.add(getListChangeObservable(getProject().getId(), getId()));
		return observables;
	}

	public IssueFacade getFacade() {
		return new IssueFacade(getId(), getProject().getId(), getNumber());
	}

	@Nullable
	public Object getFieldValue(String fieldName) {
		Input input = getFieldInputs().get(fieldName);
		if (input != null) 
			return input.getTypedValue(getIssueSetting().getFieldSpec(fieldName));
		else
			return null;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingService.class).getIssueSetting();
	}
	
	public long getFieldOrdinal(String fieldName, String fieldValue) {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingService.class).getIssueSetting();
		FieldSpec fieldSpec = issueSetting.getFieldSpec(fieldName);
		if (fieldSpec != null) 
			return fieldSpec.getOrdinal(fieldValue);
		else 
			return -1;
	}
	
	public Serializable getFieldBean(Class<?> fieldBeanClass, boolean withDefaultValue) {
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBeanClass);
		Serializable fieldBean = (Serializable) beanDescriptor.newBeanInstance();

		for (List<PropertyDescriptor> groupProperties: beanDescriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				Input input = getFieldInputs().get(property.getDisplayName());
				if (input != null) {
					FieldSpec fieldSpec = getIssueSetting().getFieldSpec(input.getName());
					property.setPropertyValue(fieldBean, input.getTypedValue(fieldSpec));
				} else if (!withDefaultValue) {
					property.setPropertyValue(fieldBean, null);
				}
			}
		}
		return fieldBean;
	}
	
	public void removeFields(Collection<String> fieldNames) {
		for (Iterator<IssueField> it = getFields().iterator(); it.hasNext();) {
			if (fieldNames.contains(it.next().getName()))
				it.remove();
		}
		fieldInputs = null;
	}
	
	public void setFieldValues(Map<String, Object> fieldValues) {
		for (Map.Entry<String, Object> entry: fieldValues.entrySet())
			setFieldValue(entry.getKey(), entry.getValue());
	}
	
	public void setFieldValue(String fieldName, @Nullable Object fieldValue) {
		for (Iterator<IssueField> it = getFields().iterator(); it.hasNext();) {
			if (fieldName.equals(it.next().getName()))
				it.remove();
		}
		
		FieldSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec != null) {
			List<String> strings = fieldSpec.convertToStrings(fieldValue);
			if (!strings.isEmpty()) {
				for (String string: strings) {
					IssueField field = new IssueField();
					field.setIssue(this);
					field.setName(fieldName);
					field.setOrdinal(getFieldOrdinal(fieldName, string));
					field.setType(fieldSpec.getType());
					field.setValue(string);
					getFields().add(field);
				}
			} else {
				IssueField field = new IssueField();
				field.setIssue(this);
				field.setName(fieldName);
				field.setOrdinal(getFieldOrdinal(fieldName, null));
				field.setType(fieldSpec.getType());
				getFields().add(field);
			}
		}
		fieldInputs = null;
	}

	public boolean isFieldVisible(String fieldName) {
		return isFieldVisible(fieldName, Sets.newHashSet());
	}

	public void addMissingFields(Collection<String> fieldNames) {
		Project.push(getProject());
		try {
			var fieldBean = FieldUtils.getFieldBeanClass().getConstructor().newInstance();
			var existingFieldNames = getFieldNames();
			var fieldValues = FieldUtils.getFieldValues(null, fieldBean, fieldNames);
			for (var entry: fieldValues.entrySet()) {
				if (!existingFieldNames.contains(entry.getKey())) 
					setFieldValue(entry.getKey(), entry.getValue());
			}			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		} finally {
			Project.pop();
		}
	}

	@SuppressWarnings("unchecked")
	public void validateFields() {
		Project.push(getProject());
		try {
			var invalidFields = new HashMap<String, String>();
			for (var fieldName: getFieldNames()) {
				var field = getIssueSetting().getFieldSpec(fieldName);
				if (field != null) {
					var value = getFieldValue(fieldName);
					if (value != null && (!(value instanceof Collection) || !((Collection<?>) value).isEmpty())) {
						if (field instanceof ChoiceField) {
							var values = new ArrayList<String>();
							if (value instanceof Collection) 
								values.addAll((Collection<String>) value);
							else 
								values.add((String) value);
							var choiceField = (ChoiceField) field;
							var possibleValues = choiceField.getPossibleValues();
							if (!possibleValues.containsAll(values)) {
								if (field.isAllowMultiple())
									invalidFields.put(fieldName, "value should be one or more of: " + String.join(", ", possibleValues));
								else
									invalidFields.put(fieldName, "value should be one of: " + String.join(", ", possibleValues));
							}
						}	
					} else if (!field.isAllowEmpty() && isFieldVisible(fieldName) 
								&& SecurityUtils.canEditIssueField(getProject(), fieldName)) {
						if (field instanceof ChoiceField) {
							var choiceField = (ChoiceField) field;
							var possibleValues = choiceField.getPossibleValues();
							if (field.isAllowMultiple())
								invalidFields.put(fieldName, "value must not be empty, and should take one or more of: " + String.join(", ", possibleValues));
							else
								invalidFields.put(fieldName, "value must not be empty, and should take one of: " + String.join(", ", possibleValues));
						} else {
							invalidFields.put(fieldName, "value must not be empty");
						}
					}
				}
			}
			if (invalidFields.size() > 0) {
				var fieldErrors = new ArrayList<String>();
				for (var entry: invalidFields.entrySet()) {
					fieldErrors.add(entry.getKey() + " (" + entry.getValue() + ")");
				}
				throw new ValidationException("Invalid fields: " + String.join(", ", fieldErrors));
			}
		} finally {
			Project.pop();
		}
	}
	
	public List<User> getParticipants() {
		if (participants == null) {
			participants = new LinkedHashSet<>();
			participants.add(getSubmitter());
			UserService userService = OneDev.getInstance(UserService.class);
			for (IssueField field: getFields()) {
				if (field.getType().equals(InputSpec.USER)) {
					if (field.getValue() != null) {
						User user = userService.findByName(field.getValue());
						if (user != null)
							participants.add(user);
					}
				} else if (field.getType().equals(InputSpec.GROUP)) {
					if (field.getValue() != null) {
						Group group = OneDev.getInstance(GroupService.class).find(field.getValue());
						if (group != null)
							participants.addAll(group.getMembers());
					}
				}
			}
			for (IssueComment comment: getComments()) 
				participants.add(comment.getUser());
			for (IssueChange change: getChanges()) {
				if (change.getUser() != null)
					participants.add(change.getUser());
			}
			participants.remove(userService.getSystem());
			participants.remove(userService.getUnknown());
		}
		return new ArrayList<>(participants);
	}
	
	private boolean isFieldVisible(String fieldName, Set<String> checkedFieldNames) {
		if (!checkedFieldNames.add(fieldName))
			return false;
		
		FieldSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec != null) {
			if (fieldSpec.getShowCondition() != null) {
				Input dependentInput = getFieldInputs().get(fieldSpec.getShowCondition().getInputName());
				if (dependentInput != null) {
					if (fieldSpec.getShowCondition().getValueMatcher().matches(dependentInput.getValues()))
						return isFieldVisible(dependentInput.getName(), checkedFieldNames);
					else
						return false;
				} else {
					return false;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	public List<PullRequest> getPullRequests() {
		if (pullRequests == null) {
			pullRequests = new ArrayList<>();

			PullRequestInfoService infoManager = OneDev.getInstance(PullRequestInfoService.class);
			Collection<Long> pullRequestIds = new HashSet<>();
			for (ProjectScopedCommit commit: getFixCommits(false)) 
				pullRequestIds.addAll(infoManager.getPullRequestIds(commit.getProject(), commit.getCommitId()));		
			
			for (Long requestId: pullRequestIds) {
				PullRequest request = OneDev.getInstance(PullRequestService.class).get(requestId);
				if (request != null && !pullRequests.contains(request))
					pullRequests.add(request);
			}
			Collections.sort(pullRequests, (Comparator<PullRequest>) (o1, o2) -> o2.getId().compareTo(o1.getId()));
		}
		return pullRequests;
	}
	
	private List<ProjectScopedCommit> readFixCommits(boolean headOnly) {
		var fixCommits = new ArrayList<ProjectScopedCommit>();
		CommitInfoService commitInfoService = OneDev.getInstance(CommitInfoService.class);

		getProject().getTree().stream().filter(Project::isCodeManagement).forEach(it-> {
			for (ObjectId commitId: commitInfoService.getFixCommits(it.getId(), getId(), headOnly)) {
				RevCommit commit = it.getRevCommit(commitId, false);
				if (commit != null)
					fixCommits.add(new ProjectScopedCommit(it, commit.copy()));
			}
		});

		Collections.sort(fixCommits, (Comparator<ProjectScopedCommit>) (o1, o2) -> o2.getRevCommit().getCommitTime() - o1.getRevCommit().getCommitTime());
		return fixCommits;
	}
	
	public List<ProjectScopedCommit> getFixCommits(boolean headOnly) {
		if (headOnly) {
			if (headFixCommits == null)
				headFixCommits = readFixCommits(true);
			return headFixCommits;
		} else {
			if (fixCommits == null)
				fixCommits = readFixCommits(false);
			return fixCommits;
		}
	}

	@Override
	public Project getAttachmentProject() {
		return project;
	}
	
	@Override
	public String getAttachmentGroup() {
		return uuid;
	}
	
	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(this, true);
	}
	
	public Collection<Iteration> getIterations() {
		return getSchedules().stream().map(it->it.getIteration()).collect(Collectors.toList());
	}
	
	@Nullable
	public IssueSchedule addSchedule(Iteration iteration) {
		for (var schedule: getSchedules()) {
			if (schedule.getIteration().equals(iteration))
				return null;
		}
		IssueSchedule schedule = new IssueSchedule();
		schedule.setIssue(this);
		schedule.setIteration(iteration);
		getSchedules().add(schedule);
		return schedule;
	}
	
	@Nullable
	public IssueSchedule removeSchedule(Iteration iteration) {
		for (Iterator<IssueSchedule> it = getSchedules().iterator(); it.hasNext();) {
			IssueSchedule schedule = it.next();
			if (schedule.getIteration().equals(iteration)) {
				it.remove();
				return schedule;
			}
		}
		return null;
	}
		
	@Nullable
	public Issue findLinkedIssue(LinkSpec spec, boolean opposite) {
		if (spec.getOpposite() != null) {
			if (opposite) {
				for (IssueLink link: getSourceLinks()) {
					if (link.getSpec().equals(spec)) 
						return link.getSource();
				}
				return null;
			} else {
				for (IssueLink link: getTargetLinks()) {
					if (link.getSpec().equals(spec)) 
						return link.getTarget();
				}
				return null;
			}
		} else {
			for (IssueLink link: getLinks()) {
				if (link.getSpec().equals(spec)) 
					return link.getLinked(this);
			}
			return null;
		}
	}
	
	public List<Issue> findLinkedIssues(LinkSpec spec, boolean opposite) {
		class IssueComparator implements Comparator<Issue> {

			private final List<EntitySort> sorts;

			IssueComparator(List<EntitySort> sorts) {
				this.sorts = sorts;
			}

			@Override
			public int compare(Issue o1, Issue o2) {
				for (EntitySort sort : sorts) {
					int result = SORT_FIELDS.get(sort.getField()).getComparator().compare(o1, o2);
					if (sort.getDirection() == DESCENDING)
						result *= -1;
					if (result != 0)
						return result;
				}
				return 0;
			}
		}

		if (spec.getOpposite() != null) {
			if (opposite) {
				return getSourceLinks().stream()
						.filter(it->it.getSpec().equals(spec))
						.sorted()
						.map(it->it.getSource())
						.sorted(new IssueComparator(spec.getOpposite().getParsedIssueQuery(getProject()).getSorts()))
						.collect(Collectors.toList());
			} else {
				return getTargetLinks().stream()
						.filter(it->it.getSpec().equals(spec))
						.sorted()
						.map(it->it.getTarget())
						.sorted(new IssueComparator(spec.getParsedIssueQuery(getProject()).getSorts()))
						.collect(Collectors.toList());
			}
		} else {
			return getLinks().stream()
					.filter(it->it.getSpec().equals(spec))
					.sorted()
					.map(it->it.getLinked(this))
					.sorted(new IssueComparator(spec.getParsedIssueQuery(getProject()).getSorts()))
					.collect(Collectors.toList());
		}
	}
	
	public static String getSerialLockName(Long issueId) {
		return "issue-" + issueId + "-serial";
	}
	
	@Nullable
	public static Issue get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				IssueAware issueAware = WicketUtils.findInnermost(componentContext.getComponent(), IssueAware.class);
				if (issueAware != null) 
					return issueAware.getIssue();
			}
			return null;
		}
	}
	
	public static void push(@Nullable Issue issue) {
		stack.get().push(issue);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	public boolean isAggregatingTime(@Nullable String timeAggregationLink) {
		if (timeAggregationLink != null) {
			for (var link : getTargetLinks()) {
				if (link.getSpec().getName().equals(timeAggregationLink)) 
					return true;
			}
			for (var link : getSourceLinks()) {
				if (link.getSpec().getOpposite() != null && link.getSpec().getOpposite().getName().equals(timeAggregationLink)) 
					return true;
			}
		}
		return false;
	}
	
	public String getSummary(@Nullable Project currentProject) {
		return Emojis.getInstance().apply(getTitle()) + " (" + getReference().toString(currentProject)+ ")";		
	}
	
}
