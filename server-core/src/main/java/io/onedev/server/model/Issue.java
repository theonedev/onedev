package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.web.UrlManager;
import io.onedev.server.annotation.Editable;
import io.onedev.server.attachment.AttachmentStorageSupport;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.*;
import io.onedev.server.entityreference.Referenceable;
import io.onedev.server.xodus.CommitInfoManager;
import io.onedev.server.xodus.PullRequestInfoManager;
import io.onedev.server.xodus.VisitInfoManager;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.ProjectBelonging;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.search.entity.SortField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.Input;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.facade.IssueFacade;
import io.onedev.server.web.component.milestone.burndown.BurndownIndicators;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.util.IssueAware;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.server.model.AbstractEntity.PROP_NUMBER;
import static io.onedev.server.model.Issue.*;
import static io.onedev.server.model.IssueSchedule.NAME_MILESTONE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList=PROP_STATE), 
				@Index(columnList=PROP_UUID),
				@Index(columnList=PROP_TITLE), @Index(columnList=PROP_NO_SPACE_TITLE),  
				@Index(columnList=PROP_NUMBER), @Index(columnList=PROP_SUBMIT_DATE), 
				@Index(columnList="o_submitter_id"), @Index(columnList=PROP_VOTE_COUNT), 
				@Index(columnList=PROP_COMMENT_COUNT), @Index(columnList= LastActivity.COLUMN_DATE), 
				@Index(columnList="o_numberScope_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", PROP_NUMBER})})
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate
@Editable
public class Issue extends ProjectBelonging implements Referenceable, AttachmentStorageSupport {

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
	
	public static final String PROP_COMMENTS = "comments";
	
	public static final String PROP_CHANGES = "changes";
	
	public static final String NAME_SUBMITTER = "Submitter";
	
	public static final String PROP_SUBMITTER = "submitter";
	
	public static final String NAME_SUBMIT_DATE = "Submit Date";
	
	public static final String PROP_SUBMIT_DATE = "submitDate";
	
	public static final String NAME_VOTE_COUNT = "Vote Count";
	
	public static final String PROP_VOTE_COUNT = "voteCount";
	
	public static final String NAME_COMMENT_COUNT = "Comment Count";
	
	public static final String PROP_COMMENT_COUNT = "commentCount";
	
	public static final String NAME_LAST_ACTIVITY_DATE = "Last Activity Date";
	
	public static final String PROP_LAST_ACTIVITY = "lastActivity";
	
	public static final String PROP_FIELDS = "fields";
	
	public static final String PROP_SCHEDULES = "schedules";
	
	public static final String PROP_UUID = "uuid";
	
