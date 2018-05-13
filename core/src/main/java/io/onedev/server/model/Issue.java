package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.support.Referenceable;
import io.onedev.server.model.support.issue.PromptedField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.Markdown;
import io.onedev.server.util.inputspec.InputSpec;

@Entity
@Table(
		indexes={
				@Index(columnList="g_project_id"), @Index(columnList="state"), 
				@Index(columnList="title"), @Index(columnList="noSpaceTitle"),  
				@Index(columnList="number"), @Index(columnList="numberStr"), 
				@Index(columnList="submitDate"), @Index(columnList="g_submitter_id"),
				@Index(columnList="votes")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Issue extends AbstractEntity implements Referenceable {

	private static final long serialVersionUID = 1L;
	
	public static final Map<String, String> BUILTIN_FIELDS = new HashMap<>();
	
	public static final String NUMBER = "Number";
	
	public static final String STATE = "State";
	
	public static final String TITLE = "Title";
	
	public static final String DESCRIPTION = "Description";
	
	public static final String SUBMITTER = "Submitter";
	
	public static final String SUBMIT_DATE = "Submit Date";
	
	public static final String VOTES = "Votes";
	
	static {
		BUILTIN_FIELDS.put(NUMBER, "number");
		BUILTIN_FIELDS.put(STATE, "state");
		BUILTIN_FIELDS.put(TITLE, "title");
		BUILTIN_FIELDS.put(DESCRIPTION, "description");
		BUILTIN_FIELDS.put(SUBMITTER, "submitter");
		BUILTIN_FIELDS.put(SUBMIT_DATE, "submitDate");
		BUILTIN_FIELDS.put(VOTES, "votes");
	}
	
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
	private User submitter;
	
	private String submitterName;
	
	@Column(nullable=false)
	private long submitDate;
	
	private int votes;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();

	private long number;
	
	// used for number search in markdown editor
	@Column(nullable=false)
	private String numberStr;
	
	// used for title search in markdown editor
	@Column(nullable=false)
	private String noSpaceTitle;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueField> fields = new ArrayList<>();
	
	@OneToMany(mappedBy="current", cascade=CascadeType.REMOVE)
	private Collection<IssueRelation> relationsByCurrent = new ArrayList<>();
	
	@OneToMany(mappedBy="other", cascade=CascadeType.REMOVE)
	private Collection<IssueRelation> relationsByOther = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueComment> comments = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueChange> changes = new ArrayList<>();
	
	private transient Map<String, PromptedField> promptedFields;
	
	public long getVersion() {
		return version;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Editable(order=100)
	@NotEmpty
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		noSpaceTitle = StringUtils.deleteWhitespace(title);
	}

	@Editable(order=200)
	@Markdown
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
		return new Date(submitDate);
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate.getTime();
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

	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}

	public Collection<IssueField> getFields() {
		return fields;
	}

	public void setFields(Collection<IssueField> fields) {
		this.fields = fields;
		promptedFields = null;
	}
	
	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitManager.class).getIssueVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public Map<String, PromptedField> getPromptedFields() {
		if (promptedFields == null) {
			promptedFields = new LinkedHashMap<>();

			Map<String, List<IssueField>> fieldMap = new HashMap<>(); 
			for (IssueField field: getFields()) {
				if (field.isPrompted()) {
					List<IssueField> fieldsOfName = fieldMap.get(field.getName());
					if (fieldsOfName == null) {
						fieldsOfName = new ArrayList<>();
						fieldMap.put(field.getName(), fieldsOfName);
					}
					fieldsOfName.add(field);
				}
			}
			
			for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFields()) {
				String fieldName = fieldSpec.getName();
				List<IssueField> fields = fieldMap.get(fieldName);
				if (fields != null) {
					String type = fields.iterator().next().getType();
					List<String> values = new ArrayList<>();
					for (IssueField field: fields) {
						if (field.getValue() != null)
							values.add(field.getValue());
					}
					promptedFields.put(fieldName, new PromptedField(this, fieldName, type, values));
				}
			}
		}
		return promptedFields;
	}
	
	public Collection<IssueRelation> getRelationsByCurrent() {
		return relationsByCurrent;
	}

	public void setRelationsByCurrent(Collection<IssueRelation> relationsByCurrent) {
		this.relationsByCurrent = relationsByCurrent;
	}

	public Collection<IssueRelation> getRelationsByOther() {
		return relationsByOther;
	}

	public void setRelationsByOther(Collection<IssueRelation> relationsByOther) {
		this.relationsByOther = relationsByOther;
	}

	public static String getWebSocketObservable(Long issueId) {
		return Issue.class.getName() + ":" + issueId;
	}
	
}
