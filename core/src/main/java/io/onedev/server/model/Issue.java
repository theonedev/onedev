package io.onedev.server.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.Markdown;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.utils.BeanUtils;

@Entity
@Table(
		indexes={
				@Index(columnList="g_project_id"), @Index(columnList="state"), 
				@Index(columnList="title"), @Index(columnList="g_reporter_id"), 
				@Index(columnList="votes")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Issue extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final Map<String, String> BUILTIN_FIELDS = new HashMap<>();
	
	static {
		for (Method getter: BeanUtils.findGetters(Issue.class)) {
			if (BeanUtils.findSetter(getter) != null) {
				Field field = BeanUtils.findField(getter);
				if (field != null && field.getAnnotation(ManyToOne.class) == null 
						&& field.getAnnotation(OneToMany.class) == null) {
					BUILTIN_FIELDS.put(EditableUtils.getDisplayName(field), field.getName());
				}
			}
		}
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
	private Date submitDate;
	
	private int votes;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueField> fields = new ArrayList<>();
	
	private transient Map<String, MultiValueIssueField> multiValueFields;
	
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
	}
	
	public Map<String, MultiValueIssueField> getMultiValueFields() {
		if (multiValueFields == null) {
			multiValueFields = new LinkedHashMap<>();

			Map<String, List<IssueField>> fieldMap = new HashMap<>(); 
			for (IssueField field: getFields()) {
				if (field.isCollected()) {
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
					multiValueFields.put(fieldName, new MultiValueIssueField(this, fieldName, type, values));
				}
			}
		}
		return multiValueFields;
	}
	
}