	public static final String PROP_NO_SPACE_TITLE = "noSpaceTitle";
	
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
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_PROJECT, NAME_NUMBER, NAME_STATE, NAME_TITLE, NAME_SUBMITTER, 
			NAME_DESCRIPTION, NAME_COMMENT, NAME_SUBMIT_DATE, NAME_LAST_ACTIVITY_DATE, 
			NAME_VOTE_COUNT, NAME_COMMENT_COUNT, NAME_MILESTONE,
			NAME_ESTIMATED_TIME, NAME_SPENT_TIME, NAME_PROGRESS,
			BurndownIndicators.ISSUE_COUNT, BurndownIndicators.REMAINING_TIME);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_NUMBER, NAME_STATE, NAME_TITLE, NAME_DESCRIPTION,
			NAME_ESTIMATED_TIME, NAME_SPENT_TIME, NAME_PROGRESS,
			NAME_COMMENT, NAME_SUBMIT_DATE, NAME_LAST_ACTIVITY_DATE, NAME_VOTE_COUNT, 
			NAME_COMMENT_COUNT, NAME_MILESTONE);

	public static final Map<String, SortField<Issue>> ORDER_FIELDS = new LinkedHashMap<>();
	
	static {
		ORDER_FIELDS.put(NAME_VOTE_COUNT, new SortField<>(PROP_VOTE_COUNT, comparingInt(Issue::getVoteCount)));
		ORDER_FIELDS.put(NAME_COMMENT_COUNT, new SortField<>(PROP_COMMENT_COUNT, comparingInt(Issue::getCommentCount)));
		ORDER_FIELDS.put(NAME_NUMBER, new SortField<>(PROP_NUMBER, (o1, o2) -> (int)(o1.getNumber() - o2.getNumber())));
		ORDER_FIELDS.put(NAME_STATE, new SortField<>(PROP_STATE_ORDINAL, comparingInt(Issue::getStateOrdinal)));
		ORDER_FIELDS.put(NAME_SUBMIT_DATE, new SortField<>(PROP_SUBMIT_DATE, comparing(Issue::getSubmitDate)));
		ORDER_FIELDS.put(NAME_PROJECT, new SortField<>(PROP_PROJECT, comparing(o -> o.getProject().getId())));
		ORDER_FIELDS.put(NAME_LAST_ACTIVITY_DATE, new SortField<>(PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE, new Comparator<Issue>() {

			@Override
			public int compare(Issue o1, Issue o2) {
				return o1.getLastActivity().getDate().compareTo(o2.getLastActivity().getDate());
			}
			
		}));
		ORDER_FIELDS.put(NAME_ESTIMATED_TIME, new SortField<>(PROP_TOTAL_ESTIMATED_TIME, comparingInt(Issue::getTotalEstimatedTime)));
		ORDER_FIELDS.put(NAME_SPENT_TIME, new SortField<>(PROP_TOTAL_SPENT_TIME, comparingInt(Issue::getTotalSpentTime)));
		ORDER_FIELDS.put(NAME_PROGRESS, new SortField<>(PROP_PROGRESS, comparingInt(Issue::getProgress)));
	}
	
	private static ThreadLocal<Stack<Issue>> stack =  new ThreadLocal<Stack<Issue>>() {

		@Override
		protected Stack<Issue> initialValue() {
			return new Stack<Issue>();
		}
	
	};
	
	@Column(nullable=false)
	private String state;
	
	@Column(nullable=false)
	private int stateOrdinal;
	
	@Column(nullable=false, length=MAX_TITLE_LEN)
	private String title;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	@Api(description = "May be null")
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
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User submitter;
	
	@Column(nullable=false)
	private Date submitDate = new Date();
	
	private int voteCount;
	
	private int commentCount;
	
	private int totalEstimatedTime;
	
	private int totalSpentTime;
	
	private int ownEstimatedTime;
	
	private int ownSpentTime;
	
	private int progress = -1;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();

	private long number;
	
	private String threadingReference;
	
	@Embedded
	private LastActivity lastActivity;
	
	// used for title search in markdown editor
	@Column(nullable=false)
	@JsonIgnore
	private String noSpaceTitle;
	
	private boolean confidential;
	
	private Date pinDate;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueField> fields = new ArrayList<>();
	
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
	
	private transient List<ProjectScopedCommit> commits;
	
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
		noSpaceTitle = StringUtils.deleteWhitespace(this.title);
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
	public String getThreadingReference() {
		return threadingReference;
	}

	public void setThreadingReference(String threadingReference) {
		this.threadingReference = threadingReference;
	}
	
	public String getEffectiveThreadingReference() {
		String threadingReference = getThreadingReference();
		if (threadingReference == null)
			threadingReference = "<" + getUUID() + "@onedev>";
		return threadingReference;
	}
	
	@Override
	public long getNumber() {
		return number;
	}
	
	@Override
	public String getType() {
		return "issue";
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

	public Collection<IssueField> getFields() {
		return fields;
	}

	public void setFields(Collection<IssueField> fields) {
		this.fields = fields;
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
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitInfoManager.class).getIssueVisitDate(user, this);
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

	@Override
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
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public long getFieldOrdinal(String fieldName, String fieldValue) {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
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
	
	public Collection<User> getParticipants() {
		if (participants == null) {
			participants = new LinkedHashSet<>();
			if (getSubmitter() != null)
				participants.add(getSubmitter());
			UserManager userManager = OneDev.getInstance(UserManager.class);
			for (IssueField field: getFields()) {
				if (field.getType().equals(InputSpec.USER)) {
					if (field.getValue() != null) {
						User user = userManager.findByName(field.getValue());
						if (user != null)
							participants.add(user);
					}
				} else if (field.getType().equals(InputSpec.GROUP)) {
					if (field.getValue() != null) {
						Group group = OneDev.getInstance(GroupManager.class).find(field.getValue());
						if (group != null)
							participants.addAll(group.getMembers());
					}
				}
			}
			for (IssueComment comment: getComments()) {
				if (comment.getUser() != null)
					participants.add(comment.getUser());
			}
			for (IssueChange change: getChanges()) {
				if (change.getUser() != null)
					participants.add(change.getUser());
			}
			participants.remove(userManager.getSystem());
		}
		return participants;
	}
	
	public String getReference(@Nullable Project currentProject) {
		return Referenceable.asReference(this, currentProject);
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

			PullRequestInfoManager infoManager = OneDev.getInstance(PullRequestInfoManager.class); 
			Collection<Long> pullRequestIds = new HashSet<>();
			for (ProjectScopedCommit commit: getCommits()) 
				pullRequestIds.addAll(infoManager.getPullRequestIds(commit.getProject(), commit.getCommitId()));		
			
			for (Long requestId: pullRequestIds) {
				PullRequest request = OneDev.getInstance(PullRequestManager.class).get(requestId);
				if (request != null && !pullRequests.contains(request))
					pullRequests.add(request);
			}
			Collections.sort(pullRequests, new Comparator<PullRequest>() {

				@Override
				public int compare(PullRequest o1, PullRequest o2) {
					return o2.getId().compareTo(o1.getId());
				}
				
			});
		}
		return pullRequests;
	}
	
	public List<ProjectScopedCommit> getCommits() {
		if (commits == null) {
			commits = new ArrayList<>();
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class); 
			
			getProject().getTree().stream().filter(it->it.isCodeManagement()).forEach(it-> {
				for (ObjectId commitId: commitInfoManager.getFixCommits(it.getId(), getId())) {
					RevCommit commit = it.getRevCommit(commitId, false);
					if (commit != null)
						commits.add(new ProjectScopedCommit(it, commit.copy()));
				}
			});
			
			Collections.sort(commits, new Comparator<ProjectScopedCommit>() {
	
				@Override
				public int compare(ProjectScopedCommit o1, ProjectScopedCommit o2) {
					return o2.getRevCommit().getCommitTime() - o1.getRevCommit().getCommitTime();
				}
				
			});
		}
		return commits;		
	}

	@Override
	public Project getAttachmentProject() {
		return project;
	}
	
	@Override
	public String getAttachmentGroup() {
		return uuid;
	}

	public ProjectScopedNumber getFQN() {
		return new ProjectScopedNumber(getProject(), getNumber());
	}
	
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(this);
	}
	
	public Collection<Milestone> getMilestones() {
		return getSchedules().stream().map(it->it.getMilestone()).collect(Collectors.toList());
	}
	
	public IssueSchedule addSchedule(Milestone milestone) {
		IssueSchedule schedule = new IssueSchedule();
		schedule.setIssue(this);
		schedule.setMilestone(milestone);
		getSchedules().add(schedule);
		return schedule;
	}
	
	@Nullable
	public IssueSchedule removeSchedule(Milestone milestone) {
		for (Iterator<IssueSchedule> it = getSchedules().iterator(); it.hasNext();) {
			IssueSchedule schedule = it.next();
			if (schedule.getMilestone().equals(milestone)) {
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
		if (spec.getOpposite() != null) {
			if (opposite) {
				return getSourceLinks().stream()
						.filter(it->it.getSpec().equals(spec))
						.sorted()
						.map(it->it.getSource())
						.sorted(spec.getOpposite().getParsedIssueQuery(getProject()))
						.collect(Collectors.toList());
			} else {
				return getTargetLinks().stream()
						.filter(it->it.getSpec().equals(spec))
						.sorted()
						.map(it->it.getTarget())
						.sorted(spec.getParsedIssueQuery(getProject()))
						.collect(Collectors.toList());
			}
		} else {
			return getLinks().stream()
					.filter(it->it.getSpec().equals(spec))
					.sorted()
					.map(it->it.getLinked(this))
					.sorted(spec.getParsedIssueQuery(getProject()))
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
}
