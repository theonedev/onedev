package io.onedev.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.Referenceable;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputSpec;
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
				@Index(columnList="g_project_id"), @Index(columnList="state"), 
				@Index(columnList="title"), @Index(columnList="noSpaceTitle"),  
				@Index(columnList="number"), @Index(columnList="numberStr"), 
				@Index(columnList="submitDate"), @Index(columnList="g_submitter_id"),
				@Index(columnList="voteCount"), @Index(columnList="commentCount"),
				@Index(columnList="g_milestone_id"), @Index(columnList="LAST_ACT_DATE")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Issue extends AbstractEntity implements Referenceable {

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
	
	// used for number search in markdown editor
	@Column(nullable=false)
	private String numberStr;
	
	// used for title search in markdown editor
	@Column(nullable=false)
	private String noSpaceTitle;
	
	@Embedded
	private LastActivity lastActivity;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueFieldUnary> fieldUnaries = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueComment> comments = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueAction> actions = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueVote> votes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueWatch> watches = new ArrayList<>();
	
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
		numberStr = String.valueOf(number);
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

	public Collection<IssueAction> getActions() {
		return actions;
	}

	public void setChanges(Collection<IssueAction> actions) {
		this.actions = actions;
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

	public Collection<IssueFieldUnary> getFieldUnaries() {
		return fieldUnaries;
	}

	public void setFieldUnaries(Collection<IssueFieldUnary> fieldUnaries) {
		this.fieldUnaries = fieldUnaries;
	}
	
	public LastActivity getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(LastActivity lastActivity) {
		this.lastActivity = lastActivity;
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
		return getFieldUnaries().stream().map(it->it.getName()).collect(Collectors.toSet());
	}
	
	public Map<String, IssueField> getFields() {
		Map<String, IssueField> fields = new LinkedHashMap<>();

		Map<String, List<IssueFieldUnary>> unaryMap = new HashMap<>(); 
		for (IssueFieldUnary unary: getFieldUnaries()) {
			List<IssueFieldUnary> fieldsOfName = unaryMap.get(unary.getName());
			if (fieldsOfName == null) {
				fieldsOfName = new ArrayList<>();
				unaryMap.put(unary.getName(), fieldsOfName);
			}
			fieldsOfName.add(unary);
		}
		
		for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFieldSpecs()) {
			String fieldName = fieldSpec.getName();
			List<IssueFieldUnary> unaries = unaryMap.get(fieldName);
			if (unaries != null) {
				String type = unaries.iterator().next().getType();
				List<String> values = new ArrayList<>();
				for (IssueFieldUnary unary: unaries) {
					if (unary.getValue() != null)
						values.add(unary.getValue());
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
	
	public long getFieldOrdinal(String fieldName, Object fieldValue) {
		InputSpec fieldSpec = getProject().getIssueWorkflow().getFieldSpec(fieldName);
		if (fieldSpec != null) 
			return fieldSpec.getOrdinal(fieldValue);
		else 
			return -1;
	}
	
	public Serializable getFieldBean(Class<?> fieldBeanClass, boolean withDefaultValue) {
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBeanClass);
		Serializable fieldBean = (Serializable) beanDescriptor.newBeanInstance();

		for (PropertyDescriptor property: beanDescriptor.getPropertyDescriptors()) {
			IssueField field = getFields().get(property.getDisplayName());
			if (field != null)
				property.setPropertyValue(fieldBean, field.getValue(getProject()));
			else if (!withDefaultValue)
				property.setPropertyValue(fieldBean, null);
		}
		return fieldBean;
	}
	
	public void removeFields(Collection<String> fieldNames) {
		for (Iterator<IssueFieldUnary> it = getFieldUnaries().iterator(); it.hasNext();) {
			if (fieldNames.contains(it.next().getName()))
				it.remove();
		}
	}
	
	public void setFieldValues(Map<String, Object> fieldValues) {
		for (Map.Entry<String, Object> entry: fieldValues.entrySet())
			setFieldValue(entry.getKey(), entry.getValue());
	}
	
	public void setFieldValue(String fieldName, @Nullable Object fieldValue) {
		for (Iterator<IssueFieldUnary> it = getFieldUnaries().iterator(); it.hasNext();) {
			if (fieldName.equals(it.next().getName()))
				it.remove();
		}
		
		InputSpec fieldSpec = getProject().getIssueWorkflow().getFieldSpec(fieldName);
		if (fieldSpec != null) {
			long ordinal = getFieldOrdinal(fieldName, fieldValue);

			IssueFieldUnary field = new IssueFieldUnary();
			field.setIssue(this);
			field.setName(fieldName);
			field.setOrdinal(ordinal);
			field.setType(fieldSpec.getType());
			
			if (fieldValue != null) {
				List<String> strings = fieldSpec.convertToStrings(fieldValue);
				if (!strings.isEmpty()) {
					for (String string: strings) {
						IssueFieldUnary cloned = (IssueFieldUnary) SerializationUtils.clone(field);
						cloned.setIssue(this);
						cloned.setValue(string);
						getFieldUnaries().add(cloned);
					}
				} else {
					getFieldUnaries().add(field);
				}
			} else {
				getFieldUnaries().add(field);
			}
		}
	}

	public boolean isFieldVisible(String fieldName) {
		IssueWorkflow workflow = getProject().getIssueWorkflow();
		InputSpec fieldSpec = workflow.getFieldSpec(fieldName);
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

}
