package io.onedev.server.model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import static io.onedev.server.model.IssueField.*;

@Entity
@Table(
		indexes={
				@Index(columnList="o_issue_id"), @Index(columnList=PROP_NAME), @Index(columnList=PROP_VALUE), 
				@Index(columnList=PROP_TYPE), @Index(columnList=PROP_ORDINAL)}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_issue_id", PROP_NAME, PROP_VALUE})})
public class IssueField extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ISSUE = "issue";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_VALUE = "value";
	
	public static final String PROP_ORDINAL = "ordinal";
	
	public static final String PROP_TYPE = "type";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@Column(nullable=false)
	private String name;

	private String value;

	@Column(nullable=false)
	private String type;

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

}
