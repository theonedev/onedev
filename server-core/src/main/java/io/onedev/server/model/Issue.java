package io.onedev.server.model;

import static io.onedev.server.model.Issue.PROP_COMMENT_COUNT;
import static io.onedev.server.model.Issue.PROP_NO_SPACE_TITLE;
import static io.onedev.server.model.Issue.PROP_NUMBER;
import static io.onedev.server.model.Issue.PROP_STATE;
import static io.onedev.server.model.Issue.PROP_SUBMIT_DATE;
import static io.onedev.server.model.Issue.PROP_TITLE;
import static io.onedev.server.model.Issue.PROP_VOTE_COUNT;

import java.io.Serializable;
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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
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

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.infomanager.PullRequestInfoManager;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageSupport;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.facade.IssueFacade;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Editable;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList=PROP_STATE), 
				@Index(columnList=PROP_TITLE), @Index(columnList=PROP_NO_SPACE_TITLE),  
				@Index(columnList=PROP_NUMBER), @Index(columnList=PROP_SUBMIT_DATE), 
				@Index(columnList="o_submitter_id"), @Index(columnList=PROP_VOTE_COUNT), 
				@Index(columnList=PROP_COMMENT_COUNT), @Index(columnList="o_milestone_id"), 
				@Index(columnList=LastUpdate.COLUMN_DATE), @Index(columnList="o_numberScope_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_numberScope_id", PROP_NUMBER})})
//use dynamic update in order not to overwrite other edits while background threads change update date
@DynamicUpdate
@Editable
public class Issue extends AbstractEntity implements Referenceable, AttachmentStorageSupport {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NUMBER_SCOPE = "numberScope";
	
	public static final String NAME_NUMBER = "Number";
	
	public static final String PROP_NUMBER = "number";
	
	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";
	
	public static final String NAME_STATE = "State";
	
	public static final String PROP_STATE = "state";
	
	public static final String NAME_TITLE = "Title";
	
	public static final String PROP_TITLE = "title";
	
	public static final String NAME_DESCRIPTION = "Description";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String NAME_COMMENT = "Comment";
	
	public static final String PROP_COMMENTS = "comments";
	
	public static final String NAME_SUBMITTER = "Submitter";
	
	public static final String PROP_SUBMITTER = "submitter";
	
	public static final String NAME_SUBMIT_DATE = "Submit Date";
	
	public static final String PROP_SUBMIT_DATE = "submitDate";
	
	public static final String NAME_VOTE_COUNT = "Vote Count";
	
	public static final String PROP_VOTE_COUNT = "voteCount";
	
	public static final String NAME_COMMENT_COUNT = "Comment Count";
	
	public static final String PROP_COMMENT_COUNT = "commentCount";
	
	public static final String NAME_UPDATE_DATE = "Update Date";
	
	public static final String PROP_LAST_UPDATE = "lastUpdate";
	
	public static final String NAME_MILESTONE = "Milestone";
	
	public static final String PROP_MILESTONE = "milestone";
	
	public static final String PROP_FIELDS = "fields";
		
	public static final String PROP_ID = "id";
	
