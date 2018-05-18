package io.onedev.server.model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
		indexes={
				@Index(columnList="g_issue_id"), @Index(columnList="name"), 
				@Index(columnList="value"), @Index(columnList="type"), 
				@Index(columnList="ordinal")})
public class IssueField extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "name";
	
	public static final String VALUE = "value";
	
	public static final String ORDINAL = "ordinal";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@Column(nullable=false)
	private String name;

	private String value;

	@Column(nullable=false)
	private String type;
	
	private boolean prompted;
	
	private long ordinal;
	
	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get issue field value 
	 * 
	 * @return
	 * 			a single value of the field, <tt>null</tt> if the field is not prompted yet
	 */
	@Nullable
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(long ordinal) {
		this.ordinal = ordinal;
	}

	public boolean isPrompted() {
		return prompted;
	}

	public void setPrompted(boolean prompted) {
		this.prompted = prompted;
	}
	
}
