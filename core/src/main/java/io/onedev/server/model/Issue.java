package io.onedev.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.cache.UserInfoManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageSupport;
import io.onedev.server.util.IssueField;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.facade.IssueFacade;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.jackson.DefaultView;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Editable;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList="state"), 
				@Index(columnList="title"), @Index(columnList="noSpaceTitle"),  
				@Index(columnList="number"), @Index(columnList="submitDate"), 
				@Index(columnList="o_submitter_id"), @Index(columnList="voteCount"), 
				@Index(columnList="commentCount"), @Index(columnList="o_milestone_id"), 
				@Index(columnList="updateDate")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "number"})})
@Editable
public class Issue extends AbstractEntity implements Referenceable, AttachmentStorageSupport {

	private static final long serialVersionUID = 1L;

	@Version
	private long version;
	
	@Column(nullable=false)
	private String state;
	
	@Column(nullable=false)
	private String title;
	
	@Lob
	@Column(length=65535)
	private String description;
	
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
	
	// used for title search in markdown editor
	@Column(nullable=false)
	@JsonView(DefaultView.class)
	private String noSpaceTitle;
	
	@Column(nullable=false)
	private Date updateDate = new Date();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueFieldEntity> fieldEntities = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueComment> comments = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueChange> changes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueVote> votes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueWatch> watches = new ArrayList<>();
	
	private transient List<RevCommit> commits;
	
	public long getVersion() {
		return version;
	}

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

	public void setNumber(long number) {
		this.number = number;
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

	public Collection<IssueFieldEntity> getFieldEntities() {
		return fieldEntities;
	}

	public void setFieldEntities(Collection<IssueFieldEntity> fieldEntities) {
		this.fieldEntities = fieldEntities;
	}
	
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
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
		return getFieldEntities().stream().map(it->it.getName()).collect(Collectors.toSet());
	}
	
	public Map<String, IssueField> getFields() {
		Map<String, IssueField> fields = new LinkedHashMap<>();

		Map<String, List<IssueFieldEntity>> entityMap = new HashMap<>(); 
		for (IssueFieldEntity entity: getFieldEntities()) {
			List<IssueFieldEntity> fieldsOfName = entityMap.get(entity.getName());
			if (fieldsOfName == null) {
				fieldsOfName = new ArrayList<>();
				entityMap.put(entity.getName(), fieldsOfName);
			}
			fieldsOfName.add(entity);
		}
		for (InputSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
			String fieldName = fieldSpec.getName();
			List<IssueFieldEntity> unaries = entityMap.get(fieldName);
			if (unaries != null) {
				String type = unaries.iterator().next().getType();
				List<String> values = new ArrayList<>();
				for (IssueFieldEntity entity: unaries) {
					if (entity.getValue() != null)
						values.add(entity.getValue());
				}
				Collections.sort(values);
				if (!fieldSpec.isAllowMultiple() && values.size() > 1) 
					values = Lists.newArrayList(values.iterator().next());
				fields.put(fieldName, new IssueField(fieldName, type, values));
			}
		}
		return fields;
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
		IssueField field = getFields().get(fieldName);
		
		if (field != null) 
			return field.getValue(getProject());
		else
			return null;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public long getFieldOrdinal(String fieldName, Object fieldValue) {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		InputSpec fieldSpec = issueSetting.getFieldSpec(fieldName);
		if (fieldSpec != null) 
			return fieldSpec.getOrdinal(fieldValue);
		else 
			return -1;
	}
	
	public Serializable getFieldBean(Class<?> fieldBeanClass, boolean withDefaultValue) {
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBeanClass);
		Serializable fieldBean = (Serializable) beanDescriptor.newBeanInstance();

		for (List<PropertyDescriptor> groupProperties: beanDescriptor.getPropertyDescriptors().values()) {
			for (PropertyDescriptor property: groupProperties) {
				IssueField field = getFields().get(property.getDisplayName());
				if (field != null)
					property.setPropertyValue(fieldBean, field.getValue(getProject()));
				else if (!withDefaultValue)
					property.setPropertyValue(fieldBean, null);
			}
		}
		return fieldBean;
	}
	
	public void removeFields(Collection<String> fieldNames) {
		for (Iterator<IssueFieldEntity> it = getFieldEntities().iterator(); it.hasNext();) {
			if (fieldNames.contains(it.next().getName()))
				it.remove();
		}
	}
	
	public void setFieldValues(Map<String, Object> fieldValues) {
		for (Map.Entry<String, Object> entry: fieldValues.entrySet())
			setFieldValue(entry.getKey(), entry.getValue());
	}
	
	public void setFieldValue(String fieldName, @Nullable Object fieldValue) {
		for (Iterator<IssueFieldEntity> it = getFieldEntities().iterator(); it.hasNext();) {
			if (fieldName.equals(it.next().getName()))
				it.remove();
		}
		
		InputSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec != null) {
			long ordinal = getFieldOrdinal(fieldName, fieldValue);

			IssueFieldEntity field = new IssueFieldEntity();
			field.setIssue(this);
			field.setName(fieldName);
			field.setOrdinal(ordinal);
			field.setType(fieldSpec.getType());
			
			if (fieldValue != null) {
				List<String> strings = fieldSpec.convertToStrings(fieldValue);
				if (!strings.isEmpty()) {
					for (String string: strings) {
						IssueFieldEntity cloned = (IssueFieldEntity) SerializationUtils.clone(field);
						cloned.setIssue(this);
						cloned.setValue(string);
						getFieldEntities().add(cloned);
					}
				} else {
					getFieldEntities().add(field);
				}
			} else {
				getFieldEntities().add(field);
			}
		}
	}

	public boolean isFieldVisible(String fieldName) {
		InputSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec != null) {
			if (fieldSpec.getShowCondition() != null) {
				IssueField dependentField = getFields().get(fieldSpec.getShowCondition().getInputName());
				if (dependentField != null) {
					String value;
					if (!dependentField.getValues().isEmpty())
						value = dependentField.getValues().iterator().next();
					else
						value = null;
					return fieldSpec.getShowCondition().getValueMatcher().matches(value);
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
	
}