	public static final String PROP_NO_SPACE_TITLE = "noSpaceTitle";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_PROJECT, NAME_NUMBER, NAME_STATE, NAME_TITLE, NAME_SUBMITTER, 
			NAME_DESCRIPTION, NAME_COMMENT, NAME_SUBMIT_DATE, NAME_UPDATE_DATE, 
			NAME_VOTE_COUNT, NAME_COMMENT_COUNT, NAME_MILESTONE);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_NUMBER, NAME_STATE, NAME_TITLE, NAME_DESCRIPTION, 
			NAME_COMMENT, NAME_SUBMIT_DATE, NAME_UPDATE_DATE, NAME_VOTE_COUNT, 
			NAME_COMMENT_COUNT, NAME_MILESTONE);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_VOTE_COUNT, PROP_VOTE_COUNT,
			NAME_COMMENT_COUNT, PROP_COMMENT_COUNT,
			NAME_NUMBER, PROP_NUMBER,
			NAME_SUBMIT_DATE, PROP_SUBMIT_DATE,
			NAME_PROJECT, PROP_PROJECT,
			NAME_UPDATE_DATE, PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE);	
	
	@Column(nullable=false)
	private String state;
	
	@Column(nullable=false)
	private String title;
	
	@Column(length=14000)
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project numberScope;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Milestone milestone;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User submitter;
	
	private String submitterName;
	
	@Column(nullable=false)
	private Date submitDate;
	
	private int voteCount;
	
	private int commentCount;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();

	private long number;
	
	@Embedded
	private LastUpdate lastUpdate;
	
	// used for title search in markdown editor
	@Column(nullable=false)
	@JsonView(DefaultView.class)
	private String noSpaceTitle;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueField> fields = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueComment> comments = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueChange> changes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueVote> votes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueWatch> watches = new ArrayList<>();
	
	private transient List<RevCommit> commits;
	
	private transient List<PullRequest> pullRequests;
	
	private transient Map<String, Input> fieldInputs;
	
	private transient Collection<User> participants;
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		noSpaceTitle = StringUtils.deleteWhitespace(title);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	@Override
	public long getNumber() {
		return number;
	}
	
	@Override
	public String getPrefix() {
		return "issue";
	}
	
	public void setNumber(long number) {
		this.number = number;
	}

	public LastUpdate getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LastUpdate lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

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

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
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

	public Collection<IssueField> getFields() {
		return fields;
	}

	public void setFields(Collection<IssueField> fields) {
		this.fields = fields;
	}
	
	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(UserInfoManager.class).getIssueVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public Collection<String> getFieldNames() {
		return getFields().stream().map(it->it.getName()).collect(Collectors.toSet());
	}
	
	public Map<String, Input> getFieldInputs() {
		if (fieldInputs == null) {
			fieldInputs = new LinkedHashMap<>();
	
			Map<String, List<IssueField>> fieldMap = new HashMap<>(); 
			for (IssueField field: getFields()) {
				List<IssueField> fieldsOfName = fieldMap.get(field.getName());
				if (fieldsOfName == null) {
					fieldsOfName = new ArrayList<>();
					fieldMap.put(field.getName(), fieldsOfName);
				}
				fieldsOfName.add(field);
			}
			for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
				String fieldName = fieldSpec.getName();
				List<IssueField> fields = fieldMap.get(fieldName);
				if (fields != null) {
					Collections.sort(fields, new Comparator<IssueField>() {

						@Override
						public int compare(IssueField o1, IssueField o2) {
							long result = o1.getOrdinal() - o2.getOrdinal();
							if (result > 0)
								return 1;
							else if (result < 0)
								return -1;
							else
								return 0;
						}
						
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
	
	public static String getWebSocketObservable(Long issueId) {
		return Issue.class.getName() + ":" + issueId;
	}
	
	@Nullable
	public String getMilestoneName() {
		return getMilestone()!=null? getMilestone().getName():null;
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
	
	public IssueFacade getFacade() {
		return new IssueFacade(getId(), getProject().getId(), getNumber());
	}
	
	public List<PullRequest> getPullRequests() {
		if (pullRequests == null) {
			pullRequests = new ArrayList<>();

			PullRequestInfoManager infoManager = OneDev.getInstance(PullRequestInfoManager.class); 
			Collection<Long> pullRequestIds = new HashSet<>();
			for (ObjectId commit: getCommits()) 
				pullRequestIds.addAll(infoManager.getPullRequestIds(getProject(), commit));		
			
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
	
	public List<RevCommit> getCommits() {
		if (commits == null) {
			commits = new ArrayList<>();
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class); 
			for (ObjectId commitId: commitInfoManager.getFixCommits(getProject(), getNumber())) {
				RevCommit commit = getProject().getRevCommit(commitId, false);
				if (commit != null)
					commits.add(commit);
			}
			Collections.sort(commits, new Comparator<RevCommit>() {
	
				@Override
				public int compare(RevCommit o1, RevCommit o2) {
					return o2.getCommitTime() - o1.getCommitTime();
				}
				
			});
		}
		return commits;		
	}

	@Override
	public String getAttachmentStorageUUID() {
		return uuid;
	}

	@Override
	public Project getAttachmentProject() {
		return getProject();
	}

	public ProjectScopedNumber getFQN() {
		return new ProjectScopedNumber(getProject(), getNumber());
	}
	
	public String getNumberAndTitle() {
		return "#" + getNumber() + " - " + getTitle();
	}
	
}
