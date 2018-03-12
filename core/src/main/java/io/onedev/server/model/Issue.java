package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

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

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.Markdown;

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
	private User reporter;
	
	private int votes;

	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueField> fields = new ArrayList<>();
	
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

	public User getReporter() {
		return reporter;
	}

	public void setReporter(User reporter) {
		this.reporter = reporter;
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
	
}
